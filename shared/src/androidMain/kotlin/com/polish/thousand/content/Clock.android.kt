package com.polish.thousand.content

internal actual fun currentEpochDay(): Long = System.currentTimeMillis() / MillisPerDay

private const val MillisPerDay = 86_400_000L
