package com.yourmoney.app.data.local

import androidx.room.TypeConverter
import com.yourmoney.app.model.Category
import com.yourmoney.app.model.ExpenseSource
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromEpochDay(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun toEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun fromCategoryName(name: String?): Category? = name?.let(Category::valueOf)

    @TypeConverter
    fun toCategoryName(category: Category?): String? = category?.name

    @TypeConverter
    fun fromSourceName(name: String?): ExpenseSource? = name?.let(ExpenseSource::valueOf)

    @TypeConverter
    fun toSourceName(source: ExpenseSource?): String? = source?.name
}
