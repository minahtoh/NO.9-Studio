package com.example.no9studio.worker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toFile
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.no9studio.filters.FilterType
import com.example.no9studio.filters.ImageFilters
import com.example.no9studio.viewmodel.StudioViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ImageFilterWorker(context: Context, workerParams: WorkerParameters)
    : CoroutineWorker(context, workerParams) {

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

    override suspend fun doWork(): Result = withContext(Dispatchers.Default) {
            val inputUriString = inputData.getString(KEY_INPUT_URI)
            val filterTypeString = inputData.getString(KEY_FILTER_TYPE)
            var filteredBitmap : Bitmap? = null

        if (inputUriString != null && filterTypeString != null) {
            val inputUri = Uri.parse(inputUriString)
            val filterType = FilterType.valueOf(filterTypeString)

            val originalBitmap = loadBitmapFromUri(applicationContext,inputUri)

            filteredBitmap = originalBitmap?.let { applyFilter(it, filterType) }

            // Save both original and filtered images
            val originalUri = originalBitmap?.let { saveBitmap(it, "original") }
            val filteredUri = filteredBitmap?.let { saveBitmap(it, "filtered") }

            // Pass the URIs to the result
            val outputData = workDataOf(
                KEY_ORIGINAL_URI to originalUri.toString(),
                KEY_FILTERED_URI to filteredUri.toString()
            )
            Result.success(outputData)
        } else {
            Result.failure()
        }
    }

    private fun applyFilter(bitmap: Bitmap, filterType: FilterType): Bitmap {
        return when (filterType) {
            FilterType.GRAYSCALE -> ImageFilters.applyGrayscale(bitmap)
            FilterType.INVERT -> ImageFilters.applyInvert(bitmap)
            FilterType.SEPIA -> ImageFilters.applySepia(bitmap)
            FilterType.VINTAGE -> ImageFilters.applyVintage(bitmap)
            FilterType.THRESHOLD -> ImageFilters.applyThreshold(bitmap)
            FilterType.HIGH_CONTRAST -> ImageFilters.applyHighContrast(bitmap)
            FilterType.HIGH_SATURATION -> ImageFilters.applyHighSaturation(bitmap)
            FilterType.BLACK_AND_WHITE_WITH_GRAIN -> ImageFilters.applyBlackAndWhiteWithGrain(bitmap)
            else -> {bitmap}
        }
    }

    private fun saveBitmap(bitmap: Bitmap, prefix: String): Uri {
        val outputDir = File(applicationContext.filesDir, FILTERED_IMAGES_DIR)
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        val outputFile = File(outputDir, "$prefix ${System.currentTimeMillis()}.png")
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputFile.outputStream())

        return Uri.fromFile(outputFile)
    }

    companion object {
        const val KEY_INPUT_URI = "input_uri"
        const val KEY_FILTER_TYPE = "filter_type"
        const val KEY_ORIGINAL_URI = "original_uri"
        const val KEY_FILTERED_URI = "filtered_uri"
        const val FILTERED_IMAGES_DIR = "filtered_images"
    }
}