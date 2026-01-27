# 찾아Dream: 숭실대학교 중심의 맞춤형 분실물 관리 서비스

찾아Dream은 에브리타임, 당근마켓 등 여러 커뮤니티에 파편화되어 올라오는 교내 분실물 정보를 하나로 통합합니다. 지도 기반의 실시간 발견 정보를 제공함은 물론, 리워드 시스템을 통해 구성원들이 자발적으로 분실물을 찾아주는 선순환 생태계를 구축하는 플랫폼이며 숭실대학교 컴퓨터학부 **사용자인터페이스및실습** 전공수업 프로젝트로 진행되었습니다. 

<br>

## 🌟 주요 특징 (Key Features)

1. 지도 기반 분실물 확인: 현재 어떤 분실물이 어디서 발견되었는지 지도상에서 실제 이미지 마커를 통해 한눈에 확인할 수 있습니다.

2. Gemini AI 기반 맞춤형 알림: Gemini API를 활용해 습득물 게시글의 내용을 분석하고, 비슷한 물건을 찾는 사용자에게 자동으로 알림을 전송합니다.
> [!NOTE]
> Gemini API Key값 변경으로 인해, 현재 분실물 매칭기능은 제공되지 않습니다.

3. 실시간 채팅: 분실물 습득자와 실시간으로 소통하며 이미지 전송 등을 통해 물건을 안전하게 확인할 수 있습니다.

4. 포인트 시스템 및 상점: 분실물을 찾아준 사용자에게 포인트를 지급하고, 이를 교내 식권이나 상품으로 교환할 수 있는 리워드 시스템을 제공합니다.

<br>

## 🛠 기술 스택 (Tech Stack)

1. Language & Framework: Android Studio (Java), XML 기반 UI 디자인

2. Backend: Firebase (Authentication, Realtime Database, Cloud Storage)

3. AI: Gemini API - Gemini 2.5 Flash (분실물 데이터 분석 및 매칭)

4. API: Google Maps API (위치 정보 표시)

<br>

## 📅 개발 기간 (Development Period)

**2024.11.03 ~ 2024.12.14 (6주)**

<br>

## 👥 팀원 (Team Members)

박동준(22학번): UX 구현, Google Maps API, Gemini API 연동 및 FCM 구현, Firebase Authentication

김승렬(22학번): 게시판 기능 구현, UI 디자인(Figma), Firebase realtime DB 관리

오현빈(22학번): 실시간 채팅 및 포인트 상점 구현, Firebase realtime DB 관리

<br>

## 📺 시연 영상 (Demo Video)

https://youtu.be/m_PcV5sAyH8?si=hqFFD631C2sIWfx9

> 위 이미지를 클릭하면 YouTube 시연 영상으로 이동합니다.
