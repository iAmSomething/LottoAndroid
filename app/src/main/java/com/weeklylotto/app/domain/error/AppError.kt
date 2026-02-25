package com.weeklylotto.app.domain.error

sealed interface AppError {
    data class NetworkError(val message: String, val code: Int? = null) : AppError

    data class ParseError(val message: String) : AppError

    data class ValidationError(val message: String) : AppError

    data class StorageError(val message: String, val cause: Throwable? = null) : AppError
}
