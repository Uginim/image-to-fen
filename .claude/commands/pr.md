---
name: pr
description: "GitHub Pull Request 생성, 업데이트, 리뷰 대응을 자동화하는 에이전트. PR 생성부터 머지까지 전체 라이프사이클을 관리합니다. 사용 예시: '/pr create', '/pr update 2', '/pr review 2'"
---

# GitHub PR Manager Agent

GitHub Pull Request의 생성, 업데이트, 리뷰 피드백 대응을 자동화하는 전문 에이전트입니다.

## 핵심 원칙

- **명확한 커뮤니케이션**: 영어 타이틀 + 한글 설명으로 국제/국내 협업 모두 지원
- **체계적인 문서화**: 구조화된 PR 본문으로 리뷰어 이해도 향상
- **자동화된 프로세스**: 반복 작업 최소화, 일관성 유지
- **코드 품질 우선**: 모든 테스트 통과 후 PR 생성

## PR 제목 형식

현재 프로젝트 컨벤션을 따릅니다:

```
type: Brief description in English
```

### Type 종류

- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `docs`: 문서 변경
- `refactor`: 코드 리팩토링
- `test`: 테스트 추가/수정
- `chore`: 빌드, 설정 등 기타 작업
- `perf`: 성능 개선
- `style`: 코드 스타일 변경

### 예시

✅ GOOD:
- `feat: Add perspective transform for board detection`
- `fix: Resolve resource leak in ImageUtils.getImageInfo()`
- `docs: Add comprehensive ML model training guide`
- `refactor: Simplify FenBuilder matrix conversion logic`

❌ BAD:
- `Add feature` (type 누락)
- `feat: 체스판 검출 추가` (영어로 작성)
- `Update code` (설명이 모호)

## PR 본문 구조

```markdown
## Summary

Brief 1-3 sentence overview of what this PR does.

## Changes

### Implementation
- Detailed list of changes made
- File paths and key modifications
- New classes/functions added

### Tests
- Test coverage added/modified
- Test results:
```
./gradlew test
BUILD SUCCESSFUL
```

### Documentation
- Docs updated (if any)
- CLAUDE.md changes (if architectural)

## Breaking Changes

[If any breaking changes, document here with migration guide]

## Review Focus

[Areas requiring special attention from reviewers]

## Related Issues

Fixes #X (if applicable)

---

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

## 명령어 사용법

### 1. PR 생성: `/pr create`

현재 브랜치의 변경사항으로 새 PR을 생성합니다.

**실행 과정**:

1. **브랜치 분석**
   ```bash
   git status
   git log main..HEAD --oneline
   git diff main...HEAD --stat
   ```

2. **테스트 실행** (필수)
   ```bash
   ./gradlew test
   # 모든 테스트가 통과해야 PR 생성 가능
   ```

3. **PR 타이틀 생성**
   - 커밋 메시지 분석
   - 주요 변경사항 파악
   - type 자동 선택
   - 간결한 영어 설명 작성

4. **PR 본문 생성**
   - 전체 커밋 히스토리 분석
   - 변경된 파일 및 라인 수 파악
   - 구조화된 본문 작성
   - 테스트 결과 포함

5. **PR 생성 실행**
   ```bash
   # 안전한 multiline body 처리
   cat <<'EOF' > /tmp/pr_body.md
   [Full PR description]
   EOF

   gh pr create \
     --title "feat: Add new feature" \
     --body-file /tmp/pr_body.md \
     --base main

   rm /tmp/pr_body.md
   ```

6. **후속 작업**
   - PR URL 출력
   - CI/CD 체크 모니터링
   - 다음 단계 안내

**사용 예시**:
```bash
# 기본 사용
/pr create

# Base 브랜치 지정
/pr create --base develop
```

### 2. PR 업데이트: `/pr update <NUMBER>`

기존 PR에 변경사항을 반영하고 본문을 업데이트합니다.

**실행 과정**:

1. **PR 정보 조회**
   ```bash
   gh pr view <NUMBER> --json number,title,body,headRefName
   ```

2. **새 커밋 확인**
   ```bash
   # 마지막 업데이트 이후 커밋
   git log <LAST_SHA>..HEAD --oneline
   git diff <LAST_SHA>..HEAD --stat
   ```

3. **기존 본문 가져오기**
   ```bash
   gh pr view <NUMBER> --json body -q .body > /tmp/current_pr_body.md
   ```

4. **변경 이력 추가**
   ```bash
   TIMESTAMP=$(date +"%Y-%m-%d %H:%M")

   cat >> /tmp/current_pr_body.md <<EOF

   ---

   ## 📝 Change History

   ### Update $(date +"%Y-%m-%d %H:%M")

   **New Commits**:
   $(git log <LAST_SHA>..HEAD --oneline)

   **Files Changed**:
   $(git diff <LAST_SHA>..HEAD --stat)

   **Reason**: [Why these changes were made]
   EOF
   ```

5. **PR 업데이트 실행**
   ```bash
   gh pr edit <NUMBER> --body-file /tmp/current_pr_body.md
   rm /tmp/current_pr_body.md
   ```

6. **검증**
   ```bash
   # 업데이트된 본문 확인
   gh pr view <NUMBER> --json body -q .body | tail -30
   ```

**사용 예시**:
```bash
/pr update 2
/pr update 5 --reason "Apply review feedback"
```

### 3. 리뷰 피드백 대응: `/pr review <NUMBER>`

PR 리뷰 코멘트를 분석하고 대응합니다.

**실행 과정**:

1. **리뷰 코멘트 수집**
   ```bash
   # 일반 코멘트
   gh pr view <NUMBER> --json comments --jq '.comments[] | {
     author: .author.login,
     created: .createdAt,
     body: .body
   }'

   # 코드 인라인 코멘트 (중요!)
   OWNER=$(gh repo view --json owner --jq '.owner.login')
   REPO=$(gh repo view --json name --jq '.name')

   gh api /repos/$OWNER/$REPO/pulls/<NUMBER>/comments --jq '.[] | {
     file: .path,
     line: .line,
     body: .body,
     author: .user.login
   }'
   ```

2. **우선순위 분류**
   - 🔴 High: 버그, 보안, 테스트 실패
   - 🟡 Medium: 코드 품질, 명확성, 컨벤션
   - 🟢 Low: 스타일, 마이너 개선

3. **피드백 대응**
   - High priority부터 처리
   - 각 파일/라인별로 수정
   - 테스트로 검증
   - 커밋 메시지에 피드백 참조

4. **학습 자료 생성** (선택)
   ```bash
   # 중요한 개념이면 /learn 호출
   /learn [개념 설명 요청]
   ```

5. **변경사항 푸시**
   ```bash
   git add .
   git commit -m "fix: Apply review feedback from PR #<NUMBER>

   - Address [reviewer]'s comment on [file]
   - Improve [aspect] as suggested
   - Add test for [scenario]
   "
   git push
   ```

6. **PR 업데이트**
   ```bash
   /pr update <NUMBER> --reason "Apply review feedback"
   ```

**사용 예시**:
```bash
/pr review 2
/pr review 3 --learn  # 학습 자료도 함께 생성
```

## 자동화 스크립트

### PR 생성 전 체크리스트

```bash
#!/bin/bash
# .claude/scripts/pr-checklist.sh

set -eo pipefail

echo "=== PR 생성 전 체크리스트 ==="

# 1. 테스트 실행
echo "1. Running tests..."
./gradlew test || { echo "✗ Tests failed"; exit 1; }
echo "✓ All tests passed"

# 2. 브랜치 상태 확인
echo "2. Checking branch status..."
if [[ $(git status --porcelain) ]]; then
    echo "✗ Uncommitted changes found"
    git status --short
    exit 1
fi
echo "✓ All changes committed"

# 3. Main과 차이 확인
echo "3. Checking diff with main..."
COMMITS=$(git log main..HEAD --oneline | wc -l | tr -d ' ')
if [[ $COMMITS -eq 0 ]]; then
    echo "✗ No commits to create PR"
    exit 1
fi
echo "✓ Found $COMMITS commits"

# 4. 원격 브랜치 동기화
echo "4. Syncing with remote..."
git fetch origin
BEHIND=$(git rev-list --count HEAD..origin/main)
if [[ $BEHIND -gt 0 ]]; then
    echo "⚠ Your branch is $BEHIND commits behind main"
    read -p "Continue anyway? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi
echo "✓ Branch is up to date"

echo ""
echo "✅ Ready to create PR!"
echo ""
echo "Run: /pr create"
```

### PR 리뷰 코멘트 분석

```bash
#!/bin/bash
# .claude/scripts/analyze-pr-comments.sh

set -eo pipefail

PR_NUMBER=$1

if [[ -z "$PR_NUMBER" ]]; then
    echo "Usage: $0 <PR_NUMBER>"
    exit 1
fi

OWNER=$(gh repo view --json owner --jq '.owner.login')
REPO=$(gh repo view --json name --jq '.name')

echo "=== PR #$PR_NUMBER 리뷰 코멘트 분석 ==="
echo "Repository: $OWNER/$REPO"
echo ""

echo "📝 General Comments:"
echo "-------------------"
gh pr view $PR_NUMBER --json comments --jq '.comments[] | "[\(.createdAt)] @\(.author.login):\n\(.body)\n"' -r
echo ""

echo "💬 Inline Code Comments:"
echo "-----------------------"
gh api /repos/$OWNER/$REPO/pulls/$PR_NUMBER/comments --jq '.[] | "File: \(.path):\(.line)\n@\(.user.login):\n\(.body)\n---"' -r
echo ""

echo "✅ Analysis complete."
```

## 특수 상황 처리

### 1. 긴급 Hotfix PR

```bash
# 빠른 PR 생성 (간소화된 본문)
/pr create --hotfix

# 제목에 명시
# 예: fix: Critical memory leak in ImageUtils [HOTFIX]
```

### 2. 대규모 PR (500+ 라인)

```bash
# 모듈별로 리뷰 요청
/pr create --split-review

# 본문에 모듈 섹션 자동 생성
# - Module 1: Vision API (250 lines)
# - Module 2: ML Integration (300 lines)
```

### 3. CI 실패 처리

```bash
# CI 로그 자동 분석
/pr check <NUMBER>

# 실패 원인 파악 및 수정 제안
# 수정 후 자동 재실행
```

### 4. Merge Conflict

```bash
# Conflict 자동 감지
/pr conflicts <NUMBER>

# Main 브랜치 변경사항 표시
# Conflict 해결 가이드 제공
```

## 품질 체크리스트

PR 생성/업데이트 시 자동으로 확인합니다:

### 코드 품질
- ✓ 모든 테스트 통과 (`./gradlew test`)
- ✓ 컴파일 에러 없음 (`./gradlew build`)
- ✓ 린트 규칙 준수
- ✓ 디버깅 코드 제거
- ✓ 민감정보 제거 (토큰, 비밀번호 등)

### 문서화
- ✓ API 변경사항 문서화
- ✓ 아키텍처 변경 시 CLAUDE.md 업데이트
- ✓ 새 모듈/클래스에 KDoc 주석
- ✓ README 업데이트 (필요시)

### 커밋 히스토리
- ✓ 의미 있는 커밋 메시지
- ✓ 논리적 커밋 분리
- ✓ Co-authored-by 태그 (Claude와 작업 시)

### PR 본문
- ✓ Summary 명확
- ✓ Changes 상세
- ✓ 테스트 결과 포함
- ✓ Breaking changes 명시 (있다면)

## 통합 워크플로우

### 새 기능 개발 → PR

```bash
# 1. 설계
/design [기능명]

# 2. 구현 (TDD)
# ... 코딩 ...

# 3. 테스트
./gradlew test

# 4. 학습 자료 생성
/learn [핵심 개념]

# 5. PR 생성
/pr create

# 6. 리뷰 대응
/pr review <NUMBER>

# 7. 업데이트
/pr update <NUMBER>

# 8. 머지
gh pr merge <NUMBER> --squash
```

### 버그 수정 → PR

```bash
# 1. 이슈 확인
gh issue view <NUMBER>

# 2. 재현 테스트 작성
# ... 테스트 코드 ...

# 3. 수정
# ... 코드 수정 ...

# 4. 테스트 확인
./gradlew test

# 5. PR 생성
/pr create

# 6. 이슈 링크
# PR 본문에 "Fixes #<NUMBER>" 자동 추가
```

## GitHub CLI 필수 설정

```bash
# 1. 설치 확인
gh --version

# 2. 인증
gh auth login

# 3. 기본 브랜치 확인
gh repo view --json defaultBranchRef --jq '.defaultBranchRef.name'

# 4. PR 템플릿 (선택사항)
mkdir -p .github
cat > .github/pull_request_template.md <<EOF
## Summary

## Changes

## Tests

## Related Issues
EOF
```

## 에이전트 실행 옵션

### `/pr create [options]`

**Options**:
- `--base <branch>`: Base 브랜치 지정 (기본: main)
- `--draft`: Draft PR로 생성
- `--hotfix`: 긴급 수정 (간소화된 본문)
- `--no-test`: 테스트 스킵 (비추천)

**예시**:
```bash
/pr create
/pr create --base develop
/pr create --draft
/pr create --hotfix
```

### `/pr update <NUMBER> [options]`

**Options**:
- `--reason <text>`: 업데이트 이유 명시
- `--commits <sha>`: 특정 커밋부터 변경사항 추적

**예시**:
```bash
/pr update 2
/pr update 2 --reason "Apply review feedback"
/pr update 2 --commits abc1234
```

### `/pr review <NUMBER> [options]`

**Options**:
- `--learn`: 중요 개념 학습 자료 자동 생성
- `--priority <high|medium|low>`: 특정 우선순위만 처리

**예시**:
```bash
/pr review 2
/pr review 2 --learn
/pr review 2 --priority high
```

## 출력 형식

에이전트는 다음 형식으로 결과를 보고합니다:

```
=== PR 생성 완료 ===

📋 PR 정보
- Number: #2
- Title: feat: Add perspective transform for board detection
- URL: https://github.com/Uginim/image-to-fen/pull/2
- Base: main ← feature/board-detection
- Status: Open

📊 변경사항
- Commits: 5
- Files: 8 (+450 -120)
- Tests: 12 added, all passing ✅

🎯 다음 단계
1. CI/CD 체크 확인
2. 리뷰어 대기
3. 피드백 받으면: /pr review 2

✅ PR이 성공적으로 생성되었습니다.
```

## 오류 처리

### 일반적인 오류

**1. GitHub CLI 인증 실패**
```
✗ Error: gh not authenticated
→ Solution: Run 'gh auth login'
```

**2. 테스트 실패**
```
✗ Error: Tests failed, cannot create PR
→ Solution: Fix failing tests first
→ Run: ./gradlew test --info
```

**3. Merge Conflict**
```
⚠ Warning: Merge conflicts detected
→ Solution: Resolve conflicts with main
→ Run: git pull origin main
```

**4. PR 이미 존재**
```
✗ Error: PR already exists for this branch
→ Existing PR: #5
→ Solution: Use '/pr update 5' instead
```

## 참고 자료

- [GitHub CLI Manual](https://cli.github.com/manual/)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [GitHub PR Best Practices](https://github.com/github/platform-samples/blob/master/.github/PULL_REQUEST_TEMPLATE.md)

---

이 에이전트는 image-to-fen 프로젝트의 PR 워크플로우를 자동화하여 일관성 있고 고품질의 Pull Request를 생성합니다.
