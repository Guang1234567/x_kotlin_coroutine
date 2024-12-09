package com.x.coroutines.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class HomeViewModel : ViewModel() {

    private val _text2 = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text2: LiveData<String> = _text2

    private val _text: MutableStateFlow<String> = MutableStateFlow("This is home Fragment")
    val text: Flow<String> = _text
}