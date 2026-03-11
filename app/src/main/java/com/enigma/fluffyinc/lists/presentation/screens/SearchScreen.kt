package com.enigma.fluffyinc.lists.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.enigma.fluffyinc.lists.domain.model.EntryType
import com.enigma.fluffyinc.lists.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController, vm: SearchViewModel) {
    val query by vm.query.collectAsState()
    val results by vm.results.collectAsState()
    val allTags by vm.allTags.collectAsState()
    val selectedTag by vm.selectedTagFilter.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { vm.setQuery(it) },
                        placeholder = { Text("Search everything...") },
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        ),
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) IconButton(onClick = { vm.setQuery("") }) {
                                Icon(Icons.Default.Close, null)
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Tag filters
            if (allTags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TagFilterChip(label = "All", color = Color.Gray, selected = selectedTag == null, onClick = { vm.setTagFilter(null) })
                    allTags.forEach { tag ->
                        TagFilterChip(
                            label = tag.name,
                            color = Color(tag.color),
                            selected = selectedTag?.id == tag.id,
                            onClick = { vm.setTagFilter(if (selectedTag?.id == tag.id) null else tag) }
                        )
                    }
                }
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            }

            if (results.isEmpty() && (query.isNotBlank() || selectedTag != null)) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(56.dp), tint = Color.LightGray)
                        Spacer(Modifier.height(8.dp))
                        Text("No results found", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn {
                    items(results, key = { "${it.entryType}-${it.id}" }) { item ->
                        SearchResultRow(item = item, onClick = {
                            when (item.entryType) {
                                EntryType.NOTE -> navController.navigate(Screen.NoteDetail.createRoute(item.id))
                                EntryType.CHECKLIST -> navController.navigate(Screen.ChecklistDetail.createRoute(item.id))
                                EntryType.LIST -> navController.navigate(Screen.SimpleListDetail.createRoute(item.id))
                                EntryType.DIARY -> navController.navigate(Screen.DiaryDetail.createRoute(item.id))
                                EntryType.JOURNAL -> navController.navigate(Screen.JournalDetail.createRoute(item.id))
                            }
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun TagFilterChip(label: String, color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) color else color.copy(alpha = 0.15f))
            .border(1.dp, color, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 13.sp, color = if (selected) Color.White else color, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun SearchResultRow(item: SearchResultItem, onClick: () -> Unit) {
    val (icon, tint) = when (item.entryType) {
        EntryType.NOTE -> Icons.AutoMirrored.Filled.Notes to Color(0xFF7C4DFF)
        EntryType.CHECKLIST -> Icons.Default.CheckBox to Color(0xFF00BCD4)
        EntryType.LIST -> Icons.AutoMirrored.Filled.FormatListBulleted to Color(0xFF4CAF50)
        EntryType.DIARY -> Icons.Default.CalendarMonth to Color(0xFFFF5722)
        EntryType.JOURNAL -> Icons.Default.AutoStories to Color(0xFF9C27B0)
    }
    val typeLabel = when (item.entryType) {
        EntryType.NOTE -> "Note"
        EntryType.CHECKLIST -> "Checklist"
        EntryType.LIST -> "List"
        EntryType.DIARY -> "Diary"
        EntryType.JOURNAL -> "Journal"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp)) }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text("$typeLabel  ·  ${item.subtitle}", fontSize = 12.sp, color = Color.Gray, maxLines = 1)
        }
        Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = 70.dp),
        thickness = DividerDefaults.Thickness,
        color = Color.LightGray.copy(alpha = 0.5f)
    )
}
