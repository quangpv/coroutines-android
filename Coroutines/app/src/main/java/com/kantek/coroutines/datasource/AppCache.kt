package com.kantek.coroutines.datasource

import androidx.lifecycle.MutableLiveData
import android.content.Context
import android.support.core.di.Inject
import android.support.core.extensions.loadOnDisk
import com.google.gson.Gson
import com.kantek.coroutines.models.User

@Inject(true)
class AppCache(context: Context) {
    private val mShared = context.getSharedPreferences("test:cache", Context.MODE_PRIVATE)
    private var mUser: User? = null
    var user: User?
        get() {
            if (mUser == null) {
                mUser = Gson().fromJson(mShared.getString(User::class.java.name, ""), User::class.java)
            }
            return mUser
        }
        set(value) {
            mUser = value
            mShared.edit().putString(User::class.java.name, Gson().toJson(user)).apply()
            (userLive as MutableLiveData).postValue(value)
        }

    val userLive = MutableLiveData<User>().loadOnDisk { user }
}
