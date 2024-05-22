package org.citruscircuits.viewer.fragments.team_details

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.team_details_cell.view.data_bar
import kotlinx.android.synthetic.main.team_details_cell.view.data_bar_reverse
import kotlinx.android.synthetic.main.team_details_cell.view.tv_datapoint_name
import kotlinx.android.synthetic.main.team_details_cell.view.tv_datapoint_value
import kotlinx.android.synthetic.main.team_details_cell.view.tv_ranking
import org.citruscircuits.viewer.MainViewerActivity
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.constants.Translations
import org.citruscircuits.viewer.data.getTeamDataValue
import org.citruscircuits.viewer.fragments.match_schedule.MatchScheduleFragment
import org.citruscircuits.viewer.fragments.notes.NotesFragment
import org.citruscircuits.viewer.fragments.pickability.PickabilityFragment
import org.citruscircuits.viewer.fragments.ranking.RankingFragment
import org.citruscircuits.viewer.fragments.team_ranking.TeamRankingFragment
import org.citruscircuits.viewer.getRankingList
import org.citruscircuits.viewer.getRankingTeam
import java.util.regex.Pattern

/**
 * Custom list adapter class for each list view featured in every
 * [Team Details][TeamDetailsFragment] display.
 */
class TeamDetailsAdapter(
    private val context: FragmentActivity,
    private val dataPointsDisplayed: List<String>,
    private val teamNumber: String,
    private val visualDataBar: Boolean
) : BaseAdapter() {
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    /**
     * @return The number of data points to be displayed.
     */
    override fun getCount() = dataPointsDisplayed.size

    /**
     * @return The specific data point given the position of the data point.
     */
    override fun getItem(position: Int) = dataPointsDisplayed[position]

    /**
     * @return The position of the cell.
     */
    override fun getItemId(position: Int) = position.toLong()

    /**
     * Populates the elements of the custom cell.
     */
    @SuppressLint("ViewHolder", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val graphsFragment = GraphsFragment()
        val modeStartPositionFragment = StartPositionFragment()
        val graphsFragmentArguments = Bundle()
        val modeStartPositionFragmentArguments = Bundle()
        val pickabilityFragment = PickabilityFragment()
        val rankingFragment = RankingFragment()
        val field = getItem(position)
        val regex: Pattern = Pattern.compile("-?" + "[0-9]+" + Regex.escape(".") + "[0-9]+")
        if (field == "Visual Data Bars") return View(context)
        val rowView = inflater.inflate(R.layout.team_details_cell, parent, false)
        var isHeader = false
        /*
        Displays either a readable name if it there is one, or displays the untranslated
        datapoint name
         */
        if (Translations.ACTUAL_TO_HUMAN_READABLE.containsKey(field.removePrefix("lfm_"))) {
            /*
            If the datapoint should be shown in L4M, displays the normal readable name of the
            datapoint except with L4M at the front of the name. Otherwise, shows the readable name.
             */
            if (
                Constants.FIELDS_TO_BE_DISPLAYED_LFM.contains(field) &&
                !Constants.TEAM_AND_LFM_SHARED_DATA_POINTS.contains(field)
            ) {
                rowView.tv_datapoint_name.text = "L4M " + Translations.ACTUAL_TO_HUMAN_READABLE[field.removePrefix("lfm_")]
            } else {
                rowView.tv_datapoint_name.text = Translations.ACTUAL_TO_HUMAN_READABLE[field.removePrefix("lfm_")]
            }
        }
        else {
            rowView.tv_datapoint_name.text = field
        }
        if (field in Constants.CATEGORY_NAMES) {
            isHeader = true
            rowView.tv_datapoint_name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28F)
            rowView.tv_datapoint_name.gravity = Gravity.CENTER_HORIZONTAL
            val noWidth = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f)
            val allWidth = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            rowView.tv_ranking.layoutParams = noWidth
            rowView.tv_datapoint_name.layoutParams = allWidth
            rowView.tv_datapoint_value.layoutParams = noWidth
            // Sets colors and text sizes
            when (field) {
                "See Matches" -> rowView.tv_datapoint_name.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.MediumGray)
                )
                "Notes Label" -> {
                    rowView.tv_datapoint_name.setBackgroundColor(ContextCompat.getColor(context, R.color.Highlighter))
                    rowView.tv_datapoint_name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18F)
                    rowView.tv_datapoint_name.text = "Notes (click below to edit)"
                }
                else -> {
                    rowView.tv_datapoint_name.setBackgroundColor(ContextCompat.getColor(context, R.color.LightGray))
                    rowView.tv_datapoint_name.setTextColor(ContextCompat.getColor(context, R.color.Black))
                }
            }
            rowView.tv_datapoint_value.text = ""
            // Opens the Stand Strat Notes page when clicked
            if (field == "Stand Strat Notes") {
                rowView.setOnClickListener {
                    val standStratFragment = StandStratFragment(teamNumber)
                    context.supportFragmentManager.beginTransaction().addToBackStack(null).replace(
                        it.rootView.findViewById<ViewGroup>(R.id.nav_host_fragment)!!.id, standStratFragment
                    ).commit()
                }
            }
            // Opens the Notes page when clicked, displays the notes
            if (field == "Notes") {
                Log.d("notes", "SETTING UP NOTES CELL IN TEAM DETAILS")
                rowView.tv_datapoint_name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)
                rowView.tv_datapoint_name.setBackgroundColor(ContextCompat.getColor(context, R.color.Highlighter))
                rowView.setOnClickListener {
                    Log.d("notes", "notes button clicked in team details")
                    val notesFragment = NotesFragment()
                    val notesFragmentArgs = Bundle()
                    notesFragmentArgs.putString(Constants.TEAM_NUMBER, teamNumber)
                    notesFragment.arguments = notesFragmentArgs
                    val notesFragmentTransaction = context.supportFragmentManager.beginTransaction()
                    notesFragmentTransaction.addToBackStack(null).replace(
                        it.rootView.findViewById<ViewGroup>(R.id.nav_host_fragment)!!.id, notesFragment
                    ).commit()
                }
                if (MainViewerActivity.notesCache.containsKey(teamNumber)) {
                    rowView.tv_datapoint_name.text = MainViewerActivity.notesCache[teamNumber]
                } else {
                    Log.d("notes", "notesCache does not contain team $teamNumber")
                    rowView.tv_datapoint_name.text = ""
                }
            }
        } else {
            // Gets the value of the datapoint
            val teamDataValue = getTeamDataValue(teamNumber, field)
            // Displays the value
            if (regex.matcher(teamDataValue).matches()) {
                rowView.tv_datapoint_value.text = "%.1f".format(teamDataValue.toFloat())
            } else {
                rowView.tv_datapoint_value.text = teamDataValue
            }
            if (field == "avg_defense_rating") if (teamDataValue == "-1.0") rowView.tv_datapoint_value.text = "N/A"
            if (Constants.PERCENT_DATA.contains(field)) {
                if (teamDataValue != "?") {
                    rowView.tv_datapoint_value.text = "${rowView.tv_datapoint_value.text}%"
                    if (visualDataBar) {
                        (rowView.data_bar.layoutParams as LinearLayout.LayoutParams).weight =
                            teamDataValue.toFloat() / 100
                    }
                }
            }
            // Shows Data Bars if enabled
            if (
                visualDataBar &&
                teamDataValue != Constants.NULL_CHARACTER &&
                (getRankingList(field)?.last()?.value
                    ?: Constants.NULL_CHARACTER) != Constants.NULL_CHARACTER
            ) {
                rowView.data_bar.setBackgroundColor(ContextCompat.getColor(context, R.color.DataBar))
                if (
                    Constants.PIT_DATA.contains(field) ||
                    Constants.FIELDS_TO_BE_DISPLAYED_RANKING.contains(field) ||
                    "pickability" in field || "start" in field || "matches_played" == field ||
                    "shoot_specific_area_only" == field || "compatible_auto" in field
                ) {
                    /*
                    Hides the data bar if the datapoint is a Pit, Rankings, start position, pickability
                    datapoint, or is matches_played
                     */
                    (rowView.data_bar.layoutParams as LinearLayout.LayoutParams).weight = 0F
                    (rowView.data_bar_reverse.layoutParams as LinearLayout.LayoutParams).weight = 0F
                }
                else if (
                    "incap" in field || "foul" in field ||
                    "fail" in field || "cycle_time" in field ||
                    field == "matches_with_broken_mechanism"
                ) {
                    /*
                    Incap, foul, fail, cycle time, and tippiness rankings are reversed,
                    so divides by last value in rankings
                     */
                    (rowView.data_bar_reverse.layoutParams as LinearLayout.LayoutParams).weight =
                        teamDataValue.toFloat() / getRankingList(field).last().value!!.toFloat()
                    (rowView.data_bar.layoutParams as LinearLayout.LayoutParams).weight =
                        1 - (rowView.data_bar_reverse.layoutParams as LinearLayout.LayoutParams).weight
                    rowView.data_bar.setBackgroundColor(ContextCompat.getColor(context, R.color.White))
                    rowView.data_bar_reverse.setBackgroundColor(ContextCompat.getColor(context, R.color.Pink))
                } else if (
                    "max" in field || "avg" in field || "attempt" in field || "success" in field ||
                    Constants.DRIVER_DATA.contains(field) || "defense" in field || "sd_" in field ||
                    "median" in field || field == "parks" || field == "climbs_while_spotlit" ||
                    field == "matches_harmonized" || field == "preload_successes"
                ) {
                    // Changes weight based on how datapoint compares to highest rank of that datapoint
                    (rowView.data_bar.layoutParams as LinearLayout.LayoutParams).weight =
                        teamDataValue.toFloat() / getRankingList(field).first().value!!.toFloat()
                    (rowView.data_bar_reverse.layoutParams as LinearLayout.LayoutParams).weight =
                        1 - (rowView.data_bar.layoutParams as LinearLayout.LayoutParams).weight
                }
            } else {
                (rowView.data_bar.layoutParams as LinearLayout.LayoutParams).weight = 0F
                (rowView.data_bar_reverse.layoutParams as LinearLayout.LayoutParams).weight = 0F
                rowView.data_bar.setBackgroundColor(ContextCompat.getColor(context, R.color.DataBar))
            }
        }
        // Displays the team's rank for the datapoint if needed
        if (field in Constants.RANKABLE_FIELDS) {
            rowView.tv_ranking.text =
                if (
                    field in Constants.PIT_DATA ||
                    "mode_start_position" in field ||
                    (
                        field in Constants.STAND_STRAT_TEAM_DATA &&
                        "rating" !in field
                    ) || "compatible_auto" in field
                )
                    ""
                else getRankingTeam(teamNumber, field)?.placement?.toString() ?: Constants.NULL_CHARACTER
        }
        // Only add Graphable onClick listener if Constants contains the datapoint
        if (Constants.GRAPHABLE.contains(dataPointsDisplayed[position])) {
            rowView.setOnClickListener {
                // Opens the graph for the datapoint
                // If clicking on things about starting position
                if (Constants.STARTING_POSITION_GRAPHING.contains(dataPointsDisplayed[position])) {
                    // Show starting position map
                    modeStartPositionFragmentArguments.putString(Constants.TEAM_NUMBER, teamNumber)
                    modeStartPositionFragmentArguments.putString(
                        "datapoint", Constants.STARTING_POSITION_GRAPHING[dataPointsDisplayed[position]]
                    )
                    modeStartPositionFragment.arguments = modeStartPositionFragmentArguments
                    context.supportFragmentManager.beginTransaction().addToBackStack(null)
                        .replace(R.id.nav_host_fragment, modeStartPositionFragment, "mode_start_position").commit()
                } else {
                    graphsFragmentArguments.putString(Constants.TEAM_NUMBER, teamNumber)
                    // Get the tim datapoint from the team datapoint and add as an argument
                    graphsFragmentArguments.putString(
                        "datapoint", Constants.GRAPHABLE[dataPointsDisplayed[position]]
                    )
                    graphsFragment.arguments = graphsFragmentArguments
                    context.supportFragmentManager.beginTransaction().addToBackStack(null)
                        .replace(R.id.nav_host_fragment, graphsFragment, "graphs").commit()
                }
            }
        }
        // Displays all matches the team was in when clicked
        if (field == "See Matches") {
            rowView.setOnClickListener {
                val matchScheduleFragment = MatchScheduleFragment()
                val matchScheduleFragmentArguments = Bundle()
                val matchScheduleFragmentTransaction = context.supportFragmentManager.beginTransaction()
                matchScheduleFragmentArguments.putString(Constants.TEAM_NUMBER, teamNumber)
                matchScheduleFragment.arguments = matchScheduleFragmentArguments
                matchScheduleFragmentTransaction.addToBackStack(null).replace(
                    it.rootView.findViewById<ViewGroup>(R.id.nav_host_fragment)!!.id, matchScheduleFragment
                ).commit()
            }
        }
        // Most headers don't need on click handlers
        if (!isHeader) {
            // Some fields (eg drivetrain_motor_type) don't need to be rankable
            if (field in Constants.FIELDS_TO_BE_DISPLAYED_RANKING) {
                rowView.setOnLongClickListener {
                    // Opens the Rankings page
                    context.supportFragmentManager.beginTransaction().addToBackStack(null)
                        .replace(R.id.nav_host_fragment, rankingFragment, "rankings").commit()
                    return@setOnLongClickListener true
                }
            }
            else if ("pickability" in field) {
                rowView.setOnLongClickListener {
                    // Opens the Pickability page
                    val ft = context.supportFragmentManager.beginTransaction()
                    if (context.supportFragmentManager.fragments.last().tag != "pickability") ft.addToBackStack(null)
                    ft.replace(R.id.nav_host_fragment, pickabilityFragment, "pickability").commit()
                    return@setOnLongClickListener true
                }
            }
            else if (field in Constants.RANKABLE_FIELDS.keys) {
                // Displays the rankings for the datapoint when long clicked
                rowView.setOnLongClickListener {
                    val teamRankingFragment = TeamRankingFragment()
                    val teamRankingFragmentTransaction = context.supportFragmentManager.beginTransaction()
                    teamRankingFragment.arguments = bundleOf(
                        TeamRankingFragment.DATA_POINT to field, TeamRankingFragment.TEAM_NUMBER to teamNumber
                    )
                    teamRankingFragmentTransaction.addToBackStack(null).replace(
                        it.rootView.findViewById<ViewGroup>(R.id.nav_host_fragment)!!.id, teamRankingFragment
                    ).commit()
                    println(field)
                    return@setOnLongClickListener true
                }
            }
        }
        return rowView
    }
}
