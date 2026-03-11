package com.enigma.fluffyinc.lists.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.enigma.fluffyinc.lists.presentation.navigation.Screen


data class HomeSection(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

@Composable
fun HomeScreen(navController: NavController) {
    val sections = listOf(
        HomeSection("Notes", "Topics & subtopics",
            Icons.AutoMirrored.Filled.Notes, Color(0xFF7C4DFF), Screen.NoteList.route),
        HomeSection("Checklists", "Check off tasks", Icons.Default.CheckBox, Color(0xFF00BCD4), Screen.ChecklistList.route),
        HomeSection("Lists", "Simple bullet lists",
            Icons.AutoMirrored.Filled.FormatListBulleted, Color(0xFF4CAF50), Screen.SimpleListList.route),
        HomeSection("Diary", "Plan future events", Icons.Default.CalendarMonth, Color(0xFFFF5722), Screen.DiaryList.route),
        HomeSection("Journal", "Mood & emotions", Icons.Default.AutoStories, Color(0xFF9C27B0), Screen.JournalList.route),
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.Search.route) }, containerColor = Color(0xFF6650A4)) {
                Icon(Icons.Default.Search, null, tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
                .padding(16.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                "My Notebook",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp)
            )
            Text("Everything in one place", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(sections) { section ->
                    HomeSectionCard(section = section, onClick = { navController.navigate(section.route) })
                }
            }
        }
    }
}

@Composable
fun HomeSectionCard(section: HomeSection, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(section.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(section.icon, contentDescription = null, tint = section.color, modifier = Modifier.size(26.dp))
            }
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Text(section.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(section.subtitle, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
