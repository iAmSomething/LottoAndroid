# 데이터 모델 / 로컬 스키마 / API 계약

## 1. 핵심 도메인 타입
- `LottoNumber(1..45)`
- `LottoGame(slot, numbers[6], lockedNumbers, mode)`
- `TicketBundle(round, games, source, status)`
- `DrawResult(round, main[6], bonus, drawDate)`
- `EvaluationResult(rank, matchedMainCount, bonusMatched, highlightedNumbers)`
- `ReminderConfig(day/time, enabled)`

## 2. Room 스키마
### ticket_bundle
- `id: Long (PK)`
- `roundNumber: Int`
- `drawDate: String`
- `source: String`
- `status: String`
- `createdAtEpochMillis: Long`

### ticket_game
- `id: Long (PK)`
- `bundleId: Long (FK -> ticket_bundle.id)`
- `slot: String`
- `numbers: List<Int>`
- `lockedNumbers: List<Int>`
- `mode: String`

### draw_result
- `roundNumber: Int (PK)`
- `drawDate: String`
- `mainNumbers: List<Int>`
- `bonusNumber: Int`
- `fetchedAtEpochMillis: Long`

## 3. 외부 API 계약
- 1순위 endpoint: `https://www.dhlottery.co.kr/common.do?method=getLottoNumber&drwNo={round}`
- 2순위 fallback endpoint: `https://smok95.github.io/lotto/results/{round}.json`
- 1순위 필수 필드:
  - `returnValue`
  - `drwNo`
  - `drwNoDate`
  - `drwtNo1..drwtNo6`
  - `bnusNo`
- 2순위 필수 필드:
  - `draw_no`
  - `numbers[6]`
  - `bonus_no`
  - `date` (ISO offset datetime)
- 매핑 규칙:
  - `date` -> `LocalDate(yyyy-MM-dd)`로 변환 후 `drawDate` 저장
  - 1순위가 네트워크/파싱 실패 시 2순위로 자동 재시도

## 4. QR 파싱 계약
- 형식 A: `?drwNo={round}&numbers=3,14,25,31,38,42;...`
- 형식 B: `?v={round}q031425313842...` (12자리 단위 게임)
- 반환: `ParsedTicket(round, games)`

## 5. 에러 표준
- `NetworkError`
- `ParseError`
- `ValidationError`
- `StorageError`
