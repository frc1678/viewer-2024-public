package org.citruscircuits.viewer.fragments.user_preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.fragment_user_pref.view.lv_user_datapoints
import kotlinx.android.synthetic.main.fragment_user_pref.view.reset_button
import kotlinx.android.synthetic.main.fragment_user_pref.view.select_all_button
import kotlinx.android.synthetic.main.fragment_user_pref.view.user_datapoints_header
import org.citruscircuits.viewer.MainViewerActivity.UserDataPoints
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.constants.Constants
import java.io.InputStreamReader
import java.util.Locale

/**
 * Page to select the user's preference of datapoints to be displayed. Navigated to from Preferences page.
 */
class UserPreferencesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_user_pref, container, false)
        val userName = UserDataPoints.contents?.get("selected")?.asString
        root.user_datapoints_header.text = when (userName) {
            "OTHER" -> "User's Datapoints"
            "STAND STRATEGIST" -> "Stand Strategist's Datapoints"
            else -> "${
                userName?.lowercase(Locale.getDefault())
                    ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            }'s Datapoints"
        }
        updateUserDataPointsListView(root)
        // Reset a user's preferences
        root.reset_button.setOnClickListener {
            // Get default preferences for a user
            val defaultsJsonArray: JsonArray = JsonParser.parseReader(
                InputStreamReader(context?.resources?.openRawResource(R.raw.default_prefs))
            ).asJsonObject.get(userName).asJsonArray
            // Remove the user's datapoints
            UserDataPoints.contents?.remove(userName)
            // Add back the default ones (wiping all changes)
            UserDataPoints.contents?.add(userName, defaultsJsonArray)
            // Update User Preferences File
            UserDataPoints.write()
            root.lv_user_datapoints.invalidateViews()
            updateUserDataPointsListView(root)
        }
        //Onclick listener to select all
        root.select_all_button.setOnClickListener {
            val newJsonArray = JsonArray()
            for (datapoint in Constants.FIELDS_TO_BE_DISPLAYED_TEAM_DETAILS) newJsonArray.add(
                datapoint
            )
            for (datapoint in Constants.FIELDS_TO_BE_DISPLAYED_MATCH_DETAILS_PLAYED) newJsonArray.add(
                datapoint
            )
            UserDataPoints.contents?.remove(userName)
            UserDataPoints.contents?.add(userName, newJsonArray)
            UserDataPoints.write()
            // Rebuild views to reload the page
            root.lv_user_datapoints.invalidateViews()
            updateUserDataPointsListView(root)
        }
        return root
    }

    /**Populates the list view with the adapter that contains all the user's datapoints*/
    private fun updateUserDataPointsListView(root: View) {
        val dataPointsDisplay =
            listOf("TEAM") + Constants.FIELDS_TO_BE_DISPLAYED_TEAM_DETAILS + listOf("TIM") + Constants.FIELDS_TO_BE_DISPLAYED_MATCH_DETAILS_PLAYED
        val adapter = UserPreferencesAdapter(
            context = requireActivity(),
            datapointsDisplayed = dataPointsDisplay - listOf(
                "See Matches",
                "Stand Strat Notes",
                "Notes Label",
                "Notes"
            )
        )
        root.lv_user_datapoints.adapter = adapter
    }
}