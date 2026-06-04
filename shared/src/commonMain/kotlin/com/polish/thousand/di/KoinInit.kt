package com.polish.thousand.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

fun initKoin(
    appDeclaration: KoinApplication.() -> Unit = {},
    extraModules: List<Module> = emptyList()
) {
    startKoin {
        appDeclaration(this)
        modules(appModule)
        modules(extraModules)
    }
}
