package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.domain.error.AppError
import com.weeklylotto.app.domain.error.AppErrorCategory
import com.weeklylotto.app.domain.error.toErrorCategory
import org.junit.Test

class AppErrorCategoryTest {
    @Test
    fun timeout_메시지는_timeout으로_분류된다() {
        val category = AppError.NetworkError(message = "request timeout").toErrorCategory()
        assertThat(category).isEqualTo(AppErrorCategory.TIMEOUT)
    }

    @Test
    fun http4xx_코드는_http4xx로_분류된다() {
        val category = AppError.NetworkError(message = "bad request", code = 400).toErrorCategory()
        assertThat(category).isEqualTo(AppErrorCategory.HTTP_4XX)
    }

    @Test
    fun http5xx_코드는_http5xx로_분류된다() {
        val category = AppError.NetworkError(message = "server error", code = 503).toErrorCategory()
        assertThat(category).isEqualTo(AppErrorCategory.HTTP_5XX)
    }

    @Test
    fun 네트워크_기타_오류는_unknown으로_분류된다() {
        val category = AppError.NetworkError(message = "connection reset").toErrorCategory()
        assertThat(category).isEqualTo(AppErrorCategory.UNKNOWN)
    }

    @Test
    fun parse_error는_schema로_분류된다() {
        val category = AppError.ParseError(message = "invalid json").toErrorCategory()
        assertThat(category).isEqualTo(AppErrorCategory.SCHEMA)
    }

    @Test
    fun storage_full_메시지는_storage_full로_분류된다() {
        val category = AppError.StorageError(message = "저장 공간 부족").toErrorCategory()
        assertThat(category).isEqualTo(AppErrorCategory.STORAGE_FULL)
    }

    @Test
    fun storage_disk_io_메시지는_disk_io로_분류된다() {
        val category = AppError.StorageError(message = "disk i/o failure").toErrorCategory()
        assertThat(category).isEqualTo(AppErrorCategory.STORAGE_DISK_IO)
    }

    @Test
    fun storage_migration_메시지는_migration으로_분류된다() {
        val category = AppError.StorageError(message = "migration required").toErrorCategory()
        assertThat(category).isEqualTo(AppErrorCategory.STORAGE_MIGRATION)
    }
}
