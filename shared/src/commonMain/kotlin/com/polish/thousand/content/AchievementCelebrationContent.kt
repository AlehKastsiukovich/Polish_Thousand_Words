package com.polish.thousand.content

internal data class AchievementCelebrationContent(
    val eyebrow: String,
    val headline: String,
    val message: String
)

internal fun LearningMilestone.celebrationContentFor(
    language: SupportLanguage
): AchievementCelebrationContent = when (wordCount) {
    100 -> if (language == SupportLanguage.Ukrainian) {
        AchievementCelebrationContent(
            eyebrow = "Перша велика відмітка",
            headline = "Перші 100 слів уже ваші.",
            message = "Сильний старт. Польська вже звучить ближче."
        )
    } else {
        AchievementCelebrationContent(
            eyebrow = "Первая большая отметка",
            headline = "Первые 100 слов уже ваши.",
            message = "Сильный старт. Польский уже звучит ближе."
        )
    }

    250 -> if (language == SupportLanguage.Ukrainian) {
        AchievementCelebrationContent(
            eyebrow = "Опора вже є",
            headline = "250 слів. Ви вже в мові.",
            message = "База зібрана. Далі буде легше."
        )
    } else {
        AchievementCelebrationContent(
            eyebrow = "Опора уже есть",
            headline = "250 слов. Вы уже в языке.",
            message = "База собрана. Дальше будет легче."
        )
    }

    500 -> if (language == SupportLanguage.Ukrainian) {
        AchievementCelebrationContent(
            eyebrow = "Половина шляху",
            headline = "500 слів — уже впевнена база.",
            message = "Продовжуйте у своєму ритмі — наступний крок уже чекає."
        )
    } else {
        AchievementCelebrationContent(
            eyebrow = "Половина пути",
            headline = "500 слов — уже уверенная база.",
            message = "Продолжайте в своем ритме — следующий шаг уже ждёт."
        )
    }

    750 -> if (language == SupportLanguage.Ukrainian) {
        AchievementCelebrationContent(
            eyebrow = "Фініш уже близько",
            headline = "750 слів. Дуже сильний хід.",
            message = "Лишився останній відрізок."
        )
    } else {
        AchievementCelebrationContent(
            eyebrow = "Финиш уже близко",
            headline = "750 слов. Очень сильный ход.",
            message = "Остался последний отрезок."
        )
    }

    1000 -> if (language == SupportLanguage.Ukrainian) {
        AchievementCelebrationContent(
            eyebrow = "Режим закріплення",
            headline = "Тисяча в дії.",
            message = "Усі слова пройдено."
        )
    } else {
        AchievementCelebrationContent(
            eyebrow = "Режим закрепления",
            headline = "Тысяча в деле.",
            message = "Все слова пройдены."
        )
    }

    else -> if (language == SupportLanguage.Ukrainian) {
        AchievementCelebrationContent(
            eyebrow = "Нова відмітка",
            headline = "${wordCount} слів уже з вами.",
            message = "Рухайтесь далі у своєму темпі."
        )
    } else {
        AchievementCelebrationContent(
            eyebrow = "Новая отметка",
            headline = "$wordCount слов уже с вами.",
            message = "Продолжайте в своем темпе."
        )
    }
}
