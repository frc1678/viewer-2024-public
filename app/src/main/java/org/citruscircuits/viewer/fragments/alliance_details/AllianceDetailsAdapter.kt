package org.citruscircuits.viewer.fragments.alliance_details

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import co.yml.charts.common.extensions.isNotNull
import kotlinx.android.synthetic.main.alliance_details_cell.view.alliance_details_alliance_num
import kotlinx.android.synthetic.main.alliance_details_cell.view.alliance_details_auto_score
import kotlinx.android.synthetic.main.alliance_details_cell.view.alliance_details_endgame_score
import kotlinx.android.synthetic.main.alliance_details_cell.view.alliance_details_team1
import kotlinx.android.synthetic.main.alliance_details_cell.view.alliance_details_team2
import kotlinx.android.synthetic.main.alliance_details_cell.view.alliance_details_team3
import kotlinx.android.synthetic.main.alliance_details_cell.view.alliance_details_tele_score
import kotlinx.android.synthetic.main.alliance_details_cell.view.alliance_details_total_score
import kotlinx.android.synthetic.main.alliance_details_cell.view.checkbox_strike
import kotlinx.android.synthetic.main.alliance_details_cell.view.playoff_alliances
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.citruscircuits.viewer.MainViewerActivity
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.StartupActivity
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.data.getPredictedAllianceDataByKey


/**
 * Adapter for elims alliance details page
 */
class AllianceDetailsAdapter(private val context: FragmentActivity) : BaseAdapter() {

    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount() = StartupActivity.databaseReference?.alliance?.size!!
    override fun getItem(position: Int) =
        StartupActivity.databaseReference?.alliance?.get("${position + 1}")

    override fun getItemId(position: Int) = position.toLong()

    /**
     * Populates the predicted data for each elim alliance
     *
     * Sets the view to ? if data is null
     */
    @SuppressLint("ViewHolder", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView = inflater.inflate(R.layout.alliance_details_cell, parent, false)
        Log.e("Alliance Number", "$position")
        val allianceNumber = "${position + 1}"
        Log.e("Alliance Number", allianceNumber)

        rowView.alliance_details_alliance_num.text = "$allianceNumber\n\n${if (position in 0..7) "3rd" else "4th"}"
        Log.e("Alliance Row", allianceNumber)

        // Gets the team number for each pick on an alliance
        val picks = Json.parseToJsonElement(getPredictedAllianceDataByKey(
            allianceNumber.toInt(), "picks"
        )!!.filter { it != '\'' }).jsonArray
        Log.e("Alliance Picks", picks.toString())

        // Creates a list of every team text in the given alliance
        val teamTextList = listOf(
            rowView.alliance_details_alliance_num,
            rowView.alliance_details_team1,
            rowView.alliance_details_team2,
            rowView.alliance_details_team3
        )

        Log.e("Alliance Text", "${picks[0].jsonPrimitive.content}, ${picks[1].jsonPrimitive.content}, ${picks[2].jsonPrimitive.content}")
        // Sets the team number for each team text according to their pick order
        rowView.alliance_details_team1.text = picks[0].jsonPrimitive.content
        rowView.alliance_details_team2.text = picks[1].jsonPrimitive.content
        rowView.alliance_details_team3.text = picks[2].jsonPrimitive.content
        Log.e("Alliance Text", "${rowView.alliance_details_team1.text}, ${rowView.alliance_details_team2.text}, ${rowView.alliance_details_team3.text}")

        // Sets the text for all the predicted data for the given alliance
        rowView.alliance_details_auto_score.text = if (getPredictedAllianceDataByKey(allianceNumber.toInt(), "predicted_score_auto")?.toFloat().isNotNull()) "%.2f".format(
            getPredictedAllianceDataByKey(allianceNumber.toInt(), "predicted_score_auto")?.toFloatOrNull()
        ) else Constants.NULL_CHARACTER

        rowView.alliance_details_tele_score.text = if (getPredictedAllianceDataByKey(allianceNumber.toInt(), "predicted_score_tele")?.toFloat().isNotNull()) "%.2f".format(
            getPredictedAllianceDataByKey(allianceNumber.toInt(), "predicted_score_tele")?.toFloatOrNull()
        ) else Constants.NULL_CHARACTER

        rowView.alliance_details_endgame_score.text = if (getPredictedAllianceDataByKey(allianceNumber.toInt(), "predicted_score_endgame")?.toFloat().isNotNull()) "%.2f".format(
            getPredictedAllianceDataByKey(allianceNumber.toInt(), "predicted_score_endgame")?.toFloatOrNull()
        ) else Constants.NULL_CHARACTER

        rowView.alliance_details_total_score.text = if (getPredictedAllianceDataByKey(allianceNumber.toInt(), "predicted_score")?.toFloat().isNotNull()) "%.2f".format(
            getPredictedAllianceDataByKey(allianceNumber.toInt(), "predicted_score")?.toFloatOrNull()
        ) else Constants.NULL_CHARACTER

        if (MainViewerActivity.eliminatedAlliances.contains(allianceNumber)) {
            for (team in teamTextList) {
                team.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            }
            rowView.playoff_alliances.setBackgroundColor(ContextCompat.getColor(context, R.color.MediumGray))
            rowView.checkbox_strike.isChecked = true
        } else {
            for (team in teamTextList) {
                team.paintFlags = 0
            }
            rowView.playoff_alliances.setBackgroundColor(ContextCompat.getColor(context, R.color.White))
            rowView.checkbox_strike.isChecked = false
        }

        // Strikes through an alliance when the user checks the given checkbox
        rowView.checkbox_strike.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                MainViewerActivity.eliminatedAlliances.add(allianceNumber)
            } else {
                MainViewerActivity.eliminatedAlliances.remove(allianceNumber)
            }
            MainViewerActivity.EliminatedAlliances.input()

            if (MainViewerActivity.eliminatedAlliances.contains(allianceNumber)) {
                for (team in teamTextList) {
                    team.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                }
                rowView.playoff_alliances.setBackgroundColor(ContextCompat.getColor(context, R.color.MediumGray))
                rowView.checkbox_strike.isChecked = true
            } else {
                for (team in teamTextList) {
                    team.paintFlags = 0
                }
                rowView.playoff_alliances.setBackgroundColor(ContextCompat.getColor(context, R.color.White))
                rowView.checkbox_strike.isChecked = false
            }
        }
        return rowView
    }
}
