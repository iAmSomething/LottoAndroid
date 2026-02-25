# 매주로또

매주 로또 구매자를 위한 Android 앱 프로젝트입니다.

## 주요 기능
- 스마트 번호 생성(자동/수동/반자동)
- QR 기반 구매번호 등록
- 당첨번호 자동 연동 및 자동 채점
- 주간 알림(구매/결과)
- 홈 위젯 2종
- 누적 통계

## 문서
- `docs/README.md` (전체 문서 운영 인덱스)

## 실행
```bash
./gradlew :app:assembleDebug
```

## 릴리즈 사전 점검
```bash
./scripts/setup-local-release-signing.sh
./scripts/release-preflight.sh --with-build
```

## CI 릴리즈 점검
- 워크플로우: `.github/workflows/release-preflight.yml`
- 실행 모드: `./scripts/release-preflight.sh --with-build-ci --skip-adb --require-signing`
- 시크릿 동기화:
  - `./scripts/sync-gh-release-secrets.sh --repo <owner>/<repo> --dry-run`
  - `./scripts/sync-gh-release-secrets.sh --repo <owner>/<repo> --apply`
