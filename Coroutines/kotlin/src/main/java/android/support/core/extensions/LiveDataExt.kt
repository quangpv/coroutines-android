package android.support.core.extensions

import androidx.lifecycle.*
import android.os.Looper
import android.support.core.base.BaseFragment
import android.support.core.base.BaseViewModel
import android.support.core.event.SingleLiveEvent
import android.support.core.helpers.AppExecutors
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope

fun <T> LiveData<T>.observe(owner: LifecycleOwner, function: (T?) -> Unit) {
    if (owner is Fragment) {
        observe(if (owner is BaseFragment) owner.viewLife else owner.viewLifecycleOwner, Observer(function))
    } else {
        observe(owner, Observer(function))
    }
}

private fun isRunOnMainThread() = Looper.myLooper() == Looper.getMainLooper()

fun <T> MutableLiveData<T>.call() {
    if (isRunOnMainThread()) value = value
    else postValue(value)
}

fun <T> MutableLiveData<T>.refresh() {
    if (value != null) if (isRunOnMainThread()) value = value else postValue(value)
}

fun <T> MutableLiveData<T>.loadOnDisk(function: () -> T?): LiveData<T> {
    AppExecutors.onDisk { postValue(function()) }
    return this
}

fun <T> MutableLiveData<T>.load(function: () -> T?): LiveData<T> {
    value = function()
    return this
}

fun <T, V> LiveData<T>.map(function: (T?) -> V?): LiveData<V> =
    MediatorLiveData<V>().also { next ->
        next.addSource(this) {
            next.value = function(it)
        }
    }

fun <T, V> LiveData<T>.switchMap(function: (T?) -> LiveData<V>) =
    Transformations.switchMap(this, function)

fun <T, V> LiveData<T>.mapLive(function: MutableLiveData<V>.(T?) -> Unit): LiveData<V> =
    MediatorLiveData<V>().also { next ->
        next.addSource(this) {
            function(next, it)
        }
    }

fun <T> LiveData<T>.asSingleEvent() = SingleLiveEvent<T>().also { next ->
    next.addSource(this) { next.value = it }
}

fun <T> LiveData<T>.submit(owner: LifecycleOwner) {
    observe(owner, Observer { })
}