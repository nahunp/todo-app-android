package com.nahunp.todoapp.presentation.todolist.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nahunp.todoapp.domain.model.DueDateState
import com.nahunp.todoapp.domain.model.TodoItem
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListDetailScreen(
    onBack: () -> Unit,
    viewModel: TodoListDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var itemPickingDueDate by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.list?.name?.ifBlank { "(untitled)" } ?: "Todo List") },
                // Text arrow, not an Icon — same reasoning as the rest of
                // this app avoiding the material-icons-core dependency for
                // one glyph; matches the web frontend's own "← Back" link
                // style (legal pages' back link).
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", style = MaterialTheme.typography.headlineSmall)
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = state.newItemTitle,
                    onValueChange = viewModel::onNewItemTitleChange,
                    label = { Text("New item") },
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = viewModel::addItem) { Text("Add") }
            }

            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            val list = state.list
            when {
                state.isLoading -> CircularProgressIndicator()
                list == null -> Unit
                list.items.isEmpty() -> Text("No items yet.")
                else -> LazyColumn {
                    items(list.items, key = { it.id }) { item ->
                        TodoItemRow(
                            item = item,
                            onToggleDone = { viewModel.toggleDone(item.id, item.isDone) },
                            onRemove = { viewModel.removeItem(item.id) },
                            onCyclePriority = { viewModel.cyclePriority(item.id, item.priority) },
                            onCycleCategory = { viewModel.cycleCategory(item.id, item.category) },
                            onDueDateClick = { itemPickingDueDate = item.id },
                        )
                    }
                }
            }
        }
    }

    val pickingItemId = itemPickingDueDate
    if (pickingItemId != null) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { itemPickingDueDate = null },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    viewModel.setDueDate(pickingItemId, millis?.let { Instant.ofEpochMilli(it) })
                    itemPickingDueDate = null
                }) { Text("Set") }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.setDueDate(pickingItemId, null)
                    itemPickingDueDate = null
                }) { Text("Clear due date") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun TodoItemRow(
    item: TodoItem,
    onToggleDone: () -> Unit,
    onRemove: () -> Unit,
    onCyclePriority: () -> Unit,
    onCycleCategory: () -> Unit,
    onDueDateClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = item.isDone, onCheckedChange = { onToggleDone() })
            Text(
                item.title,
                modifier = Modifier.weight(1f),
                textDecoration = if (item.isDone) TextDecoration.LineThrough else null,
                color = if (item.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            )
            IconButton(onClick = onRemove) {
                Text("✕", color = MaterialTheme.colorScheme.error)
            }
        }
        Row(
            modifier = Modifier.padding(start = 40.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Tap-to-cycle rather than a dropdown/menu — fewer moving
            // parts for a first pass. A real picker (dropdown for
            // category, a segmented control for priority) is a reasonable
            // follow-up, not a must for this to be useful.
            AssistChip(onClick = onCyclePriority, label = { Text(item.priority.name) })
            AssistChip(onClick = onCycleCategory, label = { Text(item.category.name) })
            AssistChip(
                onClick = onDueDateClick,
                label = { Text(dueDateLabel(item)) },
                colors = if (item.dueDateState == DueDateState.Overdue) {
                    AssistChipDefaults.assistChipColors(labelColor = MaterialTheme.colorScheme.error)
                } else {
                    AssistChipDefaults.assistChipColors()
                },
            )
        }
    }
}

private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC)

private fun dueDateLabel(item: TodoItem): String {
    val dueDate = item.dueDate ?: return "No due date"
    val formatted = dateFormatter.format(dueDate)
    return when (item.dueDateState) {
        DueDateState.Overdue -> "Overdue: $formatted"
        DueDateState.Today -> "Today"
        DueDateState.Upcoming -> formatted
        DueDateState.None -> formatted
    }
}
