package com.nahunp.todoapp.core.network

import com.nahunp.todoapp.data.remote.dto.CreateTodoItemRequestDto
import com.nahunp.todoapp.data.remote.dto.CreateTodoListRequestDto
import com.nahunp.todoapp.data.remote.dto.LoginRequestDto
import com.nahunp.todoapp.data.remote.dto.LoginResponseDto
import com.nahunp.todoapp.data.remote.dto.PasswordPolicyDto
import com.nahunp.todoapp.data.remote.dto.RegisterRequestDto
import com.nahunp.todoapp.data.remote.dto.TodoListDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * One interface, matching docs/api/openapi.json in the web repo route for
 * route — /api/v1 is the whole surface, versioned so a future /v2 can be
 * added without breaking this client (see the web repo's CLAUDE.md, WebApi
 * section, on why routes are versioned at all).
 */
interface TodoApiService {

    @POST("api/v1/auth/register")
    suspend fun register(@Body body: RegisterRequestDto)

    @POST("api/v1/auth/login")
    suspend fun login(@Body body: LoginRequestDto): LoginResponseDto

    @GET("api/v1/auth/password-policy")
    suspend fun getPasswordPolicy(): PasswordPolicyDto

    @GET("api/v1/todolists")
    suspend fun getTodoLists(): List<TodoListDto>

    @GET("api/v1/todolists/{id}")
    suspend fun getTodoList(@Path("id") id: Int): TodoListDto

    @POST("api/v1/todolists")
    suspend fun createTodoList(@Body body: CreateTodoListRequestDto): TodoListDto

    @DELETE("api/v1/todolists/{id}")
    suspend fun deleteTodoList(@Path("id") id: Int)

    @POST("api/v1/todolists/{listId}/items")
    suspend fun addTodoItem(@Path("listId") listId: Int, @Body body: CreateTodoItemRequestDto)

    @PATCH("api/v1/todolists/{listId}/items/{itemId}/complete")
    suspend fun completeTodoItem(@Path("listId") listId: Int, @Path("itemId") itemId: Int)

    @PATCH("api/v1/todolists/{listId}/items/{itemId}/reopen")
    suspend fun reopenTodoItem(@Path("listId") listId: Int, @Path("itemId") itemId: Int)

    @DELETE("api/v1/todolists/{listId}/items/{itemId}")
    suspend fun removeTodoItem(@Path("listId") listId: Int, @Path("itemId") itemId: Int)
}
