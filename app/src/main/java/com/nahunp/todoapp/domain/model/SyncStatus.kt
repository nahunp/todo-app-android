package com.nahunp.todoapp.domain.model

/**
 * What TodoListScreen/TodoListDetailScreen's small status line reflects —
 * see SyncManager, the one place that actually updates this.
 */
data class SyncStatus(
    val isOnline: Boolean = false,
    val pendingCount: Int = 0,
    val isSyncing: Boolean = false,
    val lastError: String? = null,
)
