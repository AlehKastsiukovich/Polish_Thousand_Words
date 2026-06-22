package com.polish.thousand.content

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.time

@OptIn(ExperimentalForeignApi::class)
internal actual fun currentEpochDay(): Long = time(null) / SecondsPerDay

private const val SecondsPerDay = 86_400L
