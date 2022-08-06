package com.jigar.audioplayer.ui.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AudioViewModelFactory() : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AudioViewModel() as T
    }
}