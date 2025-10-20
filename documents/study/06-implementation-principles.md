# 구현 원리 (Image-to-FEN Pipeline)

## 전체 파이프라인 개요

```
사진 촬영
   ↓
[1] 이미지 로딩 & 전처리
   ↓
[2] 체스판 검출 & 정규화
   ↓
[3] 64칸 분할
   ↓
[4] 각 칸 기물 분류 (ML)
   ↓
[5] FEN 문자열 생성
```

---

## [1] 이미지 로딩 & 전처리

### 원리

**문제**: 핸드폰으로 찍은 사진은 너무 크다 (4000x3000 픽셀 등)
- 메모리 많이 사용
- 처리 느림
- 배터리 소모

**해결**: 리사이징

```kotlin
// 원본: 4000x3000 (12MB)
// 리사이징: 800x600 (500KB)

fun loadAndResize(file: File, maxSize: Int = 800): ByteArray {
    val bitmap = BitmapFactory.decodeFile(file.path)

    // 비율 유지하며 리사이징
    val ratio = maxSize.toFloat() / max(bitmap.width, bitmap.height)
    val newWidth = (bitmap.width * ratio).toInt()
    val newHeight = (bitmap.height * ratio).toInt()

    val resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    bitmap.recycle()  // 메모리 해제

    return bitmapToByteArray(resized)
}
```

**왜 800px?**
- 너무 작으면 기물 디테일 손실
- 너무 크면 느려짐
- 800px는 균형점 (모바일 최적)

---

## [2] 체스판 검출 & 정규화

### 원리: 원근 변환 (Perspective Transform)

**문제**: 사진이 비스듬하게 찍힘

```
촬영한 사진:           원하는 결과:
    ╱────╲               ┌────┐
   ╱      ╲              │    │
  ╱  체스판 ╲      →     │체스판│  (정면)
 ╱          ╲            │    │
╱────────────╲           └────┘
```

### 2-1. 사용자 지정 방식 (간단, 정확)

**과정**:
1. 사용자가 체스판 4개 코너를 터치로 지정
2. 4개 점의 좌표 → (0,0), (800,0), (800,800), (0,800)로 변환

```kotlin
// 원근 변환 행렬 계산
val srcPoints = floatArrayOf(
    x1, y1,  // 왼쪽 위
    x2, y2,  // 오른쪽 위
    x3, y3,  // 오른쪽 아래
    x4, y4   // 왼쪽 아래
)

val dstPoints = floatArrayOf(
    0f, 0f,
    800f, 0f,
    800f, 800f,
    0f, 800f
)

val matrix = Matrix()
matrix.setPolyToPoly(srcPoints, 0, dstPoints, 0, 4)

// 변환 적용
val normalized = Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
```

**장점**:
- 매우 정확 (사용자가 직접 지정)
- 가볍고 빠름 (ML 불필요)
- 조명/배경에 영향 안 받음

**단점**:
- 수동 작업 필요 (한 번만 하면 됨)

### 2-2. 자동 검출 방식 (향후 개선)

**OpenCV 사용**:

```kotlin
// 1. 그레이스케일 변환
val gray = Imgproc.cvtColor(src, ColorConversionCodes.BGR2GRAY)

// 2. 엣지 검출 (Canny)
val edges = Imgproc.Canny(gray, 50, 150)

// 3. 선 검출 (Hough Transform)
val lines = Imgproc.HoughLinesP(edges, ...)

// 4. 교차점 찾기 → 코너 4개
val corners = findChessboardCorners(lines)

// 5. 원근 변환
val normalized = perspectiveTransform(src, corners)
```

**원리**:
- 체스판은 격자 무늬 → 많은 직선
- 직선들의 교차점 → 코너
- 4개 코너 찾으면 변환 가능

---

## [3] 64칸 분할

### 원리: 단순 기하학

정규화된 800x800 이미지를 64칸으로 나누기

```
800 ÷ 8 = 100픽셀

┌─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┐
│ a8  │ b8  │ c8  │ d8  │ e8  │ f8  │ g8  │ h8  │  각 칸: 100x100
│100x │     │     │     │     │     │     │     │
│100  │     │     │     │     │     │     │     │
├─────┼─────┼─────┼─────┼─────┼─────┼─────┼─────┤
│ a7  │ b7  │ ...
```

```kotlin
fun splitIntoSquares(board: Bitmap): List<SquarePatch> {
    val squareSize = 100  // 800 ÷ 8
    val patches = mutableListOf<SquarePatch>()

    for (rank in 8 downTo 1) {
        for (file in 'a'..'h') {
            val row = 8 - rank  // rank 8 = row 0
            val col = file - 'a'  // file 'a' = col 0

            val x = col * squareSize
            val y = row * squareSize

            // 100x100 영역 추출
            val patch = Bitmap.createBitmap(board, x, y, squareSize, squareSize)

            patches += SquarePatch(
                coord = SquareCoord(file, rank),
                pixels = bitmapToByteArray(patch)
            )
        }
    }

    return patches
}
```

**결과**: 64개의 100x100 이미지 패치

---

## [4] 기물 분류 (머신러닝)

### 4-1. 왜 머신러닝인가?

**문제**: 기물 모양이 다양함
- 체스 세트마다 디자인 다름
- 각도에 따라 모양 변함
- 조명에 따라 색상 변함

**규칙 기반으로는 한계**:
```kotlin
// ❌ 이런 식으로는 불가능
if (평균색상 == 검정색 && 높이 > 80%) {
    return "퀸"  // 틀릴 확률 높음
}
```

**머신러닝 접근**:
```
수천 개의 예시 이미지 학습 → 패턴 인식
```

### 4-2. CNN (Convolutional Neural Network) 원리

**핵심 아이디어**: 이미지의 특징을 단계적으로 추출

```
입력 이미지 (100x100x3)
   ↓
[합성곱 레이어 1] → 엣지 검출
   ↓
[합성곱 레이어 2] → 간단한 패턴 (곡선, 각도)
   ↓
[합성곱 레이어 3] → 복잡한 패턴 (기물 부위)
   ↓
[풀링 레이어] → 크기 축소, 중요한 특징만
   ↓
[완전 연결 레이어] → 분류 결정
   ↓
출력: [빈칸, wK, wQ, wR, ..., bK, bQ, ...] (13가지)
```

### 4-3. MobileNetV3 (경량 모델)

**일반 CNN 문제점**:
- 무거움 (50-100MB)
- 느림 (추론 500ms~1s)
- 배터리 많이 먹음

**MobileNetV3 해결책**:
```
- Depthwise Separable Convolution (연산량 1/9)
- Squeeze-and-Excitation (중요한 채널 강조)
- Hard-Swish 활성화 함수 (빠름)
```

**결과**:
- 모델 크기: 5MB
- 추론 시간: 50-100ms (저사양 기기)
- 정확도: 90%+

### 4-4. TensorFlow Lite 추론

```kotlin
// 1. 모델 로드
val model = Interpreter(loadModelFile("chess_piece_classifier.tflite"))

// 2. 입력 준비 (100x100x3 → 정규화)
val inputBuffer = ByteBuffer.allocateDirect(4 * 100 * 100 * 3)
inputBuffer.order(ByteOrder.nativeOrder())

for (pixel in patchPixels) {
    inputBuffer.putFloat(pixel.red / 255.0f)
    inputBuffer.putFloat(pixel.green / 255.0f)
    inputBuffer.putFloat(pixel.blue / 255.0f)
}

// 3. 추론
val outputBuffer = Array(1) { FloatArray(13) }  // 13개 클래스
model.run(inputBuffer, outputBuffer)

// 4. 결과 해석
val probabilities = outputBuffer[0]
val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] }!!
val label = indexToLabel(maxIndex)  // 0=빈칸, 1=wK, 2=wQ, ...
val confidence = probabilities[maxIndex]

return Prediction(label, confidence)
```

### 4-5. 양자화 (Quantization)

**원리**: Float32 → Int8 변환

```
일반 모델:
- 각 가중치: 32비트 (4바이트)
- 10만 개 가중치 = 400KB

양자화 모델:
- 각 가중치: 8비트 (1바이트)
- 10만 개 가중치 = 100KB
- 크기 1/4, 속도 2-3배
- 정확도 손실 < 1%
```

```python
# 양자화 (학습 후)
converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_model = converter.convert()
```

---

## [5] FEN 문자열 생성

### 원리: 매트릭스 → FEN 변환

이미 구현되어 있습니다! (`FenBuilder`)

```kotlin
// 64개 예측 결과 → 8x8 매트릭스
val matrix = Array(8) { CharArray(8) { '1' } }

for (patch in patches) {
    val prediction = classifier.predict(patch.pixels)
    val fileIdx = patch.coord.file - 'a'
    val rankIdx = 8 - patch.coord.rank
    matrix[rankIdx][fileIdx] = prediction.label.fenChar ?: '1'
}

// 매트릭스 → FEN
val fen = FenBuilder.build(matrix.toList())
```

---

## 전체 성능 분석

### 저사양 기기 (예: Galaxy A 시리즈)

| 단계 | 시간 | 메모리 | 설명 |
|-----|------|--------|------|
| 이미지 로딩 | 100ms | 5MB | 리사이징 포함 |
| 정규화 | 50ms | 3MB | 원근 변환 |
| 64칸 분할 | 10ms | 6MB | 단순 분할 |
| ML 추론 (64회) | 3-5초 | 10MB | 배치 처리로 최적화 가능 |
| FEN 생성 | 1ms | <1MB | 매우 가벼움 |
| **합계** | **4-6초** | **25MB** | 실용적! |

### 최적화 기법

**1. 배치 추론** (64개 한 번에)
```kotlin
// 느림: 64번 호출
for (patch in patches) {
    val result = model.run(patch)  // 64번 * 80ms = 5초
}

// 빠름: 1번 호출
val batchInput = Array(64) { patches[it].pixels }
val batchOutput = model.run(batchInput)  // 1번 * 1초 = 1초
```

**2. 멀티스레딩**
```kotlin
val results = patches.chunked(16).map { chunk ->
    async { chunk.map { classifier.predict(it.pixels) } }
}.awaitAll().flatten()
```

**3. 캐싱**
```kotlin
// 같은 이미지 다시 처리 안 함
val cache = mutableMapOf<ByteArray, Prediction>()
```

---

## 왜 이 방법인가?

### 다른 접근법 비교

**❌ 규칙 기반 (템플릿 매칭)**
- 정확도: 60-70%
- 특정 체스 세트에만 동작
- 조명 변화에 약함

**❌ 클라우드 API (Google Vision 등)**
- 인터넷 필요
- 비용 발생
- 느림 (네트워크 지연)
- 개인정보 유출 가능

**✅ 온디바이스 ML (우리 선택)**
- 정확도: 85-95%
- 오프라인 동작
- 무료
- 빠름 (로컬)
- 개인정보 안전

### 트레이드오프

| 항목 | 규칙 기반 | 온디바이스 ML | 클라우드 API |
|-----|----------|--------------|-------------|
| 정확도 | 60-70% | 85-95% | 95-99% |
| 속도 | 빠름 (1초) | 중간 (4-6초) | 느림 (10초+) |
| 메모리 | 적음 (10MB) | 중간 (25MB) | 적음 (5MB) |
| 인터넷 | 불필요 | 불필요 | 필수 |
| 비용 | 무료 | 무료 | 유료 |

---

## 핵심 정리

1. **원근 변환**으로 비스듬한 사진 → 정면 보정
2. **기하학적 분할**로 64칸 생성
3. **CNN (MobileNetV3)**로 각 칸 기물 분류
4. **TFLite 양자화**로 모바일 최적화
5. **FenBuilder**로 최종 FEN 생성

**전체 시간**: 4-6초 (저사양 기기)
**정확도**: 85-95% (테스트 필요)
**메모리**: 25MB (실용적)

---

## 다음 단계

이제 실제로 구현해봅시다!

1. Phase 1: 이미지 로딩 & 리사이징
2. Phase 2: 원근 변환 (수동 코너 지정)
3. Phase 3: 64칸 분할
4. Phase 5: ML 모델 통합

**준비되셨나요?** 🚀
