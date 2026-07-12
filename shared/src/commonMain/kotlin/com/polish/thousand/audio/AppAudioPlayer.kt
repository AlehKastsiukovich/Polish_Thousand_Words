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

private const val WordAudioExtension = "m4a"

internal class AppAudioPlayerImpl(
    dispatchers: AppDispatchers,
    private val platformAudioPlayer: PlatformAudioPlayer
) : AppAudioPlayer {

    private val scope = CoroutineScope(SupervisorJob() + dispatchers.io)

    override fun playWord(item: LessonItemContent) {
        scope.launch {
            loadWordAudio(item)?.let { bytes ->
                platformAudioPlayer.play(bytes, WordAudioExtension)
            }
        }
    }

    override fun playExample(item: LessonItemContent, exampleIndex: Int) = Unit

    override fun stop() {
        platformAudioPlayer.stop()
    }
}

private suspend fun loadWordAudio(item: LessonItemContent): ByteArray? = runCatching {
    Res.readBytes("files/audio/words/${item.id.replace('_', '-')}.${WordAudioExtension}")
}.getOrNull()

internal expect class PlatformAudioPlayer {
    fun play(bytes: ByteArray, extension: String)
    fun stop()
}

internal expect fun providePlatformAudioPlayer(): PlatformAudioPlayer
