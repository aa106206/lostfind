package com.example.lostfind;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiMatcher extends AppCompatActivity {

    // ------------------------
    //   CALLBACK 인터페이스
    // ------------------------
    public interface MatchCallback {
        void onSuccess(JSONArray matches);
        void onError(String error);
    }

    private static final String API_KEY = "AIzaSyCQvTfmmo_dZXngI5yYG8otAVO3_4KYuTM";  // 나중에 BuildConfig로 변경 가능
    private static final String MODEL_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;



    // ----------------------------------------------------------
    //   FOUND + LOST LIST → Gemini에게 매칭 요청하는 핵심 함수
    // ----------------------------------------------------------
    public static void matchFoundWithLost(JSONObject foundJson,
                                          List<Post> lostPosts,
                                          MatchCallback callback) {

        try {
            // 1) LOST POST들을 JSON 배열로 변환
            JSONArray lostArray = new JSONArray();
            for (Post p : lostPosts) {
                JSONObject o = new JSONObject();
                o.put("postId", p.getPostId());
                o.put("title", p.getTitle());
                o.put("description", p.getDescription());
                o.put("location", p.getLocation());
                lostArray.put(o);
            }

            // -----------------------------
            // 2) Gemini 프롬프트 생성
            // -----------------------------
            String prompt =
                    "You are an expert in matching lost items with found items.\n\n" +
                            "FOUND ITEM FEATURES:\n" +
                            foundJson.toString() + "\n\n" +
                            "LOST ITEMS LIST:\n" +
                            lostArray.toString() + "\n\n" +

                            "Task:\n" +
                            "- Compare FOUND with each LOST.\n" +
                            "- For each LOST item, give a match_score (0.0 to 1.0).\n" +
                            "- Output ONLY JSON in this structure:\n" +
                            "{\n" +
                            "  \"matches\": [\n" +
                            "    { \"lostPostId\": \"\", \"match_score\": 0.0 }\n" +
                            "  ]\n" +
                            "}\n\n" +
                            "Only include items with match_score >= 0.55.\n" +
                            "Do NOT add explanations or code blocks.";

            // 3) JSON body 구성
            JSONObject requestBody = new JSONObject();
            JSONArray contentsArray = new JSONArray();
            JSONObject contentObj = new JSONObject();
            JSONArray parts = new JSONArray();

            parts.put(new JSONObject().put("text", prompt));
            contentObj.put("parts", parts);
            contentsArray.put(contentObj);
            requestBody.put("contents", contentsArray);


            // 4) OkHttp 요청 생성
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(MODEL_URL)
                    .post(RequestBody.create(
                            requestBody.toString(),
                            MediaType.parse("application/json")
                    ))
                    .build();

            // 5) 네트워크 요청 실행
            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        String result = response.body().string();
                        Log.d("GeminiMatcher", "매칭 응답: " + result);

                        JSONArray matches = extractMatches(result);
                        callback.onSuccess(matches);

                    } catch (Exception e) {
                        callback.onError("Parsing error: " + e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call call, java.io.IOException e) {
                    callback.onError("Network error: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            callback.onError("Internal error: " + e.getMessage());
        }
    }

    // ------------------------------------------------
    //      Gemini 응답(JSON)에서 matches 배열만 추출
    // ------------------------------------------------
    private static JSONArray extractMatches(String res) throws Exception {
        JSONObject root = new JSONObject(res);

        JSONArray candidates = root.getJSONArray("candidates");
        JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
        JSONArray parts = content.getJSONArray("parts");

        String jsonText = parts.getJSONObject(0).getString("text");

        // 1) ```json 과 ``` 제거하기
        jsonText = jsonText.replace("```json", "")
                .replace("```", "")
                .trim();

        // 2) 불필요한 줄바꿈 제거
        if (jsonText.startsWith("\n")) {
            jsonText = jsonText.substring(1).trim();
        }

        // 3) 이제 JSON 파싱
        JSONObject obj = new JSONObject(jsonText);
        return obj.getJSONArray("matches");
    }
//    private static JSONArray extractMatches(String res) throws Exception {
//        JSONObject root = new JSONObject(res);
//
//        JSONArray candidates = root.getJSONArray("candidates");
//        JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
//        JSONArray parts = content.getJSONArray("parts");
//
//        String jsonText = parts.getJSONObject(0).getString("text");  // JSON 문자열
//
//        JSONObject obj = new JSONObject(jsonText);
//        return obj.getJSONArray("matches");I
//    }

}
