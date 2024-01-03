package com.example.no9studio

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.DelegatingWorkerFactory
import com.example.no9studio.db.LabsDatabase
import com.example.no9studio.viewmodel.LabsRepository
import com.example.no9studio.viewmodel.LabsWorkerFactory

class No9StudioApp : Application(), Configuration.Provider {

    override fun getWorkManagerConfiguration(): Configuration {
        val studioWorkerFactory = DelegatingWorkerFactory()
        studioWorkerFactory.addFactory(LabsWorkerFactory(LabsRepository(LabsDatabase.getDatabase(this))))

        return Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setWorkerFactory(studioWorkerFactory)
            .build()
    }
}