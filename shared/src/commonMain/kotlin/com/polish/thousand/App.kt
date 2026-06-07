package com.polish.thousand

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.polish.thousand.content.LessonContent
import com.polish.thousand.content.MvpSeedContent
import com.polish.thousand.content.SupportLanguage
import com.polish.thousand.content.TopicContent
import kotlinx.coroutines.delay
import com.polish.thousand.ui.HomeScreen
import com.polish.thousand.core.designsystem.PolishThousandTheme
import com.polish.thousand.ui.LanguageSelectionScreen
import com.polish.thousand.ui.LessonCompletionScreen
import com.polish.thousand.ui.LessonIntroScreen
import com.polish.thousand.ui.LessonStudyScreen
import com.polish.thousand.ui.SettingsScreen
import com.polish.thousand.ui.SoftPaywallScreen
import com.polish.thousand.ui.SplashScreen
import com.polish.thousand.ui.TopicSelectionScreen
import com.polish.thousand.ui.WelcomeScreen

@Composable
fun App() {
    PolishThousandTheme {
        var screen by remember { mutableStateOf(AppScreen.Splash) }
        var selectedTopic by remember { mutableStateOf<TopicContent?>(null) }
        var selectedLesson by remember { mutableStateOf<LessonContent?>(null) }
        var supportLanguage by remember { mutableStateOf(SupportLanguage.Ukrainian) }
        var completedLessonIds by remember { mutableStateOf(setOf<String>()) }
        var hasPremium by remember { mutableStateOf(false) }
        var hasSeenPaywall by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(1100)
            screen = AppScreen.LanguageSelection
        }

        when (screen) {
            AppScreen.Splash -> SplashScreen()
            AppScreen.LanguageSelection -> LanguageSelectionScreen(
                selectedLanguage = supportLanguage,
                onLanguageSelected = { language ->
                    supportLanguage = language
                    screen = AppScreen.Welcome
                }
            )
            AppScreen.Welcome -> WelcomeScreen(
                onStartLearningClick = { screen = AppScreen.Home },
                onExploreTopicsClick = { screen = AppScreen.Home }
            )
            AppScreen.Home -> HomeScreen(
                supportLanguage = supportLanguage,
                completedLessonIds = completedLessonIds,
                hasPremium = hasPremium,
                onContinueClick = {
                    val nextTopic = nextTopicForProgress(completedLessonIds)
                    val nextLesson = nextLessonForTopic(nextTopic, completedLessonIds)
                    if (nextTopic != null && nextLesson != null) {
                        selectedTopic = nextTopic
                        selectedLesson = nextLesson
                        screen = AppScreen.LessonIntro
                    } else {
                        screen = AppScreen.Topics
                    }
                },
                onBrowseTopicsClick = { screen = AppScreen.Topics },
                onOpenSettingsClick = { screen = AppScreen.Settings }
            )
            AppScreen.Topics -> TopicSelectionScreen(
                supportLanguage = supportLanguage,
                completedLessonIds = completedLessonIds,
                onStartTopicClick = { topic ->
                    selectedTopic = topic
                    selectedLesson = nextLessonForTopic(topic, completedLessonIds)
                    screen = AppScreen.LessonIntro
                }
            )
            AppScreen.LessonIntro -> {
                val topic = selectedTopic
                val lesson = selectedLesson

                if (topic != null && lesson != null) {
                    LessonIntroScreen(
                        topic = topic,
                        lesson = lesson,
                        supportLanguage = supportLanguage,
                        onBackClick = { screen = AppScreen.Topics },
                        onStartLessonClick = { screen = AppScreen.LessonStudy }
                    )
                }
            }
            AppScreen.LessonStudy -> {
                val topic = selectedTopic
                val lesson = selectedLesson

                if (topic != null && lesson != null) {
                    LessonStudyScreen(
                        topic = topic,
                        lesson = lesson,
                        supportLanguage = supportLanguage,
                        onBackClick = { screen = AppScreen.LessonIntro },
                        onCompleteClick = {
                            val updatedCompletedLessonIds = completedLessonIds + lesson.id
                            completedLessonIds = updatedCompletedLessonIds
                            if (!hasPremium && !hasSeenPaywall && updatedCompletedLessonIds.size >= 2) {
                                hasSeenPaywall = true
                                screen = AppScreen.Paywall
                            } else {
                                screen = AppScreen.LessonCompletion
                            }
                        }
                    )
                }
            }
            AppScreen.LessonCompletion -> {
                val topic = selectedTopic
                val lesson = selectedLesson

                if (topic != null && lesson != null) {
                    LessonCompletionScreen(
                        topic = topic,
                        lesson = lesson,
                        completedLessonIds = completedLessonIds,
                        onBackToTopicsClick = { screen = AppScreen.Home },
                        onContinueClick = {
                            val nextLesson = nextLessonForTopic(topic, completedLessonIds)
                            if (nextLesson != null) {
                                selectedLesson = nextLesson
                                screen = AppScreen.LessonIntro
                            } else {
                                screen = AppScreen.Home
                            }
                        }
                    )
                }
            }
            AppScreen.Paywall -> SoftPaywallScreen(
                completedLessons = completedLessonIds.size,
                onUnlockClick = {
                    hasPremium = true
                    screen = AppScreen.Home
                },
                onContinueFreeClick = { screen = AppScreen.LessonCompletion },
                onCloseClick = { screen = AppScreen.LessonCompletion }
            )
            AppScreen.Settings -> SettingsScreen(
                selectedLanguage = supportLanguage,
                hasPremium = hasPremium,
                completedLessonIds = completedLessonIds,
                onBackClick = { screen = AppScreen.Home },
                onLanguageSaved = { language ->
                    supportLanguage = language
                    screen = AppScreen.Home
                }
            )
        }
    }
}

private enum class AppScreen {
    Splash,
    LanguageSelection,
    Welcome,
    Home,
    Topics,
    LessonIntro,
    LessonStudy,
    LessonCompletion,
    Paywall,
    Settings
}

private fun nextTopicForProgress(completedLessonIds: Set<String>): TopicContent? {
    return MvpSeedContent.topics.firstOrNull { topic ->
        topic.lessons.any { it.id !in completedLessonIds }
    } ?: MvpSeedContent.topics.firstOrNull()
}

private fun nextLessonForTopic(
    topic: TopicContent?,
    completedLessonIds: Set<String>
): LessonContent? {
    return topic?.lessons?.firstOrNull { it.id !in completedLessonIds }
        ?: topic?.lessons?.firstOrNull()
}
