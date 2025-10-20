# 프로젝트 개요

## 무엇을 만들었나요?

체스판 이미지를 입력받아 **FEN (Forsyth-Edwards Notation)** 문자열로 변환하는 라이브러리입니다.

```
이미지 → 보드 검출 → 64칸 분할 → 기물 인식 → FEN 문자열
```

## 모듈 구조

프로젝트는 **2개 모듈**로 구성되어 있습니다:

### 1. `:core` - 핵심 라이브러리

다른 프로젝트에서 **모듈로 가져다 쓸 수 있는** 핵심 코드입니다.

```
core/
├── fen/         - FEN 문자열 생성
├── vision/      - 이미지 처리 인터페이스
├── ml/          - 기물 분류 인터페이스
└── engine/      - 전체 파이프라인 조합
```

**특징**:
- 순수 Kotlin 코드만 사용
- 플랫폼 독립적 (Android, Desktop, Server 어디서나 사용 가능)
- 외부 라이브러리 의존성 없음 (인터페이스만 정의)

### 2. `:demo-cli` - 데모/테스트

실제로 실행해볼 수 있는 데모 앱입니다.

```
demo-cli/
└── demo/
    ├── Main.kt                  - 실행 진입점
    ├── DummyBoardDetector       - 테스트용 보드 검출기
    ├── DummyGridExtractor       - 테스트용 격자 분할기
    └── DummyPieceClassifier     - 테스트용 기물 분류기
```

**역할**:
- `:core` 모듈 사용 예제
- 실제 구현체 없이도 전체 플로우 테스트
- 체스 시작 포지션 FEN 출력 데모

## 왜 이렇게 나눴나요?

### 모듈로 나눈 이유

1. **재사용성**: 다른 프로젝트에서 `:core`만 dependency로 추가하면 됨
2. **테스트 용이성**: 더미 구현으로 쉽게 테스트 가능
3. **플랫폼 독립성**:
   - `:core`는 순수 Kotlin (어디서나 사용 가능)
   - 실제 구현은 각 플랫폼에서 제공 (Android에서 TensorFlow Lite, Desktop에서 OpenCV 등)

### 인터페이스 기반 설계

`:core`는 실제 구현을 포함하지 않고 **인터페이스만** 정의합니다:

- `BoardDetector`: 이미지에서 체스판 검출 (실제 구현: OpenCV, ML Kit 등)
- `GridExtractor`: 체스판을 64칸으로 분할 (실제 구현: 이미지 처리 라이브러리)
- `PieceClassifier`: 각 칸의 기물 인식 (실제 구현: TensorFlow Lite, PyTorch Mobile 등)

## 사용 예시

### 라이브러리로 사용하기

```kotlin
// 당신의 프로젝트 build.gradle.kts
dependencies {
    implementation(project(":core"))
}

// 당신의 코드
import com.example.fenvision.engine.convertImageToFen

val imageBytes = File("chessboard.jpg").readBytes()
val fen = convertImageToFen(
    imageBytes = imageBytes,
    detector = MyOpenCVDetector(),      // 당신이 구현
    grid = MyGridExtractor(),           // 당신이 구현
    classifier = MyTFLiteClassifier()   // 당신이 구현
)
println(fen)  // "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 1"
```

### 데모 실행하기

```bash
# 프로젝트 빌드
./gradlew build

# 데모 실행 (더미 구현 사용)
./gradlew :demo-cli:run
```

## 다음 학습 자료

- `01-fen-notation.md` - FEN 표기법이 무엇인지
- `02-architecture.md` - 왜 이런 구조로 설계했는지
- `03-coordinates.md` - 체스 좌표 시스템
- `04-usage-guide.md` - 실제로 어떻게 사용하는지
