package android.support.core.extensions

import android.app.Activity
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Parcelable
import android.support.core.annotations.SharedOf
import android.support.core.base.BaseActivity
import android.support.core.base.BaseFragment
import android.support.core.factory.ViewModelFactory
import android.support.core.functional.Dispatcher
import android.support.core.lifecycle.ResultLifecycle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import java.io.Serializable
import kotlin.reflect.KClass

const val REQUEST_FOR_RESULT_INSTANTLY = 1000

fun Dispatcher.open(clazz: KClass<out AppCompatActivity>, vararg args: Pair<String, Any>): Dispatcher {
    when (this) {
        is Fragment -> startActivity(Intent(activity, clazz.java).put(args))
        is Activity -> startActivity(Intent(this, clazz.java).put(args))
        else -> throw IllegalArgumentException("This is not instance of activity or fragment")
    }
    return this
}

fun Dispatcher.openForResult(clazz: KClass<out AppCompatActivity>, vararg args: Pair<String, Any>): ResultLifecycle {
    when (this) {
        is BaseFragment -> startActivityForResult(Intent(activity, clazz.java).put(args), REQUEST_FOR_RESULT_INSTANTLY)
        is BaseActivity -> startActivityForResult(Intent(this, clazz.java).put(args), REQUEST_FOR_RESULT_INSTANTLY)
        else -> throw IllegalArgumentException("This is not instance of BaseActivity or BaseFragment")
    }
    return getResultLifecycle()
}

fun Dispatcher.close() {
    when (this) {
        is Fragment -> activity!!.finish()
        is Activity -> finish()
    }
}

private fun Intent.put(args: Array<out Pair<String, Any>>): Intent {
    args.forEach {
        when {
            it.second is Serializable -> putExtra(it.first, it.second as Serializable)
            it.second is Parcelable -> putExtra(it.first, it.second as Parcelable)
            else -> throw RuntimeException("Not support this type ${it.second.javaClass.name}")
        }
    }
    return this
}

inline fun <reified T : ViewModel> Fragment.viewModel(sharedOf: SharedOf) =
    lazy {
        val factory = ViewModelFactory.sInstance
        val provider = when (sharedOf) {
            SharedOf.NONE -> ViewModelProviders.of(this, factory)
            SharedOf.PARENT -> {
                if (parentFragment != null)
                    ViewModelProviders.of(parentFragment!!, factory)
                else
                    ViewModelProviders.of(activity!!, factory)
            }
            else -> ViewModelProviders.of(activity!!, factory)
        }
        provider.get(T::class.java)
    }

inline fun <reified T : ViewModel> FragmentActivity.viewModel() = lazy {
    val factory = ViewModelFactory.sInstance
    ViewModelProviders.of(this, factory).get(T::class.java)
}