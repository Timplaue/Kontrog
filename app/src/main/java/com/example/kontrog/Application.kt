package com.example.kontrog

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey("e9ce40c1-7892-45b3-b25a-1fb074736216")

        MapKitFactory.initialize(this)
    }
}