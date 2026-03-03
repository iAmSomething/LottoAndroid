package com.weeklylotto.app.domain.error

enum class AppErrorCategory(
    val analyticsValue: String,
) {
    TIMEOUT("timeout"),
    HTTP_4XX("http_4xx"),
    HTTP_5XX("http_5xx"),
    SCHEMA("schema"),
    VALIDATION("validation"),
    STORAGE_FULL("storage_full"),
    STORAGE_DISK_IO("disk_io"),
    STORAGE_MIGRATION("migration"),
    STORAGE("storage"),
    UNKNOWN("unknown"),
}

@Suppress("CyclomaticComplexMethod")
fun AppError.toErrorCategory(): AppErrorCategory =
    when (this) {
        is AppError.NetworkError -> {
            when {
                code != null && code in 400..499 -> AppErrorCategory.HTTP_4XX
                code != null && code in 500..599 -> AppErrorCategory.HTTP_5XX
                message.contains("timeout", ignoreCase = true) ||
                    message.contains("timed out", ignoreCase = true) ->
                    AppErrorCategory.TIMEOUT
                else -> AppErrorCategory.UNKNOWN
            }
        }

        is AppError.ParseError -> AppErrorCategory.SCHEMA
        is AppError.ValidationError -> AppErrorCategory.VALIDATION
        is AppError.StorageError -> {
            val causeName = cause?.javaClass?.simpleName.orEmpty()
            when {
                causeName == "SQLiteFullException" ||
                    message.contains("저장 공간", ignoreCase = true) ||
                    message.contains("disk full", ignoreCase = true) ->
                    AppErrorCategory.STORAGE_FULL

                causeName == "SQLiteDiskIOException" ||
                    message.contains("disk i/o", ignoreCase = true) ||
                    message.contains("읽기", ignoreCase = true) && message.contains("쓰기", ignoreCase = true) ->
                    AppErrorCategory.STORAGE_DISK_IO

                causeName.contains("Migration", ignoreCase = true) ||
                    message.contains("migration", ignoreCase = true) ->
                    AppErrorCategory.STORAGE_MIGRATION

                else -> AppErrorCategory.STORAGE
            }
        }
    }
