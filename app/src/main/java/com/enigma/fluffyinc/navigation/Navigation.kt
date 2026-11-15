package com.enigma.fluffyinc.navigation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.graphics.vector.ImageVector
import com.enigma.fluffyinc.ui.core.AppCard
import com.enigma.fluffyinc.ui.core.AppScaffold
import com.enigma.fluffyinc.apps.finance.FinanceApp
import com.enigma.fluffyinc.apps.games.GameDashboard
import com.enigma.fluffyinc.apps.games.absurdle.viewmodel.AbsurdleViewModel
import com.enigma.fluffyinc.apps.games.explodingkitties3.game.ExplodingKittens
import com.enigma.fluffyinc.apps.games.explodingkitties3.game.GameViewModel
import com.enigma.fluffyinc.apps.games.killersudoku.KillerSudokuGame
import com.enigma.fluffyinc.apps.games.killersudoku.SudokuGame
import com.enigma.fluffyinc.apps.games.lexicon.ScrabbleGameViewModelFactory
import com.enigma.fluffyinc.apps.games.lexicon.data.WordDictionaryManager
import com.enigma.fluffyinc.apps.games.lexicon.ui.ScrabbleGameApp
import com.enigma.fluffyinc.apps.games.lexicon.ui.ScrabbleGameViewModel
import com.enigma.fluffyinc.apps.games.wildtactics.ui.WildTacticsGameScreen
import com.enigma.fluffyinc.apps.readables.ReadingMainScreen
import com.enigma.fluffyinc.apps.settings.SettingsScreen
import com.enigma.fluffyinc.lists.presentation.navigation.listsGraph
import com.enigma.fluffyinc.apps.games.absurdle.ui.GameScreen as AbsurdleScreen
import com.enigma.fluffyinc.apps.games.betweenle.ui.GameScreen as BetweenleScreen
import com.enigma.fluffyinc.apps.games.chromaword.ui.GameScreen as ChromaWordScreen
import com.enigma.fluffyinc.apps.games.chromaword.viewmodel.GameViewModel as ChromaWordViewModel
import com.enigma.fluffyinc.apps.games.lightsOut.LightsOutScreen
import com.enigma.fluffyinc.apps.games.lightsOut.LightsOutViewModel

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
            GameDashboard(
                onGameSelected = { route -> navController.navigate(route) },
                onBack = { navController.popBackStack() }
            )
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
        
        composable(Screens.Lexicon.route) {
            val context = LocalContext.current
            val dictionaryManager = remember { WordDictionaryManager(context) }
            val lexiconViewModel: ScrabbleGameViewModel = viewModel(
                factory = ScrabbleGameViewModelFactory(dictionaryManager, context)
            )
            ScrabbleGameApp(viewModel = lexiconViewModel)
        }

        composable(Screens.Betweenle.route) {
            BetweenleScreen()
        }

        composable(Screens.Absurdle.route) {
            val absurdleViewModel: AbsurdleViewModel = viewModel()
            AbsurdleScreen(absurdleViewModel)
        }

        composable(Screens.ChromaWord.route) {
            val chromaWordViewModel: ChromaWordViewModel = viewModel()
            ChromaWordScreen(chromaWordViewModel)
        }

        composable(Screens.KillerSudoku.route) {
            KillerSudokuGame()
        }

        composable(Screens.Sudoku.route) {
            SudokuGame()
        }

        composable(Screens.LightsOut.route) {
            val lightsOutViewModel: LightsOutViewModel = viewModel()
            LightsOutScreen(lightsOutViewModel)
        }

        composable(Screens.Settings.route) { SettingsScreen(navController) }
        listsGraph(navController)
    }
}

data class MenuItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

val menuItems = listOf(
    MenuItem(Screens.Finance.route, "Finance", Icons.Default.Money),
    MenuItem(Screens.Readables.route, "Readables", Icons.Default.Book),
    MenuItem(Screens.Games.route, "Games", Icons.Default.Games),
    MenuItem(Screens.Lists.route, "Lists", Icons.AutoMirrored.Filled.List),
    MenuItem(Screens.Settings.route, "Settings", Icons.Default.Settings),
)

@Composable
fun HomeScreen(navController: NavHostController) {
    AppScaffold(title = "FluffyInc") { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(menuItems) { item ->
                MenuItemCard(item) { navController.navigate(item.route) }
            }
        }
    }
}

@Composable
fun MenuItemCard(item: MenuItem, onClick: () -> Unit) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = item.title,
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
