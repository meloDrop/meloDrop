package com.app.melodrop.ui.components

import kotlin.math.roundToInt

// Like rate of a drop: the likes (picks included) over the views, "—" before the first view.
// A view is a swipe: a like, a pick or a skip
fun likeRateLabel(likes: Int, views: Int): String =
    if (views == 0) "—" else "${(100.0 * likes / views).roundToInt()} %"

// "12 vues · 58 %" under a drop of the user, "Pas encore vu" before the first swipe
fun dropStatsLabel(views: Int, likes: Int): String = when (views) {
    0 -> "Pas encore vu"
    1 -> "1 vue · ${likeRateLabel(likes, views)}"
    else -> "$views vues · ${likeRateLabel(likes, views)}"
}
