package org.citruscircuits.viewer.fragments.team_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.constants.Translations
import org.citruscircuits.viewer.data.getTeamDataValue
import org.citruscircuits.viewer.databinding.FragmentGraphsBinding

/**
 * [Fragment] used for showing intake buttons in [StartPositionFragment]
 */
class StartPositionFragment : Fragment() {

    private var _binding: FragmentGraphsBinding? = null

    /**
     * This property is only valid between [onCreateView] and [onDestroyView].
     */
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGraphsBinding.inflate(inflater, container, false)
        // Get the team number from the fragment arguments
        val teamNumber = requireArguments().getString(Constants.TEAM_NUMBER, Constants.NULL_CHARACTER)
        // Get the data point name from the fragment arguments
        val dataPoint = requireArguments().getString("datapoint", Constants.NULL_CHARACTER)
        // Set the page content to the graph
        binding.composeView.setContent {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
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
                // No Show
                Text(
                    text = "No Show: " + getTeamDataValue(teamNumber, "position_zero_starts"),
                    modifier = Modifier.padding(vertical = 10.dp),
                    style = TextStyle(fontSize = 30.sp, color = Color.Gray)
                )
                // Start position map
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(
                            id = R.drawable.mode_start_position_map
                        ),
                        contentDescription = "Map with start positions",
                        modifier = Modifier.size(850.dp)
                    )
                    Column(modifier = Modifier.padding(bottom = 70.dp, end = 225.dp)) {
                        Text(
                            text = getTeamDataValue(teamNumber, "position_four_starts"),
                            modifier = Modifier.padding(top = 90.dp),
                            style = TextStyle(fontSize = 30.sp, color = Color.Gray)
                        )
                        Text(
                            text = getTeamDataValue(teamNumber, "position_three_starts"),
                            modifier = Modifier.padding(top = 40.dp),
                            style = TextStyle(fontSize = 30.sp, color = Color.Gray)
                        )
                        Text(
                            text = getTeamDataValue(teamNumber, "position_two_starts"),
                            modifier = Modifier.padding(top = 40.dp),
                            style = TextStyle(fontSize = 30.sp, color = Color.Gray)
                        )
                        Text(
                            text = getTeamDataValue(teamNumber, "position_one_starts"),
                            modifier = Modifier.padding(top = 40.dp),
                            style = TextStyle(fontSize = 30.sp, color = Color.Gray)
                        )
                    }
                }
            }
        }
        return binding.root
    }
}
