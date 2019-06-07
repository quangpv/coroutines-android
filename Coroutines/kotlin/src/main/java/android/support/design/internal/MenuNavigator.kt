package android.support.design.internal

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.R
import android.support.annotation.IdRes
import android.support.annotation.NonNull
import android.support.core.base.BaseFragment
import android.support.core.extensions.addArgs
import android.support.core.functional.navigableOptions
import android.support.design.widget.MenuHostFragment
import android.support.design.widget.SupportNavHostFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.util.AttributeSet
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.NavHostFragment

abstract class MenuNavigator(val fragmentManager: FragmentManager) :
    Navigator<MenuNavigator.Destination>() {
    companion object {
        private const val KEY_LAST_DESTINATION = "android:menu:navigator:key:last:destination"
    }

    lateinit var findDestinationById: (Int) -> Destination
    private var mCurrentTransaction: FragmentTransaction? = null
    private var mCurrentId: Int = 0
    private lateinit var mOnNavigateChangedListener: (Int) -> Unit

    val currentDestinationId get() = mCurrentId
    val currentDestination get() = findDestinationById(mCurrentId) as? Destination

    override fun createDestination() = Destination(this)

    override fun popBackStack() = false

    override fun onSaveState(): Bundle? {
        val state = Bundle()
        state.putInt(KEY_LAST_DESTINATION, mCurrentId)
        return state
    }

    override fun onRestoreState(savedState: Bundle) {
        val lastId = savedState.getInt(KEY_LAST_DESTINATION, 0)
        if (lastId == 0) return
        transaction { findFragment(lastId)?.apply { hideFragmentsIfNeeded(this) } }
        mCurrentId = lastId
    }

    override fun navigate(destination: Destination,
                          args: Bundle?,
                          navOptions: NavOptions?,
                          navigatorExtras: Extras?): NavDestination? {
        if (mCurrentId == destination.id) {
            if (args != null) notifyArgumentsChanged(destination, args)
            return destination
        }
        navigateTo(destination, args, navOptions, navigatorExtras)
        return destination
    }

    fun navigate(hostDestination: Destination, @IdRes childDestination: Int, args: Bundle? = null, navOptions: NavOptions? = null): NavDestination? {
        return navigate(hostDestination, navigableOptions(childDestination, args), navOptions, null)
    }

    private fun navigateTo(destination: Destination,
                           args: Bundle?,
                           navOptions: NavOptions?,
                           navigatorExtras: Extras?) {
        startUpdate()
        if (navOptions != null) addAnimationIfNeeded(navOptions, destination)
        if (mCurrentId != 0) destroy(mCurrentId)
        addArguments(instantiate(mCurrentTransaction!!, destination, navOptions), args)
        finishUpdate()
        notifyNavigateChanged(destination.id)
    }

    protected fun notifyNavigateChanged(destinationId: Int) {
        mCurrentId = destinationId
        mOnNavigateChangedListener(destinationId)
    }

    private fun addArguments(fragment: Fragment, args: Bundle?) {
        mCurrentTransaction!!.setPrimaryNavigationFragment(fragment)

        when (fragment) {
            is MenuHostFragment -> if (args != null) fragment.addArgs(args)
            is NavHostFragment -> if (args != null) fragment.addArgs(args)
            else -> fragment.arguments = args
        }
    }

    private fun notifyArgumentsChanged(destination: Destination, args: Bundle) {
        when (val fragment = findFragment(destination.id)) {
            is BaseFragment -> {
                fragment.arguments = args
                fragment.handleNavigateArguments(args)
            }
            is MenuHostFragment -> {
                fragment.addArgs(args)
                fragment.navigateIfNeeded()
            }
            is SupportNavHostFragment -> {
                fragment.addArgs(args)
                fragment.navigateIfNeeded()
            }
        }
    }

    protected fun addAnimationIfNeeded(navOptions: NavOptions, destination: Destination?) {
        var enterAnim = navOptions.enterAnim
        var exitAnim = navOptions.exitAnim
        var popEnterAnim = navOptions.popEnterAnim
        var popExitAnim = navOptions.popExitAnim
        enterAnim = if (enterAnim != -1) enterAnim else 0
        exitAnim = if (exitAnim != -1) exitAnim else 0
        popEnterAnim = if (popEnterAnim != -1) popEnterAnim else 0
        popExitAnim = if (popExitAnim != -1) popExitAnim else 0
        setCustomAnimations(destination, enterAnim, exitAnim, popEnterAnim, popExitAnim)
    }

    protected open fun setCustomAnimations(destination: Destination?, enterAnim: Int, exitAnim: Int, popEnterAnim: Int, popExitAnim: Int) {
        mCurrentTransaction!!.setCustomAnimations(enterAnim, exitAnim, popEnterAnim, popExitAnim)
    }

    private fun hideFragmentsIfNeeded(ignore: Fragment) {
        for (it in fragmentManager.fragments) {
            if (it.isAdded && !it.isHidden && it.userVisibleHint) {
                if (it != ignore) mCurrentTransaction!!.hide(it)
            }
        }
    }

    private fun destroy(destinationId: Int) {
        val fragment = findFragment(destinationId)!!
        mCurrentTransaction!!.hide(fragment)
    }

    protected abstract fun findFragment(destinationId: Int): Fragment?

    protected abstract fun instantiate(transaction: FragmentTransaction, destination: Destination, navOptions: NavOptions?): Fragment

    fun transaction(function: FragmentTransaction.() -> Unit) {
        startUpdate()
        function(mCurrentTransaction!!)
        finishUpdate()
    }

    private fun finishUpdate() {
        if (mCurrentTransaction != null) {
            mCurrentTransaction!!.commit()
            mCurrentTransaction = null
        }
    }

    @SuppressLint("CommitTransaction")
    private fun startUpdate() {
        if (mCurrentTransaction == null) {
            mCurrentTransaction = fragmentManager.beginTransaction()
        }
    }

    fun setOnNavigateChangedListener(function: (Int) -> Unit) {
        mOnNavigateChangedListener = function
    }

    @NavDestination.ClassType(Fragment::class)
    open class Destination(@NonNull fragmentNavigator: Navigator<out Destination>) : NavDestination(fragmentNavigator) {
        companion object {
            const val NAV_TYPE_ORDER = 1
            const val NAV_TYPE_STACK = 2
        }

        private var mNavType: Int = NAV_TYPE_ORDER
        private var mFragmentClass: Class<out Fragment>? = null
        private var mNavGraph = 0
        private var mNavMenu = 0

        val fragmentClass: Class<out Fragment>?
            @NonNull
            get() {
                if (mFragmentClass == null)
                    throw IllegalStateException("fragment class not set")

                return mFragmentClass
            }

        override fun onInflate(@NonNull context: Context, @NonNull attrs: AttributeSet) {
            super.onInflate(context, attrs)
            val a = context.resources.obtainAttributes(attrs, R.styleable.FragmentNavigator)
            val className = a.getString(R.styleable.FragmentNavigator_android_name)
            if (className != null) {
                setFragmentClass(parseClassFromName(context, className, Fragment::class.java))
            }
            a.recycle()

            val graph = context.obtainStyledAttributes(attrs, R.styleable.NavHostFragment)
            mNavGraph = graph.getResourceId(R.styleable.NavHostFragment_navGraph, 0)
            graph.recycle()

            val ta = context.obtainStyledAttributes(attrs, R.styleable.MenuHostFragment)
            mNavMenu = ta.getResourceId(R.styleable.MenuHostFragment_navMenu, 0)
            mNavType = ta.getInt(R.styleable.MenuHostFragment_navType, NAV_TYPE_ORDER)
            ta.recycle()
        }

        @NonNull
        fun setFragmentClass(@NonNull clazz: Class<out Fragment>): Destination {
            mFragmentClass = clazz
            return this
        }

        @NonNull
        fun createFragment(): Fragment {
            val clazz = fragmentClass
            val f: Fragment
            try {
                f = when {
                    NavHostFragment::class.java.isAssignableFrom(clazz!!) -> {
                        if (mNavGraph == 0) throw RuntimeException("Need a navGraph for host")
                        SupportNavHostFragment.create(mNavGraph)
                    }
                    MenuHostFragment::class.java.isAssignableFrom(clazz) -> {
                        if (mNavGraph == 0) throw RuntimeException("Need a navGraph for host")
                        MenuHostFragment.create(mNavGraph, mNavMenu, mNavType)
                    }
                    else -> clazz.newInstance()
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }

            return f
        }
    }
}