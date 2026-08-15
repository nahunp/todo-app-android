package com.nahunp.todoapp.data.local.entity

/**
 * Projection used by LocalTodoDataSource's merge logic to figure out,
 * for a batch of server rows, which ones already have a matching local
 * row (update it, keeping its localKey stable) vs. which are new to this
 * device (insert fresh) — see TodoListDao.getSyncedKeyMap /
 * TodoItemDao.getSyncedKeyMapForList.
 */
data class ServerKeyRow(val serverId: Int, val localKey: Int)
