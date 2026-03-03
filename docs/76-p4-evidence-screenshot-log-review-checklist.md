# P-004 실기기 증적(스크린샷/로그) 검수 체크리스트

## 1. 목적
- `run-p4-wear-proof-gate.sh` 실행 결과의 스크린샷/로그 품질을 일관되게 검수한다.
- `PASS` 판정 전, 누락 파일/손상 파일/수집 실패(`capture_fail`)를 사전에 차단한다.

## 2. 적용 범위
- 대상 리포트: `docs/assets/distribution/wear_p4_device_evidence_<date>.md`
- 대상 파일:
  - `docs/assets/distribution/wear_p4_<date>/screenshots/*.png`
  - `docs/assets/distribution/wear_p4_<date>/logs/*.log`

## 3. 필수 파일 패턴
- 스크린샷:
  - `small_<serial>_home.png`
  - `large_<serial>_home.png`
- 로그:
  - `small_<serial>.log`
  - `large_<serial>.log`

## 4. 1차 검수 체크 (파일 존재/무결성)
- [ ] 스크린샷 2개가 모두 존재한다.
- [ ] 로그 2개가 모두 존재한다.
- [ ] 모든 파일 크기가 0 bytes가 아니다.
- [ ] 리포트 본문에 `capture_fail` 문자열이 없다.

권장 확인 명령:
```bash
DATE_TAG=<yyyy-mm-dd>
ls -lh docs/assets/distribution/wear_p4_${DATE_TAG}/screenshots
ls -lh docs/assets/distribution/wear_p4_${DATE_TAG}/logs
rg -n "capture_fail|상태: FAIL|상태: BLOCKED" docs/assets/distribution/wear_p4_device_evidence_${DATE_TAG}.md
```

## 5. 2차 검수 체크 (리포트 내용)
- [ ] `small serial`, `large serial` 값이 서로 다르다.
- [ ] `## 요약` 표에 `small`, `large` 두 행이 모두 존재한다.
- [ ] 각 디바이스 섹션에 `screenshot`, `log` 경로가 기록되어 있다.
- [ ] `scroll_probe`, `touch_probe`가 `PASS`다.
- [ ] 최종 판정이 `FAIL`이 아닌 상태다(`PASS` 또는 `WARN 포함 PASS`).

## 6. 3차 검수 체크 (로그 품질)
- [ ] 로그에 치명 예외 문자열(`FATAL EXCEPTION`, `Process crashed`)이 없다.
- [ ] 로그가 단일 시점 덤프가 아니라 유효 길이(최소 수십 라인 이상)를 가진다.
- [ ] 실행 직후 수집된 로그인지 날짜/타임스탬프를 확인했다.

권장 확인 명령:
```bash
DATE_TAG=<yyyy-mm-dd>
rg -n "FATAL EXCEPTION|Process crashed|ANR" docs/assets/distribution/wear_p4_${DATE_TAG}/logs/*.log
wc -l docs/assets/distribution/wear_p4_${DATE_TAG}/logs/*.log
```

## 7. 판정 규칙
1. 1차 체크 미충족: `FAIL` (증적 불충분)
2. 2차 체크 미충족: `FAIL` (리포트 구조 불충분)
3. 3차 체크 미충족: `WARN` 이상으로 분류하고 재수집 여부 결정
4. 모든 체크 통과: `PASS` (문서 동기화 단계로 진행)

## 8. 후속 동기화
- [ ] `73-physical-blocker-state-sync-checklist.md` 기준으로 `10`/`11`/`18` 반영
- [ ] `74-physical-device-day0-transition-runbook.md` 실행 후 기록 단계 완료
- [ ] 필요 시 `75-physical-transition-ops-raci-timebox-checklist.md`의 T2/T3 단계에 결과 반영
