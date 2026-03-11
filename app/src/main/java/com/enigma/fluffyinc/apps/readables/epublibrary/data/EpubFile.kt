package com.enigma.fluffyinc.apps.readables.epublibrary.data

import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap

data class EpubFile(
    val uri: Uri,
    val title: String,
    val author: String,
    val coverImage: ImageBitmap? = null,
    val fileName: String = ""
)