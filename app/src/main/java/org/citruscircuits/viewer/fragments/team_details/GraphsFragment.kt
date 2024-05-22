package org.citruscircuits.viewer.fragments.team_details

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.barchart.BarChart
import co.yml.charts.ui.barchart.models.BarChartData
import co.yml.charts.ui.barchart.models.BarData
import co.yml.charts.ui.barchart.models.BarStyle
import co.yml.charts.ui.barchart.models.SelectionHighlightData
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.constants.Translations
import org.citruscircuits.viewer.data.getTIMDataValue
import org.citruscircuits.viewer.databinding.FragmentGraphsBinding
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * [Fragment] for displaying a graph of a TIM data point for a given team.
 */
class GraphsFragment : Fragment() {

    private var _binding: FragmentGraphsBinding? = null

    /**
     * This property is only valid between [onCreateView] and [onDestroyView].
     */
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Creating a binding that allows type safe access to ComposeView
        _binding = FragmentGraphsBinding.inflate(inflater, container, false)
        // Get the team number from the fragment arguments
        val teamNumber = requireArguments().getString(Constants.TEAM_NUMBER, Constants.NULL_CHARACTER)
        // Get the data point name from the fragment arguments
        val dataPoint = requireArguments().getString("datapoint", Constants.NULL_CHARACTER)
        // Get whether stage levels should be shown instead of numbers
        val showingStageLevels = dataPoint.contains("stage")
        val showingTrap = dataPoint.contains("trap")
        val showingBool =
            dataPoint == "played_defense" ||
            dataPoint.contains("park") || dataPoint.contains("leave") ||
            dataPoint.contains("preload") ||
            dataPoint == "spotlight" || dataPoint.contains("broken_mechanism") ||
                    dataPoint.contains("compatible_auto")
        val showingDefenseRating = dataPoint.contains("defense_rating")
        // Get the TIM data for the data point of the team
        val timDataMap = getTIMDataValue(teamNumber, dataPoint)
        // Set the data to be displayed by each bar
        val barData = timDataMap.toList().mapIndexed { i, (_, value) ->
            BarData(
                point = Point(
                    // Match number index
                    x = i.toFloat(),
                    // Value
                    y = if (showingStageLevels) Constants.STAGE_LEVELS
                            .indexOf(value).takeIf { it != -1 }
                            ?.toFloat()?: 0f
                        else if (showingBool) if (value == "T") 1f else 0f
                        else if (showingDefenseRating) (value?.toFloatOrNull() ?: -1f) + 1f
                        else value?.toFloatOrNull() ?: 0f
                ),
                label = "${i + 1}"
            )
        }
        // Sort the data map by highest data value and get the highest value as a float
        val maxValue = timDataMap.toList().sortedBy { it.second?.toFloatOrNull() }.reversed()[0].second?.toFloatOrNull()
        // Set the maximum y range by rounding up the max value to the next multiple of 5
        // Default to 30
        val maxRange = if (showingStageLevels) Constants.STAGE_LEVELS.lastIndex.toFloat()
            else if (showingTrap) 3f
            else if (showingBool) 1f
            else if (showingDefenseRating) 6f
            else if (maxValue != null) if (ceil(maxValue / 5) * 5 == 0f) 30f else ceil(maxValue / 5) * 5
            else 30f
        val ySteps =
            if (showingStageLevels) Constants.STAGE_LEVELS.lastIndex
            else if (showingTrap) 3
            else if (showingBool) 1
            else if (showingDefenseRating) 6
            else 5
        // Prepare the x and y axes with the bar data
        val xAxisData = AxisData.Builder()
            .axisStepSize(30.dp)
            .steps(barData.size - 1)
            .bottomPadding(40.dp)
            .axisLabelAngle(0f)
            .labelData { index -> barData[index].label }
            .build()
        val yAxisData = AxisData.Builder()
            .steps(ySteps)
            .labelAndAxisLinePadding(25.dp)
            .axisOffset(15.dp)
            .topPadding(40.dp)
            .labelData { index ->
                if (showingStageLevels) Translations.ENDGAME_VALUES_TO_READABLE[Constants.STAGE_LEVELS.getOrNull(index).toString()]!!
                else if (showingBool) if (index == 0) "FALSE" else "TRUE"
                else if (showingDefenseRating) if (index != 0) (index - 1).toString() else "N/A"
                else (index * (maxRange / ySteps)).toInt().toString()
            }
            .build()
        // Create the label to be shown when a bar is selected
        val selectionHighlightData = SelectionHighlightData(popUpLabel = { x, y ->
            "QM${timDataMap.toList()[x.toInt()].first}: ${
                if (showingStageLevels) Constants.STAGE_LEVELS.getOrNull(y.roundToInt())
                else if (showingBool) if (y == 0f) "FALSE" else "TRUE"
                else if (showingDefenseRating) if (y.toInt() != 0) "${y - 1}/5" else "N/A"
                else y
            }"
        })
        // Set the page content to the graph
        binding.composeView.setContent {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // The name of the data point
                Text(
                    text = Translations.ACTUAL_TO_HUMAN_READABLE.getOrDefault(dataPoint, dataPoint),
                    modifier = Modifier.padding(10.dp),
                    style = TextStyle(fontSize = 24.sp)
                )
                // The team number
                Text(
                    text = teamNumber,
                    modifier = Modifier.padding(bottom = 6.dp),
                    style = TextStyle(fontSize = 20.sp, color = Color.Gray)
                )
                // The bar chart
                BarChart(
                    modifier = Modifier.fillMaxHeight(0.9f),
                    barChartData = BarChartData(
                        chartData = barData,
                        xAxisData = xAxisData,
                        yAxisData = yAxisData,
                        barStyle = BarStyle(selectionHighlightData = selectionHighlightData),
                    )
                )
                // The y-axis label
                Text(
                    text = "Match Number",
                    modifier = Modifier
                        .padding(10.dp)
                        .weight(1f),
                    style = TextStyle(fontSize = 24.sp)
                )
            }
        }
        return binding.root
    }
}
