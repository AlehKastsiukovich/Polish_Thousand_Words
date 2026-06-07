package com.polish.thousand.content

internal data class AppText(
    val back: String,
    val settings: String,
    val premium: String,
    val freePlan: String,
    val lessonsLabel: String,
    val topicsDoneLabel: String,
    val languageSelectionBadge: String,
    val languageSelectionTitle: String,
    val languageSelectionDescription: String,
    val languageSelectionContinuePrefix: String,
    val languageSelectionFooter: String,
    val welcomeTopPill: String,
    val welcomeBadge: String,
    val welcomeTitle: String,
    val welcomeDescription: String,
    val startLearning: String,
    val exploreTopics: String,
    val welcomeSupportiveLine: String,
    val homeGreeting: String,
    val homeTitle: String,
    val homeDescription: String,
    val homePremiumTitle: String,
    val homePremiumDescription: String,
    val shortSupportiveLine: String,
    val startFirstLesson: String,
    val continueLearning: String,
    val continueWith: String,
    val topicSelectionTitle: String,
    val topicSelectionDescription: String,
    val topicSelectionHeaderTitle: String,
    val topicSelectionSupportiveLine: String,
    val lessonReadyTitle: String,
    val lessonMinutesSuffix: String,
    val lessonPhrasesSuffix: String,
    val whatYouWillDo: String,
    val lessonBulletOnePrefix: String,
    val lessonBulletTwo: String,
    val lessonBulletThree: String,
    val firstPhrasesInLesson: String,
    val startLesson: String,
    val lessonCompleteTitle: String,
    val completionSupportiveLine: String,
    val continueTopic: String,
    val backToTopics: String,
    val seeAllTopics: String,
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
    val chooseCorrectTranslation: String,
    val lessonPractice: String,
    val correct: String,
    val notQuite: String,
    val correctFeedback: String
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
    "basic_phrases" -> if (language == SupportLanguage.Ukrainian) "Базові фрази" else "Базовые фразы"
    "shop" -> if (language == SupportLanguage.Ukrainian) "Магазин" else "Магазин"
    "transport" -> if (language == SupportLanguage.Ukrainian) "Транспорт" else "Транспорт"
    else -> title
}

internal fun TopicContent.descriptionFor(language: SupportLanguage): String = when (id) {
    "basic_phrases" -> if (language == SupportLanguage.Ukrainian) {
        "Короткі фрази для ввічливого щоденного спілкування."
    } else {
        "Короткие фразы для вежливого ежедневного общения."
    }
    "shop" -> if (language == SupportLanguage.Ukrainian) {
        "Слова й фрази для покупок і каси."
    } else {
        "Слова и фразы для покупок и кассы."
    }
    "transport" -> if (language == SupportLanguage.Ukrainian) {
        "Корисна польська для автобусів, трамваїв, вокзалів і напрямків."
    } else {
        "Полезный польский для автобусов, трамваев, вокзалов и направлений."
    }
    else -> description
}

internal fun LessonContent.titleFor(language: SupportLanguage): String = when (id) {
    "basic_polite_essentials" -> if (language == SupportLanguage.Ukrainian) "Ввічливі основи" else "Вежливые основы"
    "basic_help_and_understanding" -> if (language == SupportLanguage.Ukrainian) "Допомога і розуміння" else "Помощь и понимание"
    "shop_buying_groceries" -> if (language == SupportLanguage.Ukrainian) "Покупка продуктів" else "Покупка продуктов"
    "shop_at_the_checkout" -> if (language == SupportLanguage.Ukrainian) "На касі" else "На кассе"
    "transport_getting_around" -> if (language == SupportLanguage.Ukrainian) "Пересування містом" else "Передвижение по городу"
    "transport_directions_and_timing" -> if (language == SupportLanguage.Ukrainian) "Напрямки і час" else "Направления и время"
    else -> title
}

internal fun LessonContent.descriptionFor(language: SupportLanguage): String = when (id) {
    "basic_polite_essentials" -> if (language == SupportLanguage.Ukrainian) {
        "Перші фрази, які потрібні майже всюди."
    } else {
        "Первые фразы, которые нужны почти везде."
    }
    "basic_help_and_understanding" -> if (language == SupportLanguage.Ukrainian) {
        "Корисно, коли потрібна допомога або уточнення."
    } else {
        "Полезно, когда нужна помощь или уточнение."
    }
    "shop_buying_groceries" -> if (language == SupportLanguage.Ukrainian) {
        "Базові слова для простих походів у магазин."
    } else {
        "Базовые слова для простых походов в магазин."
    }
    "shop_at_the_checkout" -> if (language == SupportLanguage.Ukrainian) {
        "Короткі фрази для оплати та завершення покупки."
    } else {
        "Короткие фразы для оплаты и завершения покупки."
    }
    "transport_getting_around" -> if (language == SupportLanguage.Ukrainian) {
        "Слова для пересування містом."
    } else {
        "Слова для передвижения по городу."
    }
    "transport_directions_and_timing" -> if (language == SupportLanguage.Ukrainian) {
        "Запитання, які допомагають не загубитися."
    } else {
        "Вопросы, которые помогают не потеряться."
    }
    else -> description
}

private val ukrainianAppText = AppText(
    back = "Назад",
    settings = "Налаштування",
    premium = "Преміум",
    freePlan = "Безкоштовно",
    lessonsLabel = "Уроки",
    topicsDoneLabel = "Теми",
    languageSelectionBadge = "Оберіть мову підтримки",
    languageSelectionTitle = "Почніть тією мовою, яка для вас природна.",
    languageSelectionDescription = "Ви вивчатимете польську, але пояснення й переклади мають бути зрозумілими з першого екрана.",
    languageSelectionContinuePrefix = "Продовжити",
    languageSelectionFooter = "Мову можна змінити пізніше в налаштуваннях.",
    welcomeTopPill = "Практична польська",
    welcomeBadge = "Реальні слова для реального життя в Польщі",
    welcomeTitle = "Вивчайте польську, яка справді потрібна.",
    welcomeDescription = "Чіткі фрази для магазину, транспорту, лікаря, роботи й щоденних справ. Спокійні короткі уроки, які допомагають стартувати швидше.",
    startLearning = "Почати навчання",
    exploreTopics = "Переглянути теми",
    welcomeSupportiveLine = "Починати життя в новій країні важко. Малих кроків достатньо.",
    homeGreeting = "З поверненням",
    homeTitle = "Продовжуйте практичну польську для реального життя.",
    homeDescription = "Навіть один завершений урок уже полегшує наступну покупку, поїздку чи коротку розмову.",
    homePremiumTitle = "Повний курс можна відкрити пізніше.",
    homePremiumDescription = "Спершу відчуйте користь від безкоштовного старту. Преміум з'явиться лише після кількох уроків.",
    shortSupportiveLine = "Не потрібна довга сесія. Кількох корисних фраз на сьогодні достатньо.",
    startFirstLesson = "Почати перший урок",
    continueLearning = "Продовжити навчання",
    continueWith = "Продовжити з темою",
    topicSelectionTitle = "Оберіть стартову тему",
    topicSelectionDescription = "Почніть із ситуацій, які зустрінете найшвидше. Решту можна відкривати поступово.",
    topicSelectionHeaderTitle = "Ваші перші кроки в польській",
    topicSelectionSupportiveLine = "Не треба вивчати все одразу. Оберіть одну корисну тему й спокійно завершіть один урок.",
    lessonReadyTitle = "Урок готовий",
    lessonMinutesSuffix = "хв",
    lessonPhrasesSuffix = "фраз",
    whatYouWillDo = "Що буде в уроці",
    lessonBulletOnePrefix = "Вивчите корисні польські слова з підтримкою",
    lessonBulletTwo = "Побачите короткий життєвий приклад для кожної фрази",
    lessonBulletThree = "Пройдете 3 прості типи вправ без перевантаження",
    firstPhrasesInLesson = "Перші фрази в цьому уроці",
    startLesson = "Почати урок",
    lessonCompleteTitle = "Урок завершено",
    completionSupportiveLine = "Не потрібна довга сесія. Один завершений урок уже рахується.",
    continueTopic = "Продовжити тему",
    backToTopics = "Назад до тем",
    seeAllTopics = "Усі теми",
    settingsTitle = "Ваші параметри навчання",
    settingsOverline = "Налаштування",
    supportLanguageTitle = "Мова підтримки",
    supportLanguageDescription = "Одна мова підтримки робить уроки спокійнішими й читабельнішими.",
    currentPlanTitle = "Поточний план",
    premiumUnlocked = "Преміум відкрито",
    freeStarterPlan = "Безкоштовний старт",
    premiumDescription = "Ви вже відкрили повніший шлях. Рухайтеся далі у своєму темпі.",
    freePlanDescription = "Безкоштовного старту достатньо, щоб відчути користь продукту. Преміум з'являється лише після кількох уроків.",
    saveSettings = "Зберегти",
    keepMomentum = "Не втрачайте темп",
    later = "Пізніше",
    paywallDescription = "Відкрийте повний курс, коли захочете більше тем, більше уроків і спокійніший шлях через щоденне життя польською.",
    paywallBenefitOne = "Усі практичні теми, а не лише стартовий набір",
    paywallBenefitTwo = "Стабільний маршрут для роботи, транспорту, лікаря й документів",
    paywallBenefitThree = "Чистіший навчальний шлях без зайвих зупинок",
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
    chooseCorrectTranslation = "Оберіть правильний переклад",
    lessonPractice = "Практика уроку",
    correct = "Правильно",
    notQuite = "Поки ні",
    correctFeedback = "Добре. Рухайте фразу далі."
)

private val russianAppText = AppText(
    back = "Назад",
    settings = "Настройки",
    premium = "Премиум",
    freePlan = "Бесплатно",
    lessonsLabel = "Уроки",
    topicsDoneLabel = "Темы",
    languageSelectionBadge = "Выберите язык поддержки",
    languageSelectionTitle = "Начните на языке, который для вас естественен.",
    languageSelectionDescription = "Вы будете учить польский, но объяснения и переводы должны быть понятными с первого экрана.",
    languageSelectionContinuePrefix = "Продолжить",
    languageSelectionFooter = "Язык можно будет изменить позже в настройках.",
    welcomeTopPill = "Практический польский",
    welcomeBadge = "Реальные слова для реальной жизни в Польше",
    welcomeTitle = "Учите польский, который действительно нужен.",
    welcomeDescription = "Понятные фразы для магазина, транспорта, врача, работы и повседневной жизни. Спокойные короткие уроки, которые помогают начать быстрее.",
    startLearning = "Начать обучение",
    exploreTopics = "Посмотреть темы",
    welcomeSupportiveLine = "Начинать жизнь в новой стране тяжело. Маленьких шагов достаточно.",
    homeGreeting = "С возвращением",
    homeTitle = "Продолжайте практический польский для реальной жизни.",
    homeDescription = "Даже один завершенный урок уже облегчает следующую покупку, поездку или короткий разговор.",
    homePremiumTitle = "Полный курс можно открыть позже.",
    homePremiumDescription = "Сначала почувствуйте пользу от бесплатного старта. Премиум появится только после нескольких уроков.",
    shortSupportiveLine = "Не нужна длинная сессия. Нескольких полезных фраз на сегодня достаточно.",
    startFirstLesson = "Начать первый урок",
    continueLearning = "Продолжить обучение",
    continueWith = "Продолжить с темы",
    topicSelectionTitle = "Выберите стартовую тему",
    topicSelectionDescription = "Начните с ситуаций, которые встретятся вам в первую очередь. Остальное можно открывать постепенно.",
    topicSelectionHeaderTitle = "Ваши первые шаги в польском",
    topicSelectionSupportiveLine = "Не нужно учить всё сразу. Выберите одну полезную тему и спокойно завершите один урок.",
    lessonReadyTitle = "Урок готов",
    lessonMinutesSuffix = "мин",
    lessonPhrasesSuffix = "фраз",
    whatYouWillDo = "Что будет в уроке",
    lessonBulletOnePrefix = "Выучите полезные польские слова с поддержкой",
    lessonBulletTwo = "Увидите короткий жизненный пример для каждой фразы",
    lessonBulletThree = "Пройдёте 3 простых типа упражнений без перегруза",
    firstPhrasesInLesson = "Первые фразы в этом уроке",
    startLesson = "Начать урок",
    lessonCompleteTitle = "Урок завершён",
    completionSupportiveLine = "Не нужна длинная сессия. Один завершённый урок уже считается.",
    continueTopic = "Продолжить тему",
    backToTopics = "Назад к темам",
    seeAllTopics = "Все темы",
    settingsTitle = "Ваши параметры обучения",
    settingsOverline = "Настройки",
    supportLanguageTitle = "Язык поддержки",
    supportLanguageDescription = "Один язык поддержки делает уроки спокойнее и проще для чтения.",
    currentPlanTitle = "Текущий план",
    premiumUnlocked = "Премиум открыт",
    freeStarterPlan = "Бесплатный старт",
    premiumDescription = "Вы уже открыли более полный путь. Продолжайте в своем темпе.",
    freePlanDescription = "Бесплатного старта достаточно, чтобы почувствовать пользу продукта. Премиум появляется только после нескольких уроков.",
    saveSettings = "Сохранить",
    keepMomentum = "Не теряйте темп",
    later = "Позже",
    paywallDescription = "Откройте полный курс, когда захотите больше тем, больше уроков и более спокойный путь через повседневную жизнь на польском.",
    paywallBenefitOne = "Все практические темы, а не только стартовый набор",
    paywallBenefitTwo = "Последовательный маршрут для работы, транспорта, врача и документов",
    paywallBenefitThree = "Более чистый учебный путь без лишних остановок",
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
    chooseCorrectTranslation = "Выберите правильный перевод",
    lessonPractice = "Практика урока",
    correct = "Верно",
    notQuite = "Пока нет",
    correctFeedback = "Хорошо. Двигаемся дальше."
)
