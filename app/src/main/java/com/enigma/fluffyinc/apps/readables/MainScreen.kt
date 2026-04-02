package com.enigma.fluffyinc.apps.readables

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.palette.graphics.Palette
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.enigma.fluffyinc.R
import com.enigma.fluffyinc.apps.readables.poems.Poem
import com.enigma.fluffyinc.apps.readables.poems.PoemDao
import com.enigma.fluffyinc.apps.readables.repository.Repository
import com.enigma.fluffyinc.apps.readables.screens.StoryDetailScreen
import com.enigma.fluffyinc.apps.readables.screens.StoryListScreen
import com.enigma.fluffyinc.apps.readables.stories.Story
import com.enigma.fluffyinc.readables.stories.StoryDao
import com.enigma.fluffyinc.apps.readables.viewmodel.PoemViewModel
import com.enigma.fluffyinc.apps.readables.viewmodel.StoryViewModel
import com.enigma.fluffyinc.apps.readables.epublibrary.EpubReaderApp
import com.google.accompanist.pager.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun ReadingMainScreen() {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val repository = Repository(
        poemDao = database.poemDao(),
        storyDao = database.storyDao()
    )


    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            Navigation(navController, PoemViewModel(repository = repository), StoryViewModel(repository = repository))
        }
    }
}



// Navigation.kt
@Composable
fun Navigation(navController: NavHostController, poemViewModel: PoemViewModel, storyViewModel: StoryViewModel) {
    NavHost(navController, startDestination = NavigationItem.Home.route) {
        composable(NavigationItem.Home.route) {
            ReadablesHomeScreen(navController)
        }
        composable(NavigationItem.Poems.route) {
            PoemListScreen(navController = navController, viewModel = poemViewModel)
        }
        composable("addPoem") {
            AddPoemScreen(navController = navController, viewModel = poemViewModel)
        }
        composable(
            route = "poemDetail/{poemId}",
            arguments = listOf(navArgument("poemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val poemId = backStackEntry.arguments?.getInt("poemId") ?: 0
            PoemDetailScreen(viewModel = poemViewModel, poemId = poemId, navController = navController)
        }

        composable(NavigationItem.Stories.route) {
            StoryListScreen(navController = navController, viewModel = storyViewModel)
        }
        composable("addStory") {
            AddStoryScreen(navController = navController, viewModel = storyViewModel)
        }
        composable(
            route = "storyDetail/{storyId}",
            arguments = listOf(navArgument("storyId") { type = NavType.IntType })
        ) { backStackEntry ->
            val storyId = backStackEntry.arguments?.getInt("storyId") ?: 0
            StoryDetailScreen(viewModel = storyViewModel, storyId = storyId, navController = navController)
        }

        composable(NavigationItem.Epub.route) {
            EpubReaderApp()
        }
    }
}

@Composable
fun ReadablesHomeScreen(navController: NavController) {
    val items = listOf(
        NavigationItem.Poems,
        NavigationItem.Stories,
        NavigationItem.Epub
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Readables Library",
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(bottom = 24.dp, top = 8.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { navController.navigate(item.route) },
                    elevation = 4.dp,
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = MaterialTheme.colors.surface
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.h6,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// BottomNavigationBar.kt
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.Poems,
        NavigationItem.Stories,
        NavigationItem.Epub
    )
    BottomNavigation {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
                selected = currentRoute?.startsWith(item.route) == true,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) { saveState = true }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

// NavigationItem.kt
sealed class NavigationItem(var route: String, var icon: ImageVector, var title: String) {
    object Home : NavigationItem("home", Icons.Default.GridView, "Home")
    object Poems : NavigationItem("poems", Icons.AutoMirrored.Filled.MenuBook, "Poems")
    object Stories : NavigationItem("stories", Icons.Filled.Book, "Stories")
    object Epub : NavigationItem("epub", Icons.AutoMirrored.Filled.MenuBook, "EPUB")
}



@OptIn(ExperimentalPagerApi::class)
@Composable
fun PoemListScreen(navController: NavController, viewModel: PoemViewModel) {
    val allPoems by viewModel.allPoems.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadInitialPoemsIfEmpty(context)
    }
    val cats = allPoems.groupBy { it.category }
    val category = cats.keys.toMutableList()
    category.add("All")
    category.sort()
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("addPoem") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Poem")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Poem Titles") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            // Category Tabs
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(Modifier.pagerTabIndicatorOffset(pagerState, tabPositions))
                }
            ) {
                category.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title) },
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    )
                }
            }

            HorizontalPager(
                count = category.size,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val currentCategory = category[page]
                val filteredPoems = allPoems.filter { poem ->
                    val matchesCategory = if (currentCategory == "All") true else poem.category.equals(currentCategory, ignoreCase = true)
                    val matchesSearch = poem.title.contains(searchQuery, ignoreCase = true)
                    matchesCategory && matchesSearch
                }
                PoemList(poems = filteredPoems, viewModel = viewModel, navController = navController)
            }
        }
    }
}

@Composable
fun PoemList(poems: List<Poem>, viewModel: PoemViewModel, navController: NavController) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(poems, key = { it.id }) { poem ->
            PoemCard(poem = poem, viewModel = viewModel) {
                navController.navigate("poemDetail/${poem.id}")
            }
        }
    }
}

@Composable
fun PoemCard(poem: Poem, viewModel: PoemViewModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .clickable(onClick = onClick),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colors.primary.copy(alpha = 0.4f)
                )
            }
            Text(
                text = poem.title,
                style = MaterialTheme.typography.subtitle1,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = poem.category,
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )
                IconButton(
                    onClick = {
                        val updatedPoem = poem.copy(isBookmarked = !poem.isBookmarked)
                        viewModel.updatePoem(updatedPoem)
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (poem.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun AddPoemScreen(navController: NavController, viewModel: PoemViewModel) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Love") }
    val categories = listOf("Love", "Life", "Sad")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Poem") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Poem Content") },
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            CategorySelector(
                categories = categories,
                selectedCategory = category,
                onCategorySelected = { category = it }
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val newPoem = Poem(
                        id = System.currentTimeMillis().toInt(), // Simple unique ID
                        title = title,
                        content = content,
                        category = category
                    )
                    viewModel.addPoem(newPoem)
                    navController.navigateUp()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && content.isNotBlank()
            ) {
                Text("Save Poem")
            }
        }
    }
}

@Composable
fun AddStoryScreen(navController: NavController, viewModel: StoryViewModel) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Adult") }
    val categories = listOf("Funny", "Adult", "Long")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Story") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Story Content") },
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            CategorySelector(
                categories = categories,
                selectedCategory = category,
                onCategorySelected = { category = it }
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val newStory = Story(
                        id = System.currentTimeMillis().toInt(), // Simple unique ID
                        title = title,
                        content = content,
                        category = category
                    )
                    viewModel.addStory(newStory)
                    navController.navigateUp()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && content.isNotBlank()
            ) {
                Text("Save Story")
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CategorySelector(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            readOnly = true,
            value = selectedCategory,
            onValueChange = { },
            label = { Text("Category") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { selectionOption ->
                DropdownMenuItem(
                    onClick = {
                        onCategorySelected(selectionOption)
                        expanded = false
                    }
                ) {
                    Text(text = selectionOption)
                }
            }
        }
    }
}



@Composable
fun PoemDetailScreen(viewModel: PoemViewModel, poemId: Int, navController: NavController) {
    val poemState = remember { mutableStateOf<Poem?>(null) }
    val context = LocalContext.current

    LaunchedEffect(poemId) {
        viewModel.getPoemById(poemId).collectLatest { poem ->
            poemState.value = poem
        }
    }

    val poem = poemState.value
    if (poem == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var primary = MaterialTheme.colors.primary
    var onPrimary = MaterialTheme.colors.onPrimary

    var vibrantColor by remember { mutableStateOf(primary) }
    var onVibrantColor by remember { mutableStateOf(onPrimary) }
    val systemUiController = rememberSystemUiController()
    val imageModel = poem.imageUrl ?: getFallbackImageForPoemCategory(poem.category)
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imageModel)
            .size(Size.ORIGINAL)
            .allowHardware(false)
            .build()
    )



    LaunchedEffect(painter.state) {
        val imageLoader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(imageModel)
            .allowHardware(false)
            .build()
        val result = (imageLoader.execute(request).drawable as? BitmapDrawable)?.bitmap
        if (result != null) {
            Palette.from(result).generate { palette ->
                palette?.vibrantSwatch?.let { swatch ->
                    vibrantColor = Color(swatch.rgb)
                    onVibrantColor = Color(swatch.bodyTextColor)
                }
            }
        }
    }



    val isDark = ColorUtils.calculateLuminance(vibrantColor.toArgb()) < 0.5
    SideEffect {
        systemUiController.setStatusBarColor(color = vibrantColor, darkIcons = !isDark)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(poem.title, color = onVibrantColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = onVibrantColor)
                    }
                },
                backgroundColor = vibrantColor,
                elevation = 4.dp
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { sharePoem(context, poem.title, poem.content) },
                backgroundColor = vibrantColor
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share Poem", tint = onVibrantColor)
            }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            item {
                Image(
                    painter = painter,
                    contentDescription = poem.title,
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    contentScale = ContentScale.Crop
                )
            }
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = poem.title, style = MaterialTheme.typography.h4)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Category: ${poem.category}",
                        style = MaterialTheme.typography.subtitle1,
                        color = Color.Gray
                    )
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                }
            }
            item {
                Text(
                    text = poem.content,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = TextStyle(fontSize = 20.sp, lineHeight = 32.sp, textAlign = TextAlign.Center)
                )
            }
        }
    }
}

/**

private fun getFallbackImageForPoemCategory(category: String): String {
    return when (category.lowercase()) {
        "love" -> "https://placehold.co/600x400/E91E63/FFFFFF?text=Love"
        "life" -> "https://placehold.co/600x400/4CAF50/FFFFFF?text=Life"
        "sad" -> "https://placehold.co/600x400/2196F3/FFFFFF?text=Sad"
        else -> "https://placehold.co/600x400/9E9E9E/FFFFFF?text=Poem"
    }
}
*/
fun sharePoem(context: Context, title: String, content: String) {
    val textToShare = "$title\n\n$content"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, textToShare)
    }
    context.startActivity(Intent.createChooser(intent, "Share Poem via"))
}


@Database(entities = [Poem::class, Story::class], version = 3, exportSchema = false) // Version updated
abstract class AppDatabase : RoomDatabase() {
    abstract fun poemDao(): PoemDao
    abstract fun storyDao(): StoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration() // Destructive migration for simplicity
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}


private fun getFallbackImageForPoemCategory(category: String): Int {
    return when (category.lowercase()) {
        "love" -> R.drawable.sexy // This now works!
        "life" -> R.drawable.flowerback
        "sad" -> R.drawable.sad1
        else -> R.drawable.flowerback
    }
}
