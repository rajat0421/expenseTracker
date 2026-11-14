package com.yourmoney.app.ui.review

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

/** Icon for a category's avatar, keyed by [com.yourmoney.app.data.local.CategoryEntity.key] (null for
 * custom categories, which just get a generic tag icon). */
fun categoryIcon(key: String?): ImageVector = when (key) {
    "FOOD" -> Icons.Filled.Restaurant
    "GROCERIES" -> Icons.Filled.ShoppingCart
    "TRANSPORT" -> Icons.Filled.DirectionsCar
    "SHOPPING" -> Icons.Filled.ShoppingBag
    "ENTERTAINMENT" -> Icons.Filled.Movie
    "BILLS" -> Icons.Filled.Receipt
    "HEALTH" -> Icons.Filled.LocalHospital
    "EDUCATION" -> Icons.Filled.School
    "TRAVEL" -> Icons.Filled.Flight
    "UNCATEGORIZED" -> Icons.Filled.HelpOutline
    else -> Icons.Filled.Category
}
