package android.support.core.base

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.support.core.event.LoadingEvent
import android.support.core.event.RefreshEvent
import android.support.core.event.SingleLiveEvent
import android.support.core.extensions.withIO
import android.support.core.lifecycle.LifeRegistry
import android.util.Log
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel : ViewModel(), LifecycleOwner {
    val loading = LoadingEvent()
    val error = SingleLiveEvent<Throwable>()
    val refresh = RefreshEvent<Any>(this)

    private val mLife = LifeRegistry(this)
    private val mScope = ViewModelScope()

    override fun getLifecycle() = mLife

    init {
        mLife.create().start()
    }

    fun launch(
        loading: MutableLiveData<Boolean>? = this@BaseViewModel.loading,
        error: SingleLiveEvent<out Throwable>? = this@BaseViewModel.error,
        block: suspend CoroutineScope.() -> Unit
    ) = mScope.launch {
        try {
            loading?.postValue(true)
            withIO(block)
        } catch (e: CancellationException) {
            Log.i(this@BaseViewModel.javaClass.name, e.message ?: "Unknown")
        } catch (e: Throwable) {
            e.printStackTrace()
            Log.e("CALL_ERROR", "${e.javaClass.name} ${e.message ?: "Unknown"}")
            @Suppress("UNCHECKED_CAST")
            (error as? MutableLiveData<Throwable>)?.postValue(e)
        } finally {
            loading?.postValue(false)
        }
    }

    suspend fun <T> onMain(block: suspend CoroutineScope.() -> T) =
        withContext(Dispatchers.Main, block)

    suspend fun <T> onBackground(block: suspend CoroutineScope.() -> T) =
        withContext(Dispatchers.IO, block)

    override fun onCleared() {
        mLife.stop().destroy()
        mScope.coroutineContext.cancel()
    }

    private class ViewModelScope : CoroutineScope {
        override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main
    }
}