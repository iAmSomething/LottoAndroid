package com.weeklylotto.app.feature.result

import com.weeklylotto.app.domain.error.AppError

data class ResultErrorUi(
    val title: String,
    val message: String,
)

fun AppError.toResultErrorUi(): ResultErrorUi =
    when (this) {
        is AppError.NetworkError -> {
            if (code == 404) {
                ResultErrorUi(
                    title = "최신 회차 결과가 아직 없습니다",
                    message = "추첨 직후에는 반영까지 시간이 걸릴 수 있습니다. 잠시 후 다시 시도하세요.",
                )
            } else {
                ResultErrorUi(
                    title = "네트워크 연결을 확인해 주세요",
                    message = "인터넷 상태가 불안정하거나 서버 응답이 지연되고 있습니다. 다시 시도해 주세요.",
                )
            }
        }

        is AppError.ParseError ->
            ResultErrorUi(
                title = "당첨 데이터 형식을 해석하지 못했습니다",
                message = "일시적인 서버 데이터 문제일 수 있습니다. 잠시 후 다시 시도해 주세요.",
            )

        is AppError.ValidationError ->
            ResultErrorUi(
                title = "결과 데이터가 유효하지 않습니다",
                message = "회차 정보가 완전히 반영되지 않았을 수 있습니다. 재시도해 주세요.",
            )

        is AppError.StorageError ->
            ResultErrorUi(
                title = "로컬 저장소 접근에 실패했습니다",
                message = "앱을 재시작한 뒤 다시 시도해 주세요. 계속되면 저장공간 상태를 확인해 주세요.",
            )
    }
