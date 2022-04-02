package com.cramsan.coroutineperf

import android.os.Debug
import android.os.Debug.MemoryInfo
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cramsan.coroutineperf.api.ReqbinService
import com.cramsan.coroutineperf.storage.Response
import com.cramsan.coroutineperf.storage.ResponseDao
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Scheduler
import io.reactivex.Single
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class FragmentViewModel @Inject constructor(
    private val responseDao: ResponseDao,
    private val apiService: ReqbinService,
    private val ioDispatcher: CoroutineDispatcher,
    private val ioScheduler: Scheduler,
) : ViewModel() {

    val runButtonEnabled = MutableLiveData(true)
    val taskOutput = MutableLiveData("")

    fun runTasks(taskMode: TaskMode, iterations: Int, concurrent: Boolean) {
        taskOutput.value = "Running..."
        runButtonEnabled.value = false
        when(taskMode) {
            TaskMode.RX -> executeRx(iterations, concurrent)
            TaskMode.COROUTINE -> executeSuspend(iterations, concurrent)
        }
    }

    private fun captureRxMetrics(taskName: String, block: () -> Unit) {
        Log.i("FragmentViewModel", "Starting $taskName")
        val startingRAM = getRamInMb()
        val duration = measureTimeMillis {
            block()
        }
        val endingRAM = getRamInMb()
        Log.i("FragmentViewModel", "Ended $taskName")
        taskOutput.postValue("Duration: ${duration}ms. RAM change: ${endingRAM - startingRAM}MB")
        runButtonEnabled.postValue(true)
    }

    private suspend fun captureSuspendMetrics(taskName: String, block: suspend () -> Unit) {
        Log.i("FragmentViewModel", "Starting $taskName")
        val startingRAM = getRamInMb()
        val startingTime = System.currentTimeMillis()

        block()

        val endingTime = System.currentTimeMillis()
        val endingRAM = getRamInMb()
        val duration = endingTime - startingTime
        Log.i("FragmentViewModel", "Ended $taskName")
        taskOutput.postValue("Duration: ${duration}ms. RAM change: ${endingRAM - startingRAM}MB")
        runButtonEnabled.postValue(true)
    }

    private fun executeRx(iterations: Int, concurrent: Boolean) {
        Single.create<Unit> {
            captureRxMetrics("Launching $iterations Rx tasks") {
                if (concurrent) {
                    val tasks = (0 until iterations).map {
                        executeRxTask(it.toString())
                    }

                    Single.zip(tasks) { }.blockingGet()
                } else {
                    repeat(iterations) {
                        executeRxTask(it.toString()).blockingGet()
                    }
                }
            }
            it.onSuccess(Unit)
        }.subscribeOn(ioScheduler).subscribe()
    }

    private fun executeRxTask(taskName: String): Single<Unit> {
        Log.i("FragmentViewModel", "Rx task $taskName started")
        return apiService.echo().subscribeOn(ioScheduler).map {
            val entry = Response(
                uid = taskName.hashCode(),
                status = it.success,
            )
            responseDao.insert(entry)
            responseDao.delete(entry)
            Log.i("FragmentViewModel", "Rx task $taskName completed")
        }
    }

    private fun executeSuspend(iterations: Int, concurrent: Boolean) = viewModelScope.launch(ioDispatcher) {
        captureSuspendMetrics("Launching $iterations suspending tasks") {
            if (concurrent) {
                (0 until iterations).map {
                    executeSuspendTask(it.toString())
                }.joinAll()
            } else {
                repeat(iterations) {
                    executeSuspendTask(it.toString()).join()
                }
            }
        }
    }

    private suspend fun executeSuspendTask(taskName: String) = viewModelScope.launch(ioDispatcher) {
        Log.i("FragmentViewModel", "Suspending task $taskName started")
        val response = apiService.echoSuspend()
        val entry = Response(
            uid = taskName.hashCode(),
            status = response.success,
        )
        responseDao.insertSuspend(entry)
        responseDao.deleteSuspend(entry)
        Log.i("FragmentViewModel", "Suspending task $taskName completed")
    }

    private fun getRamInMb(): Long {
        val memInfo = MemoryInfo()
        Debug.getMemoryInfo(memInfo)
        var res = memInfo.totalPrivateDirty
        res += memInfo.totalPrivateClean

        return res / 1024L
    }
}