package view.themes

import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

    private val DarkBlue = Color(0xff182260)
    private val CornflowerBlue = Color(0xff0069e0)
    private val LightColdBlue = Color(0xff8fd3d8)
    private val FlamingoPink = Color(0xffF76c6c)
    private val SmokeWhite = Color(0xfff5f5f5)
    private val DarkGrey = Color(0xff333333)
    private val LightGrey = Color(0xfff4f4f4)

    val DarkColors = darkColors(
        primary = CornflowerBlue,
        primaryVariant = LightColdBlue,
        onPrimary = SmokeWhite,
        secondary = CornflowerBlue,
        secondaryVariant = DarkBlue,
        onSecondary = SmokeWhite,
        background = DarkGrey,
        onBackground = SmokeWhite,
        surface = DarkGrey,
        onSurface = SmokeWhite,
        error = FlamingoPink,
        onError = SmokeWhite,
    )
     val LightColors = lightColors(
         primary = DarkBlue,
         primaryVariant = CornflowerBlue,
         onPrimary = SmokeWhite,
         secondary = LightColdBlue,
         secondaryVariant = CornflowerBlue,
         onSecondary = SmokeWhite,
         background = SmokeWhite,
         onBackground = DarkGrey,
         surface = LightGrey,
         onSurface = DarkGrey,
         error = FlamingoPink,
         onError = SmokeWhite,
    )
