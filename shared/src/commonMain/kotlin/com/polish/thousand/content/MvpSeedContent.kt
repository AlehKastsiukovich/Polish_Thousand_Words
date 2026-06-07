package com.polish.thousand.content

internal enum class ExerciseType {
    ChooseTranslation,
    ListenAndChoose,
    UnderstandInContext
}

internal data class LessonItemContent(
    val id: String,
    val polish: String,
    val russian: String,
    val ukrainian: String,
    val examplePolish: String,
    val exampleRussian: String,
    val exampleUkrainian: String,
    val note: String? = null
)

internal data class LessonContent(
    val id: String,
    val title: String,
    val description: String,
    val estimatedMinutes: Int,
    val exerciseTypes: List<ExerciseType>,
    val items: List<LessonItemContent>
)

internal data class TopicContent(
    val id: String,
    val title: String,
    val description: String,
    val lessons: List<LessonContent>
)

internal object MvpSeedContent {
    val topics: List<TopicContent> = listOf(
        basicPhrasesTopic,
        shopTopic,
        transportTopic
    )

    val totalLessons: Int = topics.sumOf { it.lessons.size }
    val totalItems: Int = topics.sumOf { topic -> topic.lessons.sumOf { it.items.size } }
}

private val defaultExerciseTypes = listOf(
    ExerciseType.ChooseTranslation,
    ExerciseType.ListenAndChoose,
    ExerciseType.UnderstandInContext
)

private val basicPhrasesTopic = TopicContent(
    id = "basic_phrases",
    title = "Basic phrases",
    description = "Short phrases for polite, everyday communication.",
    lessons = listOf(
        LessonContent(
            id = "basic_polite_essentials",
            title = "Polite essentials",
            description = "The first phrases you need almost everywhere.",
            estimatedMinutes = 7,
            exerciseTypes = defaultExerciseTypes,
            items = listOf(
                LessonItemContent(
                    id = "dzien_dobry",
                    polish = "Dzień dobry",
                    russian = "Здравствуйте",
                    ukrainian = "Добрий день",
                    examplePolish = "Dzień dobry, poproszę kawę.",
                    exampleRussian = "Здравствуйте, мне, пожалуйста, кофе.",
                    exampleUkrainian = "Добрий день, мені, будь ласка, каву."
                ),
                LessonItemContent(
                    id = "czesc",
                    polish = "Cześć",
                    russian = "Привет",
                    ukrainian = "Привіт",
                    examplePolish = "Cześć, jak się masz?",
                    exampleRussian = "Привет, как дела?",
                    exampleUkrainian = "Привіт, як справи?",
                    note = "Informal"
                ),
                LessonItemContent(
                    id = "dziekuje",
                    polish = "Dziękuję",
                    russian = "Спасибо",
                    ukrainian = "Дякую",
                    examplePolish = "Dziękuję za pomoc.",
                    exampleRussian = "Спасибо за помощь.",
                    exampleUkrainian = "Дякую за допомогу."
                ),
                LessonItemContent(
                    id = "prosze",
                    polish = "Proszę",
                    russian = "Пожалуйста",
                    ukrainian = "Будь ласка",
                    examplePolish = "Poproszę wodę, proszę.",
                    exampleRussian = "Мне, пожалуйста, воду.",
                    exampleUkrainian = "Мені, будь ласка, воду."
                ),
                LessonItemContent(
                    id = "przepraszam",
                    polish = "Przepraszam",
                    russian = "Извините",
                    ukrainian = "Перепрошую",
                    examplePolish = "Przepraszam, gdzie jest przystanek?",
                    exampleRussian = "Извините, где остановка?",
                    exampleUkrainian = "Перепрошую, де зупинка?"
                ),
                LessonItemContent(
                    id = "dobranoc",
                    polish = "Dobranoc",
                    russian = "Спокойной ночи",
                    ukrainian = "На добраніч",
                    examplePolish = "Dobranoc, do jutra.",
                    exampleRussian = "Спокойной ночи, до завтра.",
                    exampleUkrainian = "На добраніч, до завтра."
                )
            )
        ),
        LessonContent(
            id = "basic_help_and_understanding",
            title = "Help and understanding",
            description = "Useful when you need support or clarification.",
            estimatedMinutes = 8,
            exerciseTypes = defaultExerciseTypes,
            items = listOf(
                LessonItemContent(
                    id = "nie_rozumiem",
                    polish = "Nie rozumiem",
                    russian = "Я не понимаю",
                    ukrainian = "Я не розумію",
                    examplePolish = "Przepraszam, nie rozumiem.",
                    exampleRussian = "Извините, я не понимаю.",
                    exampleUkrainian = "Перепрошую, я не розумію."
                ),
                LessonItemContent(
                    id = "czy_mowi_pan_po_angielsku",
                    polish = "Czy mówi pan po angielsku?",
                    russian = "Вы говорите по-английски?",
                    ukrainian = "Ви говорите англійською?",
                    examplePolish = "Czy mówi pan po angielsku albo po rosyjsku?",
                    exampleRussian = "Вы говорите по-английски или по-русски?",
                    exampleUkrainian = "Ви говорите англійською або російською?",
                    note = "Formal"
                ),
                LessonItemContent(
                    id = "czy_mowi_pani_po_ukrainsku",
                    polish = "Czy mówi pani po ukraińsku?",
                    russian = "Вы говорите по-украински?",
                    ukrainian = "Ви говорите українською?",
                    examplePolish = "Przepraszam, czy mówi pani po ukraińsku?",
                    exampleRussian = "Извините, вы говорите по-украински?",
                    exampleUkrainian = "Перепрошую, ви говорите українською?",
                    note = "Formal"
                ),
                LessonItemContent(
                    id = "potrzebuje_pomocy",
                    polish = "Potrzebuję pomocy",
                    russian = "Мне нужна помощь",
                    ukrainian = "Мені потрібна допомога",
                    examplePolish = "Potrzebuję pomocy z adresem.",
                    exampleRussian = "Мне нужна помощь с адресом.",
                    exampleUkrainian = "Мені потрібна допомога з адресою."
                ),
                LessonItemContent(
                    id = "ile_to_kosztuje",
                    polish = "Ile to kosztuje?",
                    russian = "Сколько это стоит?",
                    ukrainian = "Скільки це коштує?",
                    examplePolish = "Ile to kosztuje razem?",
                    exampleRussian = "Сколько это стоит всего?",
                    exampleUkrainian = "Скільки це коштує разом?"
                ),
                LessonItemContent(
                    id = "mozna_powtorzyc",
                    polish = "Czy może pan powtórzyć?",
                    russian = "Вы можете повторить?",
                    ukrainian = "Ви можете повторити?",
                    examplePolish = "Przepraszam, czy może pan powtórzyć wolniej?",
                    exampleRussian = "Извините, вы можете повторить медленнее?",
                    exampleUkrainian = "Перепрошую, ви можете повторити повільніше?",
                    note = "Formal"
                )
            )
        )
    )
)

private val shopTopic = TopicContent(
    id = "shop",
    title = "Shop",
    description = "Words and phrases for groceries and checkout.",
    lessons = listOf(
        LessonContent(
            id = "shop_buying_groceries",
            title = "Buying groceries",
            description = "Core words for simple shopping trips.",
            estimatedMinutes = 8,
            exerciseTypes = defaultExerciseTypes,
            items = listOf(
                LessonItemContent(
                    id = "chleb",
                    polish = "Chleb",
                    russian = "Хлеб",
                    ukrainian = "Хліб",
                    examplePolish = "Poproszę jeden chleb.",
                    exampleRussian = "Мне, пожалуйста, один хлеб.",
                    exampleUkrainian = "Мені, будь ласка, один хліб."
                ),
                LessonItemContent(
                    id = "mleko",
                    polish = "Mleko",
                    russian = "Молоко",
                    ukrainian = "Молоко",
                    examplePolish = "Gdzie jest mleko?",
                    exampleRussian = "Где молоко?",
                    exampleUkrainian = "Де молоко?"
                ),
                LessonItemContent(
                    id = "woda",
                    polish = "Woda",
                    russian = "Вода",
                    ukrainian = "Вода",
                    examplePolish = "Poproszę dużą wodę.",
                    exampleRussian = "Мне, пожалуйста, большую воду.",
                    exampleUkrainian = "Мені, будь ласка, велику воду."
                ),
                LessonItemContent(
                    id = "jajka",
                    polish = "Jajka",
                    russian = "Яйца",
                    ukrainian = "Яйця",
                    examplePolish = "Potrzebuję dziesięć jajek.",
                    exampleRussian = "Мне нужно десять яиц.",
                    exampleUkrainian = "Мені потрібно десять яєць."
                ),
                LessonItemContent(
                    id = "ser",
                    polish = "Ser",
                    russian = "Сыр",
                    ukrainian = "Сир",
                    examplePolish = "Czy ten ser jest świeży?",
                    exampleRussian = "Этот сыр свежий?",
                    exampleUkrainian = "Цей сир свіжий?"
                ),
                LessonItemContent(
                    id = "torba",
                    polish = "Torba",
                    russian = "Пакет / сумка",
                    ukrainian = "Пакет / торба",
                    examplePolish = "Poproszę torbę.",
                    exampleRussian = "Мне, пожалуйста, пакет.",
                    exampleUkrainian = "Мені, будь ласка, пакет."
                )
            )
        ),
        LessonContent(
            id = "shop_at_the_checkout",
            title = "At the checkout",
            description = "Short phrases for paying and finishing your purchase.",
            estimatedMinutes = 7,
            exerciseTypes = defaultExerciseTypes,
            items = listOf(
                LessonItemContent(
                    id = "kasa",
                    polish = "Kasa",
                    russian = "Касса",
                    ukrainian = "Каса",
                    examplePolish = "Gdzie jest kasa?",
                    exampleRussian = "Где касса?",
                    exampleUkrainian = "Де каса?"
                ),
                LessonItemContent(
                    id = "rachunek",
                    polish = "Paragon",
                    russian = "Чек",
                    ukrainian = "Чек",
                    examplePolish = "Poproszę paragon.",
                    exampleRussian = "Мне, пожалуйста, чек.",
                    exampleUkrainian = "Мені, будь ласка, чек."
                ),
                LessonItemContent(
                    id = "karta",
                    polish = "Kartą",
                    russian = "Картой",
                    ukrainian = "Карткою",
                    examplePolish = "Czy mogę zapłacić kartą?",
                    exampleRussian = "Можно оплатить картой?",
                    exampleUkrainian = "Чи можна оплатити карткою?"
                ),
                LessonItemContent(
                    id = "gotowka",
                    polish = "Gotówką",
                    russian = "Наличными",
                    ukrainian = "Готівкою",
                    examplePolish = "Płacę gotówką.",
                    exampleRussian = "Я плачу наличными.",
                    exampleUkrainian = "Я плачу готівкою."
                ),
                LessonItemContent(
                    id = "za_drogo",
                    polish = "To jest za drogie",
                    russian = "Это слишком дорого",
                    ukrainian = "Це занадто дорого",
                    examplePolish = "Przepraszam, to jest za drogie.",
                    exampleRussian = "Извините, это слишком дорого.",
                    exampleUkrainian = "Перепрошую, це занадто дорого."
                ),
                LessonItemContent(
                    id = "razem",
                    polish = "Razem",
                    russian = "Итого / вместе",
                    ukrainian = "Разом / усього",
                    examplePolish = "Ile razem?",
                    exampleRussian = "Сколько всего?",
                    exampleUkrainian = "Скільки разом?"
                )
            )
        )
    )
)

private val transportTopic = TopicContent(
    id = "transport",
    title = "Transport",
    description = "Useful Polish for buses, trams, stations, and directions.",
    lessons = listOf(
        LessonContent(
            id = "transport_getting_around",
            title = "Getting around",
            description = "Words for moving around the city.",
            estimatedMinutes = 8,
            exerciseTypes = defaultExerciseTypes,
            items = listOf(
                LessonItemContent(
                    id = "bilet",
                    polish = "Bilet",
                    russian = "Билет",
                    ukrainian = "Квиток",
                    examplePolish = "Gdzie mogę kupić bilet?",
                    exampleRussian = "Где я могу купить билет?",
                    exampleUkrainian = "Де я можу купити квиток?"
                ),
                LessonItemContent(
                    id = "autobus",
                    polish = "Autobus",
                    russian = "Автобус",
                    ukrainian = "Автобус",
                    examplePolish = "Czekam na autobus numer dziesięć.",
                    exampleRussian = "Я жду автобус номер десять.",
                    exampleUkrainian = "Я чекаю автобус номер десять."
                ),
                LessonItemContent(
                    id = "tramwaj",
                    polish = "Tramwaj",
                    russian = "Трамвай",
                    ukrainian = "Трамвай",
                    examplePolish = "Ten tramwaj jedzie do centrum.",
                    exampleRussian = "Этот трамвай едет в центр.",
                    exampleUkrainian = "Цей трамвай їде до центру."
                ),
                LessonItemContent(
                    id = "przystanek",
                    polish = "Przystanek",
                    russian = "Остановка",
                    ukrainian = "Зупинка",
                    examplePolish = "Gdzie jest najbliższy przystanek?",
                    exampleRussian = "Где ближайшая остановка?",
                    exampleUkrainian = "Де найближча зупинка?"
                ),
                LessonItemContent(
                    id = "dworzec",
                    polish = "Dworzec",
                    russian = "Вокзал",
                    ukrainian = "Вокзал",
                    examplePolish = "Muszę jechać na dworzec.",
                    exampleRussian = "Мне нужно ехать на вокзал.",
                    exampleUkrainian = "Мені потрібно їхати на вокзал."
                ),
                LessonItemContent(
                    id = "centrum",
                    polish = "Centrum",
                    russian = "Центр",
                    ukrainian = "Центр",
                    examplePolish = "Czy to jedzie do centrum?",
                    exampleRussian = "Это едет в центр?",
                    exampleUkrainian = "Це їде в центр?"
                )
            )
        ),
        LessonContent(
            id = "transport_directions_and_timing",
            title = "Directions and timing",
            description = "Questions that help you avoid getting lost.",
            estimatedMinutes = 8,
            exerciseTypes = defaultExerciseTypes,
            items = listOf(
                LessonItemContent(
                    id = "o_ktorej",
                    polish = "O której?",
                    russian = "Во сколько?",
                    ukrainian = "О котрій?",
                    examplePolish = "O której odjeżdża autobus?",
                    exampleRussian = "Во сколько отправляется автобус?",
                    exampleUkrainian = "О котрій відправляється автобус?"
                ),
                LessonItemContent(
                    id = "dokad_jedzie",
                    polish = "Dokąd jedzie ten autobus?",
                    russian = "Куда едет этот автобус?",
                    ukrainian = "Куди їде цей автобус?",
                    examplePolish = "Przepraszam, dokąd jedzie ten autobus?",
                    exampleRussian = "Извините, куда едет этот автобус?",
                    exampleUkrainian = "Перепрошую, куди їде цей автобус?"
                ),
                LessonItemContent(
                    id = "musze_wysiasc_tutaj",
                    polish = "Muszę wysiąść tutaj",
                    russian = "Мне нужно выйти здесь",
                    ukrainian = "Мені потрібно вийти тут",
                    examplePolish = "Dziękuję, muszę wysiąść tutaj.",
                    exampleRussian = "Спасибо, мне нужно выйти здесь.",
                    exampleUkrainian = "Дякую, мені потрібно вийти тут."
                ),
                LessonItemContent(
                    id = "czy_to_daleko",
                    polish = "Czy to daleko?",
                    russian = "Это далеко?",
                    ukrainian = "Це далеко?",
                    examplePolish = "Przepraszam, czy to daleko stąd?",
                    exampleRussian = "Извините, это далеко отсюда?",
                    exampleUkrainian = "Перепрошую, це далеко звідси?"
                ),
                LessonItemContent(
                    id = "prosto",
                    polish = "Prosto",
                    russian = "Прямо",
                    ukrainian = "Прямо",
                    examplePolish = "Proszę iść prosto.",
                    exampleRussian = "Пожалуйста, идите прямо.",
                    exampleUkrainian = "Будь ласка, ідіть прямо."
                ),
                LessonItemContent(
                    id = "w_lewo",
                    polish = "W lewo / w prawo",
                    russian = "Налево / направо",
                    ukrainian = "Ліворуч / праворуч",
                    examplePolish = "Potem w lewo i prosto.",
                    exampleRussian = "Потом налево и прямо.",
                    exampleUkrainian = "Потім ліворуч і прямо."
                )
            )
        )
    )
)
