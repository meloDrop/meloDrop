package com.app.melodrop.ui.tutorial

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import com.app.melodrop.data.TutorialStore
import com.app.melodrop.ui.navigation.Routes

// The tabs that have a tutorial: the label shown in the bubble, and the route of the tab
enum class TutorialScreen(val label: String, val route: String) {
    DISCOVER("DISCOVER", Routes.DISCOVER),
    DROP("DROP", Routes.DROP),
    PROFILE("PROFIL", Routes.PROFILE),
}

// Elements of the example screens that a step of the tutorial can highlight
enum class TutorialTarget {
    // Discover
    CARD, // The card on top, that shows the swipes
    CHOICE_BUTTONS, // Passer, Pick and J'aime under the card
    PICK, // Pick button
    CARD_PEOPLE, // "3 personnes ont droppé cette chanson" and the likes, at the bottom of the card
    FOLLOWS_FIRST, // "Abonnements d'abord" chip
    SEARCH, // Magnifying glass
    INFO, // "i" button

    // Drop
    DROP_SEARCH, // Search field
    DROP_RESULT, // First result of the search
    DROP_CONFIRM, // DROP button
    DROP_STATS, // Likes, picks and views of the drop of the day
    DROP_TIMER, // Time left before the drop expires

    // Profile
    PROFILE_STATS, // The three counters
    PROFILE_SKIPS, // "Mes skips" link
    PROFILE_TABS, // "Mes drops" and "Mes picks" tabs
    PROFILE_PAGES, // ACTIVITÉ and MIXTAPE labels
    PODIUM, // The 3 favorite songs of the Mixtape
    LABELS, // The songs under a label
    BADGES, // The badges

    // The three tabs of the bottom bar
    TAB_DISCOVER,
    TAB_DROP,
    TAB_PROFILE,
}

// One step of the tutorial: the element to highlight, and what the bubble says about it.
// veilAlpha: how dark the veil is. Lighter when the element moves, like the card that swipes
data class TutorialStep(
    val target: TutorialTarget,
    val title: String,
    val text: String,
    val veilAlpha: Float = 0.8f,
)

// True inside the example screens drawn by the tutorial. The real screens use the same
// composables, but their elements must not be noted: only the example ones are highlighted
val LocalTutorialDemo = staticCompositionLocalOf { false }

// The tutorial in progress. Shared by the tabs, that start it on their first visit,
// and by the overlay, that draws the example screen and the veil over the whole app
object Tutorial {

    // Place of each element of the example screen, in pixels. Filled by Modifier.tutorialTarget
    val targets = mutableStateMapOf<TutorialTarget, Rect>()

    // Tab whose tutorial is shown, null when there is no tutorial
    var screen by mutableStateOf<TutorialScreen?>(null)
        private set

    // Position of the step shown, in the steps of the tab
    var index by mutableIntStateOf(0)
        private set

    // Tabs still to show after this one: "Revoir le tuto" shows the three in a row
    private var nextScreens: List<TutorialScreen> = emptyList()

    // Steps of the tab shown, empty when there is no tutorial
    val steps: List<TutorialStep>
        get() = screen?.let { stepsFor(it) } ?: emptyList()

    // Step to show now, or null when there is no tutorial
    val currentStep: TutorialStep?
        get() = steps.getOrNull(index)

    // True on the very last step: the button says "Compris" and closes everything
    val isLastStep: Boolean
        get() = index == steps.lastIndex && nextScreens.isEmpty()

    // First visit of a tab: its tutorial only
    fun start(screen: TutorialScreen) {
        nextScreens = emptyList()
        show(screen)
    }

    // "Revoir le tuto": Discover, then Drop, then Profil
    fun startAll() {
        nextScreens = listOf(TutorialScreen.DROP, TutorialScreen.PROFILE)
        show(TutorialScreen.DISCOVER)
    }

    // The places of the old example screen are forgotten: the new one notes its own
    private fun show(screen: TutorialScreen) {
        targets.clear()
        this.screen = screen
        index = 0
    }

    // "Suivant": next step, then the next tab, then the end
    fun next() {
        if (index < steps.lastIndex) {
            index++
            return
        }
        val nextScreen = nextScreens.firstOrNull()
        if (nextScreen != null) {
            nextScreens = nextScreens.drop(1)
            show(nextScreen)
        } else {
            stop()
        }
    }

    // "Passer", "Compris" or back button: the tutorial closes, the chain too
    fun stop() {
        nextScreens = emptyList()
        screen = null
        index = 0
    }
}

// Notes where the element is on the example screen, so the tutorial can highlight it,
// and scrolls it into view when its step comes. Does nothing on the real screens.
// OptIn: bringIntoViewRequester was marked as experimental in older versions of Compose
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.tutorialTarget(target: TutorialTarget): Modifier = composed {
    // A real screen: nothing to note
    if (!LocalTutorialDemo.current) return@composed Modifier

    // Scrolls the page (Drop, Profil) so the element of the step is on the screen
    val requester = remember { BringIntoViewRequester() }
    val isCurrent = Tutorial.currentStep?.target == target
    LaunchedEffect(isCurrent) {
        if (isCurrent) {
            // Waits for the next frame: an element that just appeared (new page, new state
            // of the example) must be laid out before the page can scroll to it
            withFrameNanos { }
            requester.bringIntoView()
        }
    }

    Modifier
        .bringIntoViewRequester(requester)
        .onGloballyPositioned { coordinates ->
            Tutorial.targets[target] = coordinates.boundsInRoot()
        }
}

// Opens the tutorial of a tab the first time it shows on this phone, or after a new account
@Composable
fun FirstVisitTutorial(screen: TutorialScreen) {
    val context: Context = LocalContext.current.applicationContext
    LaunchedEffect(screen) {
        // Another tutorial is open: this one waits for the next visit of the tab
        if (Tutorial.screen != null) return@LaunchedEffect
        if (TutorialStore.checkFirstVisit(context, screen.name)) {
            Tutorial.start(screen)
        }
    }
}
