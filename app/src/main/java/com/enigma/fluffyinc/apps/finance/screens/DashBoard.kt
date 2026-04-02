package com.enigma.fluffyinc.apps.finance.screens


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: FinanceViewModel, navController: NavController) {
    val income by viewModel.allIncome.collectAsState()
    val expenses by viewModel.allExpenses.collectAsState()
    val scheduledPayments by viewModel.activePayments.collectAsState()
    val loans by viewModel.activeLoans.collectAsState()

    val totalIncome = income.sumOf { it.amount }
    val totalExpenses = expenses.sumOf { it.amount }
    val balance = totalIncome - totalExpenses

    var quote by remember { mutableStateOf("Loading inspiration...") }
    var author by remember { mutableStateOf("") }

    // Fetch quote from API
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL("https://api.quotable.io/random")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                val response = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(response)
                quote = json.getString("content")
                author = json.getString("author")
            } catch (e: Exception) {
                quote = "The best way to predict the future is to create it."
                author = "Peter Drucker"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finance Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Inspirational Quote Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.FormatQuote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = quote,
                            style = MaterialTheme.typography.bodyLarge,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        if (author.isNotEmpty()) {
                            Text(
                                text = "- $author",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }

            // Financial Outlook Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (balance >= 0) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Net Worth / Outlook",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            NumberFormat.getCurrencyInstance().format(balance),
                            style = MaterialTheme.typography.displayMedium,
                            color = if (balance >= 0) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                        )
                        
                        LinearProgressIndicator(
                            progress = { if (totalIncome > 0) (totalExpenses / totalIncome).toFloat().coerceIn(0f, 1f) else 1f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .height(8.dp),
                            color = if (totalExpenses > totalIncome) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Monthly Income", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    NumberFormat.getCurrencyInstance().format(totalIncome),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Monthly Expenses", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    NumberFormat.getCurrencyInstance().format(totalExpenses),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFFF44336)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Actions Section
            item {
                Text(
                    "Quick Navigation",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CompactActionCard(
                        "Income",
                        Icons.AutoMirrored.Filled.TrendingUp,
                        Color(0xFF4CAF50),
                        Modifier.weight(1f),
                        onClick = { navController.navigate("income") }
                    )
                    CompactActionCard(
                        "Expenses",
                        Icons.AutoMirrored.Filled.TrendingDown,
                        Color(0xFFF44336),
                        Modifier.weight(1f),
                        onClick = { navController.navigate("expenses") }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CompactActionCard(
                        "Loans",
                        Icons.Default.AccountBalance,
                        Color(0xFFE91E63),
                        Modifier.weight(1f),
                        onClick = { navController.navigate("loans") }
                    )
                    CompactActionCard(
                        "Shopping",
                        Icons.Default.ShoppingCart,
                        Color(0xFF2196F3),
                        Modifier.weight(1f),
                        onClick = { navController.navigate("shopping") }
                    )
                }
            }

            // Upcoming Payments Section
            item {
                Text(
                    "Upcoming Payments",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }

            val upcomingItems = (scheduledPayments.map { it.description to it.nextPaymentDate } + 
                               loans.map { it.loanName to it.nextPaymentDate })
                               .sortedBy { it.second }
                               .take(5)

            if (upcomingItems.isEmpty()) {
                item {
                    Text(
                        "No upcoming payments scheduled.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                items(upcomingItems) { (name, date) ->
                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val isSoon = date - System.currentTimeMillis() < 2 * 24 * 60 * 60 * 1000 // 2 days

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSoon) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (isSoon) Icons.Default.Warning else Icons.Default.Event,
                                    contentDescription = null,
                                    tint = if (isSoon) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(name, style = MaterialTheme.typography.bodyLarge)
                            }
                            Text(
                                sdf.format(Date(date)),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSoon) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun CompactActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = title, tint = color)
            Text(title, style = MaterialTheme.typography.labelLarge, color = color)
        }
    }
}
