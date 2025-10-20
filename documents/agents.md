# Claude Code Agent 가이드

Image-to-FEN 프로젝트는 **5개의 특별한 Claude Code Agent**를 제공합니다.

## 📚 학습서 Agent (`/learn`)

새로운 개념이나 코드를 **상세한 학습 문서**로 변환합니다.

### 사용법

```
/learn [학습하고 싶은 주제]
```

### 예시

```bash
# 특정 개념 학습
/learn BICUBIC 보간법이 뭐야?

# 코드 설명
/learn ImageUtils.kt의 resize 함수 설명해줘

# 알고리즘 학습
/learn 원근 변환 알고리즘
```

### 생성되는 문서

- **위치**: `documents/study/XX-[주제명].md`
- **구조**:
  1. 왜 필요한가? (문제 상황)
  2. 기본 개념
  3. 작동 원리 (시각적 다이어그램)
  4. 실제 코드 예제 (3가지 난이도)
  5. 비교 및 대안
  6. 성능 고려사항
  7. 실습 과제
  8. 핵심 정리
  9. 더 알아보기

### 특징

✅ **초보자 친화적**: 전문 용어도 쉽게 설명
✅ **시각적**: ASCII 다이어그램, 비교 표
✅ **실습 가능**: 직접 돌려볼 수 있는 코드
✅ **점진적**: 쉬운 것부터 어려운 것까지
✅ **왜?를 설명**: 단순 사실이 아닌 이유 중심

### 실제 예시

기존에 생성된 학습 문서:
- `documents/study/07-image-quality-rendering-hints.md` (882줄!)
  - BICUBIC 보간법 수학 공식
  - 앤티에일리어싱 서브픽셀 샘플링
  - 3가지 난이도 실습 예제

---

## 🏗️ 설계 Agent (`/design`)

새로운 기능의 **아키텍처와 구현 계획**을 설계합니다.

### 사용법

```
/design [구현하고 싶은 기능]
```

### 예시

```bash
# 새 기능 설계
/design 원근 변환 기능을 구현하고 싶어

# 리팩토링 설계
/design ImageUtils를 더 효율적으로 리팩토링

# 통합 설계
/design OpenCV를 프로젝트에 통합하는 방법
```

### 생성되는 문서

- **위치**: `documents/design/[기능명]-design.md`
- **구조**:
  1. 요구사항 분석 (문제, 목표, 성공 기준)
  2. 접근 방법 비교 (2-3가지 대안)
  3. 아키텍처 설계 (다이어그램, 인터페이스)
  4. 의존성 분석 (모듈, 라이브러리)
  5. 구현 계획 (Phase별 단계)
  6. 테스트 전략 (단위, 통합, 성능)
  7. 리스크 분석 (문제점과 대응 방안)
  8. 마일스톤 (MVP → 확장 → 완성)
  9. 코드 스케치 (실제 구현 예시)

### 특징

✅ **체계적**: 문제부터 해결책까지 순서대로
✅ **비교 분석**: 여러 접근법의 장단점
✅ **단계별**: 작은 단위로 나눠서 구현
✅ **테스트 중심**: TDD 접근법 포함
✅ **리스크 관리**: 미리 예상하고 대비

### SOLID 원칙 적용

설계 Agent는 자동으로 SOLID 원칙을 적용합니다:
- **S**ingle Responsibility: 단일 책임
- **O**pen/Closed: 확장에 열림, 수정에 닫힘
- **L**iskov Substitution: 서브타입 치환
- **I**nterface Segregation: 인터페이스 분리
- **D**ependency Inversion: 의존성 역전

---

## 🤖 ML Agent (`/ml`)

머신러닝 모델 개발, 학습, 최적화를 전문적으로 지원합니다.

### 사용법

```
/ml [ML 관련 작업]
```

### 예시

```bash
# 모델 학습
/ml 체스 기물 분류 모델을 학습하고 싶어

# 모델 최적화
/ml TensorFlow Lite로 변환하고 INT8 양자화

# 정확도 평가
/ml 혼동 행렬(Confusion Matrix) 만들어줘
```

### 전문 분야

✅ **Teachable Machine**: 코딩 없이 빠른 프로토타입
✅ **TensorFlow/Keras**: 커스텀 모델 학습
✅ **TFLite 변환**: 모바일 최적화
✅ **양자화**: INT8로 크기 1/4, 속도 2-3배
✅ **모델 평가**: 정확도, 혼동 행렬, 오분류 분석
✅ **배치 처리**: 64칸 한 번에 추론

### 제공하는 것

- MobileNetV3 아키텍처 코드
- 데이터 증강 (Augmentation) 전략
- 학습 파이프라인 (전체 코드)
- 양자화 스크립트
- Kotlin/JVM 통합 코드
- 성능 최적화 체크리스트

### 특징

- 📱 **모바일 우선**: 저사양 기기 타겟
- 💰 **100% 무료**: Teachable Machine, Colab, TensorFlow
- ⚡ **성능 최적화**: 배치 처리, 양자화, 멀티스레딩
- 🎯 **실용성**: 논문 정확도보다 실제 동작

---

## 📷 Vision Agent (`/vision`)

이미지/영상 처리 알고리즘 개발과 최적화를 전문적으로 지원합니다.

### 사용법

```
/vision [이미지 처리 작업]
```

### 예시

```bash
# 체스판 검출
/vision 비스듬한 체스판을 정면으로 보정

# 원근 변환
/vision Perspective Transform 구현

# 64칸 분할
/vision 800x800 보드를 64칸으로 나누기

# 전처리
/vision 조명 불균형 해결 (CLAHE)
```

### 전문 분야

✅ **체스판 검출**: 수동 코너 지정, 자동 검출
✅ **원근 변환**: 호모그래피 행렬 계산
✅ **이미지 전처리**: 조명 정규화, 노이즈 제거
✅ **64칸 분할**: 기하학적, 적응적 분할
✅ **특징 검출**: 코너, 라인, 컨투어
✅ **OpenCV**: Java/Kotlin 바인딩

### 제공하는 것

- OpenCV 설정 및 초기화
- 원근 변환 전체 코드
- 체스판 자동 검출 알고리즘
- 64칸 분할 구현
- 이미지 전처리 파이프라인
- 성능 최적화 (ROI, 메모리 관리)

### 특징

- 🚀 **성능 우선**: 저사양 기기에서도 < 500ms
- 🎯 **정확성**: 엣지 케이스 처리
- 🔓 **오픈소스**: OpenCV 무료 버전
- 🌐 **크로스 플랫폼**: JVM, Android 모두 동작

---

## 🔍 PR Review Agent (`/pr-review`)

PR 리뷰 피드백을 자동으로 확인하고 대응합니다.

### 사용법

```
/pr-review [PR 번호]
```

### 예시

```bash
# 현재 브랜치의 PR 확인
/pr-review

# 특정 PR 번호 지정
/pr-review 2

# 학습 자료 자동 생성
/pr-review --learn
```

### 하는 일

**1. 피드백 수집**
```bash
gh api repos/.../pulls/2/reviews
gh api repos/.../pulls/2/comments
```

**2. 우선순위 분류**
- 🔴 High: 버그, 리소스 누수, 보안
- 🟡 Medium: 성능, 코드 품질
- 🟢 Low: 스타일, 네이밍
- 📚 Learning: 학습 필요

**3. 자동 수정**
- 코드 수정 제안
- 테스트 실행
- 논리적 커밋

**4. 학습 자료 생성** (선택)
- 중요한 개념은 `/learn` 자동 호출
- `documents/study/` 저장

**5. PR 업데이트**
- 수정사항 푸시
- 명확한 커밋 메시지

### 특징

- ✅ **완전한 대응**: 모든 피드백 처리
- 📚 **학습 자료화**: 중요 개념 문서화
- ✅ **테스트 필수**: 수정 후 검증
- 📝 **명확한 커밋**: 무엇을 왜 고쳤는지

### 실제 사용 예시

PR #2에서 3가지 피드백:
1. 리소스 누수 (High) → 즉시 수정 + 테스트
2. BICUBIC 보간 (Medium) → 수정 + 성능 비교
3. 문서 동기화 (Low) → 일괄 처리

→ 3개 커밋 + 학습 문서 (882줄!)

---

## 🔄 Agent를 함께 사용하기

### 권장 워크플로우 1: 새 기능 개발

**1단계: 설계**
```bash
/design 원근 변환 기능 구현
/vision 원근 변환 알고리즘 (기술 상세)
```
→ `documents/design/perspective-transform-design.md`

**2단계: 구현**
설계 문서를 보며 TDD로 구현:
1. 테스트 작성 (Red)
2. 최소 구현 (Green)
3. 리팩토링 (Refactor)

**3단계: 학습 문서화**
```bash
/learn 원근 변환과 호모그래피 행렬
```
→ `documents/study/XX-perspective-transform.md`

**결과**:
- ✅ 체계적인 설계
- ✅ 테스트 가능한 코드
- ✅ 완벽한 문서화
- ✅ 깊은 이해

---

### 권장 워크플로우 2: ML 모델 개발

**1단계: 설계**
```bash
/design 체스 기물 분류 모델
/ml 데이터셋 준비 전략
```

**2단계: 데이터 준비**
```bash
/ml 데이터 증강 방법 알려줘
```

**3단계: 모델 학습**
```bash
/ml MobileNetV3로 학습 (전체 코드)
```

**4단계: 최적화**
```bash
/ml TFLite INT8 양자화
```

**5단계: 통합**
```bash
/ml Kotlin/JVM 통합 코드
```

**6단계: 학습 문서**
```bash
/learn CNN과 Transfer Learning
```

---

### 권장 워크플로우 3: PR 리뷰 대응

**1단계: 피드백 확인**
```bash
/pr-review 2
```

**2단계: 자동 수정**
- High Priority → 즉시 수정
- Medium Priority → 분석 후 수정
- Low Priority → 일괄 처리

**3단계: 학습 자료** (자동)
- 중요한 개념 → `/learn` 자동 호출

**4단계: 커밋 & 푸시**
- 논리적 커밋
- 명확한 메시지

**결과**:
- ✅ 모든 피드백 반영
- ✅ 학습 자료 생성
- ✅ 테스트 통과
- ✅ 깔끔한 커밋 히스토리

---

## 📖 실제 사용 예시

### 예시 1: 이미지 품질 개선 (이미 완료)

**1. 문제 발견**: 리사이징 품질이 낮음

**2. 학습**:
```bash
/learn BICUBIC 보간법, 앤티에일리어싱
```

**3. 구현**:
```kotlin
setRenderingHints(mapOf(
    KEY_INTERPOLATION to VALUE_INTERPOLATION_BICUBIC,
    KEY_RENDERING to VALUE_RENDER_QUALITY,
    KEY_ANTIALIASING to VALUE_ANTIALIAS_ON
))
```

**4. 결과**:
- `documents/study/07-image-quality-rendering-hints.md` (882줄 학습 자료)
- ML 정확도 +10%p 향상

---

### 예시 2: 원근 변환 (Phase 2, 예정)

**1. 설계**:
```bash
/design 원근 변환으로 비스듬한 체스판을 정면으로 보정
```

**생성 문서**: `documents/design/perspective-transform-design.md`
```markdown
## 접근 방법 비교
1. 수동 코너 지정 (선택!)
2. OpenCV 자동 검출
3. ML 기반 검출

## 아키텍처
interface BoardDetector {
    fun detectAndNormalize(imageBytes: ByteArray): BoardWarpResult
}

## Phase 구분
Phase 1: 데이터 모델 (1시간)
Phase 2: 변환 로직 (2시간)
Phase 3: 테스트 (1시간)
```

**2. 구현**: (설계 문서 보며 TDD로)

**3. 학습 문서화**:
```bash
/learn 원근 변환과 호모그래피 행렬
```

**생성 문서**: `documents/study/08-perspective-transform.md`
```markdown
## 왜 필요한가?
[비스듬한 사진 문제 설명]

## 수학적 원리
[호모그래피 행렬 공식]

## 실습 예제
[3가지 난이도 코드]
```

---

## 🎯 Agent 선택 가이드

### `/learn` - 학습이 필요할 때
- ❓ "이게 뭐야?"
- ❓ "어떻게 작동해?"
- ❓ "왜 이렇게 했어?"
- ❓ "다른 방법은?"
- 📚 **목적**: 개념 이해와 학습

### `/design` - 구현 계획이 필요할 때
- 🤔 "어떻게 구현할까?"
- 🤔 "어떤 구조가 좋을까?"
- 🤔 "무엇부터 시작할까?"
- 🤔 "어떤 위험이 있을까?"
- 🏗️ **목적**: 아키텍처 설계와 계획

### `/ml` - 머신러닝 작업할 때
- 🤖 "모델을 어떻게 학습해?"
- 🤖 "양자화가 뭐야?"
- 🤖 "정확도를 어떻게 측정해?"
- 🤖 "Kotlin에서 어떻게 쓰지?"
- 📊 **목적**: ML 모델 개발과 최적화

### `/vision` - 이미지 처리할 때
- 📷 "체스판을 어떻게 찾아?"
- 📷 "원근 변환이 뭐야?"
- 📷 "OpenCV 어떻게 써?"
- 📷 "조명 불균형 해결?"
- 🎨 **목적**: 이미지/영상 처리 알고리즘

### `/pr-review` - PR 리뷰 받았을 때
- 🔍 "피드백을 어떻게 반영하지?"
- 🔍 "우선순위가 뭐야?"
- 🔍 "학습 자료도 만들어줘"
- 🔍 "테스트까지 실행해줘"
- ✅ **목적**: PR 피드백 자동 대응

---

## 💡 팁

### 효과적인 질문법

**좋은 예시**:
```bash
# 구체적
/learn ImageUtils.kt의 BICUBIC 보간법이 BILINEAR와 어떻게 다른지

# 맥락 포함
/design 저사양 폰에서도 동작하는 체스판 검출 기능

# 명확한 목표
/learn 왜 앤티에일리어싱이 ML 정확도를 높이는지
```

**나쁜 예시**:
```bash
# 너무 모호
/learn 이미지

# 목표 불명확
/design 뭔가 좋은 거

# 범위 너무 넓음
/learn 머신러닝 전부
```

### Agent 출력 활용

1. **설계 문서**: 구현 전에 팀원과 리뷰
2. **학습 문서**: 나중에 다시 볼 수 있게 정리
3. **코드 스케치**: 복붙하지 말고 이해하며 타이핑
4. **체크리스트**: 구현 진행 상황 추적

---

## 📊 성과 측정

### 학습 효과
- ✅ **이해도 향상**: "이게 뭐야?" → "이렇게 작동하는구나!"
- ✅ **기억 보존**: 나중에 다시 봐도 빠르게 상기
- ✅ **응용 능력**: 다른 프로젝트에도 적용 가능

### 개발 속도
- ✅ **설계 시간**: 막막함 해소, 명확한 계획
- ✅ **구현 시간**: 시행착오 감소
- ✅ **디버깅 시간**: 구조가 명확해서 문제 파악 쉬움
- ✅ **리팩토링**: 처음부터 잘 설계하면 나중에 편함

---

## 🚀 다음 단계

### 학습 문서 읽기
```bash
ls documents/study/
```

현재 사용 가능:
- `05-multimodule.md` - 멀티모듈 아키텍처
- `06-implementation-principles.md` - 전체 파이프라인
- `07-image-quality-rendering-hints.md` - 이미지 품질 향상 ⭐

### 새로운 기능 개발 시
1. `/design` 으로 설계
2. TDD로 구현
3. `/learn` 으로 문서화

### Agent 개선 제안
Agent가 더 유용해지려면 어떻게 개선해야 할까요?
`.claude/commands/learn.md` 또는 `design.md` 파일을 직접 수정하여 프롬프트를 개선할 수 있습니다!

---

**Happy Learning & Designing!** 🎓🏗️
