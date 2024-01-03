package com.example.no9studio.filters

import android.graphics.Bitmap
import android.graphics.Color
import com.example.no9studio.model.DownloadableFilter

class LabFilters {
    companion object{
        fun applyFilter(source: Bitmap, filterType: DownloadableFilter): Bitmap {
            val width = source.width
            val height = source.height
            val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            val pixels = IntArray(width * height)
            source.getPixels(pixels, 0, width, 0, 0, width, height)

            for (i in pixels.indices) {
                val pixel = pixels[i]

                val red = (Color.red(pixel) * filterType.redMultiplier).toInt().coerceIn(0, 255)
                val green = (Color.green(pixel) * filterType.greenMultiplier).toInt().coerceIn(0, 255)
                val blue = (Color.blue(pixel) * filterType.blueMultiplier).toInt().coerceIn(0, 255)

                pixels[i] = Color.rgb(red, green, blue)
            }

            result.setPixels(pixels, 0, width, 0, 0, width, height)

            return result
        }
    }

}