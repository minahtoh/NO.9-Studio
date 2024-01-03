package com.example.no9studio.viewmodel

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.no9studio.filters.FilterType
import com.example.no9studio.model.DownloadableFilter
import com.example.no9studio.worker.ImageFilterWorker
import com.example.no9studio.worker.LabsFilterWorker
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.log

class StudioViewModel(private val repository : LabsRepository): ViewModel() {
    private val _selectedImage = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImage.asStateFlow()
    private val _filterWorkRunning = MutableStateFlow(false)
    val filterWorkRunning = _filterWorkRunning.asStateFlow()
    private val selectedImage = MutableStateFlow<Uri?>(null)
    private val _labsFilteredBitmap = MutableStateFlow<Bitmap?>(null)
    val labsFilteredBitmap = _labsFilteredBitmap.asStateFlow()
    private val _labsFilters = MutableStateFlow<List<DownloadableFilter>>(emptyList())
    val labsFilters = _labsFilters.asStateFlow()



    fun selectDisplayImageForFilter(imageUri : Uri){
        _selectedImage.value = imageUri
    }

    fun selectImageForFilterWorker(imageUri: Uri){
        selectedImage.value = imageUri
    }

    fun getLabBitmap(context: Context, imageUri: Uri){
        val inputStream = context.contentResolver.openInputStream(imageUri)
        _labsFilteredBitmap.value = BitmapFactory.decodeStream(inputStream)
    }

    fun applyFilter(filterType: FilterType, context: Context) {
        val workManager = WorkManager.getInstance(context)


        val inputData = workDataOf(
            ImageFilterWorker.KEY_INPUT_URI to selectedImage.value.toString(),
            ImageFilterWorker.KEY_FILTER_TYPE to filterType.name,
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

    fun applyLabsFilter(filterType:String, context: Context){
        val workManager = WorkManager.getInstance(context)
        val inputData = workDataOf(
            LabsFilterWorker.KEY_FILTER_TYPE to filterType,
            LabsFilterWorker.KEY_INPUT_URI to selectedImage.value.toString()
        )
        val workRequest = OneTimeWorkRequestBuilder<LabsFilterWorker>()
            .setInputData(inputData)
            .build()
        workManager.enqueue(workRequest)
        workManager.getWorkInfoByIdLiveData(workRequest.id).observeForever {
            when(it.state){
                WorkInfo.State.SUCCEEDED ->{
                    val resultPath = it.outputData.getString(LabsFilterWorker.KEY_FILTERED_BITMAP)
                    val filteredBitmap = BitmapFactory.decodeFile(resultPath)
                    _labsFilteredBitmap.value = filteredBitmap
                    _filterWorkRunning.value = false
                }
                WorkInfo.State.ENQUEUED -> {
                    _filterWorkRunning.value = true
                }

                WorkInfo.State.RUNNING -> {
                    _filterWorkRunning.value = true
                }
                else -> {
                    _filterWorkRunning.value = false
                }
            }
        }
    }

    fun getLabsFilters(activity: Activity){
        val mFireStore = FirebaseFirestore.getInstance()
        val FILTERS_LAB = "9LabFilters"
        mFireStore.collection(FILTERS_LAB)
            .get()
            .addOnSuccessListener {
                    documents ->
                val downloadableFiltersList = mutableListOf<DownloadableFilter>()

                for (document in documents) {
                    // Use toObject to directly convert the data to DownloadableFilter
                    val downloadableFilter = document.toObject(DownloadableFilter::class.java)
                    downloadableFiltersList.add(downloadableFilter)
                }

                _labsFilters.value = downloadableFiltersList

                Log.d("Save me", "getLabsFilters: ${_labsFilters.value}")
            }
            .addOnFailureListener {
                Toast.makeText(activity.baseContext, "failed to get filters", Toast.LENGTH_SHORT).show()
            }
    }

    fun saveLabFilters(filter: DownloadableFilter){
        viewModelScope.launch {
            repository.saveFilter(filter)
        }
    }

}


class  StudioViewModelFactory(private val repository: LabsRepository): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudioViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class LabsWorkerFactory(private val repository: LabsRepository): WorkerFactory(){
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when(workerClassName){
            LabsFilterWorker::class.java.name ->
                LabsFilterWorker(appContext,workerParameters,repository)
            else ->
                null
        }
    }

}