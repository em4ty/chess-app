package com.example.kp

import android.app.Application

class ChessApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: ChessApp
            private set
    }
}
