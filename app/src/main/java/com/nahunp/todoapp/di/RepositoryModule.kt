package com.nahunp.todoapp.di

import com.nahunp.todoapp.data.repository.AuthRepositoryImpl
import com.nahunp.todoapp.data.repository.TodoListRepositoryImpl
import com.nahunp.todoapp.domain.repository.AuthRepository
import com.nahunp.todoapp.domain.repository.TodoListRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @Binds, not @Provides — both impls have @Inject constructors already, so
 * there's nothing to construct here, just an interface->impl mapping for
 * the domain layer's repository interfaces (which Application/ViewModels
 * depend on, never the impl classes directly — same dependency direction
 * the web repo's backend enforces between Application and Infrastructure).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindTodoListRepository(impl: TodoListRepositoryImpl): TodoListRepository
}
