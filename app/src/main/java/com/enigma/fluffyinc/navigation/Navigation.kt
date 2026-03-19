package com.enigma.fluffyinc.navigation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CornerSize
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
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
import kotlin.random.Random

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
    val image:  Int,
    val color: Color = Color.Blue,
    val badgeCount: Int? = null,
    val badgeColor: Color = Color.Red,
    val badgeTextColor: Color = Color.White,
    val badgeTextSize: Float = 12f,
    val badgeBackgroundColor: Color = Color.Blue,
    val badgeContentColor: Color = Color.White,
    val badgeContentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
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
            contentScale = androidx.compose.ui.layout.ContentScale.FillBounds
        )
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Transparent)
        ){
            Text(
                text = "Welcome",
                modifier = Modifier
                    .padding(
                        top = 16.dp,
                        start = 0.dp,
                        end = 16.dp
                    )
                    .fillMaxWidth(.7f)
                    .shadow(
                        elevation = 4.dp,
                        shape = MaterialTheme.shapes.medium.copy(
                            topStart = CornerSize(0.dp),
                            bottomStart = CornerSize(0.dp),
                        )
                    )
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Blue.copy(.3f),
                                Color.Transparent
                            )
                        ),
                        shape = MaterialTheme.shapes.medium.copy(
                            topStart = CornerSize(0.dp),
                            bottomStart = CornerSize(0.dp),
                        )
                    )
                    .border(
                        width = 4.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Blue.copy(.3f),
                                MaterialTheme.colorScheme.background
                            )
                        ),
                        shape = MaterialTheme.shapes.medium.copy(
                            topStart = CornerSize(0.dp),
                            bottomStart = CornerSize(0.dp),
                        )
                    )
                    .padding(
                        6.dp
                    )

                ,
                style = MaterialTheme.typography.headlineMedium.copy(
                    brush = Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(red = Random.nextInt().toFloat(), blue = Random.nextInt().toFloat(), green = Random.nextInt().toFloat(),),
                            MaterialTheme.colorScheme.primary.copy(red = Random.nextInt().toFloat(), blue = Random.nextInt().toFloat(), green = Random.nextInt().toFloat(),),
                            MaterialTheme.colorScheme.primary.copy(red = Random.nextInt().toFloat(), blue = Random.nextInt().toFloat(), green = Random.nextInt().toFloat(),),
                            MaterialTheme.colorScheme.primary.copy(red = Random.nextInt().toFloat(), blue = Random.nextInt().toFloat(), green = Random.nextInt().toFloat(),),
                        )

                    )
                )
            )
            Scaffold (
                modifier = Modifier
                    .background(color = Color.Transparent),
                contentColor = Color.Transparent,
                containerColor = Color.Transparent
            ){ paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Blue.copy(.3f),
                                    MaterialTheme.colorScheme.background
                                )
                            )

                        )
                        .fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {

                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(150.dp),
                            contentPadding = PaddingValues(8.dp),
                            modifier = Modifier.fillMaxSize()
                                .background(Color.Transparent),
                        ) {
                            items(menuItems) { item ->
                                Column(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .clickable {
                                            navController.navigate(item.route)
                                        }
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    item.color,
                                                    item.color2,
                                                    Color.Transparent,
                                                    Color.Transparent,
                                                )
                                            ),
                                            shape = MaterialTheme.shapes.medium
                                        )
                                        .border(
                                            width = 1.dp,
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    item.color,
                                                    item.color2,
                                                    Color.Transparent
                                                ).reversed()
                                            ),
                                            shape = MaterialTheme.shapes.medium
                                        )
                                        .padding(2.dp)
                                    ,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (item.icon != null) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.title,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1f),
                                            tint = MaterialTheme.colorScheme.background.copy(
                                                red = (MaterialTheme.colorScheme.background.red + item.color.red)/2,
                                                blue = (MaterialTheme.colorScheme.background.blue + item.color.blue)/2,
                                                green = (MaterialTheme.colorScheme.background.green + item.color.green)/2,
                                            )
                                        )
                                    } else {
                                        Icon(
                                            painter = painterResource(item.image),
                                            contentDescription = item.title,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1f),
                                            tint = item.color2
                                        )
                                    }
                                    Text(
                                        text = item.title,
                                        modifier = Modifier.padding(8.dp),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            brush = Brush.linearGradient(
                                                colors = listOf(item.color, item.color2)
                                            )
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }

            }
        }
    }


}


@Preview
@Composable
private fun NaviPrev() {
    HomeScreen(
        navController = rememberNavController()
    )
}
