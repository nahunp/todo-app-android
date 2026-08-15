package com.nahunp.todoapp.data.sync

import com.nahunp.todoapp.core.connectivity.ConnectivityObserver
import com.nahunp.todoapp.core.network.TodoApiService
import com.nahunp.todoapp.data.local.LocalTodoDataSource
import com.nahunp.todoapp.data.local.dao.PendingOperationDao
import com.nahunp.todoapp.data.local.entity.PendingOperationEntity
import com.nahunp.todoapp.data.local.entity.PendingOperationType
import com.nahunp.todoapp.data.remote.dto.IdResponseDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

/**
 * Covers the part of SyncManager that's actually risky: strict FIFO
 * draining, resolving a queued op's server id via a fresh lookup (not a
 * value baked in at enqueue time — see PendingOperationEntity's doc
 * comment), and the two different failure modes (HTTP error -> drop and
 * keep going, IOException -> stop and leave the rest queued). The
 * individual `when` branches in applyOperation are otherwise low-risk,
 * mechanical mappings not worth a test each.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SyncManagerTest {

    private class FakeConnectivityObserver(online: Boolean = false) : ConnectivityObserver {
        val backing = MutableStateFlow(online)
        override val isOnline: Flow<Boolean> get() = backing
    }

    private fun httpError(code: Int): HttpException =
        HttpException(Response.error<Any>(code, "".toResponseBody("application/json".toMediaType())))

    private fun buildManager(
        testScope: TestScope,
        api: TodoApiService,
        local: LocalTodoDataSource,
        pendingOps: PendingOperationDao,
        connectivity: FakeConnectivityObserver,
    ): SyncManager {
        every { pendingOps.observeCount() } returns MutableStateFlow(0)
        return SyncManager(api, local, pendingOps, connectivity, testScope)
    }

    @Test
    fun `a CREATE_ITEM queued behind CREATE_LIST resolves the list's server id by lookup, not a stale enqueue-time value`() = runTest {
        val api = mockk<TodoApiService>()
        val local = mockk<LocalTodoDataSource>(relaxed = true)
        val pendingOps = mockk<PendingOperationDao>()
        val connectivity = FakeConnectivityObserver()

        val createList = PendingOperationEntity(id = 1, type = PendingOperationType.CREATE_LIST.name, listLocalKey = 10, payload = "Groceries")
        val createItem = PendingOperationEntity(id = 2, type = PendingOperationType.CREATE_ITEM.name, listLocalKey = 10, itemLocalKey = 20, payload = "Milk")
        coEvery { pendingOps.peekFirst() } returnsMany listOf(createList, createItem, null)
        coEvery { pendingOps.delete(any()) } just runs

        coEvery { api.createTodoList(match { it.name == "Groceries" }) } returns IdResponseDto(id = 501)
        // The lookup CREATE_ITEM depends on -- proves resolution happens
        // at push time via LocalTodoDataSource, not from anything stored
        // on the PendingOperationEntity itself (it only ever had the
        // *local* key, 10).
        coEvery { local.getListServerId(10) } returns 501
        coEvery { api.addTodoItem(501, match { it.title == "Milk" }) } returns IdResponseDto(id = 900)

        coEvery { api.getTodoLists() } returns emptyList()

        val testScope = TestScope(StandardTestDispatcher(testScheduler))
        buildManager(testScope, api, local, pendingOps, connectivity)
        connectivity.backing.value = true
        advanceUntilIdle()

        coVerifyOrder {
            api.createTodoList(any())
            local.setListServerId(10, 501)
            api.addTodoItem(501, any())
            local.setItemServerId(20, 900)
        }
        coVerify(exactly = 2) { pendingOps.delete(any()) } // both ops removed once applied
    }

    @Test
    fun `an HTTP error drops the offending op and keeps draining the rest of the queue`() = runTest {
        val api = mockk<TodoApiService>()
        val local = mockk<LocalTodoDataSource>(relaxed = true)
        val pendingOps = mockk<PendingOperationDao>()
        val connectivity = FakeConnectivityObserver()

        // A rename against a list that turns out to be already deleted
        // server-side (404) -- shouldn't block a later, unrelated op.
        val staleRename = PendingOperationEntity(id = 1, type = PendingOperationType.RENAME_LIST.name, listLocalKey = 10, payload = "New name")
        val unrelatedCreate = PendingOperationEntity(id = 2, type = PendingOperationType.CREATE_LIST.name, listLocalKey = 11, payload = "Errands")
        coEvery { pendingOps.peekFirst() } returnsMany listOf(staleRename, unrelatedCreate, null)
        coEvery { pendingOps.delete(any()) } just runs

        coEvery { local.getListServerId(10) } returns 501
        coEvery { api.renameTodoList(501, any()) } throws httpError(404)
        coEvery { api.createTodoList(any()) } returns IdResponseDto(id = 502)
        coEvery { api.getTodoLists() } returns emptyList()

        val testScope = TestScope(StandardTestDispatcher(testScheduler))
        val manager = buildManager(testScope, api, local, pendingOps, connectivity)
        connectivity.backing.value = true
        advanceUntilIdle()

        // The failed op is still removed from the queue (dropping it, not
        // retrying it, is the point -- see pushQueue's HttpException
        // comment), and the op behind it still gets processed.
        coVerify { pendingOps.delete(1) }
        coVerify { api.createTodoList(any()) }
        coVerify { local.setListServerId(11, 502) }
        // A dropped op is a deliberate, silent resolution, not a sync
        // failure -- getTodoLists() succeeding means the pull ran to
        // completion right after, so nothing should be sitting in
        // lastError.
        assertEquals(null, manager.status.value.lastError)
    }

    @Test
    fun `a network failure mid-push stops the loop and leaves that op (and everything behind it) queued`() = runTest {
        val api = mockk<TodoApiService>()
        val local = mockk<LocalTodoDataSource>(relaxed = true)
        val pendingOps = mockk<PendingOperationDao>()
        val connectivity = FakeConnectivityObserver()

        val firstOp = PendingOperationEntity(id = 1, type = PendingOperationType.CREATE_LIST.name, listLocalKey = 10, payload = "Groceries")
        // peekFirst is only ever expected to be asked for the first op --
        // if the loop incorrectly continued past the IOException, this
        // would fail to answer a second call and the test would blow up
        // with a MockK "no answer found" error, which is exactly what we
        // want it to do in that case.
        coEvery { pendingOps.peekFirst() } returns firstOp

        coEvery { api.createTodoList(any()) } throws IOException("no route to host")

        val testScope = TestScope(StandardTestDispatcher(testScheduler))
        buildManager(testScope, api, local, pendingOps, connectivity)
        connectivity.backing.value = true
        advanceUntilIdle()

        coVerify(exactly = 0) { pendingOps.delete(any()) } // op stays queued, not dropped
        coVerify(exactly = 0) { api.getTodoLists() } // the pull never runs -- push didn't finish
    }
}
