package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.domain.error.AppError
import com.weeklylotto.app.feature.result.toResultErrorUi
import org.junit.Test

class ResultErrorUiTest {
    @Test
    fun timeout_오류는_지연_메시지를_보여준다() {
        val ui = AppError.NetworkError(message = "timeout").toResultErrorUi()

        assertThat(ui.title).isEqualTo("응답이 지연되고 있습니다")
        assertThat(ui.message).contains("잠시 후 다시 시도")
    }

    @Test
    fun http4xx_오류는_요청_확인_메시지를_보여준다() {
        val ui = AppError.NetworkError(message = "bad request", code = 400).toResultErrorUi()

        assertThat(ui.title).isEqualTo("요청을 처리할 수 없습니다")
        assertThat(ui.message).contains("접근 권한")
    }

    @Test
    fun http5xx_오류는_서버_불안정_메시지를_보여준다() {
        val ui = AppError.NetworkError(message = "server error", code = 503).toResultErrorUi()

        assertThat(ui.title).isEqualTo("서버가 일시적으로 불안정합니다")
        assertThat(ui.message).contains("다시 시도")
    }

    @Test
    fun 네트워크_unknown_오류는_일반_오류_메시지를_보여준다() {
        val ui = AppError.NetworkError(message = "connection reset").toResultErrorUi()

        assertThat(ui.title).isEqualTo("문제가 발생했습니다")
        assertThat(ui.message).contains("다시 시도")
    }

    @Test
    fun storage_full_오류는_저장공간_메시지를_보여준다() {
        val ui = AppError.StorageError(message = "저장 공간 부족").toResultErrorUi()

        assertThat(ui.title).isEqualTo("저장 공간이 부족합니다")
        assertThat(ui.message).contains("저장공간")
    }

    @Test
    fun storage_disk_io_오류는_읽기쓰기_메시지를_보여준다() {
        val ui = AppError.StorageError(message = "disk i/o failure").toResultErrorUi()

        assertThat(ui.title).isEqualTo("저장소 읽기/쓰기 오류가 발생했습니다")
        assertThat(ui.message).contains("재시도")
    }

    @Test
    fun storage_migration_오류는_업데이트_안내를_보여준다() {
        val ui = AppError.StorageError(message = "migration required").toResultErrorUi()

        assertThat(ui.title).isEqualTo("저장 데이터 업데이트가 필요합니다")
        assertThat(ui.message).contains("최신 버전")
    }
}
