package com.weeklylotto.app.data.local

import androidx.room.TypeConverter

class RoomConverters {
    @TypeConverter
    fun fromIntList(values: List<Int>): String = values.joinToString(",")

    @TypeConverter
    fun toIntList(value: String): List<Int> = value.split(',').filter { it.isNotBlank() }.map { it.trim().toInt() }

    @TypeConverter
    fun fromStringSet(values: Set<String>): String = values.joinToString(",")

    @TypeConverter
    fun toStringSet(value: String): Set<String> = value.split(',').filter { it.isNotBlank() }.map { it.trim() }.toSet()
}
