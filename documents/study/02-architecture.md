# 아키텍처 패턴

## 의존성 주입 (Dependency Injection)

### 왜 사용했나요?

```kotlin
// ❌ 나쁜 방법: FenEngine이 구체적인 구현에 의존
class FenEngine {
    private val detector = OpenCVBoardDetector()  // OpenCV에 강하게 결합
    private val classifier = TFLiteClassifier()   // TensorFlow Lite에 강하게 결합
    // ...
}
```

**문제점**:
- Android에서는 TensorFlow Lite를 쓰고 싶은데, Desktop에서는 다른 걸 쓰고 싶으면?
- 테스트할 때 진짜 ML 모델 없이 테스트하고 싶으면?
- OpenCV 설치 없이 빌드하고 싶으면?

```kotlin
// ✅ 좋은 방법: 인터페이스에 의존
class FenEngine(
    private val detector: BoardDetector,        // 인터페이스
    private val grid: GridExtractor,            // 인터페이스
    private val classifier: PieceClassifier     // 인터페이스
) {
    // detector, grid, classifier는 외부에서 주입됨
}
```

**장점**:
- 플랫폼별로 다른 구현 사용 가능
- 테스트 시 Dummy 구현 사용 가능
- 핵심 로직은 구현 세부사항에 무관

### 사용 예시

```kotlin
// Android 앱
val engine = FenEngine(
    detector = AndroidMLKitDetector(),
    grid = AndroidGridExtractor(),
    classifier = TFLiteClassifier()
)

// Desktop 앱
val engine = FenEngine(
    detector = OpenCVDetector(),
    grid = OpenCVGridExtractor(),
    classifier = PyTorchClassifier()
)

// 테스트
val engine = FenEngine(
    detector = DummyBoardDetector(),
    grid = DummyGridExtractor(),
    classifier = DummyPieceClassifier()
)
```

## 인터페이스 기반 설계

### 핵심 인터페이스 3개

#### 1. BoardDetector

```kotlin
interface BoardDetector {
    fun detectAndNormalize(imageBytes: ByteArray): BoardWarpResult
}
```

**역할**: 사진에서 체스판을 찾아서 정면으로 펴기

**실제 구현 예시**:
- OpenCV로 코너 검출
- ML Kit로 객체 검출
- 수동 영역 선택

#### 2. GridExtractor

```kotlin
interface GridExtractor {
    fun split(board: BoardWarpResult): List<SquarePatch>
}
```

**역할**: 정규화된 보드를 64칸으로 분할

**실제 구현 예시**:
- 이미지를 8×8 그리드로 나누기
- 각 칸의 픽셀 추출

#### 3. PieceClassifier

```kotlin
interface PieceClassifier {
    fun predict(patchBytes: ByteArray): Prediction
}
```

**역할**: 각 칸 이미지를 보고 어떤 기물인지 분류

**실제 구현 예시**:
- CNN 모델 (TensorFlow Lite, PyTorch Mobile)
- 전통적인 이미지 처리 (템플릿 매칭)
- 클라우드 API (Google Vision, AWS Rekognition)

## 파이프라인 패턴

`FenEngine`은 3단계 파이프라인입니다:

```kotlin
fun imageToFen(imageBytes: ByteArray): String {
    // 1단계: 체스판 검출 및 정규화
    val warped = detector.detectAndNormalize(imageBytes)

    // 2단계: 64칸으로 분할
    val patches = grid.split(warped)

    // 3단계: 각 칸 분류 → 8×8 매트릭스 생성
    val matrix = Array(8) { CharArray(8) { '1' } }
    for (p in patches) {
        val pred = classifier.predict(p.pixels)
        // matrix에 기물 배치
    }

    // 4단계: FEN 문자열 생성
    return FenBuilder.build(matrix.toList())
}
```

각 단계는 **독립적**이고 **교체 가능**합니다.

## 플랫폼 독립성

### :core 모듈의 제약

- **순수 Kotlin만** 사용
- **외부 라이브러리 의존성 없음**
- `ByteArray`로 이미지 표현 (플랫폼 무관)

### 플랫폼별 구현은 별도 모듈에서

```
프로젝트 루트/
├── core/                    # 순수 Kotlin, 플랫폼 독립
├── demo-cli/                # JVM 데모
├── android-impl/            # Android 전용 구현 (추가 가능)
│   ├── MLKitDetector
│   └── TFLiteClassifier
└── desktop-impl/            # Desktop 전용 구현 (추가 가능)
    ├── OpenCVDetector
    └── PyTorchClassifier
```

## 테스트 용이성

### Dummy 구현으로 전체 플로우 테스트

```kotlin
// DummyPieceClassifier는 항상 시작 포지션 반환
class DummyPieceClassifier : PieceClassifier {
    override fun predict(patchBytes: ByteArray): Prediction {
        val idx = callIndex++
        val rank = 8 - (idx / 8)
        val file = 'a' + (idx % 8)
        return Prediction(stdStartLabel(rank, file), 1.0)
    }
}
```

실제 ML 모델이나 OpenCV 없이도:
- 빌드 가능
- 실행 가능
- 전체 로직 검증 가능

## 함수형 API

간단한 사용을 위한 wrapper 함수:

```kotlin
fun convertImageToFen(
    imageBytes: ByteArray,
    detector: BoardDetector,
    grid: GridExtractor,
    classifier: PieceClassifier
): String = FenEngine(detector, grid, classifier).imageToFen(imageBytes)
```

**사용**:
```kotlin
val fen = convertImageToFen(imageBytes, myDetector, myGrid, myClassifier)
```

## 정리

### 핵심 설계 원칙

1. **의존성 역전 (Dependency Inversion)**
   - 구현이 아닌 인터페이스에 의존

2. **단일 책임 (Single Responsibility)**
   - 각 클래스/인터페이스는 하나의 역할만

3. **개방-폐쇄 (Open-Closed)**
   - 확장에는 열려있고 (새 구현 추가)
   - 수정에는 닫혀있음 (기존 코드 변경 없음)

### 실무 적용

이 패턴은 실제 안드로이드/iOS 앱 개발에서 자주 쓰입니다:
- Repository 패턴
- ViewModel과 UseCase 분리
- Clean Architecture

우리 프로젝트는 이런 실무 패턴을 단순화한 버전입니다.
