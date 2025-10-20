# FEN 표기법 (Forsyth-Edwards Notation)

## FEN이 뭔가요?

체스판의 상태를 **한 줄의 문자열**로 표현하는 표준 방법입니다.

## 예시

```
rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
```

이건 체스 시작 포지션을 나타냅니다.

## 구조 분석

FEN은 6개 필드로 구성됩니다:

```
[1. 보드] [2. 차례] [3. 캐슬링] [4. 앙파상] [5. 반수] [6. 수]
```

### 1. 보드 배치 (rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR)

**rank 8 (흑 백열)부터 rank 1 (백 백열)까지** 순서대로:

```
rank 8: rnbqkbnr  (흑의 주요 기물들)
rank 7: pppppppp  (흑 폰들)
rank 6: 8         (빈 칸 8개)
rank 5: 8         (빈 칸 8개)
rank 4: 8         (빈 칸 8개)
rank 3: 8         (빈 칸 8개)
rank 2: PPPPPPPP  (백 폰들)
rank 1: RNBQKBNR  (백의 주요 기물들)
```

**기물 문자**:
- 대문자 = 백 (White)
- 소문자 = 흑 (Black)

| 문자 | 기물 (영어) | 기물 (한국어) |
|------|------------|--------------|
| K/k  | King       | 킹           |
| Q/q  | Queen      | 퀸           |
| R/r  | Rook       | 룩           |
| B/b  | Bishop     | 비숍         |
| N/n  | Knight     | 나이트       |
| P/p  | Pawn       | 폰           |

**숫자**:
- 연속된 빈 칸의 개수
- 예: `8` = 빈 칸 8개, `3` = 빈 칸 3개

### 2. 차례 (w)

- `w` = White (백 차례)
- `b` = Black (흑 차례)

### 3. 캐슬링 권한 (KQkq)

- `K` = 백 킹사이드 캐슬링 가능
- `Q` = 백 퀸사이드 캐슬링 가능
- `k` = 흑 킹사이드 캐슬링 가능
- `q` = 흑 퀸사이드 캐슬링 가능
- `-` = 캐슬링 불가능

### 4. 앙파상 타겟 (-)

- 앙파상 가능한 칸 (예: `e3`)
- `-` = 앙파상 불가능

### 5. 반수 카운터 (0)

무승부 규칙용. 폰이 움직이거나 기물이 잡히지 않은 수 카운트.

### 6. 전체 수 (1)

몇 번째 턴인지. 흑이 한 수 두면 +1.

## 우리 프로젝트에서는?

`FenBuilder.build()` 함수가 이 형식으로 변환해줍니다:

```kotlin
val board: List<List<Char>> = ... // 8x8 배열
val fen = FenBuilder.build(
    board = board,
    sideToMove = 'w',
    castling = "KQkq",
    enPassant = "-",
    halfmove = 0,
    fullmove = 1
)
```

### 빈 칸 압축

우리 코드는 내부적으로 빈 칸을 `'1'` 문자로 표현합니다:

```kotlin
// 내부 표현
['1', '1', '1', 'R', '1', '1', '1', '1']

// FEN 출력
"3R4"  // 빈칸 3개 + 룩 + 빈칸 4개
```

`FenBuilder`의 `compressRow()` 함수가 자동으로 처리합니다.

## 예제: 중간 게임

```
r1bqkb1r/pppp1ppp/2n2n2/1B2p3/4P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 4 4
```

이걸 해석하면:
- rank 8: `r1bqkb1r` (흑 룩, 빈칸, 흑 비숍, 흑 퀸, 흑 킹, 흑 비숍, 빈칸, 흑 룩)
- rank 7: `pppp1ppp` (폰들과 빈칸)
- rank 6: `2n2n2` (빈칸 2 + 나이트 + 빈칸 2 + 나이트 + 빈칸 2)
- ...
- 백 차례 (`w`)
- 양쪽 캐슬링 가능 (`KQkq`)
- 앙파상 없음 (`-`)
- 반수 4수 (`4`)
- 전체 4턴 (`4`)

## 참고 자료

- [Wikipedia - FEN](https://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation)
- [Chess.com - FEN 설명](https://www.chess.com/terms/fen-chess)
