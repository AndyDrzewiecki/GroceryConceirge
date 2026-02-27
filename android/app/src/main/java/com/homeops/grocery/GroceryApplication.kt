package com.homeops.grocery

import android.app.Application

class GroceryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: GroceryApplication
            private set
    }
}
