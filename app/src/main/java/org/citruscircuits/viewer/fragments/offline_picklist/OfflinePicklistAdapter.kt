//package org.citruscircuits.viewer.fragments.offline_picklist
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView
//import org.citruscircuits.viewer.MainViewerActivity
//import org.citruscircuits.viewer.R
//import org.citruscircuits.viewer.constants.Constants
//import org.citruscircuits.viewer.databinding.OfflinePicklistCellBinding
//import org.citruscircuits.viewer.fragments.team_details.TeamDetailsFragment
//
///**
// * Recycler adapter for live picklist
// */
//class OfflinePicklistAdapter(val context: OfflinePicklistFragment) :
//    ListAdapter<String, OfflinePicklistAdapter.OfflinePicklistViewHolder>(object : DiffUtil.ItemCallback<String>() {
//        override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
//        override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
//    }) {
//    inner class OfflinePicklistViewHolder(private val itemViewBinding: OfflinePicklistCellBinding) :
//        RecyclerView.ViewHolder(itemViewBinding.root) {
//        fun bindRoot(teamNumber: String) = with(itemViewBinding) {
//            if (context.picklistData.ranking.contains(teamNumber)) {
//                tvLocalRank.text = context.picklistData.ranking.indexOf(teamNumber).plus(1).toString()
//                root.setBackgroundColor(context.resources.getColor(R.color.White, null))
//            } else {
//                root.setBackgroundColor(context.resources.getColor(R.color.Red, null))
//                tvLocalRank.text = "-"
//            }
//            tvTeamNumber.text = teamNumber
//            tvImportedRank.text = (context.importedPicklistData.ranking.indexOf(teamNumber).takeIf { it != -1 }?.plus(1)
//                ?: if (teamNumber in context.importedPicklistData.dnp) "DNP" else "?").toString()
//            root.setOnClickListener { onClick(teamNumber) }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = OfflinePicklistViewHolder(
//        OfflinePicklistCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//    )
//
//    override fun onBindViewHolder(holder: OfflinePicklistViewHolder, position: Int) = holder.bindRoot(getItem(position))
//
//    fun onClick(teamNumber: String) {
//        if (teamNumber in MainViewerActivity.teamList) {
//            val teamDetailsFragment = TeamDetailsFragment()
//            // Put the arguments for the team details fragment.
//            teamDetailsFragment.arguments = Bundle().also {
//                it.putString(Constants.TEAM_NUMBER, teamNumber)
//                it.putBoolean("LFM", false)
//            }
//            // Switch to the team details fragment.
//            val ft = context.parentFragmentManager.beginTransaction()
//            ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
//            context.view?.rootView?.findViewById<ViewGroup>(R.id.nav_host_fragment)?.let {
//                ft.addToBackStack(null).replace(it.id, teamDetailsFragment).commit()
//            }
//        }
//    }
//
//    fun handleOrderChange(from: Int, to: Int) {
//        val newRanking = context.picklistData.ranking.toMutableList()
//        newRanking.add(to, newRanking.removeAt(from))
//        context.saveData(newRanking, context.picklistData.dnp)
//        context.updateData()
//    }
//
//    fun handleDNPToggle(position: Int) {
//        val teamNumber = this.getItem(position)
//        val newRanking = context.picklistData.ranking.toMutableList()
//        val newDnp = context.picklistData.dnp.toMutableList()
//        if (newDnp.contains(teamNumber)) {
//            newDnp.remove(teamNumber)
//            newRanking.add(teamNumber)
//        } else {
//            newDnp.add(teamNumber)
//            newRanking.remove(teamNumber)
//        }
//        context.saveData(newRanking, newDnp)
//        context.updateData()
//    }
//}
