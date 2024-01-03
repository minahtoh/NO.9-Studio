package com.example.no9studio.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.no9studio.model.DownloadableFilter
import kotlinx.coroutines.flow.Flow

@Dao
interface LabsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFilter(filter : DownloadableFilter)

    @Query("Select * From LabFilters Order by dateSaved Asc")
    fun getDownloadedFilters() : Flow<List<DownloadableFilter>>

    @Query("Select * From LabFilters Where filterName = :filterName LIMIT 1")
    fun getFilter(filterName : String): DownloadableFilter
}