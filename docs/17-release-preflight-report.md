# 릴리즈 프리플라이트 리포트

## 실행 일시
- 2026-02-25
- 2026-02-26 (최신)

## 실행 명령
```bash
./scripts/release-preflight.sh --with-build
```

## 결과 요약
- PASS: 13
- WARN: 0
- FAIL: 0

## PASS 항목
- JDK 17 확인
- ADB 디바이스 연결(에뮬레이터 1대)
- 앱 버전 확인(`versionCode=2`, `versionName=0.2.0`)
- 스토어 스크린샷 6종 존재 및 해상도 확인(모두 `1080x2400`)
- 릴리즈 서명 값/alias 확인
- 로컬 릴리즈 키 백업 생성 및 무결성 파일(SHA-256) 생성 확인
- 품질 게이트 통과
  - `:app:ktlintCheck`
  - `:app:testDebugUnitTest`
  - `:app:connectedDebugAndroidTest` (8/8)
  - `:app:assembleRelease`
- CI 모드 품질 게이트 통과
  - `./scripts/release-preflight.sh --with-build-ci --skip-adb --require-signing`

## 실행 이력
1. 초기 실행: PASS 11 / WARN 1 / FAIL 0 (서명 값 미설정)
2. `./scripts/setup-local-release-signing.sh` 적용
3. 재실행: PASS 13 / WARN 0 / FAIL 0
4. 기능/디자인/접근성 추가 반영 후 재실행(2026-02-26): PASS 13 / WARN 0 / FAIL 0 (`connectedDebugAndroidTest` 8/8)

## 후속 조치
1. CI 환경에도 동일하게 `LOTTO_RELEASE_*` 시크릿 설정
   - 권장 명령:
     - `./scripts/sync-gh-release-secrets.sh --repo <owner>/<repo> --dry-run`
     - `./scripts/sync-gh-release-secrets.sh --repo <owner>/<repo> --apply`
2. GitHub Actions `release-preflight.yml`에서 `--with-build-ci --skip-adb --require-signing` 실행 유지
3. 배포 직전 아래 명령으로 최종 재검증
```bash
./scripts/release-preflight.sh --with-build
```
