package com.kantek.coroutines.views

import android.os.Bundle
import android.support.core.annotations.LayoutId
import android.support.core.base.BaseActivity
import android.support.core.base.EmptyViewModel
import android.support.core.extensions.argument
import android.support.core.extensions.asArgument
import android.support.core.extensions.open
import com.kantek.coroutines.R
import com.kantek.coroutines.app.AppActivity
import com.kantek.coroutines.models.Photo
import kotlinx.android.synthetic.main.activity_photo.*

@LayoutId(R.layout.activity_photo)
class PhotoActivity : AppActivity<EmptyViewModel>() {
    companion object {
        fun show(from: BaseActivity, it: Photo) {
            from.open(PhotoActivity::class, it.asArgument())
        }
    }

    private val mPhoto: Photo by argument()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        txtTitle.text = mPhoto.title
        imgPhoto.setImageUrl(mPhoto.url)
    }
}