package org.citruscircuits.viewer

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.citruscircuits.viewer.MainViewerActivity.EliminatedAlliances
import org.citruscircuits.viewer.MainViewerActivity.StarredMatches
import org.citruscircuits.viewer.MainViewerActivity.StarredTeams
import org.citruscircuits.viewer.MainViewerActivity.UserDataPoints
import org.citruscircuits.viewer.constants.Constants
import java.util.Locale

/**
 * The activity that greets the user and asks them to choose a profile.
 */
class WelcomeActivity : ViewerActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Constants.STORAGE_FOLDER = getExternalFilesDir(null)!!
        Constants.DOWNLOADS_FOLDER = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        // Create/read the user profile file, the starred matches file, and the starred teams file
        UserDataPoints.read(this)
        StarredMatches.read()
        StarredTeams.read()
        EliminatedAlliances.read()
        setContentView(R.layout.activity_welcome)
        val names = resources.getStringArray(R.array.user_array)
        val radioGroup = findViewById<RadioGroup>(R.id.profile_list)
        // set the default action bar
        setToolbarText(actionBar, supportActionBar)
        // Add the radio buttons to the radio group
        names.forEach {
            val radioButton = RadioButton(this)
            radioButton.text = it
            radioGroup.addView(radioButton)
        }
        // Find which profile is currently selected and show it
        val previouslySelected = UserDataPoints.contents?.get("selected")?.asString ?: "OTHER"
        for (i in 0 until radioGroup.childCount) {
            val radioButton = radioGroup.getChildAt(i) as RadioButton
            if (radioButton.text.toString().uppercase() == previouslySelected.uppercase()) {
                radioButton.toggle()
                break
            }
        }
        findViewById<Button>(R.id.continue_button).setOnClickListener { onContinue() }
    }

    /**
     * When the 'continue' button is clicked, disable it, save the user preference, and go to the main activity.
     */
    private fun onContinue() {
        findViewById<Button>(R.id.continue_button).isEnabled = false
        val radioGroup = findViewById<RadioGroup>(R.id.profile_list)
        for (i in 0 until radioGroup.childCount) {
            val radioButton = radioGroup.getChildAt(i) as RadioButton
            if (radioButton.isChecked) {
                with(MainViewerActivity.UserDataPoints) {
                    contents?.remove("selected")
                    contents?.addProperty(
                        "selected",
                        radioButton.text.toString().uppercase(Locale.getDefault())
                    )
                    write()
                }
                break
            }
        }
        ContextCompat.startActivity(
            this, Intent(this, MainViewerActivity::class.java), null
        )
    }
}
