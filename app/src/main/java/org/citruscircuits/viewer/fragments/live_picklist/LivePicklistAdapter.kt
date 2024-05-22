package org.citruscircuits.viewer.fragments.live_picklist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.citruscircuits.viewer.MainViewerActivity
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.databinding.LivePicklistCellBinding
import org.citruscircuits.viewer.fragments.team_details.TeamDetailsFragment

/**
 * Recycler adapter for live picklist. This has been commented out due to a lack of use this year.
 */
//class LivePicklistRecyclerAdapter(val context: LivePicklistFragment) :
//    ListAdapter<String, LivePicklistRecyclerAdapter.LivePicklistViewHolder>(object : DiffUtil.ItemCallback<String>() {
//        override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
//        override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
//    }) {
//    inner class LivePicklistViewHolder(private val itemViewBinding: LivePicklistCellBinding) :
//        RecyclerView.ViewHolder(itemViewBinding.root) {
//        fun bindRoot(teamNumber: String) {
//            if (context.picklistData.ranking.contains(teamNumber)) {
//                itemViewBinding.tvFirstRank.text = context.picklistData.ranking.indexOf(teamNumber).plus(1).toString()
//                itemViewBinding.root.setBackgroundColor(context.resources.getColor(R.color.White, null))
//            } else {
//                itemViewBinding.root.setBackgroundColor(context.resources.getColor(R.color.Red, null))
//                itemViewBinding.tvFirstRank.text = "-"
//            }
//            itemViewBinding.tvTeamNumber.text = teamNumber
//            itemViewBinding.root.setOnClickListener { onClick(teamNumber) }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = LivePicklistViewHolder(
//        LivePicklistCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//    )
//
//    override fun onBindViewHolder(holder: LivePicklistViewHolder, position: Int) = holder.bindRoot(getItem(position))
//
//    fun onClick(teamNumber: String) {
//        if (teamNumber in MainViewerActivity.teamList) {
//            val teamDetailsFragment = TeamDetailsFragment()
//            // Put the arguments for the team details fragment.
//            teamDetailsFragment.arguments = bundleOf(Constants.TEAM_NUMBER to teamNumber, "LFM" to false)
//            // Switch to the team details fragment.
//            val ft = context.parentFragmentManager.beginTransaction()
//            ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
//            context.view?.rootView?.findViewById<ViewGroup>(R.id.nav_host_fragment)?.let {
//                ft.addToBackStack(null).replace(it.id, teamDetailsFragment).commit()
//            }
//        }
//    }
//}
