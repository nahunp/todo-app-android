package com.nahunp.todoapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
object CoroutineScopeModule {

    // SupervisorJob so one failed child (e.g. a sync attempt that threw)
    // never cancels the scope itself — ConnectivityObserver's collector and
    // SyncManager both need to keep running for the rest of the process
    // lifetime regardless of any one sync's outcome.
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
