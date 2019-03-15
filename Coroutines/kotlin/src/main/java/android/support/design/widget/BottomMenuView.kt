package android.support.design.widget

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.support.R
import android.support.annotation.MenuRes
import android.support.design.internal.getMenu
import android.support.core.functional.MenuOwner
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

class BottomMenuView : TabLayout, MenuOwner {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        if (attrs == null) return
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BottomMenuView)
        val menuId = typedArray.getResourceId(R.styleable.BottomMenuView_tabMenu, 0)
        typedArray.recycle()
        if (menuId != 0) setMenu(menuId)
    }

    override fun getCurrentId() = getTabAt(selectedTabPosition)!!.tag as Int

    override fun setOnIdSelectedListener(onIdSelectedListener: (id: Int) -> Unit) {
        addOnTabSelectedListener(OnMenuChangedListener(onIdSelectedListener))
    }

    override fun selectId(id: Int) {
        if (getCurrentId() != id)
            findTabByTag(id)?.select()
    }

    internal override fun populateFromPagerAdapter() {
        if (viewPager.adapter !is IconSettable) {
            super.populateFromPagerAdapter()
            return
        }
        removeAllTabs()
        val pagerAdapter = viewPager.adapter
        if (pagerAdapter != null) {
            val adapterCount = pagerAdapter.count
            for (i in 0 until adapterCount) {
                val tab = newTab()
                    .setIcon((pagerAdapter as IconSettable).getPageIcon(i))
                    .setText(pagerAdapter.getPageTitle(i))
                addTab(tab, false)
            }

            if (viewPager != null && adapterCount > 0) {
                val curItem = viewPager.currentItem
                if (curItem != selectedTabPosition && curItem < tabCount) {
                    selectTab(getTabAt(curItem))
                }
            }
        }
    }

    private fun findTabByTag(id: Int): Tab? {
        val pos = (0 until tabCount).find { getTabAt(it)!!.tag as Int == id }
        if (pos != null)
            return getTabAt(pos)!!
        return null
    }

    fun setupWithAdapter(container: ViewGroup, adapter: FragmentPagerAdapter) {
        container.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(view: View) {
                adapter.startUpdate(container)
                adapter.instantiateItem(container, getTabAt(selectedTabPosition)?.tag as Int)
                adapter.finishUpdate(container)
            }

            override fun onViewDetachedFromWindow(view: View) {
                // Skip
            }
        })
        addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                adapter.startUpdate(container)
                adapter.instantiateItem(container, tab.tag as Int)
                adapter.finishUpdate(container)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                adapter.startUpdate(container)
                adapter.destroyItem(container, tab.tag as Int, adapter.getItem(tab.tag as Int)!!)
                adapter.finishUpdate(container)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // Skip
            }
        })
    }

    private fun setMenu(@MenuRes menuId: Int) {
        val menu = context.getMenu(menuId)
        for (i in 0 until menu.size()) {
            addMenu(menu.getItem(i), i)
        }
    }

    private fun addMenu(item: MenuItem, position: Int) {
        val tab = newTab().setTag(item.itemId)
        tab.setCustomView(R.layout.view_tab_menu)
        val view = tab.customView!! as ViewGroup
        val textView = view.getChildAt(1) as TextView
        val iconView = view.getChildAt(0) as ImageView

        item.title?.apply {
            textView.text = this
            if (tabTextSize != 0f) textView.setTextSize(0, tabTextSize)
            if (tabTextColors != null) textView.setTextColor(tabTextColors)
        }
        item.icon?.apply {
            DrawableCompat.setTintList(this, tabIconTint)
            if (tabIconTintMode != null) DrawableCompat.setTintMode(this, tabIconTintMode)
            iconView.setImageDrawable(this)
        }
        if (item.isChecked) tab.select()
        addTab(tab, position)
    }

    interface IconSettable {
        fun getPageIcon(i: Int): Int
    }

    override fun onSaveInstanceState(): Parcelable? {
        val parentState = super.onSaveInstanceState()
        val state = SaveState(parentState)
        state.tabSelected = selectedTabPosition
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SaveState) {
            super.onRestoreInstanceState(state)
            return
        }
        onRestoreInstanceState(state.superState)
        getTabAt(state.tabSelected)?.select()
    }

    class SaveState : BaseSavedState {
        var tabSelected: Int = 0

        constructor(parcel: Parcel) : super(parcel) {
            tabSelected = parcel.readInt()
        }

        constructor(parcelable: Parcelable?) : super(parcelable)

        override fun writeToParcel(out: Parcel?, flags: Int) {
            super.writeToParcel(out, flags)
            out?.writeInt(tabSelected)
        }

        companion object CREATOR : Parcelable.Creator<SaveState> {
            override fun createFromParcel(parcel: Parcel): SaveState {
                return SaveState(parcel)
            }

            override fun newArray(size: Int): Array<SaveState?> {
                return arrayOfNulls(size)
            }
        }
    }

    class OnMenuChangedListener(private val function: (Int) -> Unit) : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(p0: TabLayout.Tab?) {}

        override fun onTabUnselected(p0: TabLayout.Tab?) {}

        override fun onTabSelected(p0: TabLayout.Tab?) {
            function(p0!!.tag as Int)
        }
    }

    class OnAttachListener(private val function: () -> Unit) : OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(p0: View?) {
            function()
        }

        override fun onViewDetachedFromWindow(p0: View?) {}
    }
}