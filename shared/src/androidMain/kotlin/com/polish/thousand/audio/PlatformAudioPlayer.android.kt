package com.polish.thousand.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import org.koin.core.context.GlobalContext
import java.io.File

internal actual class PlatformAudioPlayer(
    private val appContext: Context
) {
    private var mediaPlayer: MediaPlayer? = null
    private val tempDir: File = File(appContext.cacheDir, "word-audio").apply { mkdirs() }
    private val tempFileBase = File(tempDir, "current-word-audio")

    actual fun play(bytes: ByteArray, extension: String) {
        stop()

        runCatching {
            val tempFile = File("${tempFileBase.absolutePath}.$extension")
            tempFile.writeBytes(bytes)
            val preparedPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setDataSource(tempFile.absolutePath)
                setOnCompletionListener { completedPlayer ->
                    if (mediaPlayer === completedPlayer) {
                        stop()
                    } else {
                        completedPlayer.release()
                    }
                }
                prepare()
                start()
            }
            mediaPlayer = preparedPlayer
        }.onFailure {
            stop()
        }
    }

    actual fun stop() {
        mediaPlayer?.runCatching {
            setOnCompletionListener(null)
            if (isPlaying) stop()
            reset()
        }
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

internal actual fun providePlatformAudioPlayer(): PlatformAudioPlayer {
    val appContext = GlobalContext.get().get<Context>()
    return PlatformAudioPlayer(appContext)
}
