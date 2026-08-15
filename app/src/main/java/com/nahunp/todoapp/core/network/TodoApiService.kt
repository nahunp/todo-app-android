package com.nahunp.todoapp.core.network

import com.nahunp.todoapp.data.remote.dto.CreateTodoItemRequestDto
import com.nahunp.todoapp.data.remote.dto.CreateTodoListRequestDto
import com.nahunp.todoapp.data.remote.dto.IdResponseDto
import com.nahunp.todoapp.data.remote.dto.LoginRequestDto
import com.nahunp.todoapp.data.remote.dto.LoginResponseDto
import com.nahunp.todoapp.data.remote.dto.PasswordPolicyDto
import com.nahunp.todoapp.data.remote.dto.RegisterRequestDto
import com.nahunp.todoapp.data.remote.dto.RenameTodoItemRequestDto
import com.nahunp.todoapp.data.remote.dto.RenameTodoListRequestDto
import com.nahunp.todoapp.data.remote.dto.SetCategoryRequestDto
import com.nahunp.todoapp.data.remote.dto.SetDueDateRequestDto
import com.nahunp.todoapp.data.remote.dto.SetPriorityRequestDto
import com.nahunp.todoapp.data.remote.dto.TodoListDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * One interface, matching the backend's TodoListEndpoints.cs route for
 * route and verb for verb — checked directly against that file and
 * docs/api/openapi.json in the web repo, not assumed. /api/v1 is the
 * whole surface, versioned so a future /v2 can be added without breaking
 * this client (see the web repo's CLAUDE.md, WebApi section).
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
    suspend fun createTodoList(@Body body: CreateTodoListRequestDto): IdResponseDto

    @PATCH("api/v1/todolists/{id}")
    suspend fun renameTodoList(@Path("id") id: Int, @Body body: RenameTodoListRequestDto)

    @DELETE("api/v1/todolists/{id}")
    suspend fun deleteTodoList(@Path("id") id: Int)

    @POST("api/v1/todolists/{listId}/items")
    suspend fun addTodoItem(@Path("listId") listId: Int, @Body body: CreateTodoItemRequestDto): IdResponseDto

    @PATCH("api/v1/todolists/{listId}/items/{itemId}")
    suspend fun renameTodoItem(@Path("listId") listId: Int, @Path("itemId") itemId: Int, @Body body: RenameTodoItemRequestDto)

    @PATCH("api/v1/todolists/{listId}/items/{itemId}/priority")
    suspend fun setTodoItemPriority(@Path("listId") listId: Int, @Path("itemId") itemId: Int, @Body body: SetPriorityRequestDto)

    @PATCH("api/v1/todolists/{listId}/items/{itemId}/category")
    suspend fun setTodoItemCategory(@Path("listId") listId: Int, @Path("itemId") itemId: Int, @Body body: SetCategoryRequestDto)

    @PATCH("api/v1/todolists/{listId}/items/{itemId}/due-date")
    suspend fun setTodoItemDueDate(@Path("listId") listId: Int, @Path("itemId") itemId: Int, @Body body: SetDueDateRequestDto)

    // Backend uses POST for complete/reopen, not PATCH — they're actions,
    // not partial-update-of-a-resource in the REST-purist sense the other
    // PATCH endpoints follow. Checked directly against
    // TodoListEndpoints.cs; the initial scaffold had both as PATCH and
    // that was never actually verified against the backend until now.
    @POST("api/v1/todolists/{listId}/items/{itemId}/complete")
    suspend fun completeTodoItem(@Path("listId") listId: Int, @Path("itemId") itemId: Int)

    @POST("api/v1/todolists/{listId}/items/{itemId}/reopen")
    suspend fun reopenTodoItem(@Path("listId") listId: Int, @Path("itemId") itemId: Int)

    @DELETE("api/v1/todolists/{listId}/items/{itemId}")
    suspend fun removeTodoItem(@Path("listId") listId: Int, @Path("itemId") itemId: Int)
}
