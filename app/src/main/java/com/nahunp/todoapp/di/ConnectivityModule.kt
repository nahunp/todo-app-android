package com.nahunp.todoapp.di

import com.nahunp.todoapp.core.connectivity.ConnectivityObserver
import com.nahunp.todoapp.core.connectivity.ConnectivityObserverImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ConnectivityModule {

    @Binds
    @Singleton
    abstract fun bindConnectivityObserver(impl: ConnectivityObserverImpl): ConnectivityObserver
}
