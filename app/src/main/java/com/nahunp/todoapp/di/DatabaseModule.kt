package com.nahunp.todoapp.di

import android.content.Context
import androidx.room.Room
import com.nahunp.todoapp.data.local.TodoDatabase
import com.nahunp.todoapp.data.local.dao.PendingOperationDao
import com.nahunp.todoapp.data.local.dao.TodoItemDao
import com.nahunp.todoapp.data.local.dao.TodoListDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTodoDatabase(@ApplicationContext context: Context): TodoDatabase =
        Room.databaseBuilder(context, TodoDatabase::class.java, "todoapp.db")
            // No real migration story yet (see TodoDatabase's doc comment)
            // -- a schema bump just drops and recreates the cache rather
            // than failing to open the database.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideTodoListDao(database: TodoDatabase): TodoListDao = database.todoListDao()

    @Provides
    fun provideTodoItemDao(database: TodoDatabase): TodoItemDao = database.todoItemDao()

    @Provides
    fun providePendingOperationDao(database: TodoDatabase): PendingOperationDao = database.pendingOperationDao()
}
