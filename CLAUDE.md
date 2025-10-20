# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Image-to-FEN is a chess vision system that converts chessboard images to FEN (Forsyth-Edwards Notation) strings. The project uses a modular Kotlin architecture with dependency injection for testability and platform flexibility.

## Build Commands

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Run a specific module's tests
./gradlew :core:test
./gradlew :demo-cli:test

# Clean build
./gradlew clean build

# Run the demo CLI application
./gradlew :demo-cli:run
```

## Current Architecture

The project has a **2-module structure**:

```
:core        - All core logic (FEN, vision interfaces, ML interfaces, engine)
:demo-cli    - Demo application with dummy implementations
```

**Note**: `documents/skeleton.md` documents a planned 5-module split (`:fen`, `:vision-api`, `:ml-api`, `:engine`, `:demo-cli`), but the current implementation consolidates everything into `:core` for simplicity.

### Module: :core

Contains all core logic in package `com.example.fenvision`:

**`fen/` - FEN string generation**
- `FenBuilder.build()` - Converts 8×8 char matrix to FEN notation
- `BoardCell` - Data class for chess coordinates (file 'a'-'h', rank 1-8)

**`vision/` - Computer vision interfaces**
- `BoardDetector` - Interface: detects and normalizes chessboard from image
- `GridExtractor` - Interface: splits normalized board into 64 square patches
- `BoardWarpResult`, `SquarePatch`, `SquareCoord` - Data models
- Uses `ByteArray` as platform-agnostic image representation

**`ml/` - Machine learning interfaces**
- `PieceClassifier` - Interface: classifies chess pieces from image patches
- `Label` enum - Maps to FEN piece characters (K/Q/R/B/N/P and lowercase, plus EMPTY)
- `Prediction` - Data class with label and confidence

**`engine/` - Core conversion pipeline**
- `FenEngine` - Main class: imageToFen() runs detect → split → classify → build FEN
- `convertImageToFen()` - Functional-style convenience wrapper in `Api.kt`

### Module: :demo-cli

Runnable demo application with dummy implementations:
- `DummyBoardDetector`, `DummyGridExtractor`, `DummyPieceClassifier`
- Dummy classifier generates standard chess starting position FEN
- No external dependencies (OpenCV, TensorFlow, etc.) required

## Architecture Patterns

### Dependency Injection

All vision and ML components are injected into `FenEngine`:

```kotlin
val engine = FenEngine(
    detector = myBoardDetector,
    grid = myGridExtractor,
    classifier = myPieceClassifier
)
val fen = engine.imageToFen(imageBytes)
```

This enables:
- Platform-specific implementations (Android, Desktop, Server)
- Testing with dummy implementations
- Multiple ML model backends (TensorFlow Lite, PyTorch, etc.)

### Interface-Based Design

Core module defines only interfaces (`BoardDetector`, `GridExtractor`, `PieceClassifier`). Platform-specific implementations live outside `:core`, allowing:
- Unit testing without native libraries
- Multiple platform implementations simultaneously
- Easy swapping of detection/classification backends

### Pipeline Architecture

`FenEngine.imageToFen()` is a 4-stage pipeline:

1. **Board detection**: `detector.detectAndNormalize()` → `BoardWarpResult`
2. **Grid extraction**: `grid.split()` → 64 `SquarePatch` objects (a8 to h1)
3. **Piece classification**: `classifier.predict()` for each patch → `Prediction`
4. **FEN generation**: `FenBuilder.build()` → FEN string

Each stage is independently replaceable.

### FEN Coordinate System

- Board matrix: rank 8 (top row) to rank 1 (bottom row), file a-h (left to right)
- Empty squares represented as '1' char in matrix, compressed to digit runs in FEN
- `SquareCoord` uses chess notation: file ∈ {'a'..'h'}, rank ∈ {1..8}
- Index mapping: `rankIdxFromTop = 8 - rank`, `fileIdx = file - 'a'`

## Configuration

- **Kotlin Version**: 2.0.20
- **JVM Toolchain**: Java 17
- **Group**: com.example.fenvision
- **Version**: 0.1.0-SNAPSHOT

## Testing Strategy

Each module uses JUnit Platform for tests. Use dummy implementations to test engine logic without real ML models or image processing:

```kotlin
val engine = FenEngine(
    DummyBoardDetector(),
    DummyGridExtractor(),
    DummyPieceClassifier()
)
```

## Implementation Notes

When adding real implementations (OpenCV, TensorFlow Lite, etc.):
- Create separate platform-specific modules (e.g., `:opencv-impl`, `:android-impl`)
- Depend on `:core` and implement its interfaces
- Keep `:core` pure Kotlin with no external dependencies

## Reference Documents

- `documents/skeleton.md` - Planned 5-module architecture (future refactoring)
- `documents/requirements.md` - Korean usage examples
- `documents/study/02-architecture.md` - Detailed architectural patterns explanation (Korean)
- 앞으로 한국어로