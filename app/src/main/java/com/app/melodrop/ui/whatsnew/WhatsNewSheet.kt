package com.app.melodrop.ui.whatsnew

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.melodrop.BuildConfig
import com.app.melodrop.ui.components.Unbounded
import com.app.melodrop.ui.theme.Butter
import com.app.melodrop.ui.theme.Lavender
import com.app.melodrop.ui.theme.MeloDropTheme
import com.app.melodrop.ui.theme.Mint
import com.app.melodrop.ui.theme.Peach
import com.app.melodrop.ui.theme.Rose
import kotlinx.coroutines.launch

// One pastel per line, in turn: the five colors of the app
private val BulletColors = listOf(Lavender, Peach, Butter, Mint, Rose)

// Bottom sheet with what changed in the version installed.
// Opened by itself after an update, or by the "i" button of Discover.
// OptIn: ModalBottomSheet is marked as experimental in Material 3
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsNewSheet(
    onDismiss: () -> Unit,
    // "Revoir le tuto" button: the tutorials of Discover, Drop and Profil. null: the button is hidden
    onReplayTutorial: (() -> Unit)? = null,
) {
    // skipPartiallyExpanded: the sheet opens fully at once, not half-way
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        // Swipe down, tap outside the sheet, or back button
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        WhatsNewContent(
            onOkClick = {
                // The sheet slides down first, then it is removed
                scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
            },
            showReplay = onReplayTutorial != null,
            onReplayClick = {
                // Same: the sheet slides down, then the tour starts
                scope.launch { sheetState.hide() }.invokeOnCompletion { onReplayTutorial?.invoke() }
            },
        )
    }
}

// Pure UI: title, version, one line per change, the OK button
// and, when the version has a tour, the "Revoir le tuto" button
@Composable
private fun WhatsNewContent(
    onOkClick: () -> Unit,
    showReplay: Boolean = false,
    onReplayClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // Scrolls if the list is long or the screen small
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column {
            Text(
                text = "Nouveautés",
                fontFamily = Unbounded,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            // "meloDrop v.0.6.0": the number comes from versionName in build.gradle.kts
            Text(
                text = "meloDrop ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        WhatsNewItems.forEachIndexed { index, item ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Small pastel dot, lined up with the first line of text
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(8.dp)
                        .background(BulletColors[index % BulletColors.size], CircleShape),
                )
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Button(
            onClick = onOkClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("OK")
        }

        // Shown only when this version has a tour
        if (showReplay) {
            TextButton(
                onClick = onReplayClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Revoir le tuto")
            }
        }
    }
}

// The content only: a ModalBottomSheet does not show in a Preview
@Preview(showBackground = true, backgroundColor = 0xFF18181F)
@Composable
private fun WhatsNewContentPreview() {
    MeloDropTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            WhatsNewContent(onOkClick = {}, showReplay = true)
        }
    }
}
