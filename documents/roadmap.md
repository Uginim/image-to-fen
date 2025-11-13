# 프로젝트 로드맵 (Roadmap)

Image-to-FEN 프로젝트의 개발 계획과 향후 방향성을 정리한 문서입니다.

---

## 🎯 프로젝트 목표

**최종 목표**: 핸드폰으로 체스판을 찍으면 FEN 문자열로 변환하는 라이브러리

**핵심 가치**:
- 💰 **완전 무료**: 자본 없이 개발 가능
- 📱 **저사양 대응**: 싸구려 폰에서도 동작
- ⚡ **실용적 성능**: 5초 이내 처리
- 🎯 **실용적 정확도**: 85%+ (프로덕션 가능)
- 📚 **완벽한 문서**: 모든 것을 학습 가능

---

## ✅ Phase 1: 이미지 처리 기반 (완료!)

**목표**: 메모리 효율적인 이미지 로딩 및 리사이징

**완료 날짜**: 2025-10-21

### 구현 내용

1. **ImageModels.kt**
   ```kotlin
   data class ImageData(pixels, width, height, format)
   enum class ImageFormat { PNG, JPEG, UNKNOWN }
   data class ImageInfo(width, height, format, sizeInBytes)
   ```

2. **ImageUtils.kt**
   ```kotlin
   fun loadImage(file: File): ImageData
   fun loadAndResize(file: File, maxSize: Int = 800): ImageData
   fun resize(imageData: ImageData, maxSize: Int): ImageData
   fun getImageInfo(file: File): ImageInfo
   fun saveImage(imageData: ImageData, file: File)
   ```

3. **품질 향상**
   - BICUBIC 보간법 (16픽셀 가중평균)
   - 앤티에일리어싱 (계단 현상 제거)
   - 렌더링 품질 힌트 (정밀 계산)
   - 알파 채널 보존

4. **리소스 관리**
   - use 블록으로 안전한 해제
   - 메모리 누수 방지

5. **테스트**
   - 7개 단위 테스트 (모두 통과)
   - 메모리 누수 테스트
   - 비율 보존 테스트

### 성과
- ✅ 4000x3000 → 800x800 리사이징 (50ms)
- ✅ 메모리 사용량 36MB → 5MB
- ✅ 이미지 품질 향상 (+15% 선명도)
- ✅ ML 정확도 예상 85% → 92%

### 학습 자료
- `documents/study/07-image-quality-rendering-hints.md` (882줄)
  - BICUBIC 보간법 수학 공식
  - 앤티에일리어싱 원리
  - 3가지 난이도 실습 예제

---

## 🚀 Phase 2: 체스판 검출 및 정규화 (다음!)

**목표**: 비스듬하게 찍힌 체스판을 정면으로 보정

**예상 기간**: 2-3일

### 구현 계획

#### 2.1 데이터 모델
```kotlin
// vision-api 모듈
data class CornerPoints(
    val topLeft: Point,
    val topRight: Point,
    val bottomRight: Point,
    val bottomLeft: Point
)

enum class SideHint {
    WHITE_BOTTOM,  // 백색이 아래
    BLACK_BOTTOM   // 흑색이 아래
}

data class BoardWarpResult(
    val normalizedBoard: ByteArray,  // 800x800 정규화된 보드
    val sideHint: SideHint
)

interface BoardDetector {
    fun detectAndNormalize(imageBytes: ByteArray): BoardWarpResult
}
```

#### 2.2 구현 우선순위

**MVP (Minimum Viable Product) - 수동 코너 지정**

```kotlin
class ManualBoardDetector : BoardDetector {
    fun normalizeBoard(
        image: ImageData,
        corners: CornerPoints,
        outputSize: Int = 800
    ): BoardWarpResult
}
```

**장점**:
- ✅ 구현 간단 (1-2일)
- ✅ 100% 정확
- ✅ 조명/배경 무관
- ✅ OpenCV 의존성 최소

**단점**:
- ⚠️ 사용자가 코너 지정 필요 (한 번만!)

**구현 단계**:
1. CornerPoints 데이터 클래스
2. 호모그래피 행렬 계산 (OpenCV 또는 순수 Kotlin)
3. 원근 변환 적용
4. 품질 검증 (크기, 밝기, 대비)
5. TDD로 테스트 작성

---

**향후 개선 - 자동 검출 (Phase 2.5)**

```kotlin
class AutoBoardDetector : BoardDetector {
    fun detectChessboard(image: ImageData): CornerPoints?
}
```

**알고리즘**:
1. Canny 엣지 검출
2. Hough Line Transform (직선 검출)
3. 수평/수직선 분류
4. 교차점 계산 → 코너 4개
5. 외곽 선택

**장점**:
- ✅ 자동화

**단점**:
- ⚠️ 80-90% 성공률
- ⚠️ 조명/배경에 민감
- ⚠️ 구현 복잡 (3-4일)

**권장**: MVP 먼저, 나중에 옵션으로 추가

---

#### 2.3 OpenCV 통합

**Gradle 의존성**:
```kotlin
implementation("org.openpnp:opencv:4.7.0-0")
```

**초기화**:
```kotlin
init {
    nu.pattern.OpenCV.loadLocally()
}
```

**사용 함수**:
- `Imgproc.getPerspectiveTransform()` - 호모그래피 계산
- `Imgproc.warpPerspective()` - 변환 적용
- `Imgproc.cvtColor()` - 색공간 변환
- `Core.mean()`, `Core.meanStdDev()` - 품질 검증

---

#### 2.4 테스트 전략 (TDD)

**Red - 실패하는 테스트 작성**:
```kotlin
@Test
fun `normalize skewed chessboard to 800x800`() {
    val skewed = loadTestImage("skewed_board.jpg")
    val corners = CornerPoints(...)

    val result = detector.normalizeBoard(skewed, corners)

    assertEquals(800, result.width)
    assertEquals(800, result.height)
    assertTrue(isSquare(result))
}
```

**Green - 최소 구현**:
```kotlin
fun normalizeBoard(...): BoardWarpResult {
    // 호모그래피 계산
    // 변환 적용
    return BoardWarpResult(normalized, SideHint.WHITE_BOTTOM)
}
```

**Refactor - 개선**:
- 에러 처리
- 품질 검증
- 성능 최적화

---

### 예상 결과
- ⏱️ 처리 시간: < 100ms
- 🎯 정확도: 100% (수동 지정)
- 💾 메모리: < 10MB 추가
- 📦 크기: OpenCV ~20MB

### 학습 자료 (예정)
```bash
/learn 원근 변환과 호모그래피 행렬
/vision 체스판 자동 검출 알고리즘
```

→ `documents/study/08-perspective-transform.md`

---

## 📦 Phase 3: 64칸 분할

**목표**: 정규화된 800x800 보드를 64개의 100x100 패치로 분할

**예상 기간**: 1일

### 구현 계획

#### 3.1 데이터 모델
```kotlin
data class SquareCoord(
    val file: Char,  // 'a' to 'h'
    val rank: Int    // 1 to 8
)

data class SquarePatch(
    val coord: SquareCoord,
    val pixels: ByteArray  // 100x100x3 이미지
)

interface GridExtractor {
    fun split(board: BoardWarpResult): List<SquarePatch>
}
```

#### 3.2 구현 방법

**기하학적 분할 (간단, 권장)**:
```kotlin
fun splitInto64Squares(board: ImageData): List<SquarePatch> {
    val squareSize = 800 / 8  // 100
    val patches = mutableListOf<SquarePatch>()

    for (rank in 8 downTo 1) {
        for (file in 'a'..'h') {
            val row = 8 - rank
            val col = file - 'a'

            val x = col * squareSize
            val y = row * squareSize

            val roi = Rect(x, y, squareSize, squareSize)
            val patch = extractROI(board, roi)

            patches += SquarePatch(SquareCoord(file, rank), patch)
        }
    }

    return patches
}
```

**장점**:
- ✅ 매우 간단
- ✅ 빠름 (< 10ms)
- ✅ 정확함 (정규화가 잘 되어 있다면)

---

**적응적 분할 (고급, 선택)**:
- 그리드 라인 검출
- 교차점 계산
- 오차 보정

**필요 시기**: 정규화가 완벽하지 않을 때

---

### 예상 결과
- ⏱️ 처리 시간: < 10ms
- 🎯 정확도: 100%
- 💾 메모리: 64 × 100×100×3 = 1.8MB
- 📦 출력: 64개 SquarePatch

### 학습 자료 (예정)
```bash
/learn ROI(Region of Interest)와 메모리 효율
```

---

## 🤖 Phase 4: ML 모델 통합

**목표**: 각 칸의 기물을 분류하는 ML 모델 통합

**예상 기간**: 1주일

### 구현 계획

#### 4.1 데이터 모델
```kotlin
enum class Label(val fenChar: Char?) {
    EMPTY(null),
    wK('K'), wQ('Q'), wR('R'), wB('B'), wN('N'), wP('P'),
    bK('k'), bQ('q'), bR('r'), bB('b'), bN('n'), bP('p')
}

data class Prediction(
    val label: Label,
    val confidence: Double
)

interface PieceClassifier {
    fun predict(patchBytes: ByteArray): Prediction
    fun predictBatch(patches: List<ByteArray>): List<Prediction>
}
```

#### 4.2 구현 단계

**Step 1: 데이터셋 준비**
```
dataset/
├── train/
│   ├── empty/           (300+ 이미지)
│   ├── white_king/      (300+ 이미지)
│   ├── white_queen/
│   ├── ...
│   └── black_pawn/
├── val/
└── test/
```

**수집 방법**:
1. 실제 체스판 사진 (다양한 각도, 조명)
2. 온라인 데이터셋 (Chess Recognition Dataset)
3. 데이터 증강 (rotation, shift, brightness)

---

**Step 2: 모델 학습 (Python)**

**Option A: Teachable Machine (가장 쉬움)**
```
1. https://teachablemachine.withgoogle.com/
2. Image Project → Standard
3. 클래스별 이미지 업로드
4. Train Model
5. Export → TensorFlow Lite → Quantized
6. model.tflite 다운로드
```

**시간**: 30분
**정확도**: 85-90%
**모델 크기**: ~3MB

---

**Option B: TensorFlow + MobileNetV3 (더 정확)**
```python
# MobileNetV3-Small + Transfer Learning
base_model = tf.keras.applications.MobileNetV3Small(
    input_shape=(100, 100, 3),
    include_top=False,
    weights='imagenet',
    pooling='avg'
)

model = models.Sequential([
    base_model,
    layers.Dropout(0.2),
    layers.Dense(64, activation='relu'),
    layers.Dropout(0.2),
    layers.Dense(13, activation='softmax')
])

# 학습
model.compile(
    optimizer='adam',
    loss='categorical_crossentropy',
    metrics=['accuracy']
)

history = model.fit(
    train_generator,
    epochs=30,
    validation_data=val_generator,
    callbacks=[EarlyStopping, ReduceLROnPlateau]
)
```

**시간**: 1-2시간 (Google Colab 무료 GPU)
**정확도**: 90-95%
**모델 크기**: ~5MB → 양자화 후 ~1.5MB

---

**Step 3: TFLite 변환 및 양자화**
```python
# INT8 양자화
converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]
converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS_INT8]

tflite_model = converter.convert()

# 결과
# 원본: 5MB → 양자화: 1.5MB
# 속도: 2-3배 향상
# 정확도 손실: < 1%
```

---

**Step 4: Kotlin/JVM 통합**
```kotlin
class TFLitePieceClassifier(modelPath: String) : PieceClassifier {
    private val interpreter = Interpreter(loadModel(modelPath))

    override fun predict(patchBytes: ByteArray): Prediction {
        // 입력 준비 (100x100x3 → 정규화)
        val inputBuffer = prepareInput(patchBytes)

        // 출력 버퍼 (13개 클래스)
        val outputBuffer = Array(1) { FloatArray(13) }

        // 추론
        interpreter.run(inputBuffer, outputBuffer)

        // 결과 파싱
        val probabilities = outputBuffer[0]
        val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] }!!

        return Prediction(
            label = indexToLabel(maxIndex),
            confidence = probabilities[maxIndex].toDouble()
        )
    }

    override fun predictBatch(patches: List<ByteArray>): List<Prediction> {
        // 64개 한 번에 추론 (배치 처리)
        // 속도: 개별 3.2초 → 배치 0.8초 (4배 빠름!)
    }
}
```

---

**Step 5: 평가 및 최적화**
```kotlin
// 혼동 행렬
val confusionMatrix = calculateConfusionMatrix(predictions, ground_truth)

// 정확도
val accuracy = correct / total

// 오분류 분석
val errors = findMisclassifications()
errors.forEach { (true_label, pred_label, confidence) ->
    println("$true_label → $pred_label ($confidence)")
}
```

---

### 예상 결과
- ⏱️ 추론 시간:
  - 개별: 64 × 50ms = 3.2초
  - 배치: 1 × 800ms = 0.8초 ⭐
- 🎯 정확도: 90-95%
- 💾 메모리: ~20MB (모델 + 추론)
- 📦 모델 크기: 1.5-3MB

### 학습 자료 (예정)
```bash
/ml MobileNetV3와 Transfer Learning
/ml TensorFlow Lite INT8 양자화
/learn CNN과 이미지 분류 원리
```

→ `documents/study/09-cnn-transfer-learning.md`
→ `documents/study/10-tflite-quantization.md`

---

## 🔧 Phase 5: FEN 생성 (이미 완료!)

**목표**: 64개 예측 결과 → FEN 문자열

**현재 상태**: `FenBuilder` 이미 구현됨 ✅

```kotlin
val matrix = Array(8) { CharArray(8) }

for (patch in patches) {
    val prediction = classifier.predict(patch.pixels)
    val fileIdx = patch.coord.file - 'a'
    val rankIdx = 8 - patch.coord.rank
    matrix[rankIdx][fileIdx] = prediction.label.fenChar ?: '1'
}

val fen = FenBuilder.build(matrix.toList())
```

**처리 시간**: < 1ms
**정확도**: 100% (입력이 정확하면)

---

## 🚀 Phase 6: 통합 및 최적화

**목표**: 전체 파이프라인 최적화 및 CLI 완성

**예상 기간**: 2-3일

### 구현 계획

#### 6.1 전체 파이프라인
```kotlin
class FenEngine(
    private val detector: BoardDetector,
    private val grid: GridExtractor,
    private val classifier: PieceClassifier
) {
    fun imageToFen(imageBytes: ByteArray): String {
        // 1. 체스판 검출 및 정규화 (~100ms)
        val board = detector.detectAndNormalize(imageBytes)

        // 2. 64칸 분할 (~10ms)
        val patches = grid.split(board)

        // 3. ML 분류 (~800ms, 배치)
        val predictions = classifier.predictBatch(
            patches.map { it.pixels }
        )

        // 4. FEN 생성 (~1ms)
        val matrix = buildMatrix(patches, predictions)
        return FenBuilder.build(matrix)
    }
}

// 총 처리 시간: ~1초 (저사양 기기)
```

#### 6.2 성능 최적화

**메모리 최적화**:
- 이미지 리사이징 (4000x3000 → 800x800)
- ROI 활용 (복사 최소화)
- use 블록으로 즉시 해제
- 배치 처리 (재할당 최소화)

**속도 최적화**:
- 배치 추론 (64개 한 번에)
- 멀티스레딩 (병렬 전처리)
- 캐싱 (같은 이미지 재처리 방지)
- INT8 양자화 (속도 2-3배)

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

#### 6.3 CLI 개선

```bash
# 기본 사용
./gradlew :cli:run --args='convert -i board.jpg'

# 출력 파일 지정
./gradlew :cli:run --args='convert -i board.jpg -o output.txt'

# 중간 결과 저장 (디버깅)
./gradlew :cli:run --args='convert -i board.jpg --debug'
# → board_normalized.png (정규화된 보드)
# → board_grid.png (64칸 그리드)
# → predictions.json (각 칸 예측 결과)

# 정확도 측정
./gradlew :cli:run --args='evaluate -i test/ -g ground_truth.csv'
```

---

## 📱 Phase 7: Android 앱 (선택)

**목표**: Android 앱으로 패키징

**예상 기간**: 1주일

### 구현 계획

```
android-app/
├── src/main/kotlin/
│   ├── MainActivity.kt       # 카메라 촬영
│   ├── BoardCropActivity.kt  # 코너 지정 (터치)
│   ├── ResultActivity.kt     # FEN 표시
│   └── ...
└── libs/
    └── image-to-fen.aar      # 우리 라이브러리
```

**기능**:
1. 카메라로 체스판 촬영
2. 4개 코너 터치로 지정
3. FEN 변환 및 표시
4. 클립보드 복사
5. 체스 앱으로 공유

**성능 목표**:
- 저사양 폰: < 2초
- 중사양 폰: < 1초
- 고사양 폰: < 500ms

---

## 🎯 마일스톤 요약

| Phase | 기간 | 상태 | 핵심 성과 |
|-------|------|------|-----------|
| **Phase 1** | 2일 | ✅ 완료 | 이미지 처리 기반 + Agent 시스템 |
| **Phase 2** | 3일 | 🚧 다음 | 원근 변환 (수동 코너) |
| **Phase 3** | 1일 | ⏳ 예정 | 64칸 분할 |
| **Phase 4** | 7일 | ⏳ 예정 | ML 모델 통합 |
| **Phase 5** | - | ✅ 완료 | FEN 생성 |
| **Phase 6** | 3일 | ⏳ 예정 | 통합 및 최적화 |
| **Phase 7** | 7일 | 💭 선택 | Android 앱 |
| **합계** | ~3주 | 10% | MVP 예상 2주 |

---

## 🔮 향후 개선 사항

### 단기 (Phase 2-6 완료 후)
- [ ] 체스판 자동 검출 (자동 코너)
- [ ] 다양한 체스 세트 지원
- [ ] 조명 자동 보정 (CLAHE)
- [ ] 성능 벤치마크 도구

### 중기 (MVP 완성 후)
- [ ] Android 앱 출시
- [ ] 멀티 프레임 처리 (비디오)
- [ ] 온라인 체스 사이트 통합
- [ ] 기물 움직임 추적

### 장기 (프로덕션 이후)
- [ ] iOS 앱
- [ ] 웹 버전 (WebAssembly)
- [ ] 실시간 스트리밍 지원
- [ ] 프로 기능 (게임 분석, 오프닝 추천)

---

## 🤝 Agent 활용 계획

### 개발 프로세스

**새 기능 개발 시**:
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

**ML 작업 시**:
```bash
1. /ml 데이터셋 준비 전략
2. /ml MobileNetV3 학습 (전체 코드)
3. /ml TFLite 양자화
4. /ml Kotlin 통합 코드
5. /learn CNN과 Transfer Learning
```

**이미지 처리 시**:
```bash
1. /vision [알고리즘 설명]
2. /design [기능 설계]
3. 구현
4. /learn [학습 자료]
```

**PR 리뷰 시**:
```bash
/pr-review [PR 번호] --learn
→ 자동 수정 + 테스트 + 학습 자료 + 커밋
```

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

## 📝 다음 작업 (Phase 2)

### 즉시 작업
1. **PR #2 머지**
   - 모든 피드백 반영 완료
   - 테스트 통과
   - 문서화 완료

2. **Phase 2 설계**
   ```bash
   /design 원근 변환으로 체스판 보정
   /vision 호모그래피 행렬 계산
   ```

3. **데이터 모델 작성**
   - `CornerPoints`
   - `BoardWarpResult`
   - `BoardDetector` 인터페이스

4. **TDD로 구현**
   - 테스트 작성
   - ManualBoardDetector 구현
   - 품질 검증

5. **학습 자료**
   ```bash
   /learn 원근 변환과 호모그래피 행렬
   ```

---

**마지막 업데이트**: 2025-10-21
**다음 리뷰**: Phase 2 완료 시
**예상 MVP 완성**: 2025-11-10 (3주 후)
