package com.example.no9studio.viewmodel

import com.example.no9studio.db.LabsDatabase
import com.example.no9studio.model.DownloadableFilter

class LabsRepository(private val database: LabsDatabase) {
    fun getFiltersList() = database.getDao().getDownloadedFilters()

    suspend fun saveFilter(filter:DownloadableFilter) = database.getDao().saveFilter(filter)

    fun getFilter(filterName: String) = database.getDao().getFilter(filterName)
}