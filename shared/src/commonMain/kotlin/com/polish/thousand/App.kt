package com.polish.thousand

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.polish.thousand.content.LessonContent
import com.polish.thousand.content.MvpSeedContent
import com.polish.thousand.content.PendingQuickReview
import com.polish.thousand.content.ReviewQuality
import com.polish.thousand.content.ReviewSchedule
import com.polish.thousand.content.SupportLanguage
import com.polish.thousand.content.TopicContent
import com.polish.thousand.content.currentEpochDay
import com.polish.thousand.content.itemsByIds
import com.polish.thousand.content.rememberAppPersistence
import com.polish.thousand.core.designsystem.PolishThousandTheme
import com.polish.thousand.ui.LanguageSelectionScreen
import com.polish.thousand.ui.LessonCompletionScreen
import com.polish.thousand.ui.LessonStudyScreen
import com.polish.thousand.ui.SettingsScreen
import com.polish.thousand.ui.SoftPaywallScreen
import com.polish.thousand.ui.SplashScreen
import com.polish.thousand.ui.WelcomeScreen
import kotlinx.coroutines.delay

@Composable
fun App() {
    PolishThousandTheme {
        val persistence = rememberAppPersistence()
        val initialLanguage = remember { persistence.loadSupportLanguage() }
        val backStack = remember { mutableStateListOf<AppRoute>(AppRoute.Splash) }
        var supportLanguage by remember {
            mutableStateOf(initialLanguage ?: SupportLanguage.Ukrainian)
        }
        var completedLessonIds by remember {
            mutableStateOf(persistence.loadCompletedLessonIds())
        }
        var learnedWordIds by remember {
            mutableStateOf(persistence.loadLearnedWordIds())
        }
        var reviewStates by remember {
            mutableStateOf(persistence.loadReviewStates())
        }
        var pendingQuickReview by remember {
            mutableStateOf(persistence.loadPendingQuickReview())
        }
        var hasPremium by remember { mutableStateOf(persistence.loadHasPremium()) }
        var hasSeenPaywall by remember { mutableStateOf(persistence.loadHasSeenPaywall()) }
        val todayEpochDay = currentEpochDay()
        val dueReviewCount = ReviewSchedule.dueWordCount(reviewStates, todayEpochDay)
        val dueReviewWordIds = ReviewSchedule.dueWordIds(reviewStates, todayEpochDay)
        val dueReviewItems = MvpSeedContent.lessons.itemsByIds(dueReviewWordIds)
        val sameDayQuickReview = pendingQuickReview?.takeIf { it.createdEpochDay == todayEpochDay }
        val quickReviewItems = MvpSeedContent.lessons.itemsByIds(
            sameDayQuickReview?.wordIds.orEmpty().sorted()
        )
        val nextLesson = MvpSeedContent.nextLesson(completedLessonIds)
        val reviewHostLesson = MvpSeedContent.lessons.firstOrNull { lesson ->
            lesson.items.any { it.id in dueReviewWordIds }
        }
        val quickReviewHostLesson = sameDayQuickReview?.let { review ->
            MvpSeedContent.lessons.firstOrNull { it.id == review.lessonId }
                ?: MvpSeedContent.lessons.firstOrNull { lesson -> lesson.items.any { it.id in review.wordIds } }
        }
        val currentRoute = backStack.last()

        fun push(route: AppRoute) {
            backStack.add(route)
        }

        fun replace(route: AppRoute) {
            backStack[backStack.lastIndex] = route
        }

        fun pop() {
            if (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex)
            }
        }

        fun resetTo(route: AppRoute) {
            backStack.clear()
            backStack.add(route)
        }

        fun replaceStackWith(route: AppRoute) {
            backStack.clear()
            backStack.add(AppRoute.Welcome)
            backStack.add(route)
        }

        fun gatedLessonRoute(route: AppRoute.LessonStudy): AppRoute =
            if (!hasPremium && learnedWordIds.size >= FreeWordLimit) {
                AppRoute.PaywallGate(route.topicId, route.lessonId)
            } else {
                route
            }

        fun openLesson(
            route: AppRoute.LessonStudy,
            clearHistory: Boolean = false
        ) {
            val nextRoute = gatedLessonRoute(route)

            if (clearHistory) {
                replaceStackWith(nextRoute)
            } else {
                push(nextRoute)
            }
        }

        fun openLessonWithoutGate(
            route: AppRoute.LessonStudy,
            clearHistory: Boolean = false
        ) {
            if (clearHistory) {
                replaceStackWith(route)
            } else {
                push(route)
            }
        }

        fun addLearnedWordIfNeeded(wordId: String) {
            if (wordId !in learnedWordIds) {
                val updatedLearnedWordIds = learnedWordIds + wordId
                learnedWordIds = updatedLearnedWordIds
                persistence.saveLearnedWordIds(updatedLearnedWordIds)
            }
        }

        fun recordScheduledReviewAnswer(wordId: String, quality: ReviewQuality) {
            val updatedReviewStates = ReviewSchedule.recordScheduledAnswer(
                states = reviewStates,
                wordId = wordId,
                quality = quality,
                todayEpochDay = todayEpochDay
            )
            reviewStates = updatedReviewStates
            persistence.saveReviewStates(updatedReviewStates)

            if (quality == ReviewQuality.Know) {
                addLearnedWordIfNeeded(wordId)
            }
        }

        fun recordQuickReviewAnswer(wordId: String, quality: ReviewQuality) {
            val updatedReviewStates = ReviewSchedule.recordEarlyAnswer(
                states = reviewStates,
                wordId = wordId,
                quality = quality,
                todayEpochDay = todayEpochDay
            )
            reviewStates = updatedReviewStates
            persistence.saveReviewStates(updatedReviewStates)

            if (quality == ReviewQuality.Know) {
                addLearnedWordIfNeeded(wordId)
            }
        }

        LaunchedEffect(Unit) {
            delay(1100)
            replace(if (initialLanguage == null) AppRoute.LanguageSelection else AppRoute.Welcome)
        }

        LaunchedEffect(todayEpochDay, pendingQuickReview) {
            if (pendingQuickReview != null && pendingQuickReview?.createdEpochDay != todayEpochDay) {
                pendingQuickReview = null
                persistence.savePendingQuickReview(null)
            }
        }

        when (currentRoute) {
            AppRoute.Splash -> SplashScreen()

            AppRoute.LanguageSelection -> LanguageSelectionScreen(
                selectedLanguage = supportLanguage,
                onLanguageSelected = { language ->
                    supportLanguage = language
                    persistence.saveSupportLanguage(language)
                    replace(AppRoute.Welcome)
                }
            )

            AppRoute.Welcome -> WelcomeScreen(
                supportLanguage = supportLanguage,
                learnedWords = learnedWordIds.size,
                nextLesson = nextLesson,
                completedLessonIds = completedLessonIds,
                dueReviewCount = dueReviewCount,
                quickReviewCount = quickReviewItems.size,
                onContinueClick = {
                    when {
                        nextLesson != null -> {
                            openLesson(AppRoute.LessonStudy(MvpSeedContent.path.id, nextLesson.id))
                        }
                        dueReviewItems.isNotEmpty() && reviewHostLesson != null -> {
                            push(
                                AppRoute.ReviewOnly(
                                    topicId = MvpSeedContent.path.id,
                                    lessonId = reviewHostLesson.id,
                                    wordIds = dueReviewWordIds
                                )
                            )
                        }
                    }
                },
                onOpenDueReviewClick = {
                    if (dueReviewItems.isNotEmpty() && reviewHostLesson != null) {
                        push(
                            AppRoute.ReviewOnly(
                                topicId = MvpSeedContent.path.id,
                                lessonId = reviewHostLesson.id,
                                wordIds = dueReviewWordIds
                            )
                        )
                    }
                },
                onOpenQuickReviewClick = {
                    if (quickReviewItems.isNotEmpty() && quickReviewHostLesson != null) {
                        push(AppRoute.QuickReview(MvpSeedContent.path.id, quickReviewHostLesson.id))
                    }
                },
                onOpenSettingsClick = { push(AppRoute.Settings) }
            )

            is AppRoute.LessonStudy -> {
                val lessonRoute = resolveLessonRoute(currentRoute.topicId, currentRoute.lessonId)
                if (lessonRoute != null) {
                    LessonStudyScreen(
                        lesson = lessonRoute.lesson,
                        supportLanguage = supportLanguage,
                        onBackClick = ::pop,
                        onReviewAnswer = { wordId, quality ->
                            recordScheduledReviewAnswer(wordId, quality)
                        },
                        onCompleteClick = { correctWordIds ->
                            val addedWordIds = correctWordIds - learnedWordIds
                            val updatedLearnedWordIds = learnedWordIds + correctWordIds
                            learnedWordIds = updatedLearnedWordIds
                            persistence.saveLearnedWordIds(updatedLearnedWordIds)
                            val updatedCompletedLessonIds = completedLessonIds + lessonRoute.lesson.id
                            completedLessonIds = updatedCompletedLessonIds
                            persistence.saveCompletedLessonIds(updatedCompletedLessonIds)
                            val updatedReviewStates = ReviewSchedule.scheduleNewWords(
                                states = reviewStates,
                                words = lessonRoute.lesson.items,
                                todayEpochDay = todayEpochDay
                            )
                            reviewStates = updatedReviewStates
                            persistence.saveReviewStates(updatedReviewStates)
                            val lessonWordIds = lessonRoute.lesson.items.mapTo(mutableSetOf()) { it.id }
                            val missedWordIds = lessonWordIds - correctWordIds
                            val newPendingQuickReview = missedWordIds.takeIf { it.isNotEmpty() }?.let { wordIds ->
                                PendingQuickReview(
                                    lessonId = lessonRoute.lesson.id,
                                    wordIds = wordIds,
                                    createdEpochDay = todayEpochDay
                                )
                            }
                            pendingQuickReview = newPendingQuickReview
                            persistence.savePendingQuickReview(newPendingQuickReview)
                            val learnedWords = updatedLearnedWordIds.size
                            if (!hasPremium && !hasSeenPaywall && learnedWords >= 100) {
                                hasSeenPaywall = true
                                persistence.saveHasSeenPaywall(true)
                                push(
                                    AppRoute.Paywall(
                                        topicId = lessonRoute.topic.id,
                                        lessonId = lessonRoute.lesson.id,
                                        addedWords = addedWordIds.size,
                                        attemptedWords = lessonRoute.lesson.items.size
                                    )
                                )
                            } else {
                                push(
                                    AppRoute.LessonCompletion(
                                        topicId = lessonRoute.topic.id,
                                        lessonId = lessonRoute.lesson.id,
                                        addedWords = addedWordIds.size,
                                        attemptedWords = lessonRoute.lesson.items.size
                                    )
                                )
                            }
                        }
                    )
                }
            }

            is AppRoute.ReviewOnly -> {
                val lessonRoute = resolveLessonRoute(currentRoute.topicId, currentRoute.lessonId)
                val reviewSessionItems = MvpSeedContent.lessons.itemsByIds(currentRoute.wordIds)
                if (lessonRoute != null && reviewSessionItems.isNotEmpty()) {
                    LessonStudyScreen(
                        lesson = lessonRoute.lesson,
                        reviewItems = reviewSessionItems,
                        reviewOnly = true,
                        supportLanguage = supportLanguage,
                        onBackClick = ::pop,
                        onReviewAnswer = { wordId, quality ->
                            recordScheduledReviewAnswer(wordId, quality)
                        },
                        onReviewCompleteClick = {
                            resetTo(AppRoute.Welcome)
                        }
                    )
                } else {
                    LaunchedEffect(currentRoute) {
                        resetTo(AppRoute.Welcome)
                    }
                }
            }

            is AppRoute.LessonCompletion -> {
                val lessonRoute = resolveLessonRoute(currentRoute.topicId, currentRoute.lessonId)
                if (lessonRoute != null) {
                    LessonCompletionScreen(
                        lesson = lessonRoute.lesson,
                        supportLanguage = supportLanguage,
                        learnedWords = learnedWordIds.size,
                        addedWords = currentRoute.addedWords,
                        attemptedWords = currentRoute.attemptedWords,
                        quickReviewWords = quickReviewItems.size,
                        continuesToNextLesson = nextLesson != null,
                        onQuickReviewClick = {
                            if (quickReviewItems.isNotEmpty() && quickReviewHostLesson != null) {
                                replaceStackWith(
                                    AppRoute.QuickReview(MvpSeedContent.path.id, quickReviewHostLesson.id)
                                )
                            } else {
                                resetTo(AppRoute.Welcome)
                            }
                        },
                        onContinueClick = {
                            if (nextLesson != null) {
                                openLesson(
                                    route = AppRoute.LessonStudy(MvpSeedContent.path.id, nextLesson.id),
                                    clearHistory = true
                                )
                            } else {
                                resetTo(AppRoute.Welcome)
                            }
                        }
                    )
                }
            }

            is AppRoute.QuickReview -> {
                val lessonRoute = resolveLessonRoute(currentRoute.topicId, currentRoute.lessonId)
                if (lessonRoute != null && quickReviewItems.isNotEmpty()) {
                    LessonStudyScreen(
                        lesson = lessonRoute.lesson,
                        reviewItems = quickReviewItems,
                        reviewOnly = true,
                        supportLanguage = supportLanguage,
                        onBackClick = {
                            resetTo(AppRoute.Welcome)
                        },
                        onReviewAnswer = { wordId, quality ->
                            recordQuickReviewAnswer(wordId, quality)
                        },
                        onReviewCompleteClick = {
                            pendingQuickReview = null
                            persistence.savePendingQuickReview(null)
                            resetTo(AppRoute.Welcome)
                        }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        resetTo(AppRoute.Welcome)
                    }
                }
            }

            is AppRoute.Paywall -> SoftPaywallScreen(
                supportLanguage = supportLanguage,
                completedLessons = completedLessonIds.size,
                onUnlockClick = {
                    hasPremium = true
                    persistence.saveHasPremium(true)
                    replace(
                        AppRoute.LessonCompletion(
                            topicId = currentRoute.topicId,
                            lessonId = currentRoute.lessonId,
                            addedWords = currentRoute.addedWords,
                            attemptedWords = currentRoute.attemptedWords
                        )
                    )
                },
                onContinueFreeClick = {
                    replace(
                        AppRoute.LessonCompletion(
                            topicId = currentRoute.topicId,
                            lessonId = currentRoute.lessonId,
                            addedWords = currentRoute.addedWords,
                            attemptedWords = currentRoute.attemptedWords
                        )
                    )
                },
                onCloseClick = {
                    replace(
                        AppRoute.LessonCompletion(
                            topicId = currentRoute.topicId,
                            lessonId = currentRoute.lessonId,
                            addedWords = currentRoute.addedWords,
                            attemptedWords = currentRoute.attemptedWords
                        )
                    )
                }
            )

            is AppRoute.PaywallGate -> SoftPaywallScreen(
                supportLanguage = supportLanguage,
                completedLessons = completedLessonIds.size,
                onUnlockClick = {
                    hasPremium = true
                    persistence.saveHasPremium(true)
                    openLessonWithoutGate(
                        route = AppRoute.LessonStudy(currentRoute.topicId, currentRoute.lessonId),
                        clearHistory = true
                    )
                },
                onContinueFreeClick = {
                    openLessonWithoutGate(
                        route = AppRoute.LessonStudy(currentRoute.topicId, currentRoute.lessonId),
                        clearHistory = true
                    )
                },
                onCloseClick = {
                    resetTo(AppRoute.Welcome)
                }
            )

            AppRoute.Settings -> SettingsScreen(
                selectedLanguage = supportLanguage,
                supportLanguage = supportLanguage,
                hasPremium = hasPremium,
                completedLessonIds = completedLessonIds,
                learnedWords = learnedWordIds.size,
                onBackClick = ::pop,
                onLanguageSaved = { language ->
                    supportLanguage = language
                    persistence.saveSupportLanguage(language)
                    pop()
                }
            )
        }
    }
}

private sealed interface AppRoute {
    data object Splash : AppRoute
    data object LanguageSelection : AppRoute
    data object Welcome : AppRoute
    data class LessonStudy(val topicId: String, val lessonId: String) : AppRoute
    data class ReviewOnly(val topicId: String, val lessonId: String, val wordIds: List<String>) : AppRoute
    data class QuickReview(val topicId: String, val lessonId: String) : AppRoute
    data class LessonCompletion(
        val topicId: String,
        val lessonId: String,
        val addedWords: Int,
        val attemptedWords: Int
    ) : AppRoute

    data class Paywall(
        val topicId: String,
        val lessonId: String,
        val addedWords: Int,
        val attemptedWords: Int
    ) : AppRoute

    data class PaywallGate(val topicId: String, val lessonId: String) : AppRoute
    data object Settings : AppRoute
}

private const val FreeWordLimit = 100

private data class ResolvedLessonRoute(
    val topic: TopicContent,
    val lesson: LessonContent
)

private fun resolveLessonRoute(
    topicId: String,
    lessonId: String
): ResolvedLessonRoute? {
    val topic = MvpSeedContent.path.takeIf { it.id == topicId } ?: return null
    val lesson = topic.lessons.firstOrNull { it.id == lessonId } ?: return null
    return ResolvedLessonRoute(topic = topic, lesson = lesson)
}
