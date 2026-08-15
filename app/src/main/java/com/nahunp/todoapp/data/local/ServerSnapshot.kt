package com.nahunp.todoapp.data.local

/**
 * What a sync pull merges into the local cache — deliberately not
 * TodoListDto/TodoItemDto directly (data/remote/dto), so this package
 * doesn't need to know about the network layer's wire format, only "here's
 * what the server currently says." SyncManager does the DTO -> snapshot
 * mapping (see LocalMappers.kt's toSnapshot() extensions) before calling
 * LocalTodoDataSource.mergeServerState.
 */
data class ServerListSnapshot(
    val serverId: Int,
    val name: String,
    val items: List<ServerItemSnapshot>,
)

data class ServerItemSnapshot(
    val serverId: Int,
    val title: String,
    val notes: String?,
    val isDone: Boolean,
    val completedAt: String?,
    val priority: String,
    val dueDate: String?,
    val dueDateState: String,
    val category: String,
)
