package com.example.no9studio.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.workDataOf

class StudioViewModel: ViewModel() {
    val isLoading: MutableLiveData<Boolean> = MutableLiveData(false)

    // Update progress based on Worker updates
    fun updateProgress(progress: Int) {
        isLoading.postValue(progress < 100)
    }


}