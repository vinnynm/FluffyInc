package com.enigma.fluffyinc.components.piechart

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.charts.DonutPieChart
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import kotlin.random.Random

val pieChartConfig = PieChartConfig(
    isAnimationEnable = true,
    showSliceLabels = false,
    animationDuration = 1500
)

val donutChartConfig = PieChartConfig(
    labelVisible = true,
    strokeWidth = 120f,  // This creates the donut hole
    isAnimationEnable = true,
    showSliceLabels = true,
    activeSliceAlpha = .9f
)


data class PieDataItem(
    val label:String,
    val value:Float = 0f,
    val color: Color = Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
){
    constructor(
        label:String,
        value: Int,
        color: Color = Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))

    ) : this(
        label = label,
        value = value.toFloat(),
        color = color
    )

    fun toPieData(): PieChartData.Slice {
       return PieChartData.Slice(
            label = label,
            value = value,
            color = color
        )
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PieChart(
    chartName: String ="",
    pieChartData: List<PieDataItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = chartName,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        DonutPieChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            pieChartData = PieChartData(
                slices = pieChartData.map { it.toPieData() },
                plotType = PlotType.Pie
            ),
            pieChartConfig = pieChartConfig
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DonutChart(
    chartName: String = "",
    donutChartData: List<PieDataItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = chartName,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        DonutPieChart(
            modifier =modifier
                .fillMaxWidth()
                .height(500.dp),
            pieChartData = PieChartData(
                slices = donutChartData.map { it.toPieData() },
                plotType = PlotType.Donut
            ),
            pieChartConfig = donutChartConfig
        )
    }

}