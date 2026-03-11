package com.enigma.fluffyinc.apps.finance.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.text.NumberFormat


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensePieChart(data: Map<String, Double>) {
    val total = data.values.sum()
    val colors = listOf(
        Color(0xFFE57373), Color(0xFFBA68C8), Color(0xFF64B5F6),
        Color(0xFF81C784), Color(0xFFFFD54F), Color(0xFFFF8A65)
    )

    Column {
        // Simple pie chart representation using Canvas would go here
        // For now, showing as a list with percentages
        data.entries.forEachIndexed { index, (category, amount) ->
            val percentage = (amount / total * 100).toInt()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(colors[index % colors.size], shape = MaterialTheme.shapes.small)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(category, style = MaterialTheme.typography.bodyMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        NumberFormat.getCurrencyInstance().format(amount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "$percentage%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}