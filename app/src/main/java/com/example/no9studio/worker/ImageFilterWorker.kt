package com.example.no9studio.worker

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
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
import java.io.OutputStream

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
           // val originalUri = originalBitmap?.let { saveBitmap(it, "original") }
            val filteredUri = filteredBitmap?.let { saveBitmapUri(it, "filtered") }
            val unsavedFilteredUri = filteredBitmap?.let { getUriFromUnsavedImage(it) }

            // Pass the URIs to the result
            val outputData = workDataOf(
               // KEY_ORIGINAL_URI to originalUri.toString(),
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

    private fun getUriFromUnsavedImage(bitmap: Bitmap): Uri {
        val context = applicationContext

        val dummyFilePath = File(context.cacheDir, "dummy_image.jpg")

        // Compress and save the bitmap to the dummy file path
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, dummyFilePath.outputStream())

        // Use FileProvider to get the content URI
       // return FileProvider.getUriForFile(context, context.packageName + ".provider", dummyFilePath)
        return Uri.fromFile(dummyFilePath)
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

    private fun saveBitmapUri(bitmap: Bitmap, prefix: String): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$prefix ${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            // Specify the directory where you want to save the image (e.g., Pictures/YourApp)
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/NO.9 Studio")
        }

        val resolver = applicationContext.contentResolver
        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val imageUri = resolver.insert(contentUri, values)

        imageUri?.let { safeUri ->
            resolver.openOutputStream(safeUri)?.use { outputStream: OutputStream ->
                // Save the bitmap to the output stream
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
        }

        return imageUri ?: Uri.EMPTY
    }

    companion object {
        const val KEY_INPUT_URI = "input_uri"
        const val KEY_FILTER_TYPE = "filter_type"
        const val KEY_ORIGINAL_URI = "original_uri"
        const val KEY_FILTERED_URI = "filtered_uri"
        const val FILTERED_IMAGES_DIR = "filtered_images"
    }
}