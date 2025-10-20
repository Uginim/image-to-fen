# 멀티모듈 프로젝트

## 멀티모듈이 뭔가요?

하나의 프로젝트를 **여러 개의 독립적인 모듈로 나누는** 구조입니다.

### 단일 모듈 vs 멀티모듈

**단일 모듈 (전통적인 방식)**:
```
my-project/
├── build.gradle.kts
└── src/
    └── main/kotlin/
        ├── fen/
        ├── vision/
        ├── ml/
        └── engine/
```

모든 코드가 하나의 프로젝트에 있음 → 하나의 JAR/AAR 파일로 빌드됨

**멀티모듈 (우리 프로젝트)**:
```
image-to-fen/
├── build.gradle.kts          (루트 설정)
├── settings.gradle.kts       (모듈 선언)
├── core/                     (모듈 1)
│   ├── build.gradle.kts
│   └── src/main/kotlin/...
└── demo-cli/                 (모듈 2)
    ├── build.gradle.kts
    └── src/main/kotlin/...
```

각 모듈이 독립적인 프로젝트처럼 동작 → 각각 별도로 빌드 가능

## 왜 멀티모듈을 쓰나요?

### 1. 재사용성

**시나리오**: Android 앱과 Desktop 앱 둘 다 만들고 싶어요

```
image-to-fen/
├── core/               ← 공통 로직 (FEN, 인터페이스)
├── android-app/        ← Android 앱 (core 사용)
├── desktop-app/        ← Desktop 앱 (core 사용)
└── demo-cli/           ← CLI 데모 (core 사용)
```

`:core`만 잘 만들면 → 여러 플랫폼에서 재사용!

### 2. 의존성 분리

**문제**: 모든 코드가 한 곳에 있으면?

```kotlin
// 단일 모듈에서
class FenBuilder {
    // 순수 Kotlin 코드인데...
}

// 같은 모듈에
class OpenCVDetector {
    // OpenCV 라이브러리 필요! (용량 큼, 플랫폼 의존적)
}
```

→ FenBuilder만 쓰고 싶어도 OpenCV까지 다 끌려옴

**해결**: 모듈 분리

```
:core              → 순수 Kotlin만 (0개 외부 의존성)
:opencv-impl       → OpenCV 포함 (무거움)
:tflite-impl       → TensorFlow Lite 포함 (무거움)
```

→ 필요한 모듈만 선택해서 사용!

### 3. 빌드 속도

```
core/ 수정 없이 demo-cli/만 수정
→ core/ 리빌드 안 함 (캐시 사용)
→ demo-cli/만 빌드
→ 빠름! ⚡
```

### 4. 명확한 구조

코드가 모듈 단위로 나뉘어져 있으면:
- "FEN 생성 코드는 :fen 모듈에 있구나"
- "ML 인터페이스는 :ml-api에 있구나"
- 역할이 명확!

## 우리 프로젝트의 멀티모듈 구조

### settings.gradle.kts - 모듈 선언

```kotlin
// settings.gradle.kts
rootProject.name = "image-to-fen"
include(
    ":core",
    ":demo-cli"
)
```

**의미**:
- 프로젝트 이름은 "image-to-fen"
- 2개 모듈 포함: `:core`, `:demo-cli`
- Gradle은 `core/`, `demo-cli/` 폴더를 찾아서 모듈로 인식

### build.gradle.kts (루트) - 공통 설정

```kotlin
// build.gradle.kts (루트)
plugins {
    kotlin("jvm") version "2.0.20" apply false
}

allprojects {
    group = "com.example.fenvision"
    version = "0.1.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }

    extensions.configure<KotlinJvmProjectExtension> {
        jvmToolchain(17)
    }
}
```

**의미**:
- **plugins**: Kotlin 플러그인 버전 선언 (`apply false`는 "여기선 안 쓰고 하위 모듈에서 쓸게")
- **allprojects**: 루트 포함 모든 프로젝트에 적용 (그룹명, 버전)
- **subprojects**: 하위 모듈(`:core`, `:demo-cli`)에만 적용
  - Kotlin JVM 플러그인 적용
  - Maven Central에서 라이브러리 가져오기
  - Java 17 사용

### core/build.gradle.kts - 모듈별 설정

```kotlin
// core/build.gradle.kts
plugins {
    kotlin("jvm")  // 루트에서 이미 선언했으니 버전 안 써도 됨
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
```

**의미**:
- 이 모듈은 Kotlin JVM 사용
- 테스트 의존성만 추가
- 외부 라이브러리 없음 (순수 Kotlin!)

### demo-cli/build.gradle.kts - 모듈 의존성

```kotlin
// demo-cli/build.gradle.kts
plugins {
    kotlin("jvm")
    application  // ← 실행 가능한 앱
}

dependencies {
    implementation(project(":core"))  // ← core 모듈 사용!
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("com.example.fenvision.demo.MainKt")
}
```

**의미**:
- `:demo-cli`는 `:core`에 의존
- `application` 플러그인으로 실행 가능
- Main 함수 위치 지정

## 모듈 간 의존성

### 의존성 선언

```kotlin
// demo-cli/build.gradle.kts
dependencies {
    implementation(project(":core"))
}
```

→ `:demo-cli`에서 `:core`의 모든 public 클래스 사용 가능!

```kotlin
// demo-cli/src/.../Main.kt
import com.example.fenvision.engine.FenEngine  // ← :core에서 가져옴
import com.example.fenvision.ml.PieceClassifier

fun main() {
    val engine = FenEngine(...)  // ✅ 사용 가능!
}
```

### 의존성 방향

```
:demo-cli  ──depends on──>  :core
```

- `:demo-cli`는 `:core`를 사용할 수 있음
- `:core`는 `:demo-cli`를 모름 (역방향 의존 불가)

**왜 중요해요?**
- 순환 의존 방지 (A → B → A는 안 됨)
- 핵심 모듈(`:core`)을 독립적으로 유지

### 실제 프로젝트 예시

```
:core               (순수 로직, 의존성 0)
  ↑
  ├─ :demo-cli      (dummy 구현)
  ├─ :opencv-impl   (OpenCV 사용)
  ├─ :android-app   (Android + TFLite)
  └─ :desktop-app   (JavaFX + PyTorch)
```

각 모듈은 `:core`에만 의존 → `:core` 수정하면 모든 앱에 반영!

## 빌드 명령어

### 전체 빌드

```bash
./gradlew build
```

→ 모든 모듈 빌드 (`:core`, `:demo-cli`)

### 특정 모듈만 빌드

```bash
./gradlew :core:build
./gradlew :demo-cli:build
```

### 특정 모듈 실행

```bash
./gradlew :demo-cli:run
```

### 모듈별 테스트

```bash
./gradlew :core:test
./gradlew :demo-cli:test
```

## 모듈 추가하는 법

새 모듈 `:opencv-impl` 추가하고 싶다면:

### 1. settings.gradle.kts 수정

```kotlin
// settings.gradle.kts
include(
    ":core",
    ":demo-cli",
    ":opencv-impl"  // ← 추가!
)
```

### 2. 폴더 및 빌드 파일 생성

```bash
mkdir -p opencv-impl/src/main/kotlin
```

```kotlin
// opencv-impl/build.gradle.kts
plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":core"))  // core 사용
    implementation("org.opencv:opencv:4.8.0")  // OpenCV 추가
}
```

### 3. 코드 작성

```kotlin
// opencv-impl/src/main/kotlin/.../OpenCVDetector.kt
package com.example.fenvision.opencv

import com.example.fenvision.vision.BoardDetector
import org.opencv.core.*

class OpenCVBoardDetector : BoardDetector {
    override fun detectAndNormalize(imageBytes: ByteArray): BoardWarpResult {
        // OpenCV 코드...
    }
}
```

### 4. 빌드 확인

```bash
./gradlew :opencv-impl:build
```

끝!

## 장점 정리

| 장점 | 설명 |
|-----|------|
| **재사용성** | 핵심 모듈을 여러 프로젝트에서 사용 |
| **의존성 관리** | 필요한 라이브러리만 포함 |
| **빌드 속도** | 변경된 모듈만 리빌드 |
| **명확한 구조** | 역할별로 코드 분리 |
| **팀 협업** | 모듈별로 담당자 분리 가능 |
| **독립 배포** | 모듈별로 버전 관리 가능 |

## 단점 / 주의사항

### 1. 초기 설정 복잡

단일 모듈보다 설정 파일이 많음:
- settings.gradle.kts
- 루트 build.gradle.kts
- 각 모듈별 build.gradle.kts

### 2. 순환 의존 조심

```
:moduleA ──> :moduleB ──> :moduleA  ❌ 안 됨!
```

Gradle이 에러 냄. 의존성 방향을 잘 설계해야 함.

### 3. 모듈이 너무 많으면?

모듈이 50개, 100개... → 관리 복잡
적절한 개수 유지 필요

## 실무에서는?

### Android

```
app/              (메인 앱)
├── feature-login/
├── feature-home/
├── feature-profile/
├── core-ui/      (공통 UI 컴포넌트)
├── core-data/    (데이터 레이어)
└── core-network/ (네트워크 레이어)
```

### Spring Boot (백엔드)

```
api/              (REST API)
├── domain/       (비즈니스 로직)
├── infra/        (DB, 외부 API)
└── common/       (유틸리티)
```

## 우리 프로젝트에 적용하면?

### 현재 (2-모듈)

```
:core       - 모든 핵심 로직
:demo-cli   - 데모
```

간단하고 관리 쉬움 ✅

### 미래 확장 (5-모듈)

`documents/skeleton.md`에 계획된 구조:

```
:fen          - FEN 생성만
:vision-api   - 비전 인터페이스
:ml-api       - ML 인터페이스
:engine       - 파이프라인 조합
:demo-cli     - 데모
```

역할이 더 명확하고 재사용성 높음 ✅

**어떤 게 나을까요?**
- 지금은 프로젝트 작으니 2-모듈로 충분
- 나중에 실제 구현 추가되면 5-모듈로 리팩토링 고려

## 핵심 정리

1. **멀티모듈** = 하나의 프로젝트를 여러 독립 모듈로 분리
2. **settings.gradle.kts**에서 모듈 선언
3. **project(":모듈명")**으로 모듈 간 의존성 설정
4. 재사용성, 빌드 속도, 명확한 구조가 장점
5. 적절한 모듈 개수 유지가 중요

## 다음 학습

- Gradle 빌드 시스템 이해하기
- 의존성 관리 (implementation vs api)
- 플러그인 시스템
- 멀티플랫폼 프로젝트 (Kotlin Multiplatform)
