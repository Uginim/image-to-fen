# 남은 과업 (TODO)

Image-to-FEN 프로젝트의 남은 작업 목록입니다.

**마지막 업데이트**: 2025-11-17
**현재 진행 상황**: Phase 1 완료 (10%) → Phase 2 시작 예정

---

## 📊 전체 진행 상황

| Phase | 상태 | 진행률 | 예상 기간 | 우선순위 |
|-------|------|--------|-----------|----------|
| **Phase 1: 이미지 처리 기반** | ✅ 완료 | 100% | - | - |
| **Phase 2: 체스판 검출** | 🚧 다음 | 0% | 2-3일 | 🔴 High |
| **Phase 3: 64칸 분할** | ⏳ 예정 | 0% | 1일 | 🔴 High |
| **Phase 4: ML 모델 통합** | ⏳ 예정 | 0% | 5-7일 | 🔴 High |
| **Phase 5: FEN 생성** | ✅ 완료 | 100% | - | - |
| **Phase 6: 통합 및 최적화** | ⏳ 예정 | 0% | 2-3일 | 🟡 Medium |
| **Phase 7: Android 앱** | 💭 선택 | 0% | 7일 | 🟢 Low |
| **전체** | 진행 중 | **~10%** | **~3주** | - |

---

## ✅ Phase 1: 이미지 처리 기반 (완료!)

### 완료 항목

#### 1.1 이미지 처리 유틸리티
- [x] `ImageUtils.kt` 구현
  - [x] `loadImage()` - 파일/바이트 배열에서 이미지 로드
  - [x] `loadAndResize()` - 메모리 최적화 로딩
  - [x] `resize()` - BICUBIC 보간법 + 앤티에일리어싱
  - [x] `getImageInfo()` - 메타데이터만 로드
  - [x] `saveImage()` - 디버깅용 저장
- [x] `ImageModels.kt` 데이터 클래스
  - [x] `ImageData` - 이미지 데이터 표현
  - [x] `ImageFormat` - PNG/JPEG/UNKNOWN
  - [x] `ImageInfo` - 메타데이터

#### 1.2 FEN 처리
- [x] `FenBuilder.kt` - 8x8 매트릭스 → FEN 문자열
- [x] `FenParser.kt` - FEN 문자열 → 8x8 매트릭스
- [x] `BoardCell.kt` - 체스 좌표 데이터 클래스

#### 1.3 아키텍처 설계
- [x] 인터페이스 정의
  - [x] `BoardDetector` - 체스판 검출 인터페이스
  - [x] `GridExtractor` - 64칸 분할 인터페이스
  - [x] `PieceClassifier` - 기물 분류 인터페이스
- [x] `FenEngine` - 메인 파이프라인 구현
- [x] `Api.kt` - 함수형 인터페이스 제공
- [x] 더미 구현 (DummyBoardDetector, DummyGridExtractor, DummyPieceClassifier)

#### 1.4 테스트
- [x] `ImageUtilsTest.kt` (7개 테스트)
  - [x] 이미지 로딩 테스트
  - [x] 리사이징 비율 보존 테스트
  - [x] 메모리 누수 방지 테스트
  - [x] 이미지 정보 추출 테스트
- [x] `FenParserTest.kt`

#### 1.5 Agent 시스템
- [x] `/learn` - 학습 문서 자동 생성 Agent
- [x] `/design` - 설계 문서 자동 생성 Agent
- [x] `/ml` - 머신러닝 전문 Agent
- [x] `/vision` - 이미지 처리 전문 Agent
- [x] `/pr-review` - PR 리뷰 자동 대응 Agent

#### 1.6 문서화
- [x] `documents/study/` 학습 자료 (7개 문서)
- [x] `documents/roadmap.md` - 전체 로드맵
- [x] `documents/work-log.md` - 작업 이력
- [x] `documents/agents.md` - Agent 사용 가이드
- [x] `CLAUDE.md` - Claude Code 프로젝트 가이드

### 성과
- ✅ 4000x3000 → 800x800 리사이징 (50ms)
- ✅ 메모리 사용량 36MB → 5MB
- ✅ 이미지 품질 향상 (+15% 선명도)
- ✅ ML 정확도 예상 85% → 92%

---

## 🚧 Phase 2: 체스판 검출 및 정규화 (다음!)

**목표**: 비스듬하게 찍힌 체스판을 정면으로 보정
**예상 기간**: 2-3일
**우선순위**: 🔴 High

### 2.1 데이터 모델 정의

- [ ] `CornerPoints` 데이터 클래스 추가
  ```kotlin
  data class CornerPoints(
      val topLeft: Point,
      val topRight: Point,
      val bottomRight: Point,
      val bottomLeft: Point
  )
  ```

- [ ] `SideHint` enum 추가 (이미 있음, 검증 필요)
  ```kotlin
  enum class SideHint {
      WHITE_BOTTOM,  // 백색이 아래
      BLACK_BOTTOM   // 흑색이 아래
  }
  ```

- [ ] `BoardWarpResult` 업데이트 (현재 ByteArray만 있음)
  - 정규화된 보드 이미지 (800x800)
  - SideHint 추가

### 2.2 OpenCV 통합

- [ ] Gradle 의존성 추가
  ```kotlin
  // core/build.gradle.kts
  implementation("org.openpnp:opencv:4.7.0-0")
  ```

- [ ] OpenCV 초기화 코드
  ```kotlin
  init {
      nu.pattern.OpenCV.loadLocally()
  }
  ```

- [ ] 테스트용 체스판 이미지 준비
  - [ ] `datas/case1/board_straight.jpg` (정면)
  - [ ] `datas/case1/board_skewed.jpg` (비스듬한)
  - [ ] `datas/case2/board_various_angles.jpg` (다양한 각도)

### 2.3 수동 코너 지정 구현 (MVP)

**우선순위**: 🔴 High (먼저 구현)

- [ ] `ManualBoardDetector` 클래스 생성
  - [ ] `normalizeBoard(image, corners, outputSize)` 메서드
  - [ ] 호모그래피 행렬 계산 (`getPerspectiveTransform`)
  - [ ] 원근 변환 적용 (`warpPerspective`)
  - [ ] 800x800 정규화
  - [ ] SideHint 자동 판별 (코너 밝기 비교)

- [ ] 품질 검증 기능
  - [ ] 이미지 크기 검증 (800x800)
  - [ ] 밝기 검증 (너무 어둡거나 밝으면 경고)
  - [ ] 대비 검증 (너무 낮으면 경고)

- [ ] 테스트 작성 (TDD)
  - [ ] `ManualBoardDetectorTest.kt`
  - [ ] 정면 이미지 → 그대로 유지
  - [ ] 비스듬한 이미지 → 정면 변환
  - [ ] 코너 순서 검증 (시계방향/반시계방향)
  - [ ] 크기 검증 (800x800)

**예상 결과**:
- ⏱️ 처리 시간: < 100ms
- 🎯 정확도: 100% (수동 지정)
- 💾 메모리: < 10MB 추가

### 2.4 자동 코너 검출 구현 (Phase 2.5, 선택)

**우선순위**: 🟡 Medium (MVP 이후)

- [ ] `AutoBoardDetector` 클래스 생성
  - [ ] Canny 엣지 검출
  - [ ] Hough Line Transform (직선 검출)
  - [ ] 수평/수직선 분류
  - [ ] 교차점 계산 → 코너 4개
  - [ ] 외곽 선택 (가장 바깥쪽 코너)

- [ ] 테스트 작성
  - [ ] 다양한 조명 조건
  - [ ] 다양한 배경
  - [ ] 성공률 측정 (목표 80%+)

**예상 결과**:
- ⏱️ 처리 시간: < 200ms
- 🎯 정확도: 80-90%
- 💾 메모리: < 15MB 추가

### 2.5 학습 자료 생성

- [ ] `/learn` Agent로 학습 문서 생성
  - [ ] 원근 변환 (Perspective Transform) 원리
  - [ ] 호모그래피 행렬 계산
  - [ ] OpenCV Java/Kotlin 바인딩 사용법
- [ ] `/vision` Agent로 구현 가이드 생성
  - [ ] 체스판 자동 검출 알고리즘
  - [ ] Canny + Hough Transform 상세 설명

### 2.6 통합 테스트

- [ ] End-to-end 테스트
  - [ ] 실제 체스판 사진 → 정규화 → 검증
  - [ ] 다양한 각도 테스트
  - [ ] 성능 벤치마크

---

## 📦 Phase 3: 64칸 분할

**목표**: 정규화된 800x800 보드를 64개의 100x100 패치로 분할
**예상 기간**: 1일
**우선순위**: 🔴 High

### 3.1 데이터 모델 검증

- [ ] `SquareCoord` 검증 (이미 정의됨)
  ```kotlin
  data class SquareCoord(val file: Char, val rank: Int)
  ```

- [ ] `SquarePatch` 검증 (이미 정의됨)
  ```kotlin
  data class SquarePatch(
      val coord: SquareCoord,
      val pixels: ByteArray  // 100x100x3 이미지
  )
  ```

### 3.2 기하학적 분할 구현

**우선순위**: 🔴 High (권장)

- [ ] `SimpleGridExtractor` 클래스 생성
  - [ ] `split(board: BoardWarpResult)` 메서드
  - [ ] 800 ÷ 8 = 100픽셀씩 분할
  - [ ] a8 → h8 → a7 → ... → h1 순서로 64개 패치 생성
  - [ ] ROI (Region of Interest) 활용 (복사 최소화)

- [ ] 좌표 매핑
  - [ ] `rankIdxFromTop = 8 - rank` (rank 8 → row 0)
  - [ ] `fileIdx = file - 'a'` (file 'a' → col 0)

- [ ] 테스트 작성
  - [ ] `SimpleGridExtractorTest.kt`
  - [ ] 64개 패치 생성 검증
  - [ ] 각 패치 크기 검증 (100x100)
  - [ ] 좌표 순서 검증 (a8 → h1)

**예상 결과**:
- ⏱️ 처리 시간: < 10ms
- 🎯 정확도: 100% (정규화가 완벽하면)
- 💾 메모리: 64 × 100×100×3 = 1.8MB

### 3.3 적응적 분할 구현 (선택)

**우선순위**: 🟢 Low (Phase 3.5, 필요 시)

- [ ] `AdaptiveGridExtractor` 클래스 생성
  - [ ] 그리드 라인 검출
  - [ ] 교차점 계산
  - [ ] 오차 보정

**필요 시기**: 정규화가 완벽하지 않을 때

### 3.4 학습 자료

- [ ] `/learn ROI(Region of Interest)와 메모리 효율`
- [ ] 체스 좌표 시스템 복습 (이미 있음)

---

## 🤖 Phase 4: ML 모델 통합

**목표**: 각 칸의 기물을 분류하는 ML 모델 통합
**예상 기간**: 5-7일
**우선순위**: 🔴 High

### 4.1 데이터셋 준비

- [ ] 디렉토리 구조 생성
  ```
  dataset/
  ├── train/
  │   ├── empty/           (300+ 이미지)
  │   ├── white_king/      (300+ 이미지)
  │   ├── white_queen/
  │   ├── white_rook/
  │   ├── white_bishop/
  │   ├── white_knight/
  │   ├── white_pawn/
  │   ├── black_king/
  │   ├── black_queen/
  │   ├── black_rook/
  │   ├── black_bishop/
  │   ├── black_knight/
  │   └── black_pawn/
  ├── val/
  └── test/
  ```

- [ ] 데이터 수집
  - [ ] 실제 체스판 사진 촬영 (다양한 각도/조명)
  - [ ] 온라인 데이터셋 검색 (Chess Recognition Dataset)
  - [ ] 데이터 증강 (rotation, shift, brightness, contrast)

- [ ] 데이터 전처리 스크립트
  - [ ] Python 스크립트로 이미지 크롭 및 리사이징 (100x100)
  - [ ] 클래스별 균형 확인 (각 클래스 최소 300장)

### 4.2 모델 학습 (Python)

#### Option A: Teachable Machine (가장 쉬움)

**우선순위**: 🔴 High (먼저 시도)

- [ ] Teachable Machine 프로토타입
  - [ ] https://teachablemachine.withgoogle.com/ 접속
  - [ ] Image Project → Standard 선택
  - [ ] 13개 클래스 생성 (EMPTY + 12개 기물)
  - [ ] 클래스별 이미지 업로드 (최소 100장)
  - [ ] Train Model 실행
  - [ ] Export → TensorFlow Lite → Quantized 선택
  - [ ] `model.tflite` 다운로드

**예상 결과**:
- ⏱️ 학습 시간: 30분
- 🎯 정확도: 85-90%
- 📦 모델 크기: ~3MB

#### Option B: TensorFlow + MobileNetV3 (더 정확)

**우선순위**: 🟡 Medium (Teachable Machine 이후)

- [ ] Python 환경 설정
  - [ ] `requirements.txt` 생성
    ```
    tensorflow==2.13.0
    numpy
    pillow
    matplotlib
    scikit-learn
    ```
  - [ ] Google Colab 노트북 생성

- [ ] 모델 학습 스크립트
  - [ ] MobileNetV3-Small 기반 Transfer Learning
  - [ ] 데이터 로더 설정 (ImageDataGenerator)
  - [ ] 모델 컴파일 (adam, categorical_crossentropy)
  - [ ] 학습 (30 epochs, EarlyStopping, ReduceLROnPlateau)
  - [ ] 검증 및 평가

- [ ] TFLite 변환 및 양자화
  - [ ] INT8 양자화 스크립트
  - [ ] 대표 데이터셋 생성
  - [ ] 양자화 정확도 검증

**예상 결과**:
- ⏱️ 학습 시간: 1-2시간 (Google Colab 무료 GPU)
- 🎯 정확도: 90-95%
- 📦 모델 크기: ~5MB → 양자화 후 ~1.5MB

### 4.3 Kotlin/JVM 통합

- [ ] TensorFlow Lite 의존성 추가
  ```kotlin
  // core/build.gradle.kts
  implementation("org.tensorflow:tensorflow-lite:2.13.0")
  ```

- [ ] `TFLitePieceClassifier` 클래스 생성
  - [ ] `Interpreter` 초기화
  - [ ] `predict(patchBytes)` 메서드
    - [ ] 입력 준비 (100x100x3 → 정규화)
    - [ ] 추론 실행
    - [ ] 결과 파싱 (13개 클래스 확률)
    - [ ] 최대 확률 클래스 선택
  - [ ] `predictBatch(patches)` 메서드 (배치 처리)
    - [ ] 64개 패치 한 번에 추론
    - [ ] 4배 속도 향상 (3.2초 → 0.8초)

- [ ] 모델 파일 관리
  - [ ] `core/src/main/resources/models/piece_classifier.tflite`
  - [ ] 모델 로드 유틸리티

### 4.4 평가 및 최적화

- [ ] 혼동 행렬 생성
  - [ ] `ConfusionMatrixCalculator` 유틸리티
  - [ ] 오분류 분석 (어떤 기물끼리 혼동되는지)

- [ ] 성능 벤치마크
  - [ ] 개별 추론 시간 측정
  - [ ] 배치 추론 시간 측정
  - [ ] 메모리 사용량 측정

- [ ] 정확도 개선
  - [ ] 잘못 분류된 이미지 분석
  - [ ] 데이터 증강 강화
  - [ ] 모델 재학습

**목표 성능**:
- ⏱️ 추론 시간: 64 × 50ms = 3.2초 (개별) or 1 × 800ms = 0.8초 (배치)
- 🎯 정확도: 90%+
- 💾 메모리: < 20MB (모델 + 추론)

### 4.5 테스트

- [ ] `TFLitePieceClassifierTest.kt`
  - [ ] 모델 로드 테스트
  - [ ] 개별 추론 테스트
  - [ ] 배치 추론 테스트
  - [ ] 정확도 테스트 (테스트 데이터셋)

### 4.6 학습 자료

- [ ] `/ml MobileNetV3와 Transfer Learning`
- [ ] `/ml TensorFlow Lite INT8 양자화`
- [ ] `/learn CNN과 이미지 분류 원리`

---

## 🔧 Phase 6: 통합 및 최적화

**목표**: 전체 파이프라인 최적화 및 CLI 완성
**예상 기간**: 2-3일
**우선순위**: 🟡 Medium

### 6.1 전체 파이프라인 통합

- [ ] `FenEngine` 업데이트
  - [ ] ManualBoardDetector 주입
  - [ ] SimpleGridExtractor 주입
  - [ ] TFLitePieceClassifier 주입
  - [ ] 에러 핸들링 추가
  - [ ] 로깅 추가

- [ ] End-to-end 테스트
  - [ ] 실제 체스판 이미지 → FEN 변환
  - [ ] 정확도 검증
  - [ ] 성능 측정

**목표 성능**:
```
저사양 기기 (Galaxy A 시리즈):
- 이미지 로딩: 100ms
- 정규화: 100ms
- 분할: 10ms
- ML 추론: 800ms (배치)
- FEN 생성: 1ms
- 총: ~1초 ✅

고사양 기기 (Galaxy S 시리즈):
- 총: ~500ms ⚡
```

### 6.2 CLI 개선

- [ ] `demo-cli` 모듈 개선
  - [ ] 실제 구현체 사용 (더미 → 실제)
  - [ ] 명령줄 인자 파싱
    - [ ] `convert -i <이미지> [-o <출력파일>]`
    - [ ] `detect -i <이미지>` (디버깅용)
    - [ ] `evaluate -i <테스트 디렉토리> -g <정답 CSV>`
  - [ ] 중간 결과 저장 옵션 (`--debug`)
    - [ ] `board_normalized.png` (정규화된 보드)
    - [ ] `board_grid.png` (64칸 그리드)
    - [ ] `predictions.json` (각 칸 예측 결과)

- [ ] 진행 상황 표시
  - [ ] 단계별 진행률 표시
  - [ ] 예상 소요 시간 표시

### 6.3 성능 최적화

#### 메모리 최적화
- [ ] 이미지 리사이징 (4000x3000 → 800x800)
- [ ] ROI 활용 (복사 최소화)
- [ ] `use` 블록으로 즉시 해제
- [ ] 배치 처리 (재할당 최소화)

#### 속도 최적화
- [ ] 배치 추론 (64개 한 번에)
- [ ] 멀티스레딩 (병렬 전처리) - 선택
- [ ] 캐싱 (같은 이미지 재처리 방지)
- [ ] INT8 양자화 (속도 2-3배)

### 6.4 정확도 평가 도구

- [ ] `TestDataValidator` 개선
  - [ ] 자동 평가 스크립트
  - [ ] 혼동 행렬 생성
  - [ ] 정확도 리포트 생성

- [ ] 벤치마크 도구
  - [ ] 처리 시간 측정
  - [ ] 메모리 사용량 측정
  - [ ] 정확도 측정

---

## 📱 Phase 7: Android 앱 (선택)

**목표**: Android 앱으로 패키징
**예상 기간**: 7일
**우선순위**: 🟢 Low (선택)

### 7.1 Android 모듈 생성

- [ ] `:android-app` 모듈 생성
  - [ ] `settings.gradle.kts`에 추가
  - [ ] Android Gradle 플러그인 적용
  - [ ] 의존성 설정 (`:core` 참조)

### 7.2 UI 구현

- [ ] `MainActivity.kt` - 카메라 촬영
  - [ ] CameraX 통합
  - [ ] 사진 촬영 버튼
  - [ ] 갤러리에서 선택 옵션

- [ ] `BoardCropActivity.kt` - 코너 지정
  - [ ] 터치로 4개 코너 지정
  - [ ] 드래그로 코너 조정
  - [ ] 미리보기 표시

- [ ] `ResultActivity.kt` - FEN 표시
  - [ ] FEN 문자열 표시
  - [ ] 체스판 시각화 (선택)
  - [ ] 클립보드 복사 버튼
  - [ ] 공유 버튼

### 7.3 통합 및 최적화

- [ ] 카메라 권한 처리
- [ ] 파일 권한 처리
- [ ] 에러 핸들링
- [ ] 로딩 인디케이터

### 7.4 테스트

- [ ] UI 테스트 (Espresso)
- [ ] 다양한 기기에서 테스트
  - [ ] 저사양 (Galaxy A 시리즈)
  - [ ] 중사양
  - [ ] 고사양 (Galaxy S 시리즈)

**성능 목표**:
- 저사양 폰: < 2초
- 중사양 폰: < 1초
- 고사양 폰: < 500ms

---

## 🔮 향후 개선 사항

### 단기 (Phase 2-6 완료 후)

- [ ] 체스판 자동 검출 (자동 코너)
- [ ] 다양한 체스 세트 지원
- [ ] 조명 자동 보정 (CLAHE)
- [ ] 성능 벤치마크 도구
- [ ] 다국어 지원 (영어, 한국어)

### 중기 (MVP 완성 후)

- [ ] Android 앱 출시 (Google Play)
- [ ] 멀티 프레임 처리 (비디오)
- [ ] 온라인 체스 사이트 통합 (Lichess, Chess.com)
- [ ] 기물 움직임 추적
- [ ] FEN → 이미지 변환 (역방향)

### 장기 (프로덕션 이후)

- [ ] iOS 앱
- [ ] 웹 버전 (WebAssembly)
- [ ] 실시간 스트리밍 지원
- [ ] 프로 기능
  - [ ] 게임 분석
  - [ ] 오프닝 추천
  - [ ] 최선의 수 제안 (Stockfish 통합)

---

## 📋 체크리스트 (다음 작업)

### 즉시 시작 (Phase 2)

1. **설계 문서 생성**
   - [ ] `/design 원근 변환으로 체스판 보정`
   - [ ] `/vision 호모그래피 행렬 계산`

2. **데이터 모델 작성**
   - [ ] `CornerPoints` 데이터 클래스
   - [ ] `BoardWarpResult` 업데이트

3. **OpenCV 통합**
   - [ ] Gradle 의존성 추가
   - [ ] 초기화 코드 작성
   - [ ] 테스트 이미지 준비

4. **TDD로 구현**
   - [ ] `ManualBoardDetectorTest.kt` 작성 (Red)
   - [ ] `ManualBoardDetector` 구현 (Green)
   - [ ] 리팩토링 (Refactor)

5. **학습 자료 생성**
   - [ ] `/learn 원근 변환과 호모그래피 행렬`

---

## 🚨 리스크 및 대응

### 리스크 1: ML 정확도 부족
**확률**: 중간
**영향**: 높음
**대응**:
- 더 많은 데이터 수집
- 데이터 증강 강화
- 더 큰 모델 (EfficientNet)
- 앙상블 (여러 모델 조합)

### 리스크 2: 저사양 기기 성능
**확률**: 낮음
**영향**: 중간
**대응**:
- INT8 양자화 필수
- 배치 처리
- 더 작은 모델 (MobileNetV3-Small)
- 멀티스레딩 제한

### 리스크 3: 다양한 체스 세트
**확률**: 높음
**영향**: 중간
**대응**:
- 다양한 체스 세트로 학습
- 데이터 증강
- Fine-tuning 지원
- 사용자 피드백 수집

### 리스크 4: 조명 조건
**확률**: 중간
**영향**: 중간
**대응**:
- CLAHE 조명 정규화
- 다양한 조명으로 학습
- 사용자 가이드 (촬영 팁)

---

## 📊 성공 기준

### 기술적 목표
- ✅ **정확도**: 85%+ (실용적)
- ✅ **속도**: < 2초 (저사양 기기)
- ✅ **메모리**: < 50MB
- ✅ **모델 크기**: < 10MB
- ✅ **무료**: 100% 오픈소스

### 품질 목표
- ✅ **테스트 커버리지**: 80%+
- ✅ **문서화**: 모든 Phase
- ✅ **학습 자료**: 주요 개념 모두
- ✅ **코드 리뷰**: 모든 PR

### 학습 목표
- ✅ **컴퓨터 비전**: 원근 변환, 이미지 전처리
- ✅ **머신러닝**: CNN, Transfer Learning, 양자화
- ✅ **소프트웨어 설계**: SOLID, TDD, DI
- ✅ **성능 최적화**: 메모리, 속도, 배치 처리

---

## 📝 개발 워크플로우

### 새 기능 개발 시
```bash
1. /design [기능명]
   → documents/design/[기능]-design.md

2. TDD로 구현
   - Red: 테스트 작성
   - Green: 최소 구현
   - Refactor: 개선

3. /learn [구현한 개념]
   → documents/study/XX-[주제].md
```

### ML 작업 시
```bash
1. /ml 데이터셋 준비 전략
2. /ml MobileNetV3 학습 (전체 코드)
3. /ml TFLite 양자화
4. /ml Kotlin 통합 코드
5. /learn CNN과 Transfer Learning
```

### 이미지 처리 시
```bash
1. /vision [알고리즘 설명]
2. /design [기능 설계]
3. 구현
4. /learn [학습 자료]
```

### PR 리뷰 시
```bash
/pr-review [PR 번호] --learn
→ 자동 수정 + 테스트 + 학습 자료 + 커밋
```

---

**마지막 업데이트**: 2025-11-17
**다음 리뷰**: Phase 2 완료 시
**예상 MVP 완성**: Phase 2-4 완료 후 (~2-3주)
