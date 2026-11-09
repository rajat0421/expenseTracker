package com.yourmoney.app.data.local

import androidx.room.TypeConverter
import com.yourmoney.app.model.ConfidenceLevel
import com.yourmoney.app.model.ExpenseSource
import com.yourmoney.app.model.ReviewStatus
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class Converters {
    @TypeConverter
    fun fromEpochDay(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun toEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun fromEpochMillisToDateTime(millis: Long?): LocalDateTime? =
        millis?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()) }

    @TypeConverter
    fun toEpochMillisFromDateTime(dateTime: LocalDateTime?): Long? =
        dateTime?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()

    @TypeConverter
    fun fromSourceName(name: String?): ExpenseSource? = name?.let(ExpenseSource::valueOf)

    @TypeConverter
    fun toSourceName(source: ExpenseSource?): String? = source?.name

    @TypeConverter
    fun fromConfidenceName(name: String?): ConfidenceLevel? = name?.let(ConfidenceLevel::valueOf)

    @TypeConverter
    fun toConfidenceName(confidence: ConfidenceLevel?): String? = confidence?.name

    @TypeConverter
    fun fromReviewStatusName(name: String?): ReviewStatus? = name?.let(ReviewStatus::valueOf)

    @TypeConverter
    fun toReviewStatusName(status: ReviewStatus?): String? = status?.name
}
