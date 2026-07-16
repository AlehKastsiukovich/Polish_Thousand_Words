package com.polish.thousand.content

import java.util.Locale

internal actual fun currentDeviceLanguageCode(): String = Locale.getDefault().language
