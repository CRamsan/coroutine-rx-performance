package com.cramsan.coroutineperf

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.cramsan.coroutineperf.api.EchoResponse
import com.cramsan.coroutineperf.api.ReqbinService
import com.cramsan.coroutineperf.storage.ResponseDao
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestRule

@OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
class FragmentViewModelTest {

    @get:Rule var rule: TestRule = InstantTaskExecutorRule()

    private val testCoroutineDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testCoroutineDispatcher)

    @MockK
    lateinit var responseDao: ResponseDao

    @MockK
    lateinit var apiService: ReqbinService

    val ECHO_RESPONSE = EchoResponse("true")

    lateinit var viewModel: FragmentViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testCoroutineDispatcher)

        MockKAnnotations.init(this)

        viewModel = FragmentViewModel(
            responseDao,
            apiService,
            testCoroutineDispatcher,
            Schedulers.trampoline(),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset the main dispatcher to the original Main dispatcher
    }

    @Test
    fun test_initial_state() = testScope.runTest {
        assertEquals(viewModel.runButtonEnabled.value, true)
        assertEquals(viewModel.taskOutput.value, "")
        assertEquals(viewModel.taskOutput.value, "")
    }

    @Test
    fun test_coroutine_concurrent_tasks() = testScope.runTest {
        coEvery { apiService.echoSuspend() } returns ECHO_RESPONSE
        coEvery { responseDao.insertSuspend(any()) } returns Unit
        coEvery { responseDao.deleteSuspend(any()) } returns Unit

        viewModel.runTasks(TaskMode.COROUTINE, 10, true)

        testScheduler.advanceUntilIdle()

        assertEquals(viewModel.runButtonEnabled.value, true)
        assertFalse(viewModel.taskOutput.value.isNullOrBlank())

        coVerify(exactly = 10) { apiService.echoSuspend() }
        coVerify(exactly = 10) { responseDao.insertSuspend(any()) }
        coVerify(exactly = 10) { responseDao.deleteSuspend(any()) }
    }

    @Test
    fun test_coroutine_sequential_tasks() = testScope.runTest {
        coEvery { apiService.echoSuspend() } returns ECHO_RESPONSE
        coEvery { responseDao.insertSuspend(any()) } returns Unit
        coEvery { responseDao.deleteSuspend(any()) } returns Unit

        viewModel.runTasks(TaskMode.COROUTINE, 10, false)

        testScheduler.advanceUntilIdle()

        assertEquals(true, viewModel.runButtonEnabled.value, )
        assertFalse(viewModel.taskOutput.value.isNullOrBlank())

        coVerify(exactly = 10) { apiService.echoSuspend() }
        coVerify(exactly = 10) { responseDao.insertSuspend(any()) }
        coVerify(exactly = 10) { responseDao.deleteSuspend(any()) }
    }

    @Test
    fun test_rx_concurrent_tasks() = testScope.runTest {
        every { apiService.echo() } returns Single.just(ECHO_RESPONSE)
        every { responseDao.insert(any()) } returns Unit
        every { responseDao.delete(any()) } returns Unit

        viewModel.runTasks(TaskMode.RX, 10, true)

        assertEquals(viewModel.runButtonEnabled.value, true)
        assertFalse(viewModel.taskOutput.value.isNullOrBlank())

        verify(exactly = 10) { apiService.echo() }
        verify(exactly = 10) { responseDao.insert(any()) }
        verify(exactly = 10) { responseDao.delete(any()) }
    }

    @Test
    fun test_rx_sequential_tasks() = testScope.runTest {
        every { apiService.echo() } returns Single.just(ECHO_RESPONSE)
        every { responseDao.insert(any()) } returns Unit
        every { responseDao.delete(any()) } returns Unit

        viewModel.runTasks(TaskMode.RX, 10, false)

        assertEquals(viewModel.runButtonEnabled.value, true)
        assertFalse(viewModel.taskOutput.value.isNullOrBlank())

        verify(exactly = 10) { apiService.echo() }
        verify(exactly = 10) { responseDao.insert(any()) }
        verify(exactly = 10) { responseDao.delete(any()) }
    }
}