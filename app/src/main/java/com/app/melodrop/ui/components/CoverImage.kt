package com.app.melodrop.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.app.melodrop.ui.theme.Lavender
import com.app.melodrop.ui.theme.MeloDropTheme
import com.app.melodrop.ui.theme.Night
import com.app.melodrop.ui.theme.TextPrimary
import com.app.melodrop.ui.theme.TextSecondary

// Below this width the text does not fit: only the sad drop is shown
private val MIN_WIDTH_FOR_TEXT = 96.dp

// The cover of a song. Shows a sad drop instead when Deezer has no cover
// (url is null) or when the image cannot be downloaded (broken link).
// The caller gives the size and the shape with the modifier, like for an AsyncImage
@Composable
fun CoverImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    // Becomes true if Coil fails to download the image.
    // remember(url): a new url gets a new chance to load
    var failed by remember(url) { mutableStateOf(false) }

    // Grey background: visible while the image loads, like before
    Box(modifier = modifier.background(MaterialTheme.colorScheme.outline)) {
        if (url == null || failed) {
            NoCover(modifier = Modifier.fillMaxSize())
        } else {
            AsyncImage(
                model = url,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                onState = { state ->
                    if (state is AsyncImagePainter.State.Error) failed = true
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

// The sad drop, with "Pas d'image dispo !" under it when there is room
@Composable
private fun NoCover(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val showText = maxWidth >= MIN_WIDTH_FOR_TEXT

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Bigger drop when it is alone in a small square
            Canvas(modifier = Modifier.size(maxWidth * if (showText) 0.45f else 0.8f)) {
                // Same 100 x 100 grid as the Avatar
                scale(scale = size.width / 100f, pivot = Offset.Zero) {
                    drawSadDrop()
                }
            }

            if (showText) {
                Text(
                    text = "Pas d'image dispo !",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }
        }
    }
}

// A lavender drop with a sad face and a tear, on the 100 x 100 grid
private fun DrawScope.drawSadDrop() {
    // Same drop shape as the avatar
    drawPath(dropPath(), Lavender)

    // Eyebrows: their inner ends go up, which gives the sad look
    drawLine(Night, Offset(38.5f, 51f), Offset(45f, 48.5f), strokeWidth = 2f, cap = StrokeCap.Round)
    drawLine(Night, Offset(55f, 48.5f), Offset(61.5f, 51f), strokeWidth = 2f, cap = StrokeCap.Round)

    // Eyes
    drawCircle(Night, radius = 2.8f, center = Offset(43f, 56f))
    drawCircle(Night, radius = 2.8f, center = Offset(57f, 56f))

    // Mouth: a curve that goes down on both sides
    val mouth = Path().apply {
        moveTo(44f, 70f)
        quadraticTo(50f, 64f, 56f, 70f)
    }
    drawPath(mouth, Night, style = Stroke(width = 2.4f, cap = StrokeCap.Round))

    // A tiny drop under the right eye: the tear
    translate(left = 57f, top = 56f) {
        scale(scale = 0.12f, pivot = Offset.Zero) {
            drawPath(dropPath(), TextPrimary.copy(alpha = 0.9f))
        }
    }
}

@Preview
@Composable
private fun CoverImagePreview() {
    MeloDropTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Big cover, like on a Discover card: drop and text
            CoverImage(
                url = null,
                contentDescription = null,
                modifier = Modifier
                    .width(200.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20.dp)),
            )
            // Small cover, like in "Mes likes": the drop only
            CoverImage(
                url = null,
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
            )
        }
    }
}