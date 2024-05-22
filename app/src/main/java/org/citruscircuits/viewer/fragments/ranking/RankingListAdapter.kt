package org.citruscircuits.viewer.fragments.ranking

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.data.getTeamObjectByKey
import java.util.regex.Pattern

/**
 * Custom list adapter class with aq object handling to display the custom cell for the match schedule.
 */
class RankingListAdapter(activity: Activity, private val listContents: List<String>) : BaseAdapter() {
    private val inflater = LayoutInflater.from(activity)

    /**
     * @return The size of the match schedule.
     */
    override fun getCount() = listContents.size

    /**
     * @return The match object given the match number.
     */
    override fun getItem(position: Int) = listContents[position]

    /**
     * @return The position of the cell.
     */
    override fun getItemId(position: Int) = position.toLong()

    /**
     * Populates the elements of the custom cell.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        val rowView: View?
        if (convertView == null) {
            rowView = inflater.inflate(R.layout.seeding_cell, parent, false)
            viewHolder = ViewHolder(rowView)
            rowView.tag = viewHolder
        } else {
            rowView = convertView
            viewHolder = rowView.tag as ViewHolder
        }
        viewHolder.tvTeamNumber.text = listContents[position]
        val regex: Pattern = Pattern.compile("[0-9]+" + Regex.escape(".") + "[0-9]+")
        viewHolder.tvDatapointOne.text = getTeamObject("current_rank", position)
        val currentAvgRps = getTeamObject(
            "current_avg_rps", position
        )
        viewHolder.tvDatapointTwo.text = if (currentAvgRps?.let { regex.matcher(it).matches() } == true) {
            "%.2f".format(currentAvgRps.toFloat())
        } else {
            currentAvgRps
        }
        viewHolder.tvDatapointThree.text = getTeamObject("current_rps", position)
        val predictedRps = getTeamObject("predicted_rps", position)
        viewHolder.tvDatapointFour.text = if (predictedRps?.let { regex.matcher(it).matches() } == true) {
            "%.1f".format(predictedRps.toFloat())
        } else {
            predictedRps
        }
        viewHolder.tvDatapointFive.text = getTeamObject("predicted_rank", position)
        return rowView!!
    }

    private fun getTeamObject(field: String, position: Int) = getTeamObjectByKey(listContents[position], field)
}

/**
 * View holder class to handle the elements used in the custom cells.
 */
private class ViewHolder(view: View?) {
    val tvTeamNumber: TextView = view?.findViewById(R.id.tv_team_number)!!
    val tvDatapointOne: TextView = view?.findViewById(R.id.tv_datapoint_one)!!
    val tvDatapointTwo: TextView = view?.findViewById(R.id.tv_datapoint_two)!!
    val tvDatapointThree: TextView = view?.findViewById(R.id.tv_datapoint_three)!!
    val tvDatapointFour: TextView = view?.findViewById(R.id.tv_datapoint_four)!!
    val tvDatapointFive: TextView = view?.findViewById(R.id.tv_datapoint_five)!!
}
