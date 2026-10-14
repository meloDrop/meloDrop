package com.app.melodrop.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.app.melodrop.R
import com.app.melodrop.ui.theme.Butter
import com.app.melodrop.ui.theme.Lavender
import com.app.melodrop.ui.theme.Mint
import com.app.melodrop.ui.theme.Peach
import com.app.melodrop.ui.theme.Rose

// App name font: Unbounded Bold (file res/font/unbounded_bold.ttf)
val Unbounded = FontFamily(Font(R.font.unbounded_bold, FontWeight.Bold))

// The five logo pastels, in the order of the drop's stripes
private val PastelGradient = Brush.horizontalGradient(
    listOf(
        Lavender,
        Rose,
        Peach,
        Butter,
        Mint
    )
)

@Composable
fun MeloDropTitle(modifier: Modifier = Modifier) {
    val style = TextStyle(
        fontFamily = Unbounded,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = (-0.5).sp
    )
    // Two Text side by side: the gradient only covers "Drop"
    Row(modifier = modifier) {
        Text(text = "melo", style = style, color = MaterialTheme.colorScheme.onBackground)
        Text(text = "Drop", style = style.merge(TextStyle(brush = PastelGradient)))
    }
}