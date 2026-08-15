package com.nahunp.todoapp.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nahunp.todoapp.domain.model.SyncStatus

/**
 * Shared between TodoListScreen and TodoListDetailScreen — the one place
 * offline/pending-sync state is actually shown to Diego, so a queued
 * change (or a sync failure) is never silent. See CLAUDE.md's "Offline
 * support" section: mutations don't surface errors synchronously anymore,
 * this line is where a sync failure eventually shows up instead.
 */
@Composable
fun SyncStatusLine(status: SyncStatus) {
    val text = when {
        !status.isOnline && status.pendingCount > 0 ->
            "Offline — ${changeCount(status.pendingCount)} will sync once you're back online"
        !status.isOnline -> "Offline"
        status.isSyncing -> "Syncing…"
        status.pendingCount > 0 -> "Syncing ${changeCount(status.pendingCount)}…"
        status.lastError != null -> "Sync issue: ${status.lastError}"
        else -> null
    }
    if (text != null) {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        )
    }
}

private fun changeCount(count: Int) = if (count == 1) "1 change" else "$count changes"
