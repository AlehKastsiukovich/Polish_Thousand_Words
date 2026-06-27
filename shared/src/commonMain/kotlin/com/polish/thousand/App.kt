package com.polish.thousand

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.polish.thousand.audio.AppAudioPlayer
import com.polish.thousand.content.MvpSeedContent
import com.polish.thousand.content.itemsByIds
import com.polish.thousand.content.rememberAppPersistence
import com.polish.thousand.core.designsystem.PolishThousandTheme
import com.polish.thousand.ui.LanguageSelectionScreen
import com.polish.thousand.ui.LessonCompletionScreen
import com.polish.thousand.ui.LessonEffect
import com.polish.thousand.ui.LessonIntent
import com.polish.thousand.ui.LessonStudyScreen
import com.polish.thousand.ui.LessonUiState
import com.polish.thousand.ui.LessonViewModel
import com.polish.thousand.ui.SettingsScreen
import com.polish.thousand.ui.SoftPaywallScreen
import com.polish.thousand.ui.SplashScreen
import com.polish.thousand.ui.WelcomeScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun App() {
    PolishThousandTheme {
        val persistence = rememberAppPersistence()
        val appViewModel = koinViewModel<AppViewModel>(
            parameters = { parametersOf(persistence) }
        )
        val lessonViewModel = koinViewModel<LessonViewModel>(
            parameters = { parametersOf(persistence) }
        )
        val audioPlayer = koinInject<AppAudioPlayer>()
        val appState by appViewModel.uiState.collectAsState()
        val lessonState by lessonViewModel.uiState.collectAsState()

        DisposableEffect(audioPlayer) {
            onDispose {
                audioPlayer.stop()
            }
        }

        LaunchedEffect(appViewModel) {
            if (appViewModel.uiState.value.currentRoute == AppRoute.Splash) {
                delay(1100)
                appViewModel.dispatchIntent(AppIntent.SplashFinished)
            }
        }

        LaunchedEffect(appViewModel, lessonViewModel) {
            lessonViewModel.uiEffect.collect { effect ->
                when (effect) {
                    is LessonEffect.LessonCompleted -> appViewModel.dispatchIntent(
                        AppIntent.LessonCompleted(effect.correctWordIds)
                    )
                    is LessonEffect.ReviewAnswered -> {
                        val isQuickReview = appViewModel.uiState.value.currentRoute is AppRoute.QuickReview
                        appViewModel.dispatchIntent(
                            if (isQuickReview) {
                                AppIntent.QuickReviewAnswered(effect.wordId, effect.quality)
                            } else {
                                AppIntent.ScheduledReviewAnswered(effect.wordId, effect.quality)
                            }
                        )
                        if (effect.sessionCompleted) {
                            appViewModel.dispatchIntent(
                                if (isQuickReview) AppIntent.QuickReviewCompleted
                                else AppIntent.ReviewCompleted
                            )
                        }
                    }
                }
            }
        }

        AppContent(
            appState = appState,
            lessonState = lessonState,
            dispatchAppIntent = appViewModel::dispatchIntent,
            dispatchLessonIntent = lessonViewModel::dispatchIntent,
            audioPlayer = audioPlayer
        )
    }
}

@Composable
private fun AppContent(
    appState: AppUiState,
    lessonState: LessonUiState,
    dispatchAppIntent: (AppIntent) -> Boolean,
    dispatchLessonIntent: (LessonIntent) -> Boolean,
    audioPlayer: AppAudioPlayer
) {
    when (val route = appState.currentRoute) {
        AppRoute.Splash -> SplashScreen()

        AppRoute.LanguageSelection -> LanguageSelectionScreen(
            selectedLanguage = appState.supportLanguage,
            onLanguageSelected = { language ->
                dispatchAppIntent(AppIntent.SelectLanguage(language))
            }
        )

        AppRoute.Welcome -> WelcomeScreen(
            supportLanguage = appState.supportLanguage,
            learnedWords = appState.learnedWordIds.size,
            nextLesson = appState.nextLesson,
            completedLessonIds = appState.completedLessonIds,
            dueReviewCount = appState.dueReviewCount,
            quickReviewCount = appState.quickReviewItems.size,
            onContinueClick = { dispatchAppIntent(AppIntent.ContinueFromHome) },
            onOpenDueReviewClick = { dispatchAppIntent(AppIntent.OpenDueReview) },
            onOpenQuickReviewClick = { dispatchAppIntent(AppIntent.OpenQuickReview) },
            onOpenSettingsClick = { dispatchAppIntent(AppIntent.OpenSettings) }
        )

        is AppRoute.LessonStudy -> LessonRouteContent(
            route = route,
            appState = appState,
            lessonState = lessonState,
            dispatchAppIntent = dispatchAppIntent,
            dispatchLessonIntent = dispatchLessonIntent,
            audioPlayer = audioPlayer
        )

        is AppRoute.ReviewOnly -> LessonRouteContent(
            route = route,
            appState = appState,
            lessonState = lessonState,
            dispatchAppIntent = dispatchAppIntent,
            dispatchLessonIntent = dispatchLessonIntent,
            audioPlayer = audioPlayer
        )

        is AppRoute.QuickReview -> LessonRouteContent(
            route = route,
            appState = appState,
            lessonState = lessonState,
            dispatchAppIntent = dispatchAppIntent,
            dispatchLessonIntent = dispatchLessonIntent,
            audioPlayer = audioPlayer
        )

        is AppRoute.LessonCompletion -> {
            val lessonRoute = resolveLessonRoute(route.topicId, route.lessonId)
            if (lessonRoute != null) {
                LessonCompletionScreen(
                    lesson = lessonRoute.lesson,
                    supportLanguage = appState.supportLanguage,
                    learnedWords = appState.learnedWordIds.size,
                    addedWords = route.addedWords,
                    attemptedWords = route.attemptedWords,
                    quickReviewWords = appState.quickReviewItems.size,
                    continuesToNextLesson = appState.nextLesson != null,
                    onQuickReviewClick = {
                        dispatchAppIntent(AppIntent.OpenCompletionQuickReview)
                    },
                    onContinueClick = {
                        dispatchAppIntent(AppIntent.ContinueFromCompletion)
                    },
                    onHomeClick = {
                        dispatchAppIntent(AppIntent.ReturnHomeFromCompletion)
                    }
                )
            }
        }

        is AppRoute.Paywall -> PaywallContent(
            completedLessons = appState.completedLessonIds.size,
            appState = appState,
            dispatchAppIntent = dispatchAppIntent
        )

        is AppRoute.PaywallGate -> PaywallContent(
            completedLessons = appState.completedLessonIds.size,
            appState = appState,
            dispatchAppIntent = dispatchAppIntent
        )

        AppRoute.Settings -> SettingsScreen(
            selectedLanguage = appState.supportLanguage,
            supportLanguage = appState.supportLanguage,
            hasPremium = appState.hasPremium,
            completedLessonIds = appState.completedLessonIds,
            learnedWords = appState.learnedWordIds.size,
            onBackClick = { dispatchAppIntent(AppIntent.Back) },
            onLanguageSaved = { language ->
                dispatchAppIntent(AppIntent.SaveSettings(language))
            }
        )
    }
}

@Composable
private fun LessonRouteContent(
    route: AppRoute,
    appState: AppUiState,
    lessonState: LessonUiState,
    dispatchAppIntent: (AppIntent) -> Boolean,
    dispatchLessonIntent: (LessonIntent) -> Boolean,
    audioPlayer: AppAudioPlayer
) {
    val specification = route.toLessonSessionSpecification() ?: return
    val lessonRoute = resolveLessonRoute(specification.topicId, specification.lessonId) ?: return
    val reviewItems = MvpSeedContent.lessons.itemsByIds(specification.wordIds)

    LaunchedEffect(specification.sessionKey, appState.supportLanguage) {
        dispatchLessonIntent(
            LessonIntent.StartSession(
                sessionKey = specification.sessionKey,
                lesson = lessonRoute.lesson,
                reviewItems = reviewItems,
                reviewOnly = specification.reviewOnly,
                supportLanguage = appState.supportLanguage
            )
        )
    }

    if (lessonState.sessionKey == specification.sessionKey && lessonState.isReady) {
        LessonStudyScreen(
            state = lessonState,
            onBackClick = { dispatchAppIntent(AppIntent.Back) },
            onIntent = { intent -> dispatchLessonIntent(intent) },
            onPlayWordClick = audioPlayer::playWord,
            onPlayExampleClick = audioPlayer::playExample
        )
    } else {
        Box(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun PaywallContent(
    completedLessons: Int,
    appState: AppUiState,
    dispatchAppIntent: (AppIntent) -> Boolean
) {
    SoftPaywallScreen(
        supportLanguage = appState.supportLanguage,
        completedLessons = completedLessons,
        onUnlockClick = { dispatchAppIntent(AppIntent.UnlockPremium) },
        onContinueFreeClick = { dispatchAppIntent(AppIntent.ContinueFree) },
        onCloseClick = { dispatchAppIntent(AppIntent.ClosePaywall) }
    )
}

private data class LessonSessionSpecification(
    val sessionKey: String,
    val topicId: String,
    val lessonId: String,
    val wordIds: List<String>,
    val reviewOnly: Boolean
)

private fun AppRoute.toLessonSessionSpecification(): LessonSessionSpecification? = when (this) {
    is AppRoute.LessonStudy -> LessonSessionSpecification(
        sessionKey = "lesson:$topicId:$lessonId",
        topicId = topicId,
        lessonId = lessonId,
        wordIds = emptyList(),
        reviewOnly = false
    )
    is AppRoute.ReviewOnly -> LessonSessionSpecification(
        sessionKey = "scheduled:$topicId:$lessonId:${wordIds.joinToString(",")}",
        topicId = topicId,
        lessonId = lessonId,
        wordIds = wordIds,
        reviewOnly = true
    )
    is AppRoute.QuickReview -> LessonSessionSpecification(
        sessionKey = "quick:$topicId:$lessonId:${wordIds.joinToString(",")}",
        topicId = topicId,
        lessonId = lessonId,
        wordIds = wordIds,
        reviewOnly = true
    )
    else -> null
}
