# 사용 가이드

## 빠른 시작

### 1. 프로젝트 빌드

```bash
./gradlew build
```

### 2. 데모 실행

```bash
./gradlew :demo-cli:run
```

**출력**:
```
Generated FEN: rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 1
```

## 라이브러리로 사용하기

### 의존성 추가

#### 같은 프로젝트 내에서

```kotlin
// your-module/build.gradle.kts
dependencies {
    implementation(project(":core"))
}
```

#### 외부 프로젝트에서 (로컬 모듈로)

1. `:core` 모듈을 당신의 프로젝트에 복사
2. `settings.gradle.kts`에 추가:
   ```kotlin
   include(":core")
   ```
3. 의존성 추가:
   ```kotlin
   dependencies {
       implementation(project(":core"))
   }
   ```

### 기본 사용법

#### 방법 1: FenEngine 클래스 사용

```kotlin
import com.example.fenvision.engine.FenEngine
import com.example.fenvision.vision.*
import com.example.fenvision.ml.*
import java.io.File

fun main() {
    // 1. 구현체 준비
    val detector: BoardDetector = MyBoardDetectorImpl()
    val grid: GridExtractor = MyGridExtractorImpl()
    val classifier: PieceClassifier = MyPieceClassifierImpl()

    // 2. 엔진 생성
    val engine = FenEngine(detector, grid, classifier)

    // 3. 이미지 → FEN
    val imageBytes = File("chessboard.jpg").readBytes()
    val fen = engine.imageToFen(imageBytes)

    println(fen)
}
```

#### 방법 2: 함수형 API 사용

```kotlin
import com.example.fenvision.engine.convertImageToFen

val fen = convertImageToFen(
    imageBytes = File("chessboard.jpg").readBytes(),
    detector = MyBoardDetectorImpl(),
    grid = MyGridExtractorImpl(),
    classifier = MyPieceClassifierImpl()
)
println(fen)
```

## 구현체 만들기

### 1. BoardDetector 구현

```kotlin
import com.example.fenvision.vision.*

class MyBoardDetectorImpl : BoardDetector {
    override fun detectAndNormalize(imageBytes: ByteArray): BoardWarpResult {
        // TODO: 이미지에서 체스판 찾기
        // - 코너 검출 (OpenCV Hough Transform, Canny Edge 등)
        // - 원근 변환으로 정면 보정
        // - 정규화된 이미지 반환 (예: 800x800 픽셀)

        val normalizedImage: ByteArray = ... // 정규화된 이미지
        return BoardWarpResult(
            normalizedBoard = normalizedImage,
            sideHint = SideHint.WHITE_BOTTOM
        )
    }
}
```

**구현 옵션**:
- OpenCV (Desktop/Android)
- ML Kit (Android)
- Core Image (iOS)
- 수동 영역 선택

### 2. GridExtractor 구현

```kotlin
import com.example.fenvision.vision.*

class MyGridExtractorImpl : GridExtractor {
    override fun split(board: BoardWarpResult): List<SquarePatch> {
        val patches = mutableListOf<SquarePatch>()

        // TODO: 800x800 이미지를 8×8 = 64개로 분할
        // 각 칸은 100x100 픽셀

        for (rank in 8 downTo 1) {
            for (file in 'a'..'h') {
                val pixels: ByteArray = extractSquarePixels(
                    board.normalizedBoard,
                    file, rank
                )
                patches += SquarePatch(
                    coord = SquareCoord(file, rank),
                    pixels = pixels
                )
            }
        }

        return patches
    }

    private fun extractSquarePixels(
        image: ByteArray,
        file: Char,
        rank: Int
    ): ByteArray {
        // TODO: 실제 이미지 분할 로직
        return ByteArray(0)
    }
}
```

### 3. PieceClassifier 구현

```kotlin
import com.example.fenvision.ml.*

class MyPieceClassifierImpl : PieceClassifier {
    private val model = loadModel() // TensorFlow Lite, PyTorch 등

    override fun predict(patchBytes: ByteArray): Prediction {
        // TODO: ML 모델로 기물 분류
        // 1. 이미지 전처리 (리사이즈, 정규화)
        // 2. 모델 추론
        // 3. 결과 해석

        val label: Label = ... // wK, bQ, EMPTY 등
        val confidence: Double = ... // 0.0 ~ 1.0

        return Prediction(label, confidence)
    }

    private fun loadModel() {
        // TODO: 모델 로드 (TFLite, PyTorch Mobile 등)
    }
}
```

**구현 옵션**:
- TensorFlow Lite (Android/iOS)
- PyTorch Mobile
- ONNX Runtime
- 클라우드 API

## 테스트하기

### 유닛 테스트

```kotlin
import org.junit.Test
import kotlin.test.assertEquals

class FenEngineTest {
    @Test
    fun `test starting position`() {
        val engine = FenEngine(
            detector = DummyBoardDetector(),
            grid = DummyGridExtractor(),
            classifier = DummyPieceClassifier()
        )

        val fen = engine.imageToFen(ByteArray(0))

        assertEquals(
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 1",
            fen
        )
    }
}
```

### 통합 테스트

```kotlin
class IntegrationTest {
    @Test
    fun `test with real image`() {
        val engine = FenEngine(
            detector = OpenCVBoardDetector(),
            grid = OpenCVGridExtractor(),
            classifier = TFLiteClassifier()
        )

        val imageBytes = loadTestImage("starting_position.jpg")
        val fen = engine.imageToFen(imageBytes)

        assertTrue(fen.startsWith("rnbqkbnr/pppppppp"))
    }
}
```

## 프로젝트 구조

```
your-project/
├── build.gradle.kts
├── settings.gradle.kts
├── core/                      # image-to-fen 라이브러리
│   └── src/main/kotlin/
│       └── com/example/fenvision/
│           ├── fen/
│           ├── vision/
│           ├── ml/
│           └── engine/
├── your-impl/                 # 당신의 구현
│   ├── build.gradle.kts
│   └── src/main/kotlin/
│       ├── MyBoardDetector.kt
│       ├── MyGridExtractor.kt
│       └── MyPieceClassifier.kt
└── your-app/                  # 실제 앱
    ├── build.gradle.kts       # depends on :core, :your-impl
    └── src/main/kotlin/
        └── Main.kt
```

## 고급 사용법

### 커스텀 FEN 필드

기본적으로 간단한 FEN만 생성하지만, 필요시 확장 가능:

```kotlin
val fen = FenBuilder.build(
    board = board,
    sideToMove = 'b',          // 흑 차례
    castling = "Kq",           // 백 킹사이드, 흑 퀸사이드만 가능
    enPassant = "e6",          // e6 칸에 앙파상 가능
    halfmove = 3,
    fullmove = 15
)
```

### 에러 처리

```kotlin
try {
    val fen = engine.imageToFen(imageBytes)
} catch (e: IllegalArgumentException) {
    println("Invalid board: ${e.message}")
} catch (e: Exception) {
    println("Detection failed: ${e.message}")
}
```

### 로깅

```kotlin
class LoggingFenEngine(
    detector: BoardDetector,
    grid: GridExtractor,
    classifier: PieceClassifier
) : FenEngine(detector, grid, classifier) {

    override fun imageToFen(imageBytes: ByteArray): String {
        println("Processing image (${imageBytes.size} bytes)...")
        val result = super.imageToFen(imageBytes)
        println("Generated FEN: $result")
        return result
    }
}
```

## 다음 단계

1. **ML 모델 학습**
   - 체스 기물 이미지 데이터셋 수집
   - CNN 모델 학습 (TensorFlow, PyTorch)
   - 모델 변환 (TFLite, CoreML)

2. **실제 구현**
   - OpenCV로 보드 검출 구현
   - 이미지 분할 구현
   - ML 모델 통합

3. **앱 개발**
   - Android/iOS 앱 UI
   - 카메라 연동
   - FEN 결과 활용 (체스 엔진 연동 등)

## 참고 자료

- [OpenCV 튜토리얼](https://docs.opencv.org/4.x/d6/d00/tutorial_py_root.html)
- [TensorFlow Lite](https://www.tensorflow.org/lite)
- [Chess Programming Wiki](https://www.chessprogramming.org/)
