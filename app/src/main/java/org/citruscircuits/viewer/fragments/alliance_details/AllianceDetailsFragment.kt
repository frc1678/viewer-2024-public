package org.citruscircuits.viewer.fragments.alliance_details

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_alliance_details.view.lv_alliance_details
import org.citruscircuits.viewer.MainViewerActivity
import org.citruscircuits.viewer.R

/**
 * The fragment for the page that displays information about the elims alliances.
 * Not used in 2023.
 * @see R.layout.fragment_alliance_details
 */
class AllianceDetailsFragment : Fragment() {

    private var refreshId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflates the page layout
        val root = inflater.inflate(R.layout.fragment_alliance_details, container, false)
        val adapter = AllianceDetailsAdapter(requireActivity())
        if (refreshId == null) {
            refreshId = MainViewerActivity.refreshManager.addRefreshListener {
                Log.d("data-refresh", "Updated: alliance-details")
                adapter.notifyDataSetChanged()
            }
        }
        root.lv_alliance_details.adapter = adapter
        return root
    }
}
