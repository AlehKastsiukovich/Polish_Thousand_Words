package com.polish.thousand.audio

import com.polish.thousand.content.LessonItemContent
import com.polish.thousand.core.mvi.AppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import polishthousand.shared.generated.resources.Res

internal interface AppAudioPlayer {
    fun playWord(item: LessonItemContent)
    fun playExample(item: LessonItemContent, exampleIndex: Int)
    fun stop()
}

internal class AppAudioPlayerImpl(
    dispatchers: AppDispatchers,
    private val platformAudioPlayer: PlatformAudioPlayer
) : AppAudioPlayer {

    private val scope = CoroutineScope(SupervisorJob() + dispatchers.io)

    override fun playWord(item: LessonItemContent) {
        scope.launch {
            loadWordAudio(item)?.let(platformAudioPlayer::play)
        }
    }

    override fun playExample(item: LessonItemContent, exampleIndex: Int) = Unit

    override fun stop() {
        platformAudioPlayer.stop()
    }
}

private suspend fun loadWordAudio(item: LessonItemContent): ByteArray? = runCatching {
    Res.readBytes("files/audio/words/${item.id.replace('_', '-')}.wav")
}.getOrNull()

internal expect class PlatformAudioPlayer {
    fun play(bytes: ByteArray)
    fun stop()
}

internal expect fun providePlatformAudioPlayer(): PlatformAudioPlayer
