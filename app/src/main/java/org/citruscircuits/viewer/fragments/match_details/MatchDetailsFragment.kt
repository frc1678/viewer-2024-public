package org.citruscircuits.viewer.fragments.match_details

import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.match_details.view.lv_match_details
import kotlinx.android.synthetic.main.match_details.view.tv_header_five
import kotlinx.android.synthetic.main.match_details.view.tv_header_four
import kotlinx.android.synthetic.main.match_details.view.tv_header_label_five
import kotlinx.android.synthetic.main.match_details.view.tv_header_label_four
import kotlinx.android.synthetic.main.match_details.view.tv_header_label_one
import kotlinx.android.synthetic.main.match_details.view.tv_header_label_six
import kotlinx.android.synthetic.main.match_details.view.tv_header_label_three
import kotlinx.android.synthetic.main.match_details.view.tv_header_label_two
import kotlinx.android.synthetic.main.match_details.view.tv_header_one
import kotlinx.android.synthetic.main.match_details.view.tv_header_six
import kotlinx.android.synthetic.main.match_details.view.tv_header_three
import kotlinx.android.synthetic.main.match_details.view.tv_header_two
import kotlinx.android.synthetic.main.match_details.view.tv_match_number_display
import kotlinx.android.synthetic.main.match_details.view.tv_team_five_label
import kotlinx.android.synthetic.main.match_details.view.tv_team_four_label
import kotlinx.android.synthetic.main.match_details.view.tv_team_one_label
import kotlinx.android.synthetic.main.match_details.view.tv_team_six_label
import kotlinx.android.synthetic.main.match_details.view.tv_team_three_label
import kotlinx.android.synthetic.main.match_details.view.tv_team_two_label
import kotlinx.android.synthetic.main.match_details.view.tv_win_chance_blue
import kotlinx.android.synthetic.main.match_details.view.tv_win_chance_label_blue
import kotlinx.android.synthetic.main.match_details.view.tv_win_chance_label_red
import kotlinx.android.synthetic.main.match_details.view.tv_win_chance_red
import org.citruscircuits.viewer.MainViewerActivity
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.constants.Translations
import org.citruscircuits.viewer.data.getAllianceInMatchObjectByKey
import org.citruscircuits.viewer.fragments.team_details.TeamDetailsFragment
import org.citruscircuits.viewer.getMatchSchedule

/**
 * The fragment class for the Match Details display that occurs when you click on a
 * match in the match schedule page.
 */
class MatchDetailsFragment : Fragment() {
    private var matchNumber: Int? = null
    private var hasActualData: Boolean? = null

    private var refreshId: String? = null

    private val teamDetailsFragment = TeamDetailsFragment()
    private val teamDetailsFragmentArguments = Bundle()
    private lateinit var headerDisplay: List<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.let { matchNumber = it.getInt(Constants.MATCH_NUMBER, 0) }
        hasActualData = checkHasActualData()
        //The datapoints for the header at the top displaying the four statistics, score, RPs, and win chance.
        headerDisplay =
            if (hasActualData!!) Constants.FIELDS_TO_BE_DISPLAYED_MATCH_DETAILS_HEADER_PLAYED
            else Constants.FIELDS_TO_BE_DISPLAYED_MATCH_DETAILS_HEADER_NOT_PLAYED
        //Creates a view from the xml file
        val root = inflater.inflate(R.layout.match_details, container, false)
        //Populate the match number and header labels and values
        populateMatchDetailsEssentials(root)

        if (hasActualData as Boolean) {
            //Bold and underline alliance teams that won
            if (getAllianceInMatchObjectByKey(
                    Constants.BLUE,
                    matchNumber.toString(),
                    "won_match"
                ).toBoolean()
            ) {
                for (tv in listOf(
                    root.tv_team_one_label, root.tv_team_two_label, root.tv_team_three_label
                )) {
                    tv.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                    tv.typeface = Typeface.DEFAULT_BOLD
                    tv.textSize = 12f
                }
            } else if (getAllianceInMatchObjectByKey(
                    Constants.RED,
                    matchNumber.toString(),
                    "won_match"
                ).toBoolean()
            ) {
                for (tv in listOf(
                    root.tv_team_four_label, root.tv_team_five_label, root.tv_team_six_label
                )) {
                    tv.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                    tv.typeface = Typeface.DEFAULT_BOLD
                    tv.textSize = 12f
                }
            }
        }
        //populate team numbers
        for (teamNumber in getTeamNumbersXML(root)) {
            // If the index of the team number in the team number list is below 3, it means that the
            // team number is a team from the blue alliance.

            // Get the the team number from the specific match in the match schedule
            if (getTeamNumbersXML(root).indexOf(teamNumber) < 3) {
                teamNumber.text =
                    getMatchSchedule()[matchNumber.toString()]!!.blueTeams[getTeamNumbersXML(root).indexOf(
                        teamNumber
                    )]
            } else {
                teamNumber.text =
                    getMatchSchedule()[matchNumber.toString()]!!.redTeams[getTeamNumbersXML(root).indexOf(
                        teamNumber
                    ) - 3]
            }
        }
        //
        updateTeamListViews(root)
        initTeamNumberClickListeners(root)
        return root
    }

    /** Returns each of the six team's team number xml elements. */
    private fun getTeamNumbersXML(root: View): List<TextView> {
        return listOf<TextView>(
            root.tv_team_one_label,
            root.tv_team_two_label,
            root.tv_team_three_label,
            root.tv_team_four_label,
            root.tv_team_five_label,
            root.tv_team_six_label
        )
    }

    /**
     * @return All the team numbers in a match.
     */
    private fun getTeamNumbersList(root: View): List<String> {
        return listOf(
            root.tv_team_one_label.text.toString(),
            root.tv_team_two_label.text.toString(),
            root.tv_team_three_label.text.toString(),
            root.tv_team_four_label.text.toString(),
            root.tv_team_five_label.text.toString(),
            root.tv_team_six_label.text.toString()
        )
    }

    /**
     * @return The values for the corresponding header labels that lie on the two sides of the match number.
     */
    private fun getHeaderCollection(root: View) = listOf<TextView>(
        root.tv_header_one,
        root.tv_header_two,
        root.tv_header_three,
        root.tv_win_chance_blue,
        root.tv_header_four,
        root.tv_header_five,
        root.tv_header_six,
        root.tv_win_chance_red
    )

    /** @return Each of the labels for the match details header views, e.g., Actual Score, Actual RPs, Win Chance*/
    private fun getHeaderLabelCollection(root: View) = listOf<TextView>(
        root.tv_header_label_one,
        root.tv_header_label_two,
        root.tv_header_label_three,
        root.tv_win_chance_label_blue,
        root.tv_header_label_four,
        root.tv_header_label_five,
        root.tv_header_label_six,
        root.tv_win_chance_label_red
    )

    /** On every team number's specified text view, when the user clicks on the text view it will
    then go to a new TeamDetails page for the given team number. */
    private fun initTeamNumberClickListeners(root: View) {
        val matchDetailsFragmentTransaction = parentFragmentManager.beginTransaction()
        for (tv in getTeamNumbersXML(root)) {
            tv.setOnClickListener {
                teamDetailsFragmentArguments.putString(Constants.TEAM_NUMBER, tv.text.toString())
                teamDetailsFragmentArguments.putBoolean("LFM", false)
                teamDetailsFragment.arguments = teamDetailsFragmentArguments
                matchDetailsFragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                matchDetailsFragmentTransaction.addToBackStack(null).replace(
                    (requireView().parent as ViewGroup).id, teamDetailsFragment
                ).commit()
            }
        }
    }

    /**
     * Updates the adapter for the list view of each team in the match details display.
     */
    private fun updateTeamListViews(root: View) {

        val user = MainViewerActivity.UserDataPoints.contents?.get("selected")?.asString
        val dataPoints = mutableListOf<String>()
        val userDataPoints = MainViewerActivity.UserDataPoints.contents?.get(user)?.asJsonArray
        // Display specific datapoints depending on a user's preferences
        if (userDataPoints != null) {
            for (i in userDataPoints) {
                if (Constants.FIELDS_TO_BE_DISPLAYED_MATCH_DETAILS_PLAYED.contains(i.asString)) {
                    dataPoints.add(i.asString)
                }
            }
        }
        val dataPointsDisplay = dataPoints
        // Set the datapoints to their corresponding team datapoint if the match hasn't been played
        if (!hasActualData!!) {
            for (i in 0 until dataPoints.size) {
                dataPoints[i] = Constants.TIM_TO_TEAM[dataPoints[i]] ?: dataPoints[i]
            }
        }
        // Create the adapter
        val adapter = MatchDetailsAdapter(
            context = requireActivity(),
            dataPointsDisplay = dataPointsDisplay,
            matchNumber = matchNumber!!,
            teamNumbers = getTeamNumbersList(root),
            hasActualData = hasActualData!!
        )
        /* Adds a refresh listener, once refresh() from the RefreshManager class is called, new
        data will be pulled from grosbeak and the function adapter.notifyDataSetChanged() that was passed into
        this refresh listener will be called*/
        if (refreshId == null) {
            refreshId = MainViewerActivity.refreshManager.addRefreshListener {
                Log.d("data-refresh", "Updated: match-details")
                adapter.notifyDataSetChanged()
            }
        }
        root.lv_match_details.adapter = adapter
    }

    /** Prepare the MatchDetails activity by populating each text view and XML element
    with its match-specific information. */
    private fun populateMatchDetailsEssentials(root: View) {

        root.tv_match_number_display.text = matchNumber.toString()
        //loop through each header value
        for (tv in getHeaderCollection(root)) {
            //If the alliance is blue (index less than four)
            if (getHeaderCollection(root).indexOf(tv) < 4) {
                //get the datapoint for the current header value
                val datapoint = headerDisplay[getHeaderCollection(root).indexOf(tv)]
                //get the actual value of the datapoint
                val newText = getAllianceInMatchObjectByKey(
                    Constants.BLUE, matchNumber.toString(), datapoint
                )
                // newText is a string, so we check for the string "null" or if is an actual null then replace the value with "?", 
                // if it isn't, then displays data
                if (newText == null || newText == "null") {
                    tv.text = Constants.NULL_CHARACTER
                } else {
                    val value = (if (hasActualData!!) "%.0f" else "%.1f").format(newText.toFloat())
                    tv.text =
                        if (Constants.PERCENT_DATA.contains(datapoint)) "${value.toFloat() * 100}%" else value
                }
            } else {
                //red alliance
                val datapoint = headerDisplay[getHeaderCollection(root).indexOf(tv) - 4]
                val newText =
                    getAllianceInMatchObjectByKey(Constants.RED, matchNumber.toString(), datapoint)
                if (newText == null || newText == "null") {
                    tv.text = Constants.NULL_CHARACTER
                } else {
                    val value = (if (hasActualData!!) "%.0f" else "%.1f").format(newText.toFloat())
                    tv.text =
                        if (Constants.PERCENT_DATA.contains(datapoint)) "${value.toFloat() * 100}%" else value
                }
            }
        }
        //Populate the header labels
        for (tv in getHeaderLabelCollection(root)) {
            val headerLabelIndex = getHeaderLabelCollection(root).indexOf(tv)
            tv.text = Translations.ACTUAL_TO_HUMAN_READABLE[headerDisplay[headerLabelIndex % 4]]
        }
    }

    /** Checks if there is actual data*/
    private fun checkHasActualData() = getAllianceInMatchObjectByKey(
        Constants.BLUE, matchNumber.toString(), "has_actual_data"
    ).toBoolean() && getAllianceInMatchObjectByKey(
        Constants.RED, matchNumber.toString(), "has_actual_data"
    ).toBoolean()

    /** Removes the refresh listener once the fragment is closed*/
    override fun onDestroy() {
        super.onDestroy()
        MainViewerActivity.refreshManager.removeRefreshListener(refreshId)
    }
}
