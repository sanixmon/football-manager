package com.footballmanager.app

import android.app.Application
import com.footballmanager.app.di.AppContainer
import com.footballmanager.app.di.DefaultAppContainer
import java.io.File

class FootballManagerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val saveFile = File(filesDir, "current_save.json")
        container = DefaultAppContainer(saveFile = saveFile)
    }
}
