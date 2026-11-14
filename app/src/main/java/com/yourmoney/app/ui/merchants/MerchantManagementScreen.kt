package com.yourmoney.app.ui.merchants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yourmoney.app.data.DeleteMerchantResult
import com.yourmoney.app.data.local.CategoryEntity
import com.yourmoney.app.data.local.MerchantEntity
import com.yourmoney.app.ui.rememberFactoryViewModel
import com.yourmoney.app.ui.theme.BackgroundLight
import com.yourmoney.app.ui.theme.DeepGreen
import com.yourmoney.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun MerchantManagementScreen() {
    val viewModel: MerchantManagementViewModel =
        rememberFactoryViewModel { app -> MerchantManagementViewModel(app.merchantRepository, app.categoryRepository) }
    val merchants by viewModel.merchants.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val categoriesById = remember(categories) { categories.associateBy { it.id } }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun notify(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    Scaffold(
        containerColor = BackgroundLight,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text("Manage Merchants", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Recognized businesses used to auto-categorize expenses. Change a merchant's " +
                        "category, or remove ones you've added.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Spacer(modifier = Modifier.padding(top = 12.dp))
                OutlinedTextField(
                    value = viewModel.searchText,
                    onValueChange = viewModel::onSearchTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search merchants") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepGreen),
                )
            }

            if (merchants.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No merchants found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(merchants, key = { it.id }) { merchant ->
                        MerchantRow(
                            merchant = merchant,
                            categoryName = categoriesById[merchant.categoryId]?.name ?: "Uncategorized",
                            categories = categories,
                            onCategoryChange = { newCategoryId -> viewModel.changeCategory(merchant.id, newCategoryId) },
                            onDeleteClick = {
                                viewModel.delete(merchant.id) { result ->
                                    when (result) {
                                        DeleteMerchantResult.Deleted -> notify("Deleted \"${merchant.displayName}\"")
                                        DeleteMerchantResult.BuiltIn ->
                                            notify("\"${merchant.displayName}\" is built-in and can't be deleted")
                                        DeleteMerchantResult.InUse ->
                                            notify("Can't delete \"${merchant.displayName}\" - it's used by at least one expense")
                                        DeleteMerchantResult.NotFound -> Unit
                                    }
                                }
                            },
                        )
                    }
                    item { Spacer(modifier = Modifier.padding(bottom = 24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun MerchantRow(
    merchant: MerchantEntity,
    categoryName: String,
    categories: List<CategoryEntity>,
    onCategoryChange: (Long) -> Unit,
    onDeleteClick: () -> Unit,
) {
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(merchant.displayName, style = MaterialTheme.typography.bodyLarge)
                Box {
                    TextButton(
                        onClick = { categoryMenuExpanded = true },
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text(categoryName, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Change category",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    DropdownMenu(expanded = categoryMenuExpanded, onDismissRequest = { categoryMenuExpanded = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    categoryMenuExpanded = false
                                    onCategoryChange(cat.id)
                                },
                            )
                        }
                    }
                }
            }
            if (merchant.isBuiltIn) {
                Icon(Icons.Filled.Lock, contentDescription = "Built-in merchant", tint = TextSecondary)
            } else {
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = TextSecondary)
                }
            }
        }
    }
}
