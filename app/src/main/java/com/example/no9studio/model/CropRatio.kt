package com.example.no9studio.model

data class CropRatio(
    val name : String,
    val AspectX : Int?,
    val AspectY: Int?
){
    companion object {
        private val ratioMap = mutableMapOf<String, CropRatio>()

        fun createCropRatio(name: String, aspectX: Int?, aspectY: Int?): CropRatio {
            val cropRatio = CropRatio(name, aspectX, aspectY)
            ratioMap[name] = cropRatio
            return cropRatio
        }

        // Get method to retrieve instances by name
        fun getCropRatioByName(name: String): CropRatio? {
            return ratioMap[name]
        }
    }
}

