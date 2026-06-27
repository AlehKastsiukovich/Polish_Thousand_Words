package com.polish.thousand.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

internal actual class PlatformAudioPlayer {
    private var audioTrack: AudioTrack? = null

    actual fun play(bytes: ByteArray) {
        stop()

        runCatching {
            val wav = parseWav(bytes) ?: return
            val minBufferSize = AudioTrack.getMinBufferSize(
                wav.sampleRateHz,
                wav.channelMask,
                wav.encoding
            )
            val bufferSize = maxOf(minBufferSize, wav.pcmData.size)
            val preparedTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(wav.sampleRateHz)
                        .setEncoding(wav.encoding)
                        .setChannelMask(wav.channelMask)
                        .build()
                )
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(bufferSize)
                .build()

            preparedTrack.write(wav.pcmData, 0, wav.pcmData.size)
            preparedTrack.setNotificationMarkerPosition(wav.frameCount)
            preparedTrack.setPlaybackPositionUpdateListener(
                object : AudioTrack.OnPlaybackPositionUpdateListener {
                    override fun onMarkerReached(track: AudioTrack) {
                        if (audioTrack === track) {
                            stop()
                        } else {
                            track.release()
                        }
                    }

                    override fun onPeriodicNotification(track: AudioTrack) = Unit
                }
            )
            preparedTrack.play()
            audioTrack = preparedTrack
        }.onFailure {
            stop()
        }
    }

    actual fun stop() {
        audioTrack?.runCatching {
            pause()
            flush()
            stop()
        }
        audioTrack?.release()
        audioTrack = null
    }
}

internal actual fun providePlatformAudioPlayer(): PlatformAudioPlayer = PlatformAudioPlayer()

private data class ParsedWav(
    val sampleRateHz: Int,
    val channelMask: Int,
    val encoding: Int,
    val pcmData: ByteArray,
    val frameCount: Int
)

private fun parseWav(bytes: ByteArray): ParsedWav? {
    if (bytes.size < 44) return null
    if (bytes.readAscii(0, 4) != "RIFF" || bytes.readAscii(8, 4) != "WAVE") return null

    val channels = bytes.readLeShort(22)
    val sampleRate = bytes.readLeInt(24)
    val bitsPerSample = bytes.readLeShort(34)
    val dataSize = bytes.readLeInt(40)
    val dataOffset = 44
    val dataEnd = dataOffset + dataSize

    if (sampleRate <= 0 || channels !in 1..2) return null
    if (bitsPerSample != 16) return null
    if (dataEnd > bytes.size) return null

    val pcmData = bytes.copyOfRange(dataOffset, dataEnd)
    val channelMask = when (channels) {
        1 -> AudioFormat.CHANNEL_OUT_MONO
        2 -> AudioFormat.CHANNEL_OUT_STEREO
        else -> return null
    }
    val bytesPerFrame = channels * (bitsPerSample / 8)
    val frameCount = pcmData.size / bytesPerFrame

    return ParsedWav(
        sampleRateHz = sampleRate,
        channelMask = channelMask,
        encoding = AudioFormat.ENCODING_PCM_16BIT,
        pcmData = pcmData,
        frameCount = frameCount
    )
}

private fun ByteArray.readAscii(offset: Int, length: Int): String =
    decodeToString(startIndex = offset, endIndex = offset + length)

private fun ByteArray.readLeInt(offset: Int): Int =
    (this[offset].toInt() and 0xFF) or
        ((this[offset + 1].toInt() and 0xFF) shl 8) or
        ((this[offset + 2].toInt() and 0xFF) shl 16) or
        ((this[offset + 3].toInt() and 0xFF) shl 24)

private fun ByteArray.readLeShort(offset: Int): Int =
    (this[offset].toInt() and 0xFF) or
        ((this[offset + 1].toInt() and 0xFF) shl 8)
