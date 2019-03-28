package com.kantek.coroutines.viewmodel

import android.support.core.base.BaseViewModel
import android.support.core.event.RequestEvent
import android.support.core.extensions.map
import com.kantek.coroutines.datasource.AppEvent
import com.kantek.coroutines.models.Album
import com.kantek.coroutines.repository.AlbumRepository

class AlbumViewModel(
    albumRepository: AlbumRepository,
    appEvent: AppEvent
) : BaseViewModel() {
    val album = RequestEvent<Album>(this)

    val photos = album.map(this) {
        albumRepository.getPhotos(it!!.id)
    }

    init {
        album.addEvent(appEvent.networkChanged, photos)
    }
}
