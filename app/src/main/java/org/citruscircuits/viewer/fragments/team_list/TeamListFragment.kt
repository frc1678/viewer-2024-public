package org.citruscircuits.viewer.fragments.team_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.fragments.notes.NotesFragment
import org.citruscircuits.viewer.fragments.team_details.TeamDetailsFragment

/**
 * Fragment for showing the team list in numerical order.
 */
class TeamListFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        // use compose view to embed compose content
        ComposeView(requireContext()).apply {
            setContent {
                // call page composable
                TeamListPage(
                    // open team details fragment
                    onOpenTeamDetails = { team ->
                        val ft = parentFragmentManager.beginTransaction()
                        val fragment = TeamDetailsFragment().apply {
                            arguments = bundleOf(Constants.TEAM_NUMBER to team, "LFM" to false)
                        }
                        ft.addToBackStack(null).replace((requireView().parent as ViewGroup).id, fragment).commit()
                    },
                    // open notes fragment
                    onOpenNotes = { team ->
                        val ft = parentFragmentManager.beginTransaction()
                        val fragment = NotesFragment().apply { arguments = bundleOf(Constants.TEAM_NUMBER to team) }
                        ft.addToBackStack(null).replace((requireView().parent as ViewGroup).id, fragment).commit()
                    }
                )
            }
        }
}
