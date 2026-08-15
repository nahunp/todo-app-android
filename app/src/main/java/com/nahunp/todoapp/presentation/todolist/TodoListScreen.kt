package com.nahunp.todoapp.presentation.todolist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nahunp.todoapp.domain.model.TodoList
import com.nahunp.todoapp.presentation.components.RenameDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    onOpenList: (Int) -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: TodoListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var renamingList by remember { mutableStateOf<TodoList?>(null) }
    var confirmingAccountDeletion by remember { mutableStateOf(false) }

    LaunchedEffect(state.loggedOut) {
        if (state.loggedOut) onLoggedOut()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Todo Lists") },
                actions = {
                    TextButton(onClick = viewModel::logout) { Text("Logout") }
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
                    value = state.newListName,
                    onValueChange = viewModel::onNewListNameChange,
                    label = { Text("New list name") },
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = viewModel::createList) { Text("Create") }
            }

            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            if (state.isLoading) {
                CircularProgressIndicator()
            } else if (state.lists.isEmpty()) {
                Text("No todo lists yet.")
            } else {
                LazyColumn {
                    items(state.lists, key = { it.id }) { list ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Deliberately no internal list.id shown here —
                            // same fix as the web frontend's lists UX
                            // cleanup (PR #62 in the web repo): it's an
                            // implementation detail with no value to the
                            // end user.
                            Text(
                                list.name.ifBlank { "(untitled)" },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                                    .clickable { onOpenList(list.id) },
                                color = MaterialTheme.colorScheme.primary,
                            )
                            TextButton(onClick = { renamingList = list }) { Text("Rename") }
                            // No double-click/double-delete guard yet —
                            // same open item the web frontend's daily notes
                            // flagged (todo-list.ts's delete button), not
                            // fixed there either as of this writing. Worth
                            // a confirm-step like the web version has
                            // before this ships.
                            TextButton(onClick = { viewModel.deleteList(list.id) }) {
                                Text("Delete", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // Same "danger zone" placement as the web frontend's todo-list
            // page — low-prominence link, real confirm step behind it.
            TextButton(onClick = { confirmingAccountDeletion = true }) {
                Text("Delete my account", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    renamingList?.let { list ->
        RenameDialog(
            title = "Rename list",
            initialValue = list.name,
            onConfirm = { newName ->
                viewModel.renameList(list.id, newName)
                renamingList = null
            },
            onDismiss = { renamingList = null },
        )
    }

    if (confirmingAccountDeletion) {
        AlertDialog(
            onDismissRequest = { if (!state.deletingAccount) confirmingAccountDeletion = false },
            title = { Text("Delete your account?") },
            text = { Text("This permanently deletes your account and all your lists. This can't be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteAccount() },
                    enabled = !state.deletingAccount,
                ) {
                    Text(
                        if (state.deletingAccount) "Deleting…" else "Yes, delete everything",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmingAccountDeletion = false },
                    enabled = !state.deletingAccount,
                ) { Text("Cancel") }
            },
        )
    }
}
