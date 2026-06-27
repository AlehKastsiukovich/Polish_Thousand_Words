package com.polish.thousand.di

import com.polish.thousand.AppViewModel
import com.polish.thousand.audio.AppAudioPlayer
import com.polish.thousand.audio.AppAudioPlayerImpl
import com.polish.thousand.audio.providePlatformAudioPlayer
import com.polish.thousand.core.mvi.AppDispatchers
import com.polish.thousand.core.mvi.provideAppDispatchers
import com.polish.thousand.ui.LessonViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<AppDispatchers> { provideAppDispatchers() }
    single { providePlatformAudioPlayer() }
    single<AppAudioPlayer> { AppAudioPlayerImpl(get(), get()) }
    viewModel { parameters -> AppViewModel(parameters.get(), get()) }
    viewModel { parameters -> LessonViewModel(parameters.get(), get()) }
}
