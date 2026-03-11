package com.enigma.fluffyinc.apps.readables

import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette

@Composable
fun getDominantColorImage(
    @DrawableRes imageId:Int
): MutableMap<String, Color?> {
    val context = LocalContext.current


    val dark = isSystemInDarkTheme()
    val colors = mutableMapOf<String,Color?>()

    LaunchedEffect (imageId){

        val bitmap = BitmapFactory.decodeResource(context.resources,imageId)
        Palette.from(bitmap).generate {
            val swatch = it?.dominantSwatch
            val dominant = swatch?.rgb?.let { it1 -> Color(it1) }
            val dominantText = swatch?.titleTextColor?.let { rgb-> Color(rgb) }
            val dominantBodyTextColor = swatch?.bodyTextColor?.let { rgb-> Color(rgb) }
            val vibrantSwatch = (if (dark) it?.lightVibrantSwatch else it?.darkVibrantSwatch)?.rgb?.let { rgb-> Color(rgb) }
            val vibrantTextSwatch = (if (dark) it?.lightVibrantSwatch else it?.darkVibrantSwatch)?.bodyTextColor?.let { rgb-> Color(rgb) }
            val mutedSwatch = (if (dark) it?.lightMutedSwatch else it?.darkMutedSwatch)?.rgb?.let { rgb-> Color(rgb) }

            colors[Colors.Dominant.name] = dominant
            colors[Colors.Vibrant.name] = vibrantSwatch
            colors[Colors.Muted.name] = mutedSwatch
            colors[Colors.DominantText.name] = dominantText
            colors[Colors.VibrantText.name] = vibrantTextSwatch
            colors[Colors.DominantBodyText.name] = dominantBodyTextColor


        }

    }

    return colors
}


enum class Colors{
    Dominant,
    Vibrant,
    VibrantText,
    DominantText,
    DominantBodyText,
    Muted
}