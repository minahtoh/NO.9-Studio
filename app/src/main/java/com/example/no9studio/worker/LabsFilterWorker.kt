package com.example.no9studio.worker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.example.no9studio.filters.LabFilters
import com.example.no9studio.viewmodel.LabsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class LabsFilterWorker(context: Context, workerParameters: WorkerParameters,
                       private val labsRepository: LabsRepository)
    :CoroutineWorker(context,workerParameters){

    private suspend fun loadBitmapFromUri(context: Context, inputUri: Uri): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(inputUri)
                BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                // Handle exceptions, logging, etc.
                null
            }
        }
    }
    override suspend fun doWork(): Result {
        val inputUri = inputData.getString(KEY_INPUT_URI)
        val filterType = inputData.getString(KEY_FILTER_TYPE)
        val originalBitmap = loadBitmapFromUri( applicationContext, Uri.parse(inputUri))

        if (!inputUri.isNullOrEmpty() && !filterType.isNullOrEmpty()){
            val filter = labsRepository.getFilter(filterType)
            val filteredBitmap = originalBitmap?.let { LabFilters.applyFilter(it, filter) }

            // Convert Bitmap to byte array
            val filteredBitmapFile = saveBitmapToFile(filteredBitmap)

            // Create Data object and put the byte array
            val outputData = Data.Builder()
                .putString(KEY_FILTERED_BITMAP, filteredBitmapFile?.absolutePath)
                .build()


            return Result.success(outputData)
        }else{
            return Result.failure()
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap?): File? {
        if (bitmap == null) return null

        val file = File(applicationContext.cacheDir, "filtered_image.png")
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, outputStream)
            outputStream.close()
            return file
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }



    companion object {
        const val KEY_INPUT_URI = "input_uri"
        const val KEY_FILTER_TYPE = "filter_type"
        const val KEY_FILTERED_BITMAP = "filtered_bitmap"
    }
}