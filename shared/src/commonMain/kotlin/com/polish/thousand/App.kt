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
import com.polish.thousand.content.SupportLanguage
import com.polish.thousand.content.TopicContent
import com.polish.thousand.core.designsystem.PolishThousandTheme
import com.polish.thousand.ui.HomeScreen
import com.polish.thousand.ui.LanguageSelectionScreen
import com.polish.thousand.ui.LessonCompletionScreen
import com.polish.thousand.ui.LessonIntroScreen
import com.polish.thousand.ui.LessonStudyScreen
import com.polish.thousand.ui.SettingsScreen
import com.polish.thousand.ui.SoftPaywallScreen
import com.polish.thousand.ui.SplashScreen
import com.polish.thousand.ui.TopicSelectionScreen
import com.polish.thousand.ui.WelcomeScreen
import kotlinx.coroutines.delay

@Composable
fun App() {
    PolishThousandTheme {
        val backStack = remember { mutableStateListOf<AppRoute>(AppRoute.Splash) }
        var supportLanguage by remember { mutableStateOf(SupportLanguage.Ukrainian) }
        var completedLessonIds by remember { mutableStateOf(setOf<String>()) }
        var hasPremium by remember { mutableStateOf(false) }
        var hasSeenPaywall by remember { mutableStateOf(false) }
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

        LaunchedEffect(Unit) {
            delay(1100)
            replace(AppRoute.LanguageSelection)
        }

        when (currentRoute) {
            AppRoute.Splash -> SplashScreen()

            AppRoute.LanguageSelection -> LanguageSelectionScreen(
                selectedLanguage = supportLanguage,
                onLanguageSelected = { language ->
                    supportLanguage = language
                    replace(AppRoute.Welcome)
                }
            )

            AppRoute.Welcome -> WelcomeScreen(
                supportLanguage = supportLanguage,
                onStartLearningClick = { replace(AppRoute.Home) },
                onExploreTopicsClick = { push(AppRoute.Topics) }
            )

            AppRoute.Home -> HomeScreen(
                supportLanguage = supportLanguage,
                completedLessonIds = completedLessonIds,
                hasPremium = hasPremium,
                onContinueClick = {
                    val nextTopic = nextTopicForProgress(completedLessonIds)
                    val nextLesson = nextLessonForTopic(nextTopic, completedLessonIds)
                    if (nextTopic != null && nextLesson != null) {
                        push(AppRoute.LessonIntro(nextTopic.id, nextLesson.id))
                    } else {
                        push(AppRoute.Topics)
                    }
                },
                onBrowseTopicsClick = { push(AppRoute.Topics) },
                onOpenSettingsClick = { push(AppRoute.Settings) }
            )

            AppRoute.Topics -> TopicSelectionScreen(
                supportLanguage = supportLanguage,
                completedLessonIds = completedLessonIds,
                onBackClick = ::pop,
                onStartTopicClick = { topic ->
                    nextLessonForTopic(topic, completedLessonIds)?.let { lesson ->
                        push(AppRoute.LessonIntro(topic.id, lesson.id))
                    }
                }
            )

            is AppRoute.LessonIntro -> {
                val lessonRoute = resolveLessonRoute(currentRoute.topicId, currentRoute.lessonId)
                if (lessonRoute != null) {
                    LessonIntroScreen(
                        topic = lessonRoute.topic,
                        lesson = lessonRoute.lesson,
                        supportLanguage = supportLanguage,
                        onBackClick = ::pop,
                        onStartLessonClick = {
                            push(AppRoute.LessonStudy(lessonRoute.topic.id, lessonRoute.lesson.id))
                        }
                    )
                }
            }

            is AppRoute.LessonStudy -> {
                val lessonRoute = resolveLessonRoute(currentRoute.topicId, currentRoute.lessonId)
                if (lessonRoute != null) {
                    LessonStudyScreen(
                        topic = lessonRoute.topic,
                        lesson = lessonRoute.lesson,
                        supportLanguage = supportLanguage,
                        onBackClick = ::pop,
                        onCompleteClick = {
                            val updatedCompletedLessonIds = completedLessonIds + lessonRoute.lesson.id
                            completedLessonIds = updatedCompletedLessonIds
                            if (!hasPremium && !hasSeenPaywall && updatedCompletedLessonIds.size >= 2) {
                                hasSeenPaywall = true
                                push(AppRoute.Paywall(lessonRoute.topic.id, lessonRoute.lesson.id))
                            } else {
                                push(AppRoute.LessonCompletion(lessonRoute.topic.id, lessonRoute.lesson.id))
                            }
                        }
                    )
                }
            }

            is AppRoute.LessonCompletion -> {
                val lessonRoute = resolveLessonRoute(currentRoute.topicId, currentRoute.lessonId)
                if (lessonRoute != null) {
                    LessonCompletionScreen(
                        topic = lessonRoute.topic,
                        lesson = lessonRoute.lesson,
                        supportLanguage = supportLanguage,
                        completedLessonIds = completedLessonIds,
                        onBackToTopicsClick = { replace(AppRoute.Home) },
                        onContinueClick = {
                            val nextLesson = nextLessonForTopic(lessonRoute.topic, completedLessonIds)
                            if (nextLesson != null) {
                                push(AppRoute.LessonIntro(lessonRoute.topic.id, nextLesson.id))
                            } else {
                                replace(AppRoute.Home)
                            }
                        }
                    )
                }
            }

            is AppRoute.Paywall -> SoftPaywallScreen(
                supportLanguage = supportLanguage,
                completedLessons = completedLessonIds.size,
                onUnlockClick = {
                    hasPremium = true
                    replace(AppRoute.Home)
                },
                onContinueFreeClick = {
                    replace(AppRoute.LessonCompletion(currentRoute.topicId, currentRoute.lessonId))
                },
                onCloseClick = {
                    replace(AppRoute.LessonCompletion(currentRoute.topicId, currentRoute.lessonId))
                }
            )

            AppRoute.Settings -> SettingsScreen(
                selectedLanguage = supportLanguage,
                supportLanguage = supportLanguage,
                hasPremium = hasPremium,
                completedLessonIds = completedLessonIds,
                onBackClick = ::pop,
                onLanguageSaved = { language ->
                    supportLanguage = language
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
    data object Home : AppRoute
    data object Topics : AppRoute
    data class LessonIntro(val topicId: String, val lessonId: String) : AppRoute
    data class LessonStudy(val topicId: String, val lessonId: String) : AppRoute
    data class LessonCompletion(val topicId: String, val lessonId: String) : AppRoute
    data class Paywall(val topicId: String, val lessonId: String) : AppRoute
    data object Settings : AppRoute
}

private data class ResolvedLessonRoute(
    val topic: TopicContent,
    val lesson: LessonContent
)

private fun resolveLessonRoute(
    topicId: String,
    lessonId: String
): ResolvedLessonRoute? {
    val topic = MvpSeedContent.topics.firstOrNull { it.id == topicId } ?: return null
    val lesson = topic.lessons.firstOrNull { it.id == lessonId } ?: return null
    return ResolvedLessonRoute(topic = topic, lesson = lesson)
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
