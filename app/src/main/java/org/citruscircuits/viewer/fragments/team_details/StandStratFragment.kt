package org.citruscircuits.viewer.fragments.team_details

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import co.yml.charts.common.extensions.isNotNull
import org.citruscircuits.viewer.StartupActivity
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.constants.Translations
import org.citruscircuits.viewer.data.getStandStrategistNotesByKey

class StandStratFragment(val teamNumber: String) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply { setContent { StandStratUI() } }

    @Composable
    fun StandStratUI() {
        // Displays each Stand Strategist Notes datapoint
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(Constants.STAND_STRAT_NOTES_DATA) { datapoint ->
                Translations.ACTUAL_TO_HUMAN_READABLE[datapoint]?.let { Header(it) }
                if (Constants.USE_TEST_DATA) BodyText("Nathan", teamNumber, datapoint)
                else {
                    if (StartupActivity.standStratData.isNotNull() && StartupActivity.standStratData.isNotEmpty()) {
                        if (StartupActivity.standStratUsernames!!.isNotNull() && StartupActivity.standStratUsernames!!.isNotEmpty()) {
                            // Displays each username's data
                            for (username in StartupActivity.standStratUsernames!!) {
                                BodyText(username, teamNumber, datapoint)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp).fillMaxWidth())
            }
        }
    }

    @Composable
    fun Header(headerText: String) = Text(headerText, fontSize = 22.sp, fontWeight = FontWeight.Bold)

    @Composable
    fun BodyText(username: String, teamNumber: String, datapoint: String) {
        val dataPointNotes =
            getStandStrategistNotesByKey(username = username, teamNumber = teamNumber, field = datapoint)
        if (dataPointNotes.isNotBlank() && dataPointNotes.isNotEmpty() && dataPointNotes != "" && dataPointNotes != "null") {
            Text("->  $dataPointNotes", fontSize = 15.sp)
        }
    }
}