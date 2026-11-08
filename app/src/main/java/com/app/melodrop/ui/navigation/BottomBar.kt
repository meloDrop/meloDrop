package com.app.melodrop.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.melodrop.ui.theme.Butter
import com.app.melodrop.ui.theme.MeloDropTheme
import com.app.melodrop.ui.theme.Mint
import com.app.melodrop.ui.theme.Rose
import com.app.melodrop.ui.tutorial.TutorialTarget
import com.app.melodrop.ui.tutorial.tutorialTarget

// One tab of the bottom bar: its route, its label, its color when active,
// and its name for the tour shown after an update
private data class Tab(
    val route: String,
    val label: String,
    val color: Color,
    val target: TutorialTarget,
)

private val tabs = listOf(
    Tab(Routes.DISCOVER, "DISCOVER", Butter, TutorialTarget.TAB_DISCOVER),
    Tab(Routes.DROP, "DROP", Mint, TutorialTarget.TAB_DROP),
    Tab(Routes.PROFILE, "PROFIL", Rose, TutorialTarget.TAB_PROFILE),
)

@Composable
fun BottomBar(currentRoute: String?, onTabClick: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row {

            tabs.forEach { (route, label, accent, target) ->
                val selected = route == currentRoute
                val color = if (selected) accent
                else MaterialTheme.colorScheme.onSurfaceVariant

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .tutorialTarget(target)
                        .clip(RoundedCornerShape(percent = 50))
                        .clickable { onTabClick(route) }
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(color, CircleShape)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = label,
                        color = color,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}


@Preview(showSystemUi = true)
@Composable
private fun BottomBarPreview() {
    MeloDropTheme {
        var route by remember { mutableStateOf(Routes.DISCOVER) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.BottomCenter,
        ) {
            BottomBar(currentRoute = route, onTabClick = { route = it })
        }
    }
}