package com.example.no9studio.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.no9studio.model.DownloadableFilter

@Database(entities = [DownloadableFilter::class], version = 1, exportSchema = false)
abstract class LabsDatabase: RoomDatabase() {

    abstract fun getDao() : LabsDao

    companion object{
        @Volatile
        private var INSTANCE : LabsDatabase? = null

        fun getDatabase(context: Context) : LabsDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LabsDatabase::class.java,
                    "labsDatabase"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }
    }
}