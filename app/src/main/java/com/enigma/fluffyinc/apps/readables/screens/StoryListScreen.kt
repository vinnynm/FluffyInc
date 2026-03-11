package com.enigma.fluffyinc.apps.readables.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            StoryList(stories = filteredStories, viewModel = viewModel, navController = navController)
        }
    }
}

@Composable
fun StoryList(stories: List<Story>, viewModel: StoryViewModel, navController: NavController) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        items(stories, key = { it.id }) { story ->
            StoryCard(story = story, viewModel = viewModel) {
                navController.navigate("storyDetail/${story.id}")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun StoryCard(story: Story, viewModel: StoryViewModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = 4.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = story.title, style = MaterialTheme.typography.h6)
                Text(text = story.category, style = MaterialTheme.typography.caption)
            }
            IconButton(onClick = {
                val updatedStory = story.copy(isBookmarked = !story.isBookmarked)
                viewModel.updateStory(updatedStory)
            }) {
                Icon(
                    imageVector = if (story.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = MaterialTheme.colors.primary
                )
            }
        }
    }
}
