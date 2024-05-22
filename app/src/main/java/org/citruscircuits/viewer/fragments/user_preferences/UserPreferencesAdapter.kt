package org.citruscircuits.viewer.fragments.user_preferences

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.gson.JsonArray
import kotlinx.android.synthetic.main.team_details_cell.view.tv_datapoint_name
import org.citruscircuits.viewer.MainViewerActivity.UserDataPoints
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.constants.Translations

/**
 * Adapter for the user preferences list
 */
class UserPreferencesAdapter(
    private val context: FragmentActivity, private val datapointsDisplayed: List<String>
) : BaseAdapter() {
    /** All possible datapoints*/
    private val fullDatapoints =
        listOf("TEAM") + Constants.FIELDS_TO_BE_DISPLAYED_TEAM_DETAILS + listOf("TIM") + Constants.FIELDS_TO_BE_DISPLAYED_MATCH_DETAILS_PLAYED

    /** Datapoints overlapping with chosen ones */
    private lateinit var intersectDatapoints: Set<String>

    /** Which user is selected */
    private var userName = UserDataPoints.contents?.get("selected")?.asString

    /** Datapoints a user has in their preferences*/
    private var chosenDatapoints: MutableSet<String> =
        UserDataPoints.contents!!.get(userName).asJsonArray.map { it.asString }.toMutableSet()

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount() = datapointsDisplayed.size

    /**
     * @return The specific data point given the position of the data point.
     */
    override fun getItem(position: Int) = datapointsDisplayed[position]

    /**
     * @return The position of the cell.
     */
    override fun getItemId(position: Int) = position.toLong()

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val datapointName = getItem(position)
        val rowView = inflater.inflate(R.layout.user_pref_cell, parent, false)
        var isGreen = false
        rowView.tv_datapoint_name.text =
            Translations.ACTUAL_TO_HUMAN_READABLE[datapointName] ?: datapointName
        // Specific formatting for category headers
        if (datapointsDisplayed[position] in Constants.CATEGORY_NAMES) {
            rowView.tv_datapoint_name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28F)
            rowView.tv_datapoint_name.gravity = Gravity.CENTER_HORIZONTAL
            val allWidth = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            rowView.tv_datapoint_name.layoutParams = allWidth
            if (datapointsDisplayed[position] == "TEAM" || datapointsDisplayed[position] == "TIM") {
                rowView.setBackgroundColor(ContextCompat.getColor(context, R.color.DarkGray))
            } else rowView.tv_datapoint_name.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.LightGray
                )
            )
            rowView.tv_datapoint_name.setTextColor(ContextCompat.getColor(context, R.color.Black))
            chosenDatapoints.add(datapointName)
            rowView.isEnabled = false
        }
        // Color all datapoints that match up with Constants
        for (datapoint in chosenDatapoints) {
            if (datapoint == datapointName && datapointsDisplayed[position] !in Constants.CATEGORY_NAMES) {
                rowView.setBackgroundColor(ContextCompat.getColor(context, R.color.ElectricGreen))
                isGreen = true
            }
        }
        // Adds/removes a datapoint from a user's preferences
        rowView.setOnClickListener {
            isGreen = if (!isGreen) {
                rowView.setBackgroundColor(ContextCompat.getColor(context, R.color.ElectricGreen))
                chosenDatapoints.add(datapointName)
                true
            } else {
                rowView.setBackgroundColor(ContextCompat.getColor(context, R.color.White))
                chosenDatapoints.remove(datapointName)
                false
            }
            // Get the chosen datapoints (not sure why we use intersect)
            intersectDatapoints = fullDatapoints intersect chosenDatapoints
            val jsonArray = JsonArray()
            for (datapoint in intersectDatapoints) {
                jsonArray.add(datapoint)
            }
            // Update the user's datapoint preferences
            UserDataPoints.contents?.remove(userName)
            UserDataPoints.contents?.add(userName, jsonArray)
            UserDataPoints.write()
        }
        return rowView
    }
}
