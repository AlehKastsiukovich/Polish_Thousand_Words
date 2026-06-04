package com.polish.thousand.di

import com.polish.thousand.core.mvi.AppDispatchers
import com.polish.thousand.core.mvi.provideAppDispatchers
import org.koin.dsl.module

val appModule = module {
    single<AppDispatchers> { provideAppDispatchers() }
}
