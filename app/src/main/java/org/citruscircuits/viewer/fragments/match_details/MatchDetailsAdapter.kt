package org.citruscircuits.viewer.fragments.match_details

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.match_details_cell.view.tv_datapoint_name
import kotlinx.android.synthetic.main.match_details_cell.view.tv_team_five_md
import kotlinx.android.synthetic.main.match_details_cell.view.tv_team_four_md
import kotlinx.android.synthetic.main.match_details_cell.view.tv_team_one_md
import kotlinx.android.synthetic.main.match_details_cell.view.tv_team_six_md
import kotlinx.android.synthetic.main.match_details_cell.view.tv_team_three_md
import kotlinx.android.synthetic.main.match_details_cell.view.tv_team_two_md
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.constants.Translations
import org.citruscircuits.viewer.data.getStandStrategistTIMDataByKey
import org.citruscircuits.viewer.data.getStandStrategistTeamDataByKey
import org.citruscircuits.viewer.data.getTIMDataValueByMatch
import org.citruscircuits.viewer.data.getTeamDataValue
import org.citruscircuits.viewer.fragments.ranking.RankingFragment
import org.citruscircuits.viewer.fragments.team_details.AutoPathsFragment
import org.citruscircuits.viewer.fragments.team_ranking.TeamRankingFragment

/**
 * Adapter for the match details datapoint list
 */
class MatchDetailsAdapter(
    private val context: FragmentActivity,
    private val dataPointsDisplay: List<String>,
    private val matchNumber: Int,
    private val teamNumbers: List<String>,
    private val hasActualData: Boolean
) : BaseAdapter() {
    init {
        Log.d("data-refresh", "created match details adapter: teamNumbers: $teamNumbers")
    }

    private val inflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    /**
     * @return The number of data points to be displayed.
     */
    override fun getCount() = dataPointsDisplay.size

    /**
     * @return The specific data point given the position of the data point.
     */
    override fun getItem(position: Int) = dataPointsDisplay[position]

    /**
     * @return The position of the cell.
     */
    override fun getItemId(position: Int) = position.toLong()

    /** Returns: Populated View for the Fragment to display.*/
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        //val pickabilityFragment = PickabilityFragment()
        val rankingFragment = RankingFragment()
        //Creates the View from the match details resource file
        val rowView = inflater.inflate(R.layout.match_details_cell, parent, false)
        rowView.tv_datapoint_name.text =
            Translations.ACTUAL_TO_HUMAN_READABLE[dataPointsDisplay[position]]
                ?: dataPointsDisplay[position]
        // Creates placeholders for all the category names e.g., Teleop, Auto, etc and specifically formats them
        if (dataPointsDisplay[position] in Constants.CATEGORY_NAMES) {
            val noWidth = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f)
            val allWidth = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            rowView.tv_datapoint_name.gravity = Gravity.CENTER_HORIZONTAL
            rowView.tv_datapoint_name.setTextColor(ContextCompat.getColor(context, R.color.Black))
            rowView.tv_datapoint_name.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.LightGray
                )
            )
            // Set that row to be the full width for the category name, and set everything else to be minimized
            rowView.tv_datapoint_name.layoutParams = allWidth
            rowView.tv_team_one_md.layoutParams = noWidth
            rowView.tv_team_two_md.layoutParams = noWidth
            rowView.tv_team_three_md.layoutParams = noWidth
            rowView.tv_team_four_md.layoutParams = noWidth
            rowView.tv_team_five_md.layoutParams = noWidth
            rowView.tv_team_six_md.layoutParams = noWidth
            rowView.tv_team_one_md.text = ""
            rowView.tv_team_two_md.text = ""
            rowView.tv_team_three_md.text = ""
            rowView.tv_team_four_md.text = ""
            rowView.tv_team_five_md.text = ""
            rowView.tv_team_six_md.text = ""
        } else {
            val textViews = listOf<TextView>(
                rowView.tv_team_one_md,
                rowView.tv_team_two_md,
                rowView.tv_team_three_md,
                rowView.tv_team_four_md,
                rowView.tv_team_five_md,
                rowView.tv_team_six_md
            )
            // Populates the rest of the datapoints of the fragment, does this 6 times for each team in the match
            for (i in 0..5) {
                // Get team data for that team if the match hasn't been played
                textViews[i].text =
                    if (!hasActualData) getTeamValue(teamNumbers[i], dataPointsDisplay[position])
                    // Exception for team datapoints that don't match up with grosbeak datapoint names (competition hotfix)
                    else if (
                        dataPointsDisplay[position] == "driver_ability_tim" ||
                        dataPointsDisplay[position] == "current_avg_rps_tim" ||
                        dataPointsDisplay[position] == "driver_ability" ||
                        dataPointsDisplay[position] == "current_avg_rps" ||
                        dataPointsDisplay[position] == "compatible_auto_spike" ||
                        dataPointsDisplay[position] == "compatible_auto_far"
                    ) {
                        val teamData = getTeamDataValue(
                            teamNumbers[i],
                            dataPointsDisplay[position].replace("_tim", "")
                        )
                        if (teamData != Constants.NULL_CHARACTER) "%.1f".format(teamData.toFloatOrNull())
                        else Constants.NULL_CHARACTER
                    }
                    // Exceptions for defense rating, which can only range from 0 - 5, -1 means no defense in that match
                    else if (dataPointsDisplay[position] == "defense_rating") {
                        val value = getStandStrategistTIMDataByKey(
                            teamNumbers[i],
                            matchNumber.toString(),
                            "defense_rating"
                        )
                        if (value == "-1") "N/A" else value
                    } else if (dataPointsDisplay[position] == "avg_defense_rating") {
                        val value =
                            getStandStrategistTeamDataByKey(teamNumbers[i], "avg_defense_rating")
                        if (value == "-1") "N/A" else value
                    }
                    // Exception for incap time, which needs to be pulled from team in match data
                    else if (dataPointsDisplay[position] == "incap_time") {
                        getTIMDataValueByMatch(
                            matchNumber.toString(),
                            teamNumbers[i],
                            dataPointsDisplay[position]
                        ) ?: Constants.NULL_CHARACTER
                    }
                    // Otherwise just pull the team in match data with the datapoint name without "tim" in it
                    else {
                        getTIMDataValueByMatch(
                            matchNumber.toString(),
                            teamNumbers[i],
                            dataPointsDisplay[position].replace("_tim", "")
                        ) ?: Constants.NULL_CHARACTER
                    }
                /* If the match hasn't been played and the datapoint is leave, just display if it isn't equal to 0
                 (if a team has left the starting line in past matches) */
                if (!hasActualData && dataPointsDisplay[position] == "leave") {
                    textViews[i].text = (textViews[i].text != "0").toString()
                }
                textViews[i].setOnLongClickListener {
                    if (dataPointsDisplay[position] in Constants.AUTO_DATAPOINTS) {
                        // Displays the Auto Paths for the team
                        context.supportFragmentManager.beginTransaction()
                            .addToBackStack(null)
                            .replace(R.id.nav_host_fragment, AutoPathsFragment().apply {
                                arguments = bundleOf(Constants.TEAM_NUMBER to teamNumbers[i])
                            }).commit()
                    }
                    return@setOnLongClickListener true
                }
            }
            rowView.setOnLongClickListener {
                if (dataPointsDisplay[position] == "current_avg_rps_tim" || dataPointsDisplay[position] == "current_avg_rps") {
                    // Opens the Rankings page
                    context.supportFragmentManager.beginTransaction().addToBackStack(null)
                        .replace(R.id.nav_host_fragment, rankingFragment, "rankings").commit()
                } else if (Constants.TIM_TO_TEAM[dataPointsDisplay[position]] in Constants.RANKABLE_FIELDS) {
                    // Opens the rankings list for the corresponding team datapoint
                    val teamRankingFragment = TeamRankingFragment()
                    val teamRankingFragmentTransaction =
                        context.supportFragmentManager.beginTransaction()
                    teamRankingFragment.arguments = bundleOf(
                        TeamRankingFragment.DATA_POINT to Constants.TIM_TO_TEAM[dataPointsDisplay[position]],
                        TeamRankingFragment.TEAM_NUMBER to null
                    )
                    teamRankingFragmentTransaction.addToBackStack(null).replace(
                        it.rootView.findViewById<ViewGroup>(R.id.nav_host_fragment)!!.id,
                        teamRankingFragment
                    ).commit()
                    println(Constants.TIM_TO_TEAM[dataPointsDisplay[position]])
                } else if (dataPointsDisplay[position] in Constants.RANKABLE_FIELDS) {
                    // Opens the rankings list for the corresponding team datapoint
                    val teamRankingFragment = TeamRankingFragment()
                    val teamRankingFragmentTransaction =
                        context.supportFragmentManager.beginTransaction()
                    teamRankingFragment.arguments = bundleOf(
                        TeamRankingFragment.DATA_POINT to dataPointsDisplay[position],
                        TeamRankingFragment.TEAM_NUMBER to null
                    )
                    teamRankingFragmentTransaction.addToBackStack(null).replace(
                        it.rootView.findViewById<ViewGroup>(R.id.nav_host_fragment)!!.id,
                        teamRankingFragment
                    ).commit()
                    println(dataPointsDisplay[position])
                }
                return@setOnLongClickListener true
            }
        }
        return rowView
    }

    /** @return The datafield obtained from getTeamDataValue() but rounded*/
    private fun getTeamValue(teamNumber: String, field: String): String {
        // If the datafield is a float, round the datapoint.
        // Otherwise, get returned string from getTeamDataValue.
        val regex = Regex("-?\\d+${Regex.escape(".")}\\d+")
        val dataValue = getTeamDataValue(teamNumber, field)
        return if (regex matches dataValue) {
            if (field in Constants.DRIVER_DATA) "%.2f".format(dataValue.toFloat())
            else "%.1f".format(dataValue.toFloat())
        } else getTeamDataValue(teamNumber, field)
    }
}
