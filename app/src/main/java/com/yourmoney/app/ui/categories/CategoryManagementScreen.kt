package com.yourmoney.app.ui.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import com.yourmoney.app.data.DeleteCategoryResult
import com.yourmoney.app.data.local.CategoryEntity
import com.yourmoney.app.ui.rememberFactoryViewModel
import com.yourmoney.app.ui.theme.BackgroundLight
import com.yourmoney.app.ui.theme.DeepGreen
import com.yourmoney.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun CategoryManagementScreen() {
    val viewModel: CategoryManagementViewModel =
        rememberFactoryViewModel { app -> CategoryManagementViewModel(app.categoryRepository) }
    val categories by viewModel.categories.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var renaming by remember { mutableStateOf<CategoryEntity?>(null) }
    var newCategoryName by remember { mutableStateOf("") }

    fun notify(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    Scaffold(
        containerColor = BackgroundLight,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text("Manage Categories", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Rename any category to your own wording, or add new ones. Categories still in " +
                        "use by an expense can't be deleted.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(categories, key = { it.id }) { category ->
                    CategoryRow(
                        category = category,
                        onRenameClick = { renaming = category },
                        onDeleteClick = {
                            viewModel.delete(category.id) { result ->
                                when (result) {
                                    DeleteCategoryResult.Deleted -> notify("Deleted \"${category.name}\"")
                                    DeleteCategoryResult.InUse ->
                                        notify("Can't delete \"${category.name}\" - it's used by at least one expense")
                                    DeleteCategoryResult.NotFound -> Unit
                                }
                            }
                        },
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    placeholder = { Text("New category name") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepGreen),
                )
                IconButton(onClick = {
                    val name = newCategoryName.trim()
                    if (name.isNotBlank()) {
                        viewModel.addCustom(name) { added ->
                            if (added) {
                                notify("Added \"$name\"")
                                newCategoryName = ""
                            }
                        }
                    }
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add category", tint = DeepGreen)
                }
            }
        }
    }

    renaming?.let { category ->
        RenameCategoryDialog(
            category = category,
            onDismiss = { renaming = null },
            onConfirm = { newName ->
                viewModel.rename(category.id, newName)
                renaming = null
                notify("Renamed to \"$newName\"")
            },
        )
    }
}

@Composable
private fun CategoryRow(category: CategoryEntity, onRenameClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(category.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            IconButton(onClick = onRenameClick) {
                Icon(Icons.Filled.Edit, contentDescription = "Rename", tint = TextSecondary)
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = TextSecondary)
            }
        }
    }
}

@Composable
private fun RenameCategoryDialog(category: CategoryEntity, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember(category.id) { mutableStateOf(category.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Category") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepGreen),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled = name.isNotBlank(),
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
