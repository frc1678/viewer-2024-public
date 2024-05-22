package org.citruscircuits.viewer.fragments.team_ranking

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_team_ranking.view.lv_team_ranking
import kotlinx.android.synthetic.main.fragment_team_ranking.view.tv_datapoint_header
import kotlinx.android.synthetic.main.team_ranking_cell.view.tv_team_number_ranking
import org.citruscircuits.viewer.MainViewerActivity
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.constants.Translations
import org.citruscircuits.viewer.fragments.team_details.TeamDetailsFragment
import org.citruscircuits.viewer.getRankingList

/**
 * [Fragment] that shows the ranking of a specific datapoint
 */
class TeamRankingFragment : Fragment() {
    companion object {
        const val TEAM_NUMBER = "teamNumber"
        const val DATA_POINT = "dataPoint"
    }

    private var dataPoint: String? = null
    var teamNumber: String? = null
    private var lvAdapter: TeamRankingListAdapter? = null
    private var refreshId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_team_ranking, container, false)
        populateArguments()
        setTextViews(root)
        setupAdapter(root)
        return root
    }

    private fun populateArguments() {
        arguments?.let {
            dataPoint = it.getString(DATA_POINT, Constants.NULL_CHARACTER)
            teamNumber = it.getString(TEAM_NUMBER, Constants.NULL_CHARACTER)
        }
    }

    private fun setTextViews(root: View) {
        if (Translations.ACTUAL_TO_HUMAN_READABLE.containsKey(dataPoint?.removePrefix("lfm_"))) {
            if (
                Constants.FIELDS_TO_BE_DISPLAYED_LFM.contains(dataPoint) &&
                !Constants.FIELDS_TO_BE_DISPLAYED_TEAM_DETAILS.contains(dataPoint)
            ) {
                root.tv_datapoint_header.text =
                    "L4M " + Translations.ACTUAL_TO_HUMAN_READABLE[dataPoint?.removePrefix("lfm_")]
            } else {
                root.tv_datapoint_header.text =
                    Translations.ACTUAL_TO_HUMAN_READABLE[dataPoint?.removePrefix("lfm_")]
            }
        } else {
            root.tv_datapoint_header.text = dataPoint
        }
    }

    private fun setupAdapter(root: View) {
        lvAdapter = TeamRankingListAdapter(
            requireActivity(),
            teamNumber,
            dataPoint!!,
            getRankingList(datapoint = dataPoint!!),
            dataPoint in Constants.PIT_DATA || dataPoint == "shoot_specific_area_only" ||
                    dataPoint!!.contains("compatible_auto")
        )
        if (refreshId == null) {
            refreshId = MainViewerActivity.refreshManager.addRefreshListener {
                Log.d("data-refresh", "Updated: team-ranking")
                lvAdapter?.updateItems(getRankingList(datapoint = dataPoint!!))
            }
        }
        root.lv_team_ranking.adapter = lvAdapter
        root.lv_team_ranking.setOnItemClickListener { _, view, _, _ ->
            val teamDetailsFragmentTransaction = parentFragmentManager.beginTransaction()
            val teamDetailsFragment = TeamDetailsFragment()
            teamDetailsFragment.arguments = bundleOf(
                Constants.TEAM_NUMBER to view.tv_team_number_ranking.text.toString(), "LFM" to false
            )
            teamDetailsFragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            teamDetailsFragmentTransaction.addToBackStack(null).replace(
                (root.parent as ViewGroup).id, teamDetailsFragment
            ).commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MainViewerActivity.refreshManager.removeRefreshListener(refreshId)
    }
}
