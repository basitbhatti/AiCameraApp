package com.basitbhatti.camera

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {


    private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val bitmaps = _bitmaps.asStateFlow()

    fun onPhotoTaken(bitmap: Bitmap) {
        Log.d("TAGPhoto", "bitmaps: ${_bitmaps.value.size}")
        _bitmaps.value += bitmap
    }

}