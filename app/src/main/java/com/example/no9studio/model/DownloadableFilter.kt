package com.example.no9studio.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "LabFilters")
data class DownloadableFilter(
    val filterName : String = "",
    val redMultiplier: Float = 0f,
    val greenMultiplier: Float = 0f,
    val blueMultiplier: Float = 0f,
    @PrimaryKey
    val dateSaved : Long = System.currentTimeMillis()
)
