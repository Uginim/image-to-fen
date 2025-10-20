# 테스트 데이터

이 폴더는 Image-to-FEN 시스템의 테스트 데이터를 포함합니다.

## 폴더 구조

```
datas/
├── case1/
│   ├── image1.png         # 체스판 이미지
│   └── image1_fen.txt     # 정답 FEN 문자열
├── case2/
│   ├── image2.png
│   └── image2_fen.txt
└── ...
```

## 테스트 케이스 추가 방법

### 1. 새 케이스 폴더 생성

```bash
mkdir -p datas/case2
```

### 2. 이미지 추가

체스판 사진을 촬영하거나 생성:
- 파일명: `imageN.png` (또는 `.jpg`)
- 권장 크기: 400×400 ~ 800×800 픽셀
- 체스판이 명확하게 보이도록

### 3. 정답 FEN 작성

```bash
# datas/case2/image2_fen.txt
rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
```

**FEN 생성 도구**:
- [lichess.org/editor](https://lichess.org/editor) - 웹에서 보드 설정 후 FEN 복사
- [chess.com/analysis](https://www.chess.com/analysis) - 보드 설정 후 FEN 복사

## 현재 테스트 케이스

### case1
- **이미지**: `image1.png` (470×480)
- **FEN**: `rnbqkbnr/p4ppp/1p1pp3/2p5/3PP3/2P2N2/PP3PPP/RNBQKB1R b KQkq - 0 5`
- **설명**: 5수째 진행된 게임 (English Opening 변형)
- **난이도**: 중간
- **특징**: 백 나이트가 f3에, 중앙 폰 구조 형성

## 테스트 케이스 가이드라인

### 다양한 시나리오 커버

1. **시작 포지션**
   ```
   rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
   ```

2. **중반전** (기물 교환 있음)
   ```
   r1bqkb1r/pppp1ppp/2n2n2/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 4 4
   ```

3. **엔드게임** (기물 적음)
   ```
   8/8/4k3/8/8/4K3/8/8 w - - 0 1
   ```

4. **복잡한 포지션** (캐슬링, 앙파상)
   ```
   rnbqk2r/pppp1ppp/5n2/2b1p3/2B1P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 4 4
   ```

### 이미지 품질 기준

- ✅ 체스판이 전체가 보임
- ✅ 조명이 균일함
- ✅ 기물이 칸 중앙에 위치
- ✅ 각도가 정면에 가까움
- ❌ 그림자가 심함
- ❌ 블러가 심함
- ❌ 체스판이 잘림

## 테스트 실행 방법

### 수동 테스트

```bash
# 실제 구현 완료 후
./gradlew :demo-cli:run --args='convert -i datas/case1/image1.png'
```

### 자동 테스트 (향후 구현)

```kotlin
@Test
fun testCase1() {
    val imageBytes = File("datas/case1/image1.png").readBytes()
    val expectedFen = File("datas/case1/image1_fen.txt").readText().trim()

    val actualFen = engine.imageToFen(imageBytes)

    assertEquals(expectedFen, actualFen)
}
```

## 라이선스

테스트 이미지는 프로젝트 테스트 목적으로만 사용됩니다.
