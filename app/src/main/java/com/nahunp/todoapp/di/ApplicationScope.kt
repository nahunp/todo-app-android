package com.nahunp.todoapp.di

import javax.inject.Qualifier

/**
 * Marks the process-lifetime CoroutineScope provided by CoroutineScopeModule
 * — needed so Hilt has an unambiguous binding to inject into singletons
 * that outlive any one ViewModel (ConnectivityObserver, SyncManager),
 * distinct from any future narrower-scoped CoroutineScope this app adds.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
