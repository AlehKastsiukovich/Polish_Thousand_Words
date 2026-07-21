package com.polish.thousand

import com.polish.thousand.content.ActiveSession
import com.polish.thousand.content.ActiveSessionType
import com.polish.thousand.content.AppPersistence
import com.polish.thousand.content.BootstrapLanguage
import com.polish.thousand.content.CompletionRecognition
import com.polish.thousand.content.LessonContent
import com.polish.thousand.content.LearningActivity
import com.polish.thousand.content.LearningPath
import com.polish.thousand.content.LearningTargetWords
import com.polish.thousand.content.MvpSeedContent
import com.polish.thousand.content.PendingQuickReview
import com.polish.thousand.content.ReviewQuality
import com.polish.thousand.content.ReviewSchedule
import com.polish.thousand.content.SupportLanguage
import com.polish.thousand.content.TopicContent
import com.polish.thousand.content.WordReviewState
import com.polish.thousand.content.appText
import com.polish.thousand.content.currentEpochDay
import com.polish.thousand.content.itemsByIds
import com.polish.thousand.content.initialLanguageChoice
import com.polish.thousand.core.mvi.AppDispatchers
import com.polish.thousand.core.mvi.StoreViewModel
import com.polish.thousand.core.mvi.UiEffect
import com.polish.thousand.core.mvi.UiIntent
import com.polish.thousand.core.mvi.UiState
import com.polish.thousand.payments.PaymentClient
import com.polish.thousand.payments.PaymentProduct
import com.polish.thousand.payments.PaymentResult

internal data class AppUiState(
    val supportLanguage: SupportLanguage,
    val hasSelectedLanguage: Boolean,
    val bootstrapLanguage: BootstrapLanguage,
    val suggestedSupportLanguage: SupportLanguage?,
    val completedLessonIds: Set<String>,
    val learnedWordIds: Set<String>,
    val reviewStates: Map<String, WordReviewState>,
    val pendingQuickReview: PendingQuickReview?,
    val hasPremium: Boolean,
    val hasSeenPaywall: Boolean,
    val activeDays: Set<Long>,
    val fullUnlockProduct: PaymentProduct?,
    val isPaymentInProgress: Boolean,
    val paymentMessage: String?,
    val todayEpochDay: Long,
    val restoredSession: ActiveSession?,
    val backStack: List<AppRoute> = listOf(AppRoute.Splash)
) : UiState {
    val currentRoute: AppRoute get() = backStack.last()
    val nextLesson: LessonContent? get() = MvpSeedContent.nextLesson(completedLessonIds)
    // Only words the learner has actually completed belong to scheduled review.
    // Older app versions could retain states for incorrect lesson answers.
    private val learnedReviewStates: Map<String, WordReviewState>
        get() = reviewStates.filterKeys(learnedWordIds::contains)
    val dueReviewCount: Int get() = ReviewSchedule.dueWordCount(learnedReviewStates, todayEpochDay)
    val dueReviewWordIds: List<String> get() = ReviewSchedule.dueWordIds(learnedReviewStates, todayEpochDay)
    val dueReviewItems get() = MvpSeedContent.lessons.itemsByIds(dueReviewWordIds)
    val quickReviewWordIds: List<String> get() = pendingQuickReview
        ?.wordIds
        .orEmpty()
        .sorted()
    val quickReviewItems get() = MvpSeedContent.lessons.itemsByIds(quickReviewWordIds)
    val reviewHostLesson: LessonContent? get() = MvpSeedContent.lessons.firstOrNull { lesson ->
        lesson.items.any { it.id in dueReviewWordIds }
    }
    val quickReviewHostLesson: LessonContent? get() = pendingQuickReview?.let { review ->
        MvpSeedContent.lessons.firstOrNull { it.id == review.lessonId }
            ?: MvpSeedContent.lessons.firstOrNull { lesson ->
                lesson.items.any { it.id in review.wordIds }
            }
    }
    val activityOverview get() = LearningActivity.overview(
        activeDays = activeDays,
        todayEpochDay = todayEpochDay
    )
}

internal sealed interface AppIntent : UiIntent {
    data object SplashFinished : AppIntent
    data object RefreshCurrentDay : AppIntent
    data class SelectLanguage(val language: SupportLanguage) : AppIntent
    data object Back : AppIntent
    data object ContinueFromHome : AppIntent
    data object OpenDueReview : AppIntent
    data object OpenQuickReview : AppIntent
    data object OpenFreeReview : AppIntent
    data object OpenSettings : AppIntent
    data class ChangeSupportLanguage(val language: SupportLanguage) : AppIntent
    data class LessonCompleted(val correctWordIds: Set<String>) : AppIntent
    data class ScheduledReviewAnswered(val wordId: String, val quality: ReviewQuality) : AppIntent
    data class QuickReviewAnswered(val wordId: String, val quality: ReviewQuality) : AppIntent
    data class FreeReviewAnswered(val wordId: String, val quality: ReviewQuality) : AppIntent
    data object ReviewCompleted : AppIntent
    data object QuickReviewCompleted : AppIntent
    data object FreeReviewCompleted : AppIntent
    data object OpenCompletionQuickReview : AppIntent
    data object ContinueFromAchievement : AppIntent
    data object ContinueFromCompletion : AppIntent
    data object ReturnHomeFromCompletion : AppIntent
    data object PurchaseFullUnlock : AppIntent
    data object RestorePurchases : AppIntent
    data object DismissPaymentMessage : AppIntent
    data object ContinueFree : AppIntent
    data object ClosePaywall : AppIntent
    data object ResetProgress : AppIntent
}

internal sealed interface AppEffect : UiEffect

internal class AppViewModel(
    private val persistence: AppPersistence,
    private val paymentClient: PaymentClient,
    appDispatchers: AppDispatchers
) : StoreViewModel<AppUiState, AppIntent, AppEffect>(
    initialUiState = initialAppState(persistence),
    appDispatchers = appDispatchers
) {
    override fun handleIntentAndReduce(intent: AppIntent) {
        when (intent) {
            AppIntent.SplashFinished -> finishSplash()
            AppIntent.RefreshCurrentDay -> refreshCurrentDay()
            is AppIntent.SelectLanguage -> selectLanguage(intent.language)
            AppIntent.Back -> navigateBack()
            AppIntent.ContinueFromHome -> continueFromHome()
            AppIntent.OpenDueReview -> openDueReview()
            AppIntent.OpenQuickReview -> openQuickReview()
            AppIntent.OpenFreeReview -> openFreeReview()
            AppIntent.OpenSettings -> push(AppRoute.Settings)
            is AppIntent.ChangeSupportLanguage -> changeSupportLanguage(intent.language)
            is AppIntent.LessonCompleted -> completeLesson(intent.correctWordIds)
            is AppIntent.ScheduledReviewAnswered -> recordReviewAnswer(
                wordId = intent.wordId,
                quality = intent.quality,
                isEarlyReview = false
            )
            is AppIntent.QuickReviewAnswered -> recordReviewAnswer(
                wordId = intent.wordId,
                quality = intent.quality,
                isEarlyReview = true
            )
            is AppIntent.FreeReviewAnswered -> recordReviewAnswer(
                wordId = intent.wordId,
                quality = intent.quality,
                isEarlyReview = true,
                updatesPendingQuickReview = false
            )
            AppIntent.ReviewCompleted -> registerActivityAndReset()
            AppIntent.QuickReviewCompleted -> completeQuickReview()
            AppIntent.FreeReviewCompleted -> registerActivityAndReset()
            AppIntent.OpenCompletionQuickReview -> openCompletionQuickReview()
            AppIntent.ContinueFromAchievement -> continueFromAchievement()
            AppIntent.ContinueFromCompletion -> continueFromCompletion()
            AppIntent.ReturnHomeFromCompletion -> resetToWelcome(clearSession = true)
            AppIntent.PurchaseFullUnlock -> purchaseFullUnlock()
            AppIntent.RestorePurchases -> restorePurchases()
            AppIntent.DismissPaymentMessage -> setState(uiState.value.copy(paymentMessage = null))
            AppIntent.ContinueFree -> continueFree()
            AppIntent.ClosePaywall -> closePaywall()
            AppIntent.ResetProgress -> resetProgress()
        }
    }

    init {
        reduceAsync(
            operation = {
                PaymentStartupState(
                    product = paymentClient.loadFullUnlockProduct(),
                    isFullUnlockActive = paymentClient.isFullUnlockActive()
                )
            },
            onSuccess = { state, paymentState ->
                if (paymentState.isFullUnlockActive) {
                    persistence.saveHasPremium(true)
                }
                state.copy(
                    fullUnlockProduct = paymentState.product,
                    hasPremium = state.hasPremium || paymentState.isFullUnlockActive
                )
            }
        )
    }

    private fun finishSplash() {
        val state = uiState.value
        val restoredRoute = state.restoredSession
            ?.toRoute(currentLearnedWords = state.learnedWordIds.size)
            ?.takeIf(::isValidSessionRoute)
        val target = restoredRoute
            ?: if (state.hasSelectedLanguage) AppRoute.Welcome else AppRoute.LanguageSelection
        val restoredBackStack = if (restoredRoute != null) {
            listOf(AppRoute.Welcome, restoredRoute)
        } else {
            listOf(target)
        }
        setState(state.copy(restoredSession = null, backStack = restoredBackStack))
        if (restoredRoute == null) persistence.saveActiveSession(null)
    }

    private fun refreshCurrentDay() {
        val today = currentEpochDay()
        val state = uiState.value
        if (state.todayEpochDay != today) {
            setState(state.copy(todayEpochDay = today))
        }
    }

    private fun selectLanguage(language: SupportLanguage) {
        persistence.saveSupportLanguage(language)
        setState(
            uiState.value.copy(
                supportLanguage = language,
                hasSelectedLanguage = true,
                backStack = listOf(AppRoute.Welcome)
            )
        )
    }

    private fun continueFromHome() {
        val state = uiState.value
        val nextLesson = state.nextLesson
        when {
            nextLesson != null -> openLesson(nextLesson)
            state.dueReviewItems.isNotEmpty() -> openDueReview()
        }
    }

    private fun openDueReview() {
        val state = uiState.value
        val hostLesson = state.reviewHostLesson ?: return
        if (state.dueReviewWordIds.isEmpty()) return
        push(
            AppRoute.ReviewOnly(
                topicId = MvpSeedContent.path.id,
                lessonId = hostLesson.id,
                wordIds = state.dueReviewWordIds
            )
        )
    }

    private fun openQuickReview() {
        val state = uiState.value
        val hostLesson = state.quickReviewHostLesson ?: return
        if (state.quickReviewWordIds.isEmpty()) return
        push(
            AppRoute.QuickReview(
                topicId = MvpSeedContent.path.id,
                lessonId = hostLesson.id,
                wordIds = state.quickReviewWordIds,
                learnedWordsBeforeReview = state.learnedWordIds.size
            )
        )
    }

    private fun openFreeReview() {
        val state = uiState.value
        if (state.learnedWordIds.size < LearningTargetWords) return
        val wordIds = state.learnedWordIds.sorted().shuffled().take(10)
        val hostLesson = MvpSeedContent.lessons.firstOrNull { lesson ->
            lesson.items.any { it.id in wordIds }
        } ?: return
        push(
            AppRoute.FreeReview(
                topicId = MvpSeedContent.path.id,
                lessonId = hostLesson.id,
                wordIds = wordIds
            )
        )
    }

    private fun changeSupportLanguage(language: SupportLanguage) {
        if (uiState.value.supportLanguage == language) return
        persistence.saveSupportLanguage(language)
        setState(uiState.value.copy(supportLanguage = language))
    }

    private fun completeLesson(correctWordIds: Set<String>) {
        val state = uiState.value
        val route = state.currentRoute as? AppRoute.LessonStudy ?: return
        val lessonRoute = resolveLessonRoute(route.topicId, route.lessonId) ?: return
        val previousLearnedWords = state.learnedWordIds.size
        val addedWordIds = correctWordIds - state.learnedWordIds
        val learnedWordIds = state.learnedWordIds + correctWordIds
        val completedLessonIds = state.completedLessonIds + lessonRoute.lesson.id
        val reviewStates = ReviewSchedule.scheduleNewWords(
            states = state.reviewStates.filterKeys(learnedWordIds::contains),
            words = lessonRoute.lesson.items.filter { it.id in correctWordIds },
            todayEpochDay = state.todayEpochDay
        )
        val lessonWordIds = lessonRoute.lesson.items.mapTo(mutableSetOf()) { it.id }
        val pendingWordIds = (
            state.pendingQuickReview?.wordIds.orEmpty() +
                (lessonWordIds - correctWordIds)
            ) - learnedWordIds
        val pendingQuickReview = pendingWordIds
            .takeIf { it.isNotEmpty() }
            ?.let { wordIds ->
                PendingQuickReview(
                    lessonId = if ((lessonWordIds - correctWordIds).isNotEmpty()) {
                        lessonRoute.lesson.id
                    } else {
                        state.pendingQuickReview?.lessonId ?: lessonRoute.lesson.id
                    },
                    wordIds = wordIds,
                    createdEpochDay = state.todayEpochDay
                )
            }

        val activeDays = state.activeDays.markActive(state.todayEpochDay)

        persistence.saveLearnedWordIds(learnedWordIds)
        persistence.saveCompletedLessonIds(completedLessonIds)
        persistence.saveReviewStates(reviewStates)
        persistence.savePendingQuickReview(pendingQuickReview)
        persistence.saveActiveDays(activeDays)
        clearActiveSession()

        val crossedMilestone = LearningPath.crossedCelebrationMilestone(
            previousLearnedWords = previousLearnedWords,
            learnedWords = learnedWordIds.size
        )
        val completionRecognition = LearningPath.completionRecognition(
            previousLearnedWords = previousLearnedWords,
            learnedWords = learnedWordIds.size
        )
        val shouldShowPaywall = !state.hasPremium &&
            !state.hasSeenPaywall &&
            learnedWordIds.size >= FreeWordLimit
        if (shouldShowPaywall) persistence.saveHasSeenPaywall(true)
        val lessonResultRoute: LessonResultRoute = if (shouldShowPaywall) {
            LessonResultRoute.Paywall(
                topicId = route.topicId,
                lessonId = route.lessonId,
                addedWords = addedWordIds.size,
                attemptedWords = lessonRoute.lesson.items.size
            )
        } else {
            LessonResultRoute.Completion(
                topicId = route.topicId,
                lessonId = route.lessonId,
                addedWords = addedWordIds.size,
                attemptedWords = lessonRoute.lesson.items.size,
                recognition = completionRecognition
            )
        }
        val nextRoute = crossedMilestone
            ?.let { milestone ->
                AppRoute.AchievementCelebration(
                    milestoneWordCount = milestone.wordCount,
                    nextRoute = lessonResultRoute.toAppRoute()
                )
            }
            ?: lessonResultRoute.toAppRoute()
        setState(
            state.copy(
                learnedWordIds = learnedWordIds,
                completedLessonIds = completedLessonIds,
                reviewStates = reviewStates,
                pendingQuickReview = pendingQuickReview,
                activeDays = activeDays,
                hasSeenPaywall = state.hasSeenPaywall || shouldShowPaywall,
                backStack = state.backStack + nextRoute
            )
        )
    }

    private fun recordReviewAnswer(
        wordId: String,
        quality: ReviewQuality,
        isEarlyReview: Boolean,
        updatesPendingQuickReview: Boolean = true
    ) {
        val state = uiState.value
        val reviewStates = if (isEarlyReview) {
            ReviewSchedule.recordEarlyAnswer(
                states = state.reviewStates,
                wordId = wordId,
                quality = quality,
                todayEpochDay = state.todayEpochDay
            )
        } else {
            ReviewSchedule.recordScheduledAnswer(
                states = state.reviewStates,
                wordId = wordId,
                quality = quality,
                todayEpochDay = state.todayEpochDay
            )
        }
        val learnedWordIds = if (quality == ReviewQuality.Know) {
            state.learnedWordIds + wordId
        } else {
            state.learnedWordIds
        }
        val pendingQuickReview = state.pendingQuickReview?.let { pending ->
            if (!updatesPendingQuickReview) return@let pending
            if (quality != ReviewQuality.Know || wordId !in pending.wordIds) {
                pending
            } else {
                pending.copy(wordIds = pending.wordIds - wordId)
                    .takeIf { it.wordIds.isNotEmpty() }
            }
        }
        persistence.saveReviewStates(reviewStates)
        if (learnedWordIds !== state.learnedWordIds) {
            persistence.saveLearnedWordIds(learnedWordIds)
        }
        if (pendingQuickReview != state.pendingQuickReview) {
            persistence.savePendingQuickReview(pendingQuickReview)
        }
        setState(
            state.copy(
                reviewStates = reviewStates,
                learnedWordIds = learnedWordIds,
                pendingQuickReview = pendingQuickReview
            )
        )
    }

    private fun completeQuickReview() {
        val state = uiState.value
        val reviewRoute = state.currentRoute as? AppRoute.QuickReview
        val crossedMilestone = reviewRoute?.let {
            LearningPath.crossedCelebrationMilestone(
                previousLearnedWords = it.learnedWordsBeforeReview,
                learnedWords = state.learnedWordIds.size
            )
        }
        val activeDays = state.activeDays.markActive(state.todayEpochDay)
        persistence.saveActiveDays(activeDays)
        clearActiveSession()

        if (crossedMilestone == null) {
            setState(
                state.copy(
                    activeDays = activeDays,
                    backStack = listOf(AppRoute.Welcome)
                )
            )
            return
        }

        setState(
            state.copy(
                activeDays = activeDays,
                backStack = listOf(
                    AppRoute.AchievementCelebration(
                        milestoneWordCount = crossedMilestone.wordCount,
                        nextRoute = AppRoute.Welcome
                    )
                )
            )
        )
    }

    private fun openCompletionQuickReview() {
        if (uiState.value.quickReviewItems.isNotEmpty()) openQuickReview()
        else resetToWelcome(clearSession = true)
    }

    private fun continueFromAchievement() {
        val route = uiState.value.currentRoute as? AppRoute.AchievementCelebration ?: return
        if (route.milestoneWordCount >= LearningPath.milestones.last().wordCount) {
            resetToWelcome(clearSession = true)
        } else {
            replace(route.nextRoute)
        }
    }

    private fun continueFromCompletion() {
        val nextLesson = uiState.value.nextLesson
        if (nextLesson == null) resetToWelcome(clearSession = true)
        else openLesson(nextLesson, clearHistory = true)
    }

    private fun purchaseFullUnlock() {
        reduceAsync(
            onLoading = {
                it.copy(
                    isPaymentInProgress = true,
                    paymentMessage = null
                )
            },
            operation = paymentClient::purchaseFullUnlock,
            onSuccess = { state, result ->
                handlePaymentResult(state, result)
            },
            onError = { state, _ ->
                state.copy(
                    isPaymentInProgress = false,
                    paymentMessage = state.supportLanguage.appText.paymentFailed
                )
            }
        )
    }

    private fun restorePurchases() {
        reduceAsync(
            onLoading = {
                it.copy(
                    isPaymentInProgress = true,
                    paymentMessage = null
                )
            },
            operation = paymentClient::restoreFullUnlock,
            onSuccess = { state, result ->
                handlePaymentResult(state, result)
            },
            onError = { state, _ ->
                state.copy(
                    isPaymentInProgress = false,
                    paymentMessage = state.supportLanguage.appText.paymentFailed
                )
            }
        )
    }

    private fun handlePaymentResult(state: AppUiState, result: PaymentResult): AppUiState {
        return when (result) {
            PaymentResult.Purchased,
            PaymentResult.Restored -> {
                persistence.saveHasPremium(true)
                val nextState = stateAfterContinueFree(
                    state.copy(
                        hasPremium = true,
                        isPaymentInProgress = false,
                        paymentMessage = null
                    )
                )
                persistActiveRoute(nextState.currentRoute)
                nextState
            }
            PaymentResult.Cancelled -> state.copy(
                isPaymentInProgress = false,
                paymentMessage = null
            )
            is PaymentResult.Unavailable -> state.copy(
                isPaymentInProgress = false,
                paymentMessage = state.supportLanguage.appText.storeSetupRequired
            )
            is PaymentResult.Failed -> state.copy(
                isPaymentInProgress = false,
                paymentMessage = state.supportLanguage.appText.paymentFailed
            )
        }
    }

    private fun stateAfterContinueFree(state: AppUiState): AppUiState {
        return when (val route = state.currentRoute) {
            is AppRoute.Paywall -> state.copy(
                backStack = state.backStack.dropLast(1) + AppRoute.LessonCompletion(
                    topicId = route.topicId,
                    lessonId = route.lessonId,
                    addedWords = route.addedWords,
                    attemptedWords = route.attemptedWords
                )
            )
            is AppRoute.PaywallGate -> state.copy(
                backStack = listOf(AppRoute.Welcome)
            )
            else -> state
        }
    }

    private fun continueFree() {
        val nextState = stateAfterContinueFree(uiState.value)
        setState(nextState)
        persistActiveRoute(nextState.currentRoute)
    }

    private fun closePaywall() {
        when (val route = uiState.value.currentRoute) {
            is AppRoute.Paywall -> continueFree()
            is AppRoute.PaywallGate -> resetToWelcome(clearSession = true)
            else -> Unit
        }
    }

    private fun openLesson(lesson: LessonContent, clearHistory: Boolean = false) {
        openLessonRoute(
            route = AppRoute.LessonStudy(MvpSeedContent.path.id, lesson.id),
            clearHistory = clearHistory,
            applyGate = true
        )
    }

    private fun openLessonRoute(
        route: AppRoute.LessonStudy,
        clearHistory: Boolean,
        applyGate: Boolean
    ) {
        val state = uiState.value
        val target = if (applyGate && !state.hasPremium && state.learnedWordIds.size >= FreeWordLimit) {
            AppRoute.PaywallGate(route.topicId, route.lessonId)
        } else {
            route
        }
        if (clearHistory) replaceStackWith(target) else push(target)
    }

    private fun navigateBack() {
        val state = uiState.value
        if (state.backStack.size <= 1) return
        val leavingSession = state.currentRoute.isLearningSession
        val backStack = state.backStack.dropLast(1)
        setState(state.copy(backStack = backStack))
        if (leavingSession) clearActiveSession()
        else persistActiveRoute(backStack.last())
    }

    private fun push(route: AppRoute) {
        val state = uiState.value
        setState(state.copy(backStack = state.backStack + route))
        persistActiveRoute(route)
    }

    private fun replace(route: AppRoute) {
        val state = uiState.value
        setState(state.copy(backStack = state.backStack.dropLast(1) + route))
        persistActiveRoute(route)
    }

    private fun replaceStackWith(route: AppRoute) {
        setState(uiState.value.copy(backStack = listOf(AppRoute.Welcome, route)))
        persistActiveRoute(route)
    }

    private fun resetToWelcome(
        clearSession: Boolean,
        pendingQuickReview: PendingQuickReview? = uiState.value.pendingQuickReview
    ) {
        setState(
            uiState.value.copy(
                pendingQuickReview = pendingQuickReview,
                backStack = listOf(AppRoute.Welcome)
            )
        )
        if (clearSession) clearActiveSession()
    }

    private fun registerActivityAndReset() {
        val state = uiState.value
        val activeDays = state.activeDays.markActive(state.todayEpochDay)
        persistence.saveActiveDays(activeDays)
        resetToWelcome(clearSession = true)
        setState(uiState.value.copy(activeDays = activeDays))
    }

    private fun resetProgress() {
        val state = uiState.value
        persistence.saveCompletedLessonIds(emptySet())
        persistence.saveLearnedWordIds(emptySet())
        persistence.saveReviewStates(emptyMap())
        persistence.savePendingQuickReview(null)
        persistence.saveActiveDays(emptySet())
        clearActiveSession()
        setState(
            state.copy(
                completedLessonIds = emptySet(),
                learnedWordIds = emptySet(),
                reviewStates = emptyMap(),
                pendingQuickReview = null,
                activeDays = emptySet(),
                backStack = listOf(AppRoute.Welcome)
            )
        )
    }

    private fun setState(state: AppUiState) {
        reduceState { state }
    }

    private fun persistActiveRoute(route: AppRoute) {
        persistence.saveActiveSession(route.toActiveSessionOrNull())
    }

    private fun clearActiveSession() {
        persistence.saveActiveSession(null)
        persistence.saveLessonSession(null)
    }

    private fun isValidSessionRoute(route: AppRoute): Boolean = when (route) {
        is AppRoute.LessonStudy -> resolveLessonRoute(route.topicId, route.lessonId) != null
        is AppRoute.ReviewOnly -> resolveLessonRoute(route.topicId, route.lessonId) != null &&
            MvpSeedContent.lessons.itemsByIds(route.wordIds).isNotEmpty()
        is AppRoute.QuickReview -> resolveLessonRoute(route.topicId, route.lessonId) != null &&
            MvpSeedContent.lessons.itemsByIds(route.wordIds).isNotEmpty()
        is AppRoute.FreeReview -> resolveLessonRoute(route.topicId, route.lessonId) != null &&
            MvpSeedContent.lessons.itemsByIds(route.wordIds).isNotEmpty()
        else -> false
    }
}

internal sealed interface AppRoute {
    data object Splash : AppRoute
    data object LanguageSelection : AppRoute
    data object Welcome : AppRoute
    data class LessonStudy(val topicId: String, val lessonId: String) : AppRoute
    data class ReviewOnly(
        val topicId: String,
        val lessonId: String,
        val wordIds: List<String>
    ) : AppRoute
    data class QuickReview(
        val topicId: String,
        val lessonId: String,
        val wordIds: List<String>,
        val learnedWordsBeforeReview: Int
    ) : AppRoute
    data class FreeReview(
        val topicId: String,
        val lessonId: String,
        val wordIds: List<String>
    ) : AppRoute
    data class AchievementCelebration(
        val milestoneWordCount: Int,
        val nextRoute: AppRoute
    ) : AppRoute
    data class LessonCompletion(
        val topicId: String,
        val lessonId: String,
        val addedWords: Int,
        val attemptedWords: Int,
        val recognition: CompletionRecognition? = null
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

internal sealed interface LessonResultRoute {
    data class Completion(
        val topicId: String,
        val lessonId: String,
        val addedWords: Int,
        val attemptedWords: Int,
        val recognition: CompletionRecognition? = null
    ) : LessonResultRoute

    data class Paywall(
        val topicId: String,
        val lessonId: String,
        val addedWords: Int,
        val attemptedWords: Int
    ) : LessonResultRoute
}

internal fun LessonResultRoute.toAppRoute(): AppRoute = when (this) {
    is LessonResultRoute.Completion -> AppRoute.LessonCompletion(
        topicId = topicId,
        lessonId = lessonId,
        addedWords = addedWords,
        attemptedWords = attemptedWords,
        recognition = recognition
    )
    is LessonResultRoute.Paywall -> AppRoute.Paywall(
        topicId = topicId,
        lessonId = lessonId,
        addedWords = addedWords,
        attemptedWords = attemptedWords
    )
}

internal data class ResolvedLessonRoute(
    val topic: TopicContent,
    val lesson: LessonContent
)

private data class PaymentStartupState(
    val product: PaymentProduct,
    val isFullUnlockActive: Boolean
)

internal fun resolveLessonRoute(topicId: String, lessonId: String): ResolvedLessonRoute? {
    val topic = MvpSeedContent.path.takeIf { it.id == topicId } ?: return null
    val lesson = topic.lessons.firstOrNull { it.id == lessonId } ?: return null
    return ResolvedLessonRoute(topic = topic, lesson = lesson)
}

private fun initialAppState(persistence: AppPersistence): AppUiState {
    val language = persistence.loadSupportLanguage()
    val initialLanguageChoice = initialLanguageChoice()
    val today = currentEpochDay()
    val completedLessonIds = persistence.loadCompletedLessonIds()
    val learnedWordIds = persistence.loadLearnedWordIds()
    val storedPendingReview = persistence.loadPendingQuickReview()
    val unresolvedLessons = MvpSeedContent.lessons.filter { lesson ->
        lesson.id in completedLessonIds && lesson.items.any { it.id !in learnedWordIds }
    }
    val unresolvedWordIds = unresolvedLessons
        .flatMapTo(mutableSetOf()) { lesson -> lesson.items.map { it.id } } - learnedWordIds
    val knownWordIds = MvpSeedContent.lessons
        .flatMapTo(mutableSetOf()) { lesson -> lesson.items.map { it.id } }
    val pendingWordIds = ((storedPendingReview?.wordIds.orEmpty() + unresolvedWordIds) - learnedWordIds)
        .intersect(knownWordIds)
    val pendingReview = pendingWordIds
        .takeIf { it.isNotEmpty() }
        ?.let { wordIds ->
            PendingQuickReview(
                lessonId = storedPendingReview?.lessonId
                    ?: unresolvedLessons.last().id,
                wordIds = wordIds,
                createdEpochDay = storedPendingReview?.createdEpochDay ?: today
            )
        }
    if (pendingReview != storedPendingReview) {
        persistence.savePendingQuickReview(pendingReview)
    }
    val storedReviewStates = persistence.loadReviewStates()
    val reviewStates = storedReviewStates.filterKeys(learnedWordIds::contains)
    if (reviewStates.size != storedReviewStates.size) {
        persistence.saveReviewStates(reviewStates)
    }
    return AppUiState(
        supportLanguage = language
            ?: initialLanguageChoice.suggestedSupportLanguage
            ?: SupportLanguage.Russian,
        hasSelectedLanguage = language != null,
        bootstrapLanguage = initialLanguageChoice.interfaceLanguage,
        suggestedSupportLanguage = initialLanguageChoice.suggestedSupportLanguage,
        completedLessonIds = completedLessonIds,
        learnedWordIds = learnedWordIds,
        reviewStates = reviewStates,
        pendingQuickReview = pendingReview,
        hasPremium = persistence.loadHasPremium(),
        hasSeenPaywall = persistence.loadHasSeenPaywall(),
        activeDays = persistence.loadActiveDays(),
        fullUnlockProduct = null,
        isPaymentInProgress = false,
        paymentMessage = null,
        todayEpochDay = today,
        restoredSession = persistence.loadActiveSession()
    )
}

private val AppRoute.isLearningSession: Boolean
    get() = this is AppRoute.LessonStudy || this is AppRoute.ReviewOnly || this is AppRoute.QuickReview || this is AppRoute.FreeReview

private fun AppRoute.toActiveSessionOrNull(): ActiveSession? = when (this) {
    is AppRoute.LessonStudy -> ActiveSession(ActiveSessionType.Lesson, topicId, lessonId)
    is AppRoute.ReviewOnly -> ActiveSession(ActiveSessionType.ScheduledReview, topicId, lessonId, wordIds)
    is AppRoute.QuickReview -> ActiveSession(
        type = ActiveSessionType.QuickReview,
        topicId = topicId,
        lessonId = lessonId,
        wordIds = wordIds,
        learnedWordsBeforeReview = learnedWordsBeforeReview
    )
    is AppRoute.FreeReview -> ActiveSession(
        type = ActiveSessionType.FreeReview,
        topicId = topicId,
        lessonId = lessonId,
        wordIds = wordIds
    )
    else -> null
}

private fun ActiveSession.toRoute(currentLearnedWords: Int): AppRoute = when (type) {
    ActiveSessionType.Lesson -> AppRoute.LessonStudy(topicId, lessonId)
    ActiveSessionType.ScheduledReview -> AppRoute.ReviewOnly(topicId, lessonId, wordIds)
    ActiveSessionType.QuickReview -> AppRoute.QuickReview(
        topicId = topicId,
        lessonId = lessonId,
        wordIds = wordIds,
        learnedWordsBeforeReview = learnedWordsBeforeReview ?: currentLearnedWords
    )
    ActiveSessionType.FreeReview -> AppRoute.FreeReview(topicId, lessonId, wordIds)
}

private fun Set<Long>.markActive(todayEpochDay: Long): Set<Long> =
    (this + todayEpochDay).filterTo(mutableSetOf()) { it >= todayEpochDay - 60 }

private const val FreeWordLimit = 100
