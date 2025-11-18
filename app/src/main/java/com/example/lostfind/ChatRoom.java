package com.example.lostfind;public class ChatRoom {
    private String chatRoomId;
    private String lastMessage;
    private long lastMessageTimestamp;
    private String opponentUserName; // 상대방 이름
    // 필요한 경우 상대방 프로필 이미지 URL 추가

    public ChatRoom() {}

    // Getters and Setters
    public String getChatRoomId() { return chatRoomId; }
    public void setChatRoomId(String chatRoomId) { this.chatRoomId = chatRoomId; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public long getLastMessageTimestamp() { return lastMessageTimestamp; }
    public void setLastMessageTimestamp(long lastMessageTimestamp) { this.lastMessageTimestamp = lastMessageTimestamp; }
    public String getOpponentUserName() { return opponentUserName; }
    public void setOpponentUserName(String opponentUserName) { this.opponentUserName = opponentUserName; }
}
