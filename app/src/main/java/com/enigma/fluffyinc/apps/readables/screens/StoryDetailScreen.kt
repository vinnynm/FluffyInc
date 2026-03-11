package com.enigma.fluffyinc.apps.readables.screens


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import androidx.navigation.NavController
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.enigma.fluffyinc.R
import com.enigma.fluffyinc.apps.readables.Colors
import com.enigma.fluffyinc.apps.readables.stories.Story
import com.enigma.fluffyinc.apps.readables.viewmodel.StoryViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("LocalContextResourcesRead")
@Composable
fun StoryDetailScreen(viewModel: StoryViewModel, storyId: Int, navController: NavController) {
    val storyState = remember { mutableStateOf<Story?>(null) }
    val context = LocalContext.current

    val drawables = context.resources



    // Observe story details from ViewModel
    LaunchedEffect(storyId) {
        viewModel.getStoryById(storyId).collectLatest { story ->
            storyState.value = story
        }
    }

    val story = storyState.value
    if (story == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val primary =MaterialTheme.colors.primary
    val vibrant =MaterialTheme.colors.onPrimary
    // State for colors extracted from the image
    var vibrantColor by remember { mutableStateOf(primary) }
    var onVibrantColor by remember { mutableStateOf(vibrant) }

    // System UI Controller for status bar color
    val systemUiController = rememberSystemUiController()

    // Lazy list state to track scroll position for last read position
    val scrollState = rememberLazyListState(initialFirstVisibleItemIndex = story.lastPosition)

    // Save the last read position when the screen is left
    DisposableEffect(Unit) {
        onDispose {
            val updatedStory = story.copy(lastPosition = scrollState.firstVisibleItemIndex)
            viewModel.updateStory(updatedStory)
        }
    }

    val updated = storyState.value
    val listLazyListState = rememberLazyListState()
    LaunchedEffect (listLazyListState){
        snapshotFlow {
            listLazyListState.firstVisibleItemIndex
        }.collectLatest {
            if (updated != null) {
                updated.lastPosition = it
            }else{
                updated
            }
        }
    }
    // Use the story's imageUrl if available, otherwise use the local drawable fallback.
    val imageModel: Any = story.imageUrl ?: getFallbackImageForStoryCategory(story.category) // Change type to Any or keep as is if only String/Int

// imageModel = drawables.openRawResource(imageModel) // REMOVE THIS LINE

// Image painter and palette generation
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imageModel) // Coil can handle String URLs or Int resource IDs
            .size(Size.ORIGINAL) // Use coil.size.Size instead of androidx.compose.ui.geometry.Size
            .allowHardware(true) // Required for palette generation
            .build()
    )

    val dark = isSystemInDarkTheme()
    val myColors = mutableMapOf<String,Color?>()



    LaunchedEffect(painter.state) {
        val imageLoader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(imageModel)
            .allowHardware(false)
            .build()
        val result = (imageLoader.execute(request).drawable as? BitmapDrawable)?.bitmap
        if (result != null) {
            Palette.from(result).generate { it ->
                it?.vibrantSwatch?.let { swatch ->
                    vibrantColor = Color(swatch.rgb)
                    onVibrantColor = Color(swatch.bodyTextColor)
                }
                val swatch = it?.dominantSwatch
                val dominant = swatch?.rgb?.let { it1 -> Color(it1) }
                val dominantText = swatch?.titleTextColor?.let { rgb-> Color(rgb) }
                val dominantBodyTextColor = swatch?.bodyTextColor?.let { rgb-> Color(rgb) }
                val vibrantSwatch = (if (dark) it?.lightVibrantSwatch else it?.darkVibrantSwatch)?.rgb?.let { rgb-> Color(rgb) }
                val vibrantTextSwatch = (if (dark) it?.lightVibrantSwatch else it?.darkVibrantSwatch)?.bodyTextColor?.let { rgb-> Color(rgb) }
                val mutedSwatch = (if (dark) it?.lightMutedSwatch else it?.darkMutedSwatch)?.rgb?.let { rgb-> Color(rgb) }

                myColors[Colors.Dominant.name] = dominant
                myColors[Colors.DominantText.name] = dominantText
                myColors[Colors.Vibrant.name] = vibrantSwatch
                myColors[Colors.Muted.name] = mutedSwatch
                myColors[Colors.DominantText.name] = dominantText
                myColors[Colors.VibrantText.name] = vibrantTextSwatch
                myColors[Colors.DominantBodyText.name] = dominantBodyTextColor
            }

            Log.d("mooooooooooooooooon",myColors.toString())

        }
    }




    // Determine if the vibrant color is dark or light for status bar icon colors
    val isDark = ColorUtils.calculateLuminance(vibrantColor.toArgb()) < 0.5
    val isDarker =ColorUtils.calculateLuminance((myColors[Colors.Dominant.name]?:Color.Gray).toArgb()) < 0.5
    SideEffect {
        systemUiController.setStatusBarColor(
            color = myColors[Colors.Dominant.name]?:Color.Gray.copy(.4f),
            darkIcons = !isDarker
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(story.title, color = myColors[Colors.DominantText.name] ?:Color.Yellow.copy(.6f)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = onVibrantColor)
                    }
                },
                backgroundColor = myColors[Colors.Vibrant.name] ?:Color.Gray,
                elevation = 4.dp
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { shareText(context, story.title, "Read this amazing story!") },
                backgroundColor = myColors[Colors.Vibrant.name] ?:Color.Gray
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share Story", tint = myColors[Colors.Dominant.name] ?:Color.Blue.copy(.4f))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            state = scrollState
        ) {
            // Collapsing Header Image
            item {
                Image(
                    painter = painter,
                    contentDescription = story.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Story Metadata
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = story.title, style = MaterialTheme.typography.h4)
                    Spacer(modifier = Modifier.height(4.dp))
                    myColors.get(Colors.Dominant.name)?.let {
                        Text(
                            text = "Category: ${story.category}",
                            style = MaterialTheme.typography.subtitle1,
                            color = it
                        )
                    }
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                }
            }

            // Story Content with Selection
            item {
                SelectionContainer(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = story.content,
                        style = TextStyle(fontSize = 18.sp, lineHeight = 28.sp, textAlign = TextAlign.Justify)
                    )
                }
            }
        }
    }
}

// In your getFallbackImage... functions



private fun getFallbackImageForStoryCategory(category: String): Int {
    return when (category.lowercase()) {
        "funny" -> R.drawable.flowerback
        "adult" -> R.drawable.sexy
        "long" -> R.drawable.flowerback
        else -> R.drawable.fat_kitty
    }
}

fun shareText(context: Context, subject: String, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share via"))
}