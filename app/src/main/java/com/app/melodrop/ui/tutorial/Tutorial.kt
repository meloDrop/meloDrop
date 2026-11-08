package com.app.melodrop.ui.tutorial

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

// Elements of the app that a step of the tour can highlight
enum class TutorialTarget {
    INFO, // "i" button of Discover
    SEARCH, // Magnifying glass of Discover
    FOLLOWS_FIRST, // "Abonnements d'abord" chip
    PICK, // Pick button under the card
    TAB_DISCOVER, // The three tabs of the bottom bar
    TAB_DROP,
    TAB_PROFILE,
}

// One step of the tour: the element to highlight, and what the bubble says about it
data class TutorialStep(
    val target: TutorialTarget,
    val title: String,
    val text: String,
)

// The tour in progress. Shared by the screens that hold the elements
// and by the overlay that draws the dark veil over the whole app
object Tutorial {

    // Place of each element on the screen, in pixels. Filled by Modifier.tutorialTarget
    val targets = mutableStateMapOf<TutorialTarget, Rect>()

    // Steps of the tour in progress: empty when there is no tour
    var steps by mutableStateOf<List<TutorialStep>>(emptyList())
        private set

    // Position of the step shown, in steps
    var index by mutableIntStateOf(0)
        private set

    // Step to show now, or null when there is no tour
    val currentStep: TutorialStep?
        get() = steps.getOrNull(index)

    // Starts the tour. The steps whose element is not on the screen
    // (for example the Pick button when the feed is empty) are left out
    fun start(newSteps: List<TutorialStep>) {
        steps = newSteps.filter { it.target in targets }
        index = 0
    }

    // "Suivant": next step, or end of the tour after the last one
    fun next() {
        if (index < steps.lastIndex) index++ else stop()
    }

    // "Passer", "Compris" or back button: the veil goes away
    fun stop() {
        steps = emptyList()
        index = 0
    }
}

// Notes where the element is on the screen, so the tour can highlight it
fun Modifier.tutorialTarget(target: TutorialTarget): Modifier =
    onGloballyPositioned { coordinates ->
        Tutorial.targets[target] = coordinates.boundsInRoot()
    }
