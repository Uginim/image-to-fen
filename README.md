# Image-to-FEN

체스판 이미지를 FEN (Forsyth-Edwards Notation) 문자열로 변환하는 Kotlin 라이브러리

## 빠른 시작

### 1. 빌드

```bash
./gradlew build
```

### 2. 데모 실행

```bash
./gradlew :demo-cli:run
```

**출력 예시**:
```
Generated FEN: rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 1
```

이 데모는 더미 구현을 사용하여 체스 시작 포지션의 FEN을 생성합니다.

## 프로젝트 구조

```
image-to-fen/
├── core/                   # 핵심 라이브러리 (순수 Kotlin)
│   ├── fen/               # FEN 문자열 생성
│   ├── vision/            # 이미지 처리 인터페이스
│   ├── ml/                # 기물 분류 인터페이스
│   └── engine/            # 메인 파이프라인
│
├── demo-cli/              # 데모 애플리케이션
│   └── dummy 구현체      # OpenCV/ML 없이 실행 가능
│
└── documents/             # 문서
    ├── requirements.md    # 요구사항 (한국어)
    ├── skeleton.md        # 아키텍처 상세 설명
    └── study/             # 학습 자료
        ├── 00-overview.md
        ├── 01-fen-notation.md
        ├── 02-architecture.md
        ├── 03-coordinates.md
        ├── 04-usage-guide.md
        └── 05-multimodule.md
```

## 핵심 개념

### 파이프라인

```
이미지 → 체스판 검출 → 64칸 분할 → 기물 분류 → FEN 생성
```

### 인터페이스 기반 설계

`:core` 모듈은 인터페이스만 정의:
- `BoardDetector` - 체스판 검출
- `GridExtractor` - 64칸 분할
- `PieceClassifier` - 기물 분류

실제 구현은 외부에서 주입 (OpenCV, TensorFlow Lite 등)

## 사용 예시

### 라이브러리로 사용

```kotlin
import com.example.fenvision.engine.convertImageToFen
import java.io.File

val imageBytes = File("chessboard.jpg").readBytes()

val fen = convertImageToFen(
    imageBytes = imageBytes,
    detector = MyBoardDetector(),      // 당신의 구현
    grid = MyGridExtractor(),          // 당신의 구현
    classifier = MyPieceClassifier()   // 당신의 구현
)

println(fen)  // "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 1"
```

### Gradle 의존성 추가

```kotlin
// your-project/settings.gradle.kts
include(":core")

// your-module/build.gradle.kts
dependencies {
    implementation(project(":core"))
}
```

## 테스트

### 유닛 테스트

```bash
# 전체 테스트
./gradlew test

# 특정 모듈 테스트
./gradlew :core:test
./gradlew :demo-cli:test
```

### 테스트 데이터

`datas/` 폴더에 체스판 이미지와 정답 FEN을 추가하여 테스트할 수 있습니다:

```
datas/
├── case1/
│   ├── image1.png         # 체스판 이미지
│   └── image1_fen.txt     # 정답 FEN
└── case2/
    ├── image2.png
    └── image2_fen.txt
```

자세한 내용은 [datas/README.md](datas/README.md)를 참고하세요.

## 기술 스택

- **언어**: Kotlin 2.0.20
- **빌드 도구**: Gradle 8.13
- **JVM**: Java 17
- **테스트**: JUnit 5

## 학습 자료

프로젝트를 이해하기 위한 단계별 학습 문서:

1. **[00-overview.md](documents/study/00-overview.md)** - 프로젝트 전체 개요
2. **[01-fen-notation.md](documents/study/01-fen-notation.md)** - FEN 표기법 설명
3. **[02-architecture.md](documents/study/02-architecture.md)** - 아키텍처 패턴 (의존성 주입, 인터페이스 설계)
4. **[03-coordinates.md](documents/study/03-coordinates.md)** - 체스 좌표 시스템
5. **[04-usage-guide.md](documents/study/04-usage-guide.md)** - 상세 사용 가이드
6. **[05-multimodule.md](documents/study/05-multimodule.md)** - 멀티모듈 프로젝트 이해하기

## 다음 단계

현재 프로젝트는 인터페이스와 더미 구현만 포함하고 있습니다.

실제 체스판 이미지를 FEN으로 변환하려면:

1. **BoardDetector 구현** - OpenCV, ML Kit 등으로 체스판 검출
2. **GridExtractor 구현** - 이미지를 64칸으로 분할
3. **PieceClassifier 구현** - CNN 모델로 기물 분류 (TensorFlow Lite, PyTorch 등)

자세한 내용은 [04-usage-guide.md](documents/study/04-usage-guide.md)를 참고하세요.

## 라이선스

MIT License

## 기여

이슈와 PR은 언제나 환영합니다!
