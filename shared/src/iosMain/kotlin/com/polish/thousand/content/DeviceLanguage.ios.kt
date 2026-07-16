package com.polish.thousand.content

import platform.Foundation.NSUserDefaults

internal actual fun currentDeviceLanguageCode(): String =
    NSUserDefaults.standardUserDefaults
        .stringArrayForKey("AppleLanguages")
        ?.firstOrNull()
        ?.toString()
        .orEmpty()
