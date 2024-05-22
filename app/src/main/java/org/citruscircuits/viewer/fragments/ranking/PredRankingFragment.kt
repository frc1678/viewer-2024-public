package org.citruscircuits.viewer.fragments.ranking

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_ranking.view.btn_switch_pred_rankings
import kotlinx.android.synthetic.main.fragment_ranking.view.btn_switch_rankings
import kotlinx.android.synthetic.main.fragment_ranking.view.lv_ranking
import kotlinx.android.synthetic.main.fragment_ranking.view.tv_datapoint_five
import kotlinx.android.synthetic.main.fragment_ranking.view.tv_datapoint_four
import kotlinx.android.synthetic.main.fragment_ranking.view.tv_datapoint_three
import kotlinx.android.synthetic.main.fragment_ranking.view.tv_datapoint_two
import org.citruscircuits.viewer.MainViewerActivity
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.constants.Translations
import org.citruscircuits.viewer.convertToPredFilteredTeamsList
import org.citruscircuits.viewer.fragments.team_details.TeamDetailsFragment

/**
 * Page for showing predicted rankings
 */
class PredRankingFragment : Fragment() {
    private val teamDetailsFragment = TeamDetailsFragment()
    private var refreshId: String? = null

    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_ranking, container, false)
        context?.resources?.let {
            root.btn_switch_rankings.setBackgroundColor(
                it.getColor(
                    R.color.LightGray,
                    null
                )
            )
        }
        context?.resources?.let {
            root.btn_switch_pred_rankings.setBackgroundColor(
                it.getColor(
                    R.color.ToggleColor,
                    null
                )
            )
        }
        // Headers such as "Current avg RPs, "Pred RPS"
        root.tv_datapoint_two.text =
            Translations.ACTUAL_TO_HUMAN_READABLE[Constants.FIELDS_TO_BE_DISPLAYED_RANKING[1]]
        root.tv_datapoint_three.text =
            Translations.ACTUAL_TO_HUMAN_READABLE[Constants.FIELDS_TO_BE_DISPLAYED_RANKING[2]]
        root.tv_datapoint_four.text =
            Translations.ACTUAL_TO_HUMAN_READABLE[Constants.FIELDS_TO_BE_DISPLAYED_RANKING[3]]
        root.tv_datapoint_five.text =
            Translations.ACTUAL_TO_HUMAN_READABLE[Constants.FIELDS_TO_BE_DISPLAYED_RANKING[0]]
        // Adapter with the actual data
        val adapter = PredRankingListAdapter(
            requireActivity(), convertToPredFilteredTeamsList(MainViewerActivity.teamList)
        )
        // Create a refresher that will repull data every so often
        if (refreshId == null) {
            refreshId = MainViewerActivity.refreshManager.addRefreshListener {
                Log.d("data-refresh", "Updated: pred-ranking")
                adapter.notifyDataSetChanged()
            }
        }
        root.lv_ranking.adapter = adapter
        // Navigate to Team Details page on click
        root.lv_ranking.setOnItemClickListener { _, _, position, _ ->
            val rankingFragmentTransaction = parentFragmentManager.beginTransaction()
            teamDetailsFragment.arguments = bundleOf(
                Constants.TEAM_NUMBER to convertToPredFilteredTeamsList(MainViewerActivity.teamList)[position],
                "LFM" to false
            )
            rankingFragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            rankingFragmentTransaction.addToBackStack(null).replace(
                (requireView().parent as ViewGroup).id, teamDetailsFragment
            ).commit()
        }
        root.btn_switch_pred_rankings.setOnClickListener { toggleToRanking() }
        root.btn_switch_rankings.setOnClickListener { toggleToRanking() }
        return root
    }
    /** Switch between predicted and normal rankings*/
    private fun toggleToRanking() {
        val rankingFragment = RankingFragment()
        parentFragmentManager.beginTransaction().addToBackStack(null)
            .replace(R.id.nav_host_fragment, rankingFragment, "rankings").commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        MainViewerActivity.refreshManager.removeRefreshListener(refreshId)
    }
}