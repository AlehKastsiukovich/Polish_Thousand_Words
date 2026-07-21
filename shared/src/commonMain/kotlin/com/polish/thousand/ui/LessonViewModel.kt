package com.polish.thousand.ui

import com.polish.thousand.content.AppPersistence
import com.polish.thousand.content.LessonContent
import com.polish.thousand.content.LessonItemContent
import com.polish.thousand.content.PersistedLessonPhase
import com.polish.thousand.content.PersistedLessonSession
import com.polish.thousand.content.ReviewQuality
import com.polish.thousand.content.SupportLanguage
import com.polish.thousand.content.translationForSelectedLanguage
import com.polish.thousand.core.mvi.AppDispatchers
import com.polish.thousand.core.mvi.StoreViewModel
import com.polish.thousand.core.mvi.UiEffect
import com.polish.thousand.core.mvi.UiIntent
import com.polish.thousand.core.mvi.UiState

internal enum class LessonPhase {
    Review,
    Learn,
    Practice
}

internal enum class PracticeQuestionMode {
    Read,
    Listen
}

internal data class LessonUiState(
    val sessionKey: String? = null,
    val lesson: LessonContent? = null,
    val reviewItems: List<LessonItemContent> = emptyList(),
    val reviewOnly: Boolean = false,
    val supportLanguage: SupportLanguage = SupportLanguage.Ukrainian,
    val phase: LessonPhase = LessonPhase.Learn,
    val reviewIndex: Int = 0,
    val learnIndex: Int = 0,
    val practiceIndex: Int = 0,
    val selectedAnswer: String? = null,
    val submittedAnswer: String? = null,
    val correctPracticeWordIds: Set<String> = emptySet(),
    val isReviewAnswerVisible: Boolean = false
) : UiState {
    val isReady: Boolean get() = sessionKey != null && lesson != null

    val reviewQuestionMode: PracticeQuestionMode
        get() = if (reviewIndex.isListeningReviewQuestion(reviewItems.size)) {
            PracticeQuestionMode.Listen
        } else {
            PracticeQuestionMode.Read
        }
}

private fun Int.isListeningReviewQuestion(totalReviewItems: Int): Boolean {
    val listeningQuestionCount = totalReviewItems / 5
    val firstListeningIndex = 2
    if (listeningQuestionCount == 0 || this < firstListeningIndex) return false

    // Keep the first two recall prompts text-first, then space listening prompts five words apart.
    val listeningQuestionIndex = (this - firstListeningIndex) / 5
    return (this - firstListeningIndex) % 5 == 0 &&
        listeningQuestionIndex < listeningQuestionCount
}

internal sealed interface LessonIntent : UiIntent {
    data class StartSession(
        val sessionKey: String,
        val lesson: LessonContent,
        val reviewItems: List<LessonItemContent>,
        val reviewOnly: Boolean,
        val supportLanguage: SupportLanguage
    ) : LessonIntent

    data object RevealReviewAnswer : LessonIntent
    data class SubmitReviewAnswer(val quality: ReviewQuality) : LessonIntent
    data object Previous : LessonIntent
    data object PrimaryAction : LessonIntent
    data class SelectAnswer(val answer: String) : LessonIntent
}

internal sealed interface LessonEffect : UiEffect {
    data class ReviewAnswered(
        val wordId: String,
        val quality: ReviewQuality,
        val sessionCompleted: Boolean
    ) : LessonEffect

    data class LessonCompleted(val correctWordIds: Set<String>) : LessonEffect
}

internal class LessonViewModel(
    private val persistence: AppPersistence,
    appDispatchers: AppDispatchers
) : StoreViewModel<LessonUiState, LessonIntent, LessonEffect>(
    initialUiState = LessonUiState(),
    appDispatchers = appDispatchers
) {
    override fun handleIntentAndReduce(intent: LessonIntent) {
        when (intent) {
            is LessonIntent.StartSession -> startSession(intent)
            LessonIntent.RevealReviewAnswer -> updateState {
                copy(isReviewAnswerVisible = true)
            }
            is LessonIntent.SubmitReviewAnswer -> submitReviewAnswer(intent.quality)
            LessonIntent.Previous -> previous()
            LessonIntent.PrimaryAction -> primaryAction()
            is LessonIntent.SelectAnswer -> updateState {
                if (submittedAnswer == null) copy(selectedAnswer = intent.answer) else this
            }
        }
    }

    private fun startSession(intent: LessonIntent.StartSession) {
        if (uiState.value.sessionKey == intent.sessionKey) {
            if (uiState.value.supportLanguage != intent.supportLanguage) {
                updateState { copy(supportLanguage = intent.supportLanguage) }
            }
            return
        }
        val persisted = persistence.loadLessonSession()
            ?.takeIf { it.sessionKey == intent.sessionKey }
        val initialPhase = if (intent.reviewItems.isEmpty()) LessonPhase.Learn else LessonPhase.Review
        val state = LessonUiState(
            sessionKey = intent.sessionKey,
            lesson = intent.lesson,
            reviewItems = intent.reviewItems,
            reviewOnly = intent.reviewOnly,
            supportLanguage = intent.supportLanguage,
            phase = persisted?.phase?.toLessonPhase() ?: initialPhase,
            reviewIndex = persisted?.reviewIndex
                ?.coerceIn(0, intent.reviewItems.lastIndex.coerceAtLeast(0))
                ?: 0,
            learnIndex = persisted?.learnIndex
                ?.coerceIn(0, intent.lesson.items.lastIndex)
                ?: 0,
            practiceIndex = persisted?.practiceIndex
                ?.coerceIn(0, intent.lesson.items.lastIndex)
                ?: 0,
            selectedAnswer = persisted?.selectedAnswer,
            submittedAnswer = persisted?.submittedAnswer,
            correctPracticeWordIds = persisted?.correctPracticeWordIds.orEmpty(),
            isReviewAnswerVisible = persisted?.isReviewAnswerVisible ?: false
        )
        setState(state)
    }

    private fun submitReviewAnswer(quality: ReviewQuality) {
        val state = uiState.value
        val item = state.reviewItems.getOrNull(state.reviewIndex) ?: return
        val isLast = state.reviewIndex == state.reviewItems.lastIndex
        sendEffect(
            LessonEffect.ReviewAnswered(
                wordId = item.id,
                quality = quality,
                sessionCompleted = isLast && state.reviewOnly
            )
        )
        when {
            isLast && state.reviewOnly -> persistence.saveLessonSession(null)
            isLast -> updateState {
                copy(
                    phase = LessonPhase.Learn,
                    isReviewAnswerVisible = false
                )
            }
            else -> updateState {
                copy(
                    reviewIndex = reviewIndex + 1,
                    isReviewAnswerVisible = false
                )
            }
        }
    }

    private fun previous() {
        updateState {
            val activeLesson = lesson ?: return@updateState this
            when (phase) {
                LessonPhase.Review -> this
                LessonPhase.Learn -> copy(learnIndex = (learnIndex - 1).coerceAtLeast(0))
                LessonPhase.Practice -> if (practiceIndex > 0) {
                    val currentId = activeLesson.items[practiceIndex].id
                    val previousId = activeLesson.items[practiceIndex - 1].id
                    copy(
                        practiceIndex = practiceIndex - 1,
                        selectedAnswer = null,
                        submittedAnswer = null,
                        correctPracticeWordIds = correctPracticeWordIds - currentId - previousId
                    )
                } else {
                    copy(
                        phase = LessonPhase.Learn,
                        learnIndex = activeLesson.items.lastIndex,
                        selectedAnswer = null,
                        submittedAnswer = null,
                        correctPracticeWordIds = correctPracticeWordIds - activeLesson.items.first().id
                    )
                }
            }
        }
    }

    private fun primaryAction() {
        val state = uiState.value
        val lesson = state.lesson ?: return
        when (state.phase) {
            LessonPhase.Review -> Unit
            LessonPhase.Learn -> {
                if (state.learnIndex == lesson.items.lastIndex) {
                    updateState { copy(phase = LessonPhase.Practice) }
                } else {
                    updateState { copy(learnIndex = learnIndex + 1) }
                }
            }
            LessonPhase.Practice -> {
                val item = lesson.items[state.practiceIndex]
                if (state.submittedAnswer == null) {
                    val answer = state.selectedAnswer ?: return
                    val correctAnswer = item.translationForSelectedLanguage(state.supportLanguage)
                    updateState {
                        copy(
                            submittedAnswer = answer,
                            correctPracticeWordIds = if (answer == correctAnswer) {
                                correctPracticeWordIds + item.id
                            } else {
                                correctPracticeWordIds
                            }
                        )
                    }
                } else if (state.practiceIndex == lesson.items.lastIndex) {
                    persistence.saveLessonSession(null)
                    sendEffect(LessonEffect.LessonCompleted(state.correctPracticeWordIds))
                } else {
                    updateState {
                        copy(
                            practiceIndex = practiceIndex + 1,
                            selectedAnswer = null,
                            submittedAnswer = null
                        )
                    }
                }
            }
        }
    }

    private fun updateState(reducer: LessonUiState.() -> LessonUiState) {
        val state = uiState.value.reducer()
        setState(state)
    }

    private fun setState(state: LessonUiState) {
        reduceState { state }
        state.toPersistedSessionOrNull()?.let(persistence::saveLessonSession)
    }
}

private fun LessonUiState.toPersistedSessionOrNull(): PersistedLessonSession? {
    val key = sessionKey ?: return null
    return PersistedLessonSession(
        sessionKey = key,
        phase = phase.toPersistedPhase(),
        reviewIndex = reviewIndex,
        learnIndex = learnIndex,
        practiceIndex = practiceIndex,
        selectedAnswer = selectedAnswer,
        submittedAnswer = submittedAnswer,
        correctPracticeWordIds = correctPracticeWordIds,
        isReviewAnswerVisible = isReviewAnswerVisible
    )
}

private fun LessonPhase.toPersistedPhase(): PersistedLessonPhase = when (this) {
    LessonPhase.Review -> PersistedLessonPhase.Review
    LessonPhase.Learn -> PersistedLessonPhase.Learn
    LessonPhase.Practice -> PersistedLessonPhase.Practice
}

private fun PersistedLessonPhase.toLessonPhase(): LessonPhase = when (this) {
    PersistedLessonPhase.Review -> LessonPhase.Review
    PersistedLessonPhase.Learn -> LessonPhase.Learn
    PersistedLessonPhase.Practice -> LessonPhase.Practice
}
