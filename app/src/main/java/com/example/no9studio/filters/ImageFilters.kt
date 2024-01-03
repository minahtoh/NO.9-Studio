package com.example.no9studio.filters

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking

class ImageFilters {
    companion object {
        private fun applyFilter(bitmap: Bitmap, filter: (Int, Int, Int) -> Int): Bitmap {
            val width = bitmap.width
            val height = bitmap.height

            val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    val pixel = bitmap.getPixel(x, y)
                    val red = Color.red(pixel)
                    val green = Color.green(pixel)
                    val blue = Color.blue(pixel)

                    val filteredColor = filter(red, green, blue)

                    resultBitmap.setPixel(x, y, Color.rgb(
                        Color.red(filteredColor),
                        Color.green(filteredColor),
                        Color.blue(filteredColor)
                    ))
                }
            }

            return resultBitmap
        }

        private fun applyFilter2(bitmap: Bitmap, filter: (Int, Int, Int) -> Int): Bitmap = runBlocking {
            val width = bitmap.width
            val height = bitmap.height

            val deferredList = (0 until width).map { x ->
                async(Dispatchers.Default) {
                    (0 until height).map { y ->
                        val pixel = bitmap.getPixel(x, y)
                        val red = Color.red(pixel)
                        val green = Color.green(pixel)
                        val blue = Color.blue(pixel)

                        val filteredColor = filter(red, green, blue)

                        Color.rgb(
                            Color.red(filteredColor),
                            Color.green(filteredColor),
                            Color.blue(filteredColor)
                        )
                    }
                }
            }

            val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            deferredList.awaitAll().forEachIndexed { x, column ->
                column.forEachIndexed { y, color ->
                    resultBitmap.setPixel(x, y, color)
                }
            }

            resultBitmap
        }


        // Add more filter methods as needed
        fun applyGrayscale(bitmap: Bitmap): Bitmap {
            return applyFilter(bitmap) { red, green, blue ->
                val grayscale = (red + green + blue) / 3
                Color.rgb(grayscale, grayscale, grayscale)
            }
        }

        fun applyInvert(bitmap: Bitmap): Bitmap {
            return applyFilter(bitmap) { red, green, blue ->
                Color.rgb(255 - red, 255 - green, 255 - blue)
            }
        }

        fun applySepia(bitmap: Bitmap): Bitmap {
            return applyFilter2(bitmap) { red, green, blue ->
                val sepiaRed = (red * 0.393 + green * 0.769 + blue * 0.189).coerceIn(0.0, 255.0).toInt()
                val sepiaGreen = (red * 0.349 + green * 0.686 + blue * 0.168).coerceIn(0.0, 255.0).toInt()
                val sepiaBlue = (red * 0.272 + green * 0.534 + blue * 0.131).coerceIn(0.0, 255.0).toInt()
                Color.rgb(sepiaRed, sepiaGreen, sepiaBlue)
            }
        }

        fun applyVintage(bitmap: Bitmap): Bitmap {
            return applyFilter(bitmap) { red, green, blue ->
                val vintageRed = (0.393 * red + 0.769 * green + 0.189 * blue).coerceIn(0.0, 255.0).toInt()
                val vintageGreen = (0.349 * red + 0.686 * green + 0.168 * blue).coerceIn(0.0, 255.0).toInt()
                val vintageBlue = (0.272 * red + 0.534 * green + 0.131 * blue).coerceIn(0.0, 255.0).toInt()
                Color.rgb(vintageRed, vintageGreen, vintageBlue)
            }
        }

        fun applyThreshold(bitmap: Bitmap, threshold: Int = 128): Bitmap {
            return applyFilter(bitmap) { red, green, blue ->
                val intensity = (red + green + blue) / 3
                if (intensity > threshold) Color.WHITE else Color.BLACK
            }
        }

        fun applyHighContrast(bitmap: Bitmap): Bitmap {
            return applyFilter(bitmap) { red, green, blue ->
                val contrastFactor = 1.5 // Adjust as needed for the desired contrast level
                val adjustedRed = ((red - 128) * contrastFactor + 128).coerceIn(0.0, 255.0).toInt()
                val adjustedGreen = ((green - 128) * contrastFactor + 128).coerceIn(0.0, 255.0).toInt()
                val adjustedBlue = ((blue - 128) * contrastFactor + 128).coerceIn(0.0, 255.0).toInt()
                Color.rgb(adjustedRed, adjustedGreen, adjustedBlue)
            }
        }

        fun applyHighSaturation(bitmap: Bitmap): Bitmap {
            return applyFilter(bitmap) { red, green, blue ->
                val saturationFactor = 1.5 // Adjust as needed for the desired saturation level
                val adjustedRed = ((red - 128) * saturationFactor + 128).coerceIn(0.0, 255.0).toInt()
                val adjustedGreen = ((green - 128) * saturationFactor + 128).coerceIn(0.0, 255.0).toInt()
                val adjustedBlue = ((blue - 128) * saturationFactor + 128).coerceIn(0.0, 255.0).toInt()
                Color.rgb(adjustedRed, adjustedGreen, adjustedBlue)
            }
        }

        fun applyBlackAndWhiteWithGrain(bitmap: Bitmap, grainIntensity: Int = 25): Bitmap {
            return applyFilter(bitmap) { red, green, blue ->
                val intensity = (red + green + blue) / 3
                val grain = (Math.random() * grainIntensity).toInt()
                val grayscale = (intensity + grain).coerceIn(0, 255)
                Color.rgb(grayscale, grayscale, grayscale)
            }
        }
    }
}
