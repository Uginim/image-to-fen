# 체스 좌표 시스템

## 체스판 좌표

체스판은 **파일(file)**과 **랭크(rank)**로 구성됩니다.

```
   a  b  c  d  e  f  g  h
8  ♜  ♞  ♝  ♛  ♚  ♝  ♞  ♜   rank 8
7  ♟  ♟  ♟  ♟  ♟  ♟  ♟  ♟   rank 7
6  ·  ·  ·  ·  ·  ·  ·  ·   rank 6
5  ·  ·  ·  ·  ·  ·  ·  ·   rank 5
4  ·  ·  ·  ·  ·  ·  ·  ·   rank 4
3  ·  ·  ·  ·  ·  ·  ·  ·   rank 3
2  ♙  ♙  ♙  ♙  ♙  ♙  ♙  ♙   rank 2
1  ♖  ♘  ♗  ♕  ♔  ♗  ♘  ♖   rank 1
   a  b  c  d  e  f  g  h
```

- **파일 (file)**: 세로줄, a-h (왼쪽→오른쪽)
- **랭크 (rank)**: 가로줄, 1-8 (아래→위)

### 예시

- `e4`: e파일, 4랭크 (중앙 아래쪽)
- `a8`: a파일, 8랭크 (왼쪽 위 코너)
- `h1`: h파일, 1랭크 (오른쪽 아래 코너)

## 코드에서의 표현

### SquareCoord

```kotlin
data class SquareCoord(val file: Char, val rank: Int)
```

**예시**:
```kotlin
val e4 = SquareCoord('e', 4)
val a8 = SquareCoord('a', 8)
```

**제약 조건**:
- `file`: 'a'..'h'
- `rank`: 1..8

### BoardCell

```kotlin
data class BoardCell(val file: Char, val rank: Int, val fenChar: Char) {
    init {
        require(file in 'a'..'h')
        require(rank in 1..8)
    }
}
```

`SquareCoord`에 기물 정보(`fenChar`)를 추가한 버전입니다.

## FEN vs 배열 인덱싱

### FEN 순서 (rank 8 → rank 1)

FEN 문자열은 **위에서 아래로**:

```
rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR
↑ rank 8                              ↑ rank 1
```

### 배열 인덱싱 (0-based)

우리 코드의 `matrix`:

```kotlin
val matrix = Array(8) { CharArray(8) { '1' } }
```

- `matrix[0]` = rank 8
- `matrix[1]` = rank 7
- ...
- `matrix[7]` = rank 1

### 변환 공식

```kotlin
// 체스 좌표 → 배열 인덱스
val fileIdx = p.coord.file - 'a'        // 'a'=0, 'b'=1, ..., 'h'=7
val rankIdxFromTop = 8 - p.coord.rank   // rank 8=0, rank 7=1, ..., rank 1=7
```

**예시**:
- `SquareCoord('a', 8)` → `matrix[0][0]`
- `SquareCoord('h', 8)` → `matrix[0][7]`
- `SquareCoord('a', 1)` → `matrix[7][0]`
- `SquareCoord('h', 1)` → `matrix[7][7]`
- `SquareCoord('e', 4)` → `matrix[4][4]`

## GridExtractor의 순회 순서

`DummyGridExtractor`는 이렇게 64칸을 생성합니다:

```kotlin
for (r in 8 downTo 1) {          // rank 8 → 1
    for (f in 'a'..'h') {        // file a → h
        out += SquarePatch(SquareCoord(f, r), pixels = byteArrayOf())
    }
}
```

**순서**:
```
a8, b8, c8, d8, e8, f8, g8, h8,  (rank 8)
a7, b7, c7, d7, e7, f7, g7, h7,  (rank 7)
...
a1, b1, c1, d1, e1, f1, g1, h1   (rank 1)
```

총 64개 = 8 × 8

## FenEngine에서의 처리

```kotlin
val matrix = Array(8) { CharArray(8) { '1' } }
for (p in patches) {
    val pred = classifier.predict(p.pixels)
    val fileIdx = p.coord.file - 'a'           // 0~7
    val rankIdxFromTop = 8 - p.coord.rank      // 0~7
    matrix[rankIdxFromTop][fileIdx] = pred.label.fenChar ?: '1'
}
```

### 예시 처리

패치 순서와 매트릭스 매핑:

```
patch[0] = a8 → matrix[0][0]
patch[1] = b8 → matrix[0][1]
...
patch[7] = h8 → matrix[0][7]
patch[8] = a7 → matrix[1][0]
...
patch[63] = h1 → matrix[7][7]
```

## 시각적 정리

```
체스판 시각 (플레이어 시점)     배열 인덱스             파일/랭크
┌─────────────────┐         matrix[0][0..7]      a8..h8
│ 8 ♜ ♞ ♝ ♛ ♚ ♝ ♞ ♜│  ←→    matrix[0]        ←→  rank 8
│ 7 ♟ ♟ ♟ ♟ ♟ ♟ ♟ ♟│  ←→    matrix[1]        ←→  rank 7
│ 6 · · · · · · · ·│  ←→    matrix[2]        ←→  rank 6
│ 5 · · · · · · · ·│  ←→    matrix[3]        ←→  rank 5
│ 4 · · · · · · · ·│  ←→    matrix[4]        ←→  rank 4
│ 3 · · · · · · · ·│  ←→    matrix[5]        ←→  rank 3
│ 2 ♙ ♙ ♙ ♙ ♙ ♙ ♙ ♙│  ←→    matrix[6]        ←→  rank 2
│ 1 ♖ ♘ ♗ ♕ ♔ ♗ ♘ ♖│  ←→    matrix[7]        ←→  rank 1
└─────────────────┘
  a b c d e f g h           [0][1][2][3][4][5][6][7]
```

## 주의사항

### 흔한 실수

```kotlin
// ❌ 틀린 예: rank를 그대로 인덱스로 사용
matrix[p.coord.rank][fileIdx] = ...  // rank 8이면 index 8 → 범위 초과!

// ✅ 올바른 예: rank를 변환해서 사용
val rankIdx = 8 - p.coord.rank
matrix[rankIdx][fileIdx] = ...
```

### 디버깅 팁

좌표 확인 시:
```kotlin
println("Processing ${p.coord.file}${p.coord.rank}")
println("  → matrix[$rankIdxFromTop][$fileIdx]")
```

## 실제 이미지 처리 시

카메라로 찍은 이미지는:
- **SideHint**가 필요할 수 있음
  - `WHITE_BOTTOM`: 백이 아래쪽 (일반적)
  - `BLACK_BOTTOM`: 흑이 아래쪽 (뒤집혀있음)

- `BoardDetector`가 이를 감지하고 정규화해야 함

```kotlin
data class BoardWarpResult(
    val normalizedBoard: ByteArray,
    val sideHint: SideHint? = null  // WHITE_BOTTOM 또는 BLACK_BOTTOM
)
```

정규화된 보드는 **항상 WHITE_BOTTOM 기준**으로 처리됩니다.
