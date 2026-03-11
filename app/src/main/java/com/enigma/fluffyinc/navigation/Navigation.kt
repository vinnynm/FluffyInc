package com.enigma.fluffyinc.navigation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.enigma.fluffyinc.R
import com.enigma.fluffyinc.apps.finance.FinanceApp
import com.enigma.fluffyinc.apps.games.wildtactics.apexcode.WildTacticsGameScreen
import com.enigma.fluffyinc.apps.readables.ReadingMainScreen
import com.enigma.fluffyinc.lists.presentation.navigation.listsGraph

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController,
        startDestination = Screens.Home.route
    ) {
        composable(Screens.Home.route) { HomeScreen(navController) }
        composable(Screens.Finance.route) { FinanceApp() }
        composable(Screens.Readables.route) { ReadingMainScreen() }
        composable(Screens.Games.route) { WildTacticsGameScreen() }
        listsGraph(navController)
    }
}

data class MenuItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector?,
    @param:DrawableRes
    val image:  Int
)

val menuItems = listOf(
    MenuItem(Screens.Finance.route, "Finance", Icons.Default.Money, R.drawable.beard_kitty),
    MenuItem(Screens.Readables.route, "Readables", Icons.Default.Book, R.drawable.beard_kitty),
    MenuItem(Screens.Games.route, "Games", Icons.Default.Games, R.drawable.beard_kitty),
    MenuItem(Screens.Lists.route, "Lists", Icons.AutoMirrored.Filled.List, R.drawable.beard_kitty),
    MenuItem(Screens.Settings.route, "Settings", Icons.Default.Settings, R.drawable.beard_kitty),
)

@Composable
fun HomeScreen(navController: NavHostController) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Text(
                text = "Welcome",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.headlineMedium
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(menuItems) { item ->
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable {
                                navController.navigate(item.route)
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (item.icon != null) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                painter = painterResource(item.image),
                                contentDescription = item.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f),
                                tint = Color.Unspecified
                            )
                        }
                        Text(
                            text = item.title,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}
