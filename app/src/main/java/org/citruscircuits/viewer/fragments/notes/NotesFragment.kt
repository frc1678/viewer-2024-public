package org.citruscircuits.viewer.fragments.notes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.ktor.http.HttpStatusCode
import kotlinx.android.synthetic.main.fragment_notes.view.btn_edit_notes
import kotlinx.android.synthetic.main.fragment_notes.view.et_notes
import kotlinx.coroutines.launch
import org.citruscircuits.viewer.MainViewerActivity
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.data.NotesApi
import org.citruscircuits.viewer.showError
import kotlin.collections.set

/**
 * Page that displays strategist notes
 */
class NotesFragment : Fragment() {

    private var mode = Mode.VIEW

    var teamNumber: String? = null

    private var refreshId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_notes, container, false)
        arguments?.let { teamNumber = it.getString(Constants.TEAM_NUMBER) }
        if (refreshId == null) {
            refreshId = MainViewerActivity.refreshManager.addRefreshListener { if (mode == Mode.VIEW) getNotes(root) }
        }
        setupListeners(root)
        getNotes(root)
        return root
    }

    private fun setupListeners(root: View) {
        root.btn_edit_notes.setOnClickListener {
            mode = when (mode) {
                Mode.VIEW -> {
                    setupEditMode(root)
                    Mode.EDIT
                }

                Mode.EDIT -> {
                    setupViewMode(root)
                    Mode.VIEW
                }
            }
        }
    }

    private fun setupEditMode(root: View) {
        root.btn_edit_notes.setImageResource(R.drawable.ic_baseline_save_24)
        root.et_notes.isEnabled = true
    }

    private fun setupViewMode(root: View) {
        root.btn_edit_notes.setImageResource(R.drawable.ic_baseline_edit_24)
        root.et_notes.isEnabled = false
        root.btn_edit_notes.isEnabled = false
        try {
            lifecycleScope.launch {
                teamNumber?.let { teamNumber ->
                    val notes = root.et_notes.text.toString()
                    val resp = NotesApi.set(Constants.EVENT_KEY, teamNumber, notes)
                    if (resp.status == HttpStatusCode.OK) {
                        MainViewerActivity.notesCache[teamNumber] = notes
                    } else {
                        context?.let { showError(it, "Error saving notes: ${resp.status}") }
                    }
                    root.btn_edit_notes.isEnabled = true
                }
            }
        } catch (e: Exception) {
            Log.e("NOTES", "FAILED TO SAVE NOTES. WE JUST LOST DATA. FIX IMMEDIATELY")
        }
    }

    private fun getNotes(root: View) {
        root.btn_edit_notes.isEnabled = false
        try {
            teamNumber?.let {
                lifecycleScope.launch {
                    val response = NotesApi.get(Constants.EVENT_KEY, it)
                    root.et_notes.setText(response.notes)
                    root.btn_edit_notes.isEnabled = true
                }
            }
        } catch (e: Exception) {
            Log.e("notes", "FAILED TO FETCH NOTES FOR $teamNumber.")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MainViewerActivity.refreshManager.removeRefreshListener(refreshId)
    }

    enum class Mode {
        EDIT, VIEW
    }
}
