package org.citruscircuits.viewer.fragments.pickability

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import org.citruscircuits.viewer.MainViewerActivity
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.convertToFilteredTeamsList
import org.citruscircuits.viewer.data.getTeamDataValue
import org.citruscircuits.viewer.databinding.FragmentPickabilityBinding
import org.citruscircuits.viewer.fragments.team_details.TeamDetailsFragment

/**
 * Page that ranks the pickability of each team. Previously allowed for first pickability and second pickability
 */
class PickabilityFragment : Fragment() {

    private val teamDetailsFragment = TeamDetailsFragment()
    private val teamDetailsFragmentArguments = Bundle()

    private var refreshId: String? = null

    private var mode = PickabilityMode.First
    private var _binding: FragmentPickabilityBinding? = null

    /**
     * This property is only valid between [onCreateView] and [onDestroyView].
     */
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPickabilityBinding.inflate(inflater, container, false)
        val map: Map<String, String> = updateMatchScheduleListView()
        binding.lvPickability.setOnItemClickListener { _, _, position, _ ->
            val list: List<String> = map.keys.toList()
            val pickabilityFragmentTransaction = parentFragmentManager.beginTransaction()
            teamDetailsFragmentArguments.putString(Constants.TEAM_NUMBER, list[position])
            teamDetailsFragmentArguments.putBoolean("LFM", false)
            teamDetailsFragment.arguments = teamDetailsFragmentArguments
            pickabilityFragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            pickabilityFragmentTransaction.addToBackStack(null).replace(
                (requireView().parent as ViewGroup).id, teamDetailsFragment
            ).commit()
        }
        // Creates the drop down to select pickability type
        binding.spinnerMode.adapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.pickability, R.layout.spinner_item
        )
        // Updates the pickability page when the user chooses to display a different pickability type from the dropdown
        binding.spinnerMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                PickabilityMode.fromSpinner(position)?.let {
                    mode = it
                    updateMatchScheduleListView()
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        return binding.root
    }

    // Updates the data on the pickability page using the PickabilityListAdapter
    private fun updateMatchScheduleListView(): Map<String, String> {
        val map = makeData()
        val adapter = PickabilityListAdapter(context = requireActivity(), items = map, mode = mode)
        if (refreshId == null) {
            refreshId = MainViewerActivity.refreshManager.addRefreshListener {
                Log.d("data-refresh", "Updated: Pickability")
                adapter.items = makeData()
                adapter.notifyDataSetChanged()
            }
        }
        binding.lvPickability.adapter = adapter
        return map
    }

    // Creates a map with the team number as the key and the pickability score as the value
    private fun makeData(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val rawTeamNumbers = convertToFilteredTeamsList(MainViewerActivity.teamList)
        rawTeamNumbers.forEach { e -> map[e] = getTeamDataValue(e, mode.datapoint) }
        return map.toList().sortedBy { (_, v) -> (v.substringAfter("&").toFloatOrNull()) }.reversed().toMap().toMutableMap()
    }

    override fun onDestroy() {
        super.onDestroy()
        MainViewerActivity.refreshManager.removeRefreshListener(refreshId)
    }
}

// Stores the possible pickability choices
enum class PickabilityMode {
    First, Second, SecondDefensive, SecondScoring;

    val datapoint
        get() = when (this) {
            First -> "first_pickability"
            Second -> "second_pickability"
            SecondDefensive -> "second_defensive_pickability"
            SecondScoring -> "second_scoring_pickability"
        }

    companion object {
        fun fromSpinner(spinnerPosition: Int): PickabilityMode? = when (spinnerPosition) {
            0 -> First
            1 -> Second
            2 -> SecondDefensive
            3 -> SecondScoring
            else -> null
        }
    }
}
