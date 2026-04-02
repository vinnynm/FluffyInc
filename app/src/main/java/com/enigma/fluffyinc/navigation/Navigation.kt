package com.enigma.fluffyinc.navigation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.enigma.fluffyinc.R
import com.enigma.fluffyinc.apps.finance.FinanceApp
import com.enigma.fluffyinc.apps.games.GameDashboard
import com.enigma.fluffyinc.apps.games.explodingkitties3.game.ExplodingKittens
import com.enigma.fluffyinc.apps.games.explodingkitties3.game.GameViewModel
import com.enigma.fluffyinc.apps.games.wildtactics.ui.WildTacticsGameScreen
import com.enigma.fluffyinc.apps.readables.ReadingMainScreen
import com.enigma.fluffyinc.apps.settings.SettingsScreen
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
        composable(Screens.Games.route) { 
            GameDashboard(onGameSelected = { route ->
                navController.navigate(route)
            })
        }
        composable("exploding_kittens") {
            val gameViewModel: GameViewModel = viewModel()
            val uiState by gameViewModel.uiState.collectAsState()
            ExplodingKittens(
                uiState = uiState,
                onGameModeSelected = gameViewModel::onGameModeSelected,
                onPlayerCountChange = gameViewModel::onPlayerCountChange,
                onAIDifficultyChange = gameViewModel::onAIDifficultyChange,
                onStartGame = gameViewModel::onStartGame,
                onBackToMenu = { navController.popBackStack() },
                onPlayCard = gameViewModel::onPlayCard,
                onEndTurnAndDraw = gameViewModel::onEndTurnAndDraw,
                onShowTutorial = gameViewModel::onShowTutorial,
                onCloseFuture = gameViewModel::onCloseFuture,
                onKittenPlaced = gameViewModel::onKittenPlaced,
                onHandoffConfirmed = gameViewModel::onHandoffConfirmed,
                onHostIpChange = { /* Implement if needed */ },
                onStartHost = gameViewModel::onStartHost,
                onJoinHost = gameViewModel::onJoinHost
            )
        }
        composable("wild_tactics") { WildTacticsGameScreen() }
        composable(Screens.Settings.route) { SettingsScreen(navController) }
        listsGraph(navController)
    }
}

data class MenuItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector?,
    @param:DrawableRes
    val image:  Int,
    val color: Color = Color.Blue,
    val color2: Color = Color.DarkGray
)

val menuItems = listOf(
    MenuItem(Screens.Finance.route, "Finance", Icons.Default.Money, R.drawable.beard_kitty, color = Color.Green.copy(.7f)),
    MenuItem(Screens.Readables.route, "Readables", Icons.Default.Book, R.drawable.beard_kitty, color = Color.Yellow.copy(.7f)),
    MenuItem(Screens.Games.route, "Games", Icons.Default.Games, R.drawable.beard_kitty, color = Color.Red.copy(.7f)),
    MenuItem(Screens.Lists.route, "Lists", Icons.AutoMirrored.Filled.List, R.drawable.beard_kitty, color = Color.Blue.copy(.7f)),
    MenuItem(Screens.Settings.route, "Settings", Icons.Default.Settings, R.drawable.beard_kitty, color = Color.Magenta.copy(.7f)),
)

@Composable
fun HomeScreen(navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize()
    ){
        Image(
            painter = painterResource(id = R.drawable.flowerback),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Column(
            modifier = Modifier.fillMaxSize()
        ){
            // Optimized: Moved random colors/brush out of recomposition or stabilized them
            val headlineBrush = remember {
                Brush.linearGradient(
                    listOf(
                        Color(0xFF6200EE),
                        Color(0xFF03DAC5),
                        Color(0xFF3700B3)
                    )
                )
            }

            Text(
                text = "Welcome",
                modifier = Modifier
                    .padding(top = 16.dp, end = 16.dp)
                    .fillMaxWidth(.7f)
                    .shadow(elevation = 4.dp, shape = MaterialTheme.shapes.medium.copy(topStart = CornerSize(0.dp), bottomStart = CornerSize(0.dp)))
                    .background(brush = Brush.linearGradient(colors = listOf(Color.Blue.copy(.3f), Color.Transparent)), shape = MaterialTheme.shapes.medium.copy(topStart = CornerSize(0.dp), bottomStart = CornerSize(0.dp)))
                    .border(width = 4.dp, brush = Brush.linearGradient(colors = listOf(Color.Transparent, Color.Blue.copy(.3f), MaterialTheme.colorScheme.background)), shape = MaterialTheme.shapes.medium.copy(topStart = CornerSize(0.dp), bottomStart = CornerSize(0.dp)))
                    .padding(6.dp),
                style = MaterialTheme.typography.headlineMedium.copy(
                    brush = headlineBrush
                )
            )
            Scaffold (
                containerColor = Color.Transparent
            ){ paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(150.dp),
                        contentPadding = PaddingValues(8.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(menuItems) { item ->
                            MenuItemCard(item) {
                                navController.navigate(item.route)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuItemCard(item: MenuItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onClick)
            .background(
                brush = Brush.linearGradient(colors = listOf(item.color, item.color2, Color.Transparent, Color.Transparent)),
                shape = MaterialTheme.shapes.medium
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(colors = listOf(item.color, item.color2, Color.Transparent).reversed()),
                shape = MaterialTheme.shapes.medium
            )
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (item.icon != null) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        } else {
            Icon(
                painter = painterResource(item.image),
                contentDescription = item.title,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                tint = Color.Unspecified
            )
        }
        Text(
            text = item.title,
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.titleMedium.copy(
                brush = Brush.linearGradient(colors = listOf(item.color, item.color2))
            ),
        )
    }
}
