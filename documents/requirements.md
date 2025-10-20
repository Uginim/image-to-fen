됐다! 모듈을 **함수형**이랑 **명령어(CLI)** 두 방식으로 바로 쓸 수 있게 캔버스에 추가해놨어.

## 지금 바로 쓰는 법

### 1) 함수 형태 (순수 Kotlin)

```kotlin
val fen = convertImageToFen(
    imageBytes = Files.readAllBytes(Paths.get("board.jpg")),
    detector = YourBoardDetectorImpl(),
    grid = YourGridExtractorImpl(),
    classifier = YourPieceClassifierImpl()
)
println(fen)
```

* 파일 위치: `:engine/src/main/kotlin/.../engine/Api.kt`
* 역할: 엔진에 구현체를 주입해서 **한 줄로 FEN** 얻기.

### 2) 명령어 형태 (CLI)

루트에서:

```bash
./gradlew :cli:run --args='convert -i path/to/image.jpg'
# 표준출력으로 FEN 출력

./gradlew :cli:run --args='convert -i path/to/image.jpg -o fen.txt'
# 파일로 저장
```

* 바이너리 이름: `fenconv` (내부 ArgParser 명)
* 서브커맨드:

    * `convert -i <이미지> [-o <출력파일>]`
    * `detect -i <이미지>` (디버그용, 현재는 플레이스홀더)

> 초기엔 더미 구현(Dummy*)을 물려놨어. 실제로 돌리려면 `YourBoardDetectorImpl`/`YourPieceClassifierImpl` 같은 **플랫폼 구현**만 교체하면 돼. 필요하면 Android용/데스크탑용 구현 골격도 바로 붙여줄게.
