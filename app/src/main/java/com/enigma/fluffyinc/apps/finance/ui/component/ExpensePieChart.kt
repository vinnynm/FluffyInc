package com.enigma.fluffyinc.apps.finance.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import hu.ma.charts.pie.PieChart
import hu.ma.charts.pie.data.PieChartData
import hu.ma.charts.pie.data.PieChartEntry
import androidx.compose.ui.text.AnnotatedString

@Composable
fun ExpensePieChart(data: Map<String, Double>) {
    if (data.isEmpty()) return

    val total = data.values.sum()
    val colors = listOf(
        Color(0xFFE57373), Color(0xFFBA68C8), Color(0xFF64B5F6),
        Color(0xFF81C784), Color(0xFFFFD54F), Color(0xFFFF8A65),
        Color(0xFF4DB6AC), Color(0xFF9575CD), Color(0xFFD4E157)
    )

    // Preparing data for hu.ma.charts
    val pieEntries = data.entries.mapIndexed { index, entry ->
        PieChartEntry(
            label = AnnotatedString(entry.key),
            value = entry.value.toFloat(),
            color = colors[index % colors.size]
        )
    }
    val pieData = PieChartData(entries = pieEntries)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            PieChart(
                data = pieData,
                modifier = Modifier.size(180.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            maxItemsInEachRow = 3
        ) {
            data.entries.forEachIndexed { index, (category, amount) ->
                val percentage = (amount / total * 100).toInt()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(colors[index % colors.size], shape = MaterialTheme.shapes.extraSmall)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$category ($percentage%)",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
