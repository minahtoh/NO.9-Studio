package com.example.no9studio.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.no9studio.filters.FilterType
import com.example.no9studio.worker.ImageFilterWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StudioViewModel: ViewModel() {
    private val _selectedImage = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImage.asStateFlow()
    private val _filterWorkRunning = MutableStateFlow(false)
    val filterWorkRunning = _filterWorkRunning.asStateFlow()

    fun selectImageForFilter(imageUri : Uri){
        _selectedImage.value = imageUri
    }
    fun applyFilter(filterType: FilterType, context: Context) {
        val workManager = WorkManager.getInstance(context)

        val inputData = workDataOf(
            ImageFilterWorker.KEY_INPUT_URI to _selectedImage.value.toString(),
            ImageFilterWorker.KEY_FILTER_TYPE to filterType.name
        )

        val workRequest = OneTimeWorkRequestBuilder<ImageFilterWorker>()
            .setInputData(inputData)
            .build()

        workManager.enqueue(workRequest)


        workManager.getWorkInfoByIdLiveData(workRequest.id).observeForever { workInfo ->

            when (workInfo.state) {
                WorkInfo.State.SUCCEEDED -> {
                    val outputUriString =
                        workInfo.outputData.getString(ImageFilterWorker.KEY_FILTERED_URI)
                    val outputUri = Uri.parse(outputUriString)
                    // Do something with the filtered image URI
                    _selectedImage.value = outputUri
                    _filterWorkRunning.value = false
                }

                WorkInfo.State.ENQUEUED -> {
                    _filterWorkRunning.value = true
                }

                WorkInfo.State.RUNNING -> {
                    _filterWorkRunning.value = true
                }

                WorkInfo.State.FAILED -> {
                    _filterWorkRunning.value = false
                    Toast.makeText(context, "Failed to apply filter!", Toast.LENGTH_SHORT).show()
                }

                WorkInfo.State.BLOCKED -> {
                    _filterWorkRunning.value = false
                }

                WorkInfo.State.CANCELLED -> {
                    _filterWorkRunning.value = false
                }
            }
        }
    }


}