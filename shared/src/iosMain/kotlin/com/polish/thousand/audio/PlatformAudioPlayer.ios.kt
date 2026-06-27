package com.polish.thousand.audio

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.convert
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite

@OptIn(ExperimentalForeignApi::class)
internal actual class PlatformAudioPlayer {
    private var audioPlayer: AVAudioPlayer? = null
    private val tempFilePath = "${NSTemporaryDirectory()}polish-thousand-word-audio.wav"

    actual fun play(bytes: ByteArray) {
        stop()

        runCatching {
            writeTempFile(tempFilePath, bytes)
            val preparedPlayer = AVAudioPlayer(
                contentsOfURL = NSURL.fileURLWithPath(tempFilePath),
                error = null
            )
            preparedPlayer.prepareToPlay()
            preparedPlayer.play()
            preparedPlayer
        }.onSuccess { preparedPlayer ->
            audioPlayer = preparedPlayer
        }.onFailure {
            stop()
        }
    }

    actual fun stop() {
        audioPlayer?.stop()
        audioPlayer = null
    }
}

internal actual fun providePlatformAudioPlayer(): PlatformAudioPlayer = PlatformAudioPlayer()

@OptIn(ExperimentalForeignApi::class)
private fun writeTempFile(path: String, bytes: ByteArray) {
    val file = fopen(path, "wb") ?: return
    bytes.usePinned { pinned ->
        fwrite(pinned.addressOf(0), 1.convert(), bytes.size.convert(), file)
    }
    fclose(file)
}
