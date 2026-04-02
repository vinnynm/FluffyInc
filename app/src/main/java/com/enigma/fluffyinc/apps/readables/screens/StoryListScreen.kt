package com.enigma.fluffyinc.apps.readables.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enigma.fluffyinc.apps.readables.stories.Story
import com.enigma.fluffyinc.apps.readables.viewmodel.StoryViewModel
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun StoryListScreen(navController: NavController, viewModel: StoryViewModel) {
    val allStories by viewModel.allStories.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadInitialStoriesIfEmpty()
    }

    val categories = listOf("All", "Funny", "Adult", "Long")
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().background(
            color = MaterialTheme.colors.surface
        )
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Stories") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )

        TabRow(
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(Modifier.pagerTabIndicatorOffset(pagerState, tabPositions))
            }
        ) {
            categories.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                )
            }
        }

        HorizontalPager(
            count = categories.size,
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val currentCategory = categories[page]
            val filteredStories = allStories.filter { story ->
                val matchesCategory = if (currentCategory == "All") true else story.category.equals(currentCategory, ignoreCase = true)
                val matchesSearch = story.title.contains(searchQuery, ignoreCase = true) || story.content.contains(searchQuery, ignoreCase = true)
                matchesCategory && matchesSearch
            }
            StoryGrid(stories = filteredStories, viewModel = viewModel, navController = navController)
        }
    }
}

@Composable
fun StoryGrid(stories: List<Story>, viewModel: StoryViewModel, navController: NavController) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(stories, key = { it.id }) { story ->
            StoryCard(story = story, viewModel = viewModel) {
                navController.navigate("storyDetail/${story.id}")
            }
        }
    }
}

@Composable
fun StoryCard(story: Story, viewModel: StoryViewModel, onClick: () -> Unit) {
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
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colors.primary.copy(alpha = 0.4f)
                )
            }
            Text(
                text = story.title,
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
                    text = story.category,
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )
                IconButton(
                    onClick = {
                        val updatedStory = story.copy(isBookmarked = !story.isBookmarked)
                        viewModel.updateStory(updatedStory)
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (story.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
