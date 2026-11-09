package com.yourmoney.app.ui.stats

import androidx.compose.ui.graphics.Color

/** Stable, pleasant palette for category dots/legends - picked by id so a category's color
 * never changes just because other categories were added/removed. */
private val palette = listOf(
    Color(0xFF1B5E3C),
    Color(0xFFC9A24B),
    Color(0xFF3F6FB0),
    Color(0xFFC75450),
    Color(0xFF8B5FBF),
    Color(0xFF2F9E8F),
    Color(0xFFD98247),
    Color(0xFF5C7A99),
)

fun categoryColor(categoryId: Long): Color = palette[(categoryId.mod(palette.size.toLong())).toInt()]
