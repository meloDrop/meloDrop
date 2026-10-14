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

// Police du nom de l'appli : Unbounded Bold (fichier res/font/unbounded_bold.ttf)
val Unbounded = FontFamily(Font(R.font.unbounded_bold, FontWeight.Bold))

// Les cinq pastels du logo, dans l'ordre des bandes de la goutte
private val PastelGradient = Brush.horizontalGradient(
    listOf(
        Color(0xFFB4A7F5), // lavande
        Color(0xFFF5A8C8), // rose
        Color(0xFFFFB38A), // pêche
        Color(0xFFFFE08A), // jaune beurre
        Color(0xFF9EE0B8)  // menthe
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
    // Deux Text côte à côte : le dégradé ne couvre que « Drop »
    Row(modifier = modifier) {
        Text(text = "melo", style = style, color = MaterialTheme.colorScheme.onBackground)
        Text(text = "Drop", style = style.merge(TextStyle(brush = PastelGradient)))
    }
}
