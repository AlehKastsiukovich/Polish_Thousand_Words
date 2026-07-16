package com.polish.thousand.content

internal enum class BootstrapLanguage {
    English,
    Ukrainian,
    Russian
}

internal data class InitialLanguageChoice(
    val interfaceLanguage: BootstrapLanguage,
    val suggestedSupportLanguage: SupportLanguage?
)

internal fun initialLanguageChoice(): InitialLanguageChoice {
    val languageCode = currentDeviceLanguageCode().lowercase().substringBefore('-').substringBefore('_')
    return when (languageCode) {
        "uk" -> InitialLanguageChoice(
            interfaceLanguage = BootstrapLanguage.Ukrainian,
            suggestedSupportLanguage = SupportLanguage.Ukrainian
        )
        "ru" -> InitialLanguageChoice(
            interfaceLanguage = BootstrapLanguage.Russian,
            suggestedSupportLanguage = SupportLanguage.Russian
        )
        else -> InitialLanguageChoice(
            interfaceLanguage = BootstrapLanguage.English,
            suggestedSupportLanguage = null
        )
    }
}

internal expect fun currentDeviceLanguageCode(): String
