package com.polish.thousand.content

internal enum class SupportLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String
) {
    Ukrainian(
        code = "UA",
        nativeName = "Українська",
        englishName = "Ukrainian"
    ),
    Russian(
        code = "RU",
        nativeName = "Русский",
        englishName = "Russian"
    )
}

internal val LessonItemContent.translationForSelectedLanguage: (SupportLanguage) -> String
    get() = { language ->
        when (language) {
            SupportLanguage.Ukrainian -> ukrainian
            SupportLanguage.Russian -> russian
        }
    }

internal val LessonItemContent.exampleForSelectedLanguage: (SupportLanguage) -> String
    get() = { language ->
        when (language) {
            SupportLanguage.Ukrainian -> exampleUkrainian
            SupportLanguage.Russian -> exampleRussian
        }
    }
