package org.citruscircuits.viewer.fragments.team_details

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleLeft
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import co.yml.charts.common.extensions.isNotNull
import kotlinx.coroutines.launch
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.StartupActivity
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.constants.Translations
import org.citruscircuits.viewer.data.AutoPath
import org.citruscircuits.viewer.data.getStandStrategistNotesByKey
import org.citruscircuits.viewer.fragments.match_details.MatchDetailsFragment

/**
 * [Fragment] for showing the AutoPaths of a given team.
 */
class AutoPathsFragment : Fragment() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        // Get the team number from the arguments
        val teamNumber = requireArguments().getString(Constants.TEAM_NUMBER)!!
        val autoPaths = mutableListOf<Pair<MutableState<String>, MutableState<AutoPath>>>()
        // Add each auto path to the list of auto paths
        StartupActivity.databaseReference!!.auto_paths[teamNumber]?.forEach { (pathNum, path) ->
            Log.d("Auto Path", path.toString())
            autoPaths.add(mutableStateOf(pathNum) to mutableStateOf(path))
        }
        // Sort the auto paths by the number of times they were ran
        autoPaths.sortByDescending { it.second.value.num_matches_ran }
        setContent {
            // Only show the Auto Paths UI if there are AutoPaths
            if (autoPaths.isNotEmpty()) {
                BoxWithConstraints {
                    var showAutoStrategiesPopup by remember { mutableStateOf(false) }
                    // To adjust auto path padding, only change what maxWidth.value is being multiplied by
                    Column(
                        modifier = Modifier.padding(horizontal = (15 * maxWidth.value / 392.72726).dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // The header for the page
                        Row {
                            Text(
                                "Auto Paths for $teamNumber",
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(vertical = 5.dp)
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            // Button to pull up auto path details
                            Button(onClick = { showAutoStrategiesPopup = true }) {
                                Icon(Icons.Default.Info, contentDescription = null)
                            }
                        }
                        // Displays each AutoPath
                        val state = rememberLazyListState()
                        LazyRow(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            state = state,
                            flingBehavior = rememberSnapFlingBehavior(lazyListState = state)
                        ) {
                            items(autoPaths) { path ->
                                var showAutoPathDetailsPopup by remember { mutableStateOf(false) }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Show which matches this AutoPath was ran in
                                    val matchNums = path.second.value.match_numbers_played
                                        ?.replace("[", "")?.replace("]", "")
                                        ?.replace(" ", "")?.split(",")
                                    Row {
                                        Text(text = "Match number(s): ", fontSize = 13.sp)
                                        if (matchNums != null) {
                                            for (matchNum in matchNums.sortedBy { it.toInt() }) {
                                                Text(
                                                    text = "$matchNum, ",
                                                    modifier = Modifier.clickable {
                                                        // Opens the corresponding match details page
                                                        parentFragmentManager.beginTransaction()
                                                            .addToBackStack(null)
                                                            .replace(
                                                                R.id.nav_host_fragment,
                                                                MatchDetailsFragment().apply {
                                                                    matchNum.toIntOrNull()
                                                                        ?.let { int ->
                                                                            arguments =
                                                                                bundleOf(Constants.MATCH_NUMBER to int)
                                                                        }
                                                                }
                                                            ).commit()
                                                    },
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }
                                    }
                                    // Show how many times this AutoPath was ran
                                    Text(
                                        text = "Ran ${path.second.value.num_matches_ran} time(s)",
                                        fontSize = 13.sp
                                    )
                                    // Show whether the team scores leave points in this AutoPath
                                    Text(
                                        text = "Leave: " + if (path.second.value.leave == true) "Yes" else "No",
                                        fontSize = 13.sp
                                    )
                                    // What the numbers that are displayed mean
                                    Text(
                                        text = "# of Successes / # of Attempts",
                                        fontSize = 13.sp
                                    )
                                    // AutoPath display
                                    AutoPath(path.second.value)
                                    // AutoPath number label
                                    val scope = rememberCoroutineScope()
                                    Row(horizontalArrangement = Arrangement.Center) {
                                        Button(
                                            modifier = Modifier.padding(horizontal = 10.dp),
                                            onClick = {
                                                scope.launch {
                                                    state.scrollToItem(state.firstVisibleItemIndex - 1)
                                                }
                                            },
                                            enabled = state.firstVisibleItemIndex > 0
                                        ) {
                                            Icon(
                                                Icons.Default.ArrowCircleLeft,
                                                contentDescription = null
                                            )
                                        }
                                        Text(
                                            text = "Path: #${path.first.value}/${autoPaths.size}",
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                        Button(
                                            modifier = Modifier.padding(horizontal = 10.dp),
                                            onClick = {
                                                scope.launch {
                                                    state.scrollToItem(state.firstVisibleItemIndex + 1)
                                                }
                                            },
                                            enabled = state.firstVisibleItemIndex < autoPaths.size - 1
                                        ) {
                                            Icon(
                                                Icons.Default.ArrowCircleRight,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                    // Button to pull up auto path details
                                    Button(
                                        modifier = Modifier.padding(horizontal = 40.dp),
                                        onClick = { showAutoPathDetailsPopup = true }
                                    ) {
                                        Icon(Icons.Default.RemoveRedEye, contentDescription = null)
                                    }
                                    when (showAutoPathDetailsPopup) {
                                        true -> ViewAutoPathDetails(
                                            path.first.value,
                                            path.second.value
                                        ) {
                                            showAutoPathDetailsPopup = !showAutoPathDetailsPopup
                                        }

                                        else -> {}
                                    }
                                }
                            }
                        }
                        Text("Color Key:")
                        Row {
                            Box(
                                modifier = Modifier
                                    .background(Color.Green)
                                    .wrapContentSize()
                            ) {
                                Text("Rate>=66.67%")
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color.Yellow)
                                    .wrapContentSize()
                            ) {
                                Text("66.67%>Rate>=33.33%")
                            }
                        }
                        Row {
                            Box(
                                modifier = Modifier
                                    .background(Color.Red)
                                    .wrapContentSize()
                            ) {
                                Text("33.33%>Rate")
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color(255, 140, 0))
                                    .wrapContentSize()
                            ) {
                                Text("Had the note but did not use it")
                            }
                        }
                        var colorList = mutableListOf<Color>()
                        var a = 1f
                        for (i in 0..7) {
                            colorList.add(Color(14, 99, 31).copy(alpha = a))
                            a *= 0.75f
                        }
                        Box(
                            modifier = Modifier
                                .background(Brush.horizontalGradient(colorList))
                                .wrapContentSize()
                        ) {
                            Text("First Action -> Last Action")
                        }
                    }
                    when (showAutoStrategiesPopup) {
                        true -> ViewAutoStrategies(teamNumber) {
                            showAutoStrategiesPopup = !showAutoStrategiesPopup
                        }

                        else -> {}
                    }
                }
            } else Text(
                // If there's no auto paths, show a message
                text = "No auto paths found for $teamNumber",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(10.dp)
            )
        }
    }

    @Composable
            /**
             * The dialog/popup that contains a timeline of all auto path actions for a given auto path
             * @param path The auto path being displayed
             */
    fun ViewAutoPathDetails(pathNumber: String, path: AutoPath, onCancelRequest: () -> Unit) {
        // Creates the dialog/popup that displays the details for the auto paths
        Dialog(onDismissRequest = { onCancelRequest() }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.91f)
                    .wrapContentHeight()
                    .padding(12.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                // Displays the details
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Map of intakes to corresponding pairs of scores & score successes
                    val orderedActionList =
                        buildMap {
                            if (path.has_preload == true) {
                                put(
                                    "preload", Pair(
                                        Translations.ACTUAL_TO_HUMAN_READABLE[path.score_1],
                                        path.score_1_successes.toString()
                                    )
                                )
                            }

                            put(
                                Translations.ACTUAL_TO_HUMAN_READABLE[path.intake_position_1.toString()],
                                Pair(
                                    Translations.ACTUAL_TO_HUMAN_READABLE[path.score_2],
                                    path.score_2_successes.toString()
                                )
                            )
                            put(
                                Translations.ACTUAL_TO_HUMAN_READABLE[path.intake_position_2.toString()],
                                Pair(
                                    Translations.ACTUAL_TO_HUMAN_READABLE[path.score_3],
                                    path.score_3_successes.toString()
                                )
                            )
                            put(
                                Translations.ACTUAL_TO_HUMAN_READABLE[path.intake_position_3.toString()],
                                Pair(
                                    Translations.ACTUAL_TO_HUMAN_READABLE[path.score_4],
                                    path.score_4_successes.toString()
                                )
                            )
                            put(
                                Translations.ACTUAL_TO_HUMAN_READABLE[path.intake_position_4.toString()],
                                Pair(
                                    Translations.ACTUAL_TO_HUMAN_READABLE[path.score_5],
                                    path.score_5_successes.toString()
                                )
                            )
                            put(
                                Translations.ACTUAL_TO_HUMAN_READABLE[path.intake_position_5.toString()],
                                Pair(
                                    Translations.ACTUAL_TO_HUMAN_READABLE[path.score_6],
                                    path.score_6_successes.toString()
                                )
                            )
                            put(
                                Translations.ACTUAL_TO_HUMAN_READABLE[path.intake_position_6.toString()],
                                Pair(
                                    Translations.ACTUAL_TO_HUMAN_READABLE[path.score_7],
                                    path.score_7_successes.toString()
                                )
                            )
                            put(
                                Translations.ACTUAL_TO_HUMAN_READABLE[path.intake_position_7.toString()],
                                Pair(
                                    path.score_8, path.score_8_successes.toString()
                                )
                            )
                            put(
                                Translations.ACTUAL_TO_HUMAN_READABLE[path.intake_position_8.toString()],
                                Pair(
                                    Translations.ACTUAL_TO_HUMAN_READABLE[path.score_9],
                                    path.score_9_successes.toString()
                                )
                            )
                        }
                    Text(
                        "Action Timeline:",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(5.dp)
                    )
                    Text(
                        "Path Number: $pathNumber",
                        fontSize = 15.sp,
                        modifier = Modifier.padding(3.dp)
                    )

                    orderedActionList.filter { it.key != "none" }.forEach { (item, score) ->
                        if (item == "preload") Text(
                            text = "Had Preload",
                            fontSize = 15.sp,
                            modifier = Modifier.padding(3.dp)
                        )
                        else Text(
                            text = "Intake: $item",
                            fontSize = 15.sp,
                            modifier = Modifier.padding(3.dp)
                        )
                        if (score.first != "none") {
                            Text(
                                text = "Score: ${score.first}, ${score.second}/${path.num_matches_ran}",
                                fontSize = 15.sp,
                                modifier = Modifier.padding(3.dp)
                            )
                        }
                    }
                    Spacer(
                        modifier = Modifier
                            .height(20.dp)
                            .fillMaxWidth()
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "Tap outside to close",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun ViewAutoStrategies(teamNumber: String, onCancelRequest: () -> Unit) {
        // Creates the dialog/popup that displays the details for the auto paths
        Dialog(onDismissRequest = { onCancelRequest() }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.91f)
                    .wrapContentHeight()
                    .padding(12.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                // Displays the details
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Auto Strategies Notes:",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(5.dp)
                    )
                    @Composable
                    fun BodyText(username: String, teamNumber: String, datapoint: String) {
                        val dataPointNotes =
                            getStandStrategistNotesByKey(
                                username = username,
                                teamNumber = teamNumber,
                                field = datapoint
                            )
                        if (dataPointNotes.isNotBlank() && dataPointNotes.isNotEmpty() && dataPointNotes != "" && dataPointNotes != "null") {
                            Text(
                                text = "-> $dataPointNotes",
                                fontSize = 15.sp,
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(horizontal = 10.dp)
                            )
                        }
                    }
                    if (Constants.USE_TEST_DATA) BodyText("Nathan", teamNumber, "auto_strategies")
                    else {
                        if (StartupActivity.standStratData.isNotNull() && StartupActivity.standStratData.isNotEmpty()) {
                            if (StartupActivity.standStratUsernames!!.isNotNull() && StartupActivity.standStratUsernames!!.isNotEmpty()) {
                                // Displays each username's data
                                for (username in StartupActivity.standStratUsernames!!) {
                                    BodyText(username, teamNumber, "auto_strategies")
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Tap outside to close",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }
    }
}
