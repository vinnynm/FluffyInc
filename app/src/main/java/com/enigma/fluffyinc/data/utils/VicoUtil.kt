package com.enigma.fluffyinc.data.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.HorizontalLegend
import com.patrykandpatrick.vico.core.common.component.Component
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.Shape
import kotlinx.coroutines.launch


@Composable
fun CartesianChartModelPruse(modifier: Modifier = Modifier) {
    val model = rememberLineCartesianLayer()
    val lineComponent = rememberLineComponent(
        thickness = 1.dp,
        fill = Fill(Color.Green.toArgb())
    )
    val x = {
        val list = mutableListOf<Float>()
        for (i in 1..6){
            list.add(i.toFloat())
        }
        list
    }
    val y =
        x.invoke().map {
                LineCartesianLayerModel.Entry(1*it,2*it)


    }
    val layer = LineCartesianLayerModel(
        listOf(
            y,
            y.map{it->
                LineCartesianLayerModel.Entry(
                    it.x,
                    it.y*2
                )
            },
            (
            y.map{it->
              if (it.x.toFloat() ==2f)
                  LineCartesianLayerModel.Entry(
                    it.x,
                    it.y*2
                )
                else
                  LineCartesianLayerModel.Entry(
                      it.x,
                      it.y*3
                  )
            }.toMutableList()


                    )
        )
    )


    CartesianChartHost(
        chart = rememberCartesianChart(
            layers = listOf(
                rememberLineCartesianLayer(

                )).toTypedArray(),



        ),
        model = CartesianChartModel(
            layer
        ),

    )
}

class ChartViewModel : ViewModel() {
    val modelProducer = CartesianChartModelProducer()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            modelProducer.runTransaction {
                // Column layer data
                columnSeries {
                    series(x = listOf(1, 3, 5), y = listOf(2, 3, 3))
                    series(x = listOf(1, 3, 5), y = listOf(6, 7, 5))
                    series(x = listOf(1, 3, 5), y = listOf(8, 5, 7))
                }

                // Line layer data
                lineSeries {
                    series(x = listOf(1, 3, 5, 8), y = listOf(3, 2, 7, 8))
                    series(9, 3, 1, 5)
                }

            }
        }
    }
}

@Preview
@Composable
private fun Set() {
    CartesianChartModelPruse()
}


@Composable
fun BasicColumnChart(
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier = Modifier
) {

    val valueFormatter = CartesianValueFormatter { value, _, _ ->
        "$${value}"
    }
    CartesianChartHost(
        chart = rememberCartesianChart(
            layers = listOf(
                rememberColumnCartesianLayer(
                    dataLabel = TextComponent(
                        color = Color.Green.toArgb()
                    ),
                    columnCollectionSpacing = 0.dp,
                    verticalAxisPosition = Axis.Position.Vertical.End
                ),
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        LineCartesianLayer.Line(
                            fill = LineCartesianLayer.LineFill.double(
                                topFill = Fill(Color.Red.toArgb()),
                                bottomFill = Fill(Color.Blue.toArgb())
                            ),
                            pointConnector = LineCartesianLayer.PointConnector.cubic(),
                            pointProvider = LineCartesianLayer.PointProvider.single(
                                point = LineCartesianLayer.Point(
                                    component = LineComponent(
                                        fill = Fill(Color.Red.toArgb()),
                                        shape = Shape.Rectangle
                                    )
                                )
                            ),
                            dataLabel = TextComponent(
                                color = Color.Green.toArgb()
                            ),
                        ),
                        LineCartesianLayer.Line(
                            fill = LineCartesianLayer.LineFill.double(
                                topFill = Fill(Color.Magenta.toArgb()),
                                bottomFill = Fill(Color.Blue.toArgb())
                            )
                        )

                    )
                ),
                rememberLineCartesianLayer(),


            ).toTypedArray(),

        ),
        modelProducer = modelProducer,
        modifier = modifier.background(
            color = Color.Gray
        ),

    )
}


@Preview
@Composable
private fun Aspen() {
    val viewmodel = remember { ChartViewModel() }
    BasicColumnChart(
        modelProducer = viewmodel.modelProducer

    )
}