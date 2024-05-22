package org.citruscircuits.viewer.fragments.match_schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.data.getAllianceInMatchObjectByKey
import org.citruscircuits.viewer.fragments.match_details.MatchDetailsFragment
import org.citruscircuits.viewer.fragments.team_details.TeamDetailsFragment
import org.citruscircuits.viewer.getMatchSchedule

/**
 * Fragment for showing the match schedule, with inline details about each match.
 */
class MatchScheduleFragment : Fragment() {
    companion object {
        var lastPageMatchDetails = false
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // use compose view to embed compose content
        val view = ComposeView(requireContext()).apply {
            setContent {
                // call page composable
                MatchSchedulePage(
                    getMatchSchedule = { filter, search ->
                        getMatchSchedule(
                            // build search query for matches
                            teamNumbers = buildList {
                                if (search.isNotBlank()) add(search)
                                if (filter == MatchScheduleFilter.OUR) add(Constants.MY_TEAM_NUMBER)
                            },
                            starred = filter == MatchScheduleFilter.STARRED
                        )
                    },
                    // set initial search if arguments are given
                    initialSearch = arguments?.getString(Constants.TEAM_NUMBER) ?: "",
                    // open match details fragment
                    onOpenMatchDetails = {
                        lastPageMatchDetails = true
                        parentFragmentManager
                            .beginTransaction()
                            .addToBackStack(null)
                            .replace(
                                R.id.nav_host_fragment,
                                MatchDetailsFragment().apply {
                                    it.toIntOrNull()?.let { int ->
                                        arguments = bundleOf(Constants.MATCH_NUMBER to int)
                                    }
                                }
                            ).commit()
                    },
                    // open team details fragment
                    onOpenTeamDetails = {
                        parentFragmentManager
                            .beginTransaction()
                            .addToBackStack(null)
                            .replace(
                                R.id.nav_host_fragment,
                                TeamDetailsFragment().apply {
                                    arguments = bundleOf(Constants.TEAM_NUMBER to it, "LFM" to false)
                                }
                            ).commit()
                    },
                    onOpen = { matchSchedule, state ->
                        if (!lastPageMatchDetails) {
                            var i = 0
                            for (match in matchSchedule.values) {
                                val hasActualData =
                                    getAllianceInMatchObjectByKey(Constants.RED, match.matchNumber, "has_actual_data").toBoolean() &&
                                    getAllianceInMatchObjectByKey(Constants.BLUE, match.matchNumber, "has_actual_data").toBoolean()
                                if ((!hasActualData && i != 0) || i + 1 == matchSchedule.size) {
                                    lifecycleScope.launch { state.scrollToItem(i - 1) }
                                    break
                                }
                                i++
                            }
                        }
                        lastPageMatchDetails = false
                    }
                )
            }
        }
        return view
    }
}
