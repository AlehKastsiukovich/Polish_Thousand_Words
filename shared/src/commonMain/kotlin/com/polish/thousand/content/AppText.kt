package com.polish.thousand.content

internal data class AppText(
    val back: String,
    val settings: String,
    val lessonsLabel: String,
    val wordsLearnedLabel: String,
    val languageSelectionBadge: String,
    val languageSelectionTitle: String,
    val languageSelectionDescription: String,
    val languageSelectionContinuePrefix: String,
    val languageSelectionFooter: String,
    val startLearning: String,
    val continueLearning: String,
    val settingsTitle: String,
    val settingsOverline: String,
    val supportLanguageTitle: String,
    val supportLanguageDescription: String,
    val currentPlanTitle: String,
    val premiumUnlocked: String,
    val freeStarterPlan: String,
    val premiumDescription: String,
    val freePlanDescription: String,
    val saveSettings: String,
    val keepMomentum: String,
    val later: String,
    val paywallDescription: String,
    val paywallBenefitOne: String,
    val paywallBenefitTwo: String,
    val paywallBenefitThree: String,
    val paywallSoftNote: String,
    val unlockFullCourse: String,
    val continueFree: String,
    val paywallFooter: String,
    val learnPhasePrefix: String,
    val practicePhasePrefix: String,
    val startPractice: String,
    val nextPhrase: String,
    val checkAnswer: String,
    val finishLesson: String,
    val nextQuestion: String,
    val chooseCorrectTranslation: String
)

internal val SupportLanguage.appText: AppText
    get() = when (this) {
        SupportLanguage.Ukrainian -> ukrainianAppText
        SupportLanguage.Russian -> russianAppText
    }

internal fun SupportLanguage.paywallTitle(completedLessons: Int): String = when (this) {
    SupportLanguage.Ukrainian ->
        "Ви вже завершили $completedLessons уроки. Цього достатньо, щоб зрозуміти, чи це вам допомагає."
    SupportLanguage.Russian ->
        "Вы уже завершили $completedLessons урока. Этого достаточно, чтобы понять, помогает ли вам приложение."
}

internal fun TopicContent.titleFor(language: SupportLanguage): String = when (id) {
    "b1_core" -> if (language == SupportLanguage.Ukrainian) "Polish 1000" else "Polish 1000"
    else -> title
}

internal fun TopicContent.descriptionFor(language: SupportLanguage): String = when (id) {
    "b1_core" -> if (language == SupportLanguage.Ukrainian) {
        "Найуживаніші польські слова й фрази для впевненого спілкування."
    } else {
        "Самые употребительные польские слова и фразы для уверенного общения."
    }
    else -> description
}

internal fun LessonContent.titleFor(language: SupportLanguage): String = when (id) {
    "core_connectors" -> if (language == SupportLanguage.Ukrainian) "Зв'язуйте думки" else "Связывайте мысли"
    "core_verbs" -> if (language == SupportLanguage.Ukrainian) "Головні дієслова" else "Главные глаголы"
    "core_actions" -> if (language == SupportLanguage.Ukrainian) "Вирішуйте справи" else "Решайте дела"
    else -> numericLessonTitle(language)
}

private fun LessonContent.numericLessonTitle(language: SupportLanguage): String {
    val start = items.firstOrNull()?.id?.substringBefore('_')?.toIntOrNull()
    val end = items.lastOrNull()?.id?.substringBefore('_')?.toIntOrNull()
    return if (start != null && end != null) {
        when (language) {
            SupportLanguage.Ukrainian -> "Слова $start-$end"
            SupportLanguage.Russian -> "Слова $start-$end"
        }
    } else {
        title
    }
}

internal fun LessonContent.descriptionFor(language: SupportLanguage): String = when (id) {
    "core_connectors" -> if (language == SupportLanguage.Ukrainian) {
        "Десять слів, з якими польська звучить чіткіше й природніше."
    } else {
        "Десять слов, с которыми польская речь звучит яснее и естественнее."
    }
    "core_verbs" -> if (language == SupportLanguage.Ukrainian) {
        "Дії, які постійно зустрічаються в розмовах, повідомленнях і сервісах."
    } else {
        "Действия, которые постоянно встречаются в разговорах, сообщениях и сервисах."
    }
    "core_actions" -> if (language == SupportLanguage.Ukrainian) {
        "Корисні дії для роботи, документів, оплат і щоденних справ."
    } else {
        "Полезные действия для работы, документов, оплат и повседневных дел."
    }
    else -> numericLessonDescription(language)
}

private fun LessonContent.numericLessonDescription(language: SupportLanguage): String =
    when (language) {
        SupportLanguage.Ukrainian -> "10 нових слів без зайвого шуму."
        SupportLanguage.Russian -> "10 новых слов без лишнего шума."
    }

private val ukrainianAppText = AppText(
    back = "Назад",
    settings = "Налаштування",
    lessonsLabel = "Уроки",
    wordsLearnedLabel = "Слова",
    languageSelectionBadge = "Оберіть мову підтримки",
    languageSelectionTitle = "Почніть тією мовою, яка для вас природна.",
    languageSelectionDescription = "Ви вивчатимете польську, але пояснення й переклади мають бути зрозумілими з першого екрана.",
    languageSelectionContinuePrefix = "Продовжити",
    languageSelectionFooter = "Мову можна змінити пізніше в налаштуваннях.",
    startLearning = "Почати навчання",
    continueLearning = "Продовжити навчання",
    settingsTitle = "Ваші параметри навчання",
    settingsOverline = "Налаштування",
    supportLanguageTitle = "Мова підтримки",
    supportLanguageDescription = "Одна мова підтримки робить уроки спокійнішими й читабельнішими.",
    currentPlanTitle = "Поточний план",
    premiumUnlocked = "Преміум відкрито",
    freeStarterPlan = "Безкоштовний старт",
    premiumDescription = "Ви вже відкрили повніший шлях. Рухайтеся далі у своєму темпі.",
    freePlanDescription = "Перші 100 слів безкоштовні. Далі можна один раз відкрити повний шлях до 1 000.",
    saveSettings = "Зберегти",
    keepMomentum = "Не втрачайте темп",
    later = "Пізніше",
    paywallDescription = "Відкрийте весь послідовний шлях від 100 до 1 000 найкорисніших польських слів і фраз.",
    paywallBenefitOne = "Повний шлях до 1 000 слів",
    paywallBenefitTwo = "Відмітки 250, 500, 750 і 1 000",
    paywallBenefitThree = "Одна зрозуміла послідовність без зайвих розділів",
    paywallSoftNote = "Без тиску. Можна й далі користуватися безкоштовною версією й вирішити пізніше.",
    unlockFullCourse = "Відкрити повний курс",
    continueFree = "Продовжити безкоштовно",
    paywallFooter = "Не обов'язково вирішувати зараз. Якщо хочете, просто продовжуйте навчання.",
    learnPhasePrefix = "Вивчення",
    practicePhasePrefix = "Практика",
    startPractice = "Почати практику",
    nextPhrase = "Далі",
    checkAnswer = "Перевірити",
    finishLesson = "Завершити урок",
    nextQuestion = "Наступне питання",
    chooseCorrectTranslation = "Оберіть правильний переклад"
)

private val russianAppText = AppText(
    back = "Назад",
    settings = "Настройки",
    lessonsLabel = "Уроки",
    wordsLearnedLabel = "Слова",
    languageSelectionBadge = "Выберите язык поддержки",
    languageSelectionTitle = "Начните на языке, который для вас естественен.",
    languageSelectionDescription = "Вы будете учить польский, но объяснения и переводы должны быть понятными с первого экрана.",
    languageSelectionContinuePrefix = "Продолжить",
    languageSelectionFooter = "Язык можно будет изменить позже в настройках.",
    startLearning = "Начать обучение",
    continueLearning = "Продолжить обучение",
    settingsTitle = "Ваши параметры обучения",
    settingsOverline = "Настройки",
    supportLanguageTitle = "Язык поддержки",
    supportLanguageDescription = "Один язык поддержки делает уроки спокойнее и проще для чтения.",
    currentPlanTitle = "Текущий план",
    premiumUnlocked = "Премиум открыт",
    freeStarterPlan = "Бесплатный старт",
    premiumDescription = "Вы уже открыли более полный путь. Продолжайте в своем темпе.",
    freePlanDescription = "Первые 100 слов бесплатны. Затем можно один раз открыть полный путь до 1 000.",
    saveSettings = "Сохранить",
    keepMomentum = "Не теряйте темп",
    later = "Позже",
    paywallDescription = "Откройте весь последовательный путь от 100 до 1 000 самых полезных польских слов и фраз.",
    paywallBenefitOne = "Полный путь до 1 000 слов",
    paywallBenefitTwo = "Отметки 250, 500, 750 и 1 000",
    paywallBenefitThree = "Одна понятная последовательность без лишних разделов",
    paywallSoftNote = "Без давления. Можно продолжить бесплатную версию и решить позже.",
    unlockFullCourse = "Открыть полный курс",
    continueFree = "Продолжить бесплатно",
    paywallFooter = "Не обязательно решать сейчас. Если хочешь, просто продолжай обучение.",
    learnPhasePrefix = "Изучение",
    practicePhasePrefix = "Практика",
    startPractice = "Начать практику",
    nextPhrase = "Дальше",
    checkAnswer = "Проверить",
    finishLesson = "Завершить урок",
    nextQuestion = "Следующий вопрос",
    chooseCorrectTranslation = "Выберите правильный перевод"
)
