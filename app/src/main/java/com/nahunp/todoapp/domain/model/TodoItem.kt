package com.nahunp.todoapp.domain.model

import java.time.Instant

/**
 * Mirrors the backend's TodoItemDto (GetTodoListQuery.cs) exactly — same
 * fields, same enum names. The backend is the source of truth for this
 * shape (see docs/api/openapi.json in the web repo); this is the Kotlin
 * side of that same contract, not an independent design.
 */
data class TodoItem(
    val id: Int,
    val title: String,
    val isComplete: Boolean,
    val priority: Priority,
    val dueDate: Instant?,
    val dueDateState: DueDateState,
    val category: Category,
)

enum class Priority { Low, Medium, High }

enum class Category { None, Work, Personal, Health }

/**
 * Computed by the backend at read time from `dueDate` (see TodoItem.
 * GetDueDateState(asOf) in the web repo's Domain layer) — never stored,
 * never computed client-side, so "today" always means the server's
 * definition of today, not the device's possibly-wrong clock/timezone.
 */
enum class DueDateState { None, Overdue, Today, Upcoming }
