package com.polish.thousand

import android.app.Application
import com.polish.thousand.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class PolishThousandApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(
            appDeclaration = {
                androidLogger()
                androidContext(this@PolishThousandApplication)
            }
        )
    }
}
