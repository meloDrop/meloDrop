package com.app.melodrop.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.melodrop.ui.theme.MeloDropTheme
import com.app.melodrop.ui.theme.Rose
import kotlinx.coroutines.launch

// The two pages of a profile, with the text of their label
private enum class ProfilePage(val label: String) {
    MIXTAPE("MIXTAPE"),
    ACTIVITY("ACTIVITÉ"),
}

private const val PAGE_COUNT = 2

// Width of one label: the line under the labels moves by this much from one page to the other
private val LABEL_WIDTH = 100.dp

// Width of the line under the selected label
private val LINE_WIDTH = 24.dp

// The labels MIXTAPE and ACTIVITÉ, and the two pages that slide under them.
// Shared by the user's own profile and the profile of the others
@Composable
internal fun ProfilePages(
    // Color of the line under the selected label
    accent: Color,
    // Content of each page: each one scrolls on its own
    mixtape: @Composable () -> Unit,
    activity: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    // The page that opens first, on the left: the Mixtape on the profile of the others,
    // the activity on the user's own profile
    mixtapeFirst: Boolean = true,
) {
    // The pages from left to right
    val pages = if (mixtapeFirst) {
        listOf(ProfilePage.MIXTAPE, ProfilePage.ACTIVITY)
    } else {
        listOf(ProfilePage.ACTIVITY, ProfilePage.MIXTAPE)
    }

    // Page shown, kept after a rotation. Opens on the page on the left (page 0)
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })

    // A tap on a label slides to its page: the animation needs a coroutine
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PageLabels(
            pages = pages,
            // 0.0 on the page on the left, 1.0 on the other one, in between while the finger slides
            position = pagerState.currentPage + pagerState.currentPageOffsetFraction,
            currentPage = pagerState.currentPage,
            accent = accent,
            onSelect = { page -> scope.launch { pagerState.animateScrollToPage(page) } },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                // Takes all the height left under the labels
                .weight(1f),
            // Short pages stay at the top instead of being centered
            verticalAlignment = Alignment.Top,
            // A small gap between the pages while they slide
            pageSpacing = 24.dp,
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                when (pages[page]) {
                    ProfilePage.MIXTAPE -> mixtape()
                    ProfilePage.ACTIVITY -> activity()
                }
            }
        }
    }
}

// The two labels side by side, in the order of the pages, with a short line that follows the finger
@Composable
private fun PageLabels(
    pages: List<ProfilePage>,
    position: Float,
    currentPage: Int,
    accent: Color,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.width(LABEL_WIDTH * PAGE_COUNT)) {
        Row {
            pages.forEachIndexed { index, page ->
                PageLabel(
                    text = page.label,
                    isSelected = currentPage == index,
                    onClick = { onSelect(index) },
                )
            }
        }

        // Centered under its label: half the label, minus half the line, plus one label per page
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (LABEL_WIDTH - LINE_WIDTH) / 2 + LABEL_WIDTH * position)
                .size(width = LINE_WIDTH, height = 3.dp)
                .background(accent, RoundedCornerShape(percent = 50)),
        )
    }
}

// One label: a tab that TalkBack announces as selected or not
@Composable
private fun PageLabel(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(LABEL_WIDTH)
            // 48 dp: the minimum height for a finger
            .height(48.dp)
            .selectable(selected = isSelected, onClick = onClick, role = Role.Tab),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 1.5.sp,
            color = if (isSelected) MaterialTheme.colorScheme.onBackground
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10, heightDp = 300)
@Composable
private fun ProfilePagesPreview() {
    MeloDropTheme {
        ProfilePages(
            accent = Rose,
            mixtape = { Text("Page Mixtape", color = MaterialTheme.colorScheme.onBackground) },
            activity = { Text("Page Activité", color = MaterialTheme.colorScheme.onBackground) },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

// The user's own profile: the activity on the left
@Preview(showBackground = true, backgroundColor = 0xFF0C0C10, heightDp = 300)
@Composable
private fun ProfilePagesActivityFirstPreview() {
    MeloDropTheme {
        ProfilePages(
            accent = Rose,
            mixtape = { Text("Page Mixtape", color = MaterialTheme.colorScheme.onBackground) },
            activity = { Text("Page Activité", color = MaterialTheme.colorScheme.onBackground) },
            modifier = Modifier.fillMaxSize(),
            mixtapeFirst = false,
        )
    }
}
