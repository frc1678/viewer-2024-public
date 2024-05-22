package org.citruscircuits.viewer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.startup_splash_screen.btn_retry
import kotlinx.android.synthetic.main.startup_splash_screen.et_event
import kotlinx.android.synthetic.main.startup_splash_screen.et_schedule
import kotlinx.android.synthetic.main.startup_splash_screen.splash_screen_layout
import kotlinx.android.synthetic.main.startup_splash_screen.tv_event
import kotlinx.android.synthetic.main.startup_splash_screen.tv_schedule
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.data.DataApi
import org.citruscircuits.viewer.data.StandStratApi
import org.citruscircuits.viewer.data.getDataFromWebsite
import org.citruscircuits.viewer.data.loadTestData
import org.citruscircuits.viewer.data.readStream

/**
 * Splash screen activity that waits for the data to pull from Grosbeak until it
 * begins the other Viewer activities.
 * AKA once MainViewerActivity.databaseReference is not null,
 * it will begin the actual viewer activity so ensure that all data is accessible before the viewer
 * activity begins.
 */
class StartupActivity : ViewerActivity() {
    companion object {
        var databaseReference: DataApi.ViewerData? = null
        var standStratData = mutableMapOf<String?, StandStratApi.StandStratData?>()
        var standStratUsernames: List<String>? = null
    }

    /** Create a popup for the user to reenter the event key*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.startup_splash_screen)
        btn_retry.setOnClickListener {
            btnRetryOnClick()
        }
        supportActionBar?.hide()
    }

    /** Get the proper permissions (all files access), but because this permission only exists above SDK 30,
     * separate permissions need to be granted for devices with lower SDK versions.*/
    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                // Permission is not granted
                // Request permission from the user
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            } else {
                // Permission is granted
                // Access the downloads folder here
                Constants.DOWNLOADS_FOLDER =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                MainViewerActivity.refreshManager.start(lifecycleScope)
                lifecycleScope.launch { getData() }
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ),
                        100
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                // If all permissions are granted, start the refresh coroutine and get data from grosbeak
                Constants.DOWNLOADS_FOLDER =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                MainViewerActivity.refreshManager.start(lifecycleScope)
                lifecycleScope.launch { getData() }
            }
        }
    }

    /** Get data from grosbeak*/
    private suspend fun getData() {
        try {
            if (Constants.USE_TEST_DATA) {
                loadTestData(this.resources)
                standStratData["Nathan"] = Json.decodeFromString<StandStratApi.StandStratData>(
                    readStream(resources.openRawResource(R.raw.test_stand_strat))
                )
            } else {
                // Gets all datapoints from file if it exists or copies defaults if not
                MainViewerActivity.UserDataPoints.read(this)
                if (MainViewerActivity.UserDataPoints.contents?.get(
                        "default_key"
                    )!!.asString != Constants.DEFAULT_KEY || MainViewerActivity.UserDataPoints.contents?.get(
                        "default_schedule"
                    )!!.asString != Constants.DEFAULT_SCHEDULE
                ) {
                    // Delete file if default key for the file is not updated with that of the code
                    MainViewerActivity.UserDataPoints.file.delete()
                    MainViewerActivity.UserDataPoints.copyDefaults(this)
                }
                Constants.SCHEDULE_KEY =
                    MainViewerActivity.UserDataPoints.contents?.get("schedule")!!.asString
                Constants.EVENT_KEY =
                    MainViewerActivity.UserDataPoints.contents?.get("key")!!.asString
                // Tries to get data from the website when starting the app and throws an error if fails
                getDataFromWebsite()
            }
            ContextCompat.startActivity(this, Intent(this, WelcomeActivity::class.java), null)
        } catch (e: Throwable) {
            Log.e(
                "data",
                "Error fetching data from ${if (Constants.USE_TEST_DATA) "files" else "website"}: ${
                    Log.getStackTraceString(e)
                }"
            )
            runOnUiThread {
                // Popup to allow users to edit the event and schedule keys if pulling data fails
                tv_schedule.visibility = View.VISIBLE
                et_schedule.visibility = View.VISIBLE
                tv_event.visibility = View.VISIBLE
                et_event.visibility = View.VISIBLE
                btn_retry.visibility = View.VISIBLE
                et_schedule.setText(Constants.SCHEDULE_KEY)
                et_event.setText(Constants.EVENT_KEY)
                et_event.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun afterTextChanged(p0: Editable?) {
                        // Change event key to entered one
                        if (p0.toString() != "") Constants.EVENT_KEY = p0.toString()
                    }
                })
                et_schedule.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun afterTextChanged(p0: Editable?) {
                        // Change schedule key to entered one
                        if (p0.toString() != "") Constants.SCHEDULE_KEY = p0.toString()
                    }
                })
                MainViewerActivity.UserDataPoints.read(this)
                Snackbar.make(
                    splash_screen_layout,
                    "Could not find match_schedule file for schedule key ${Constants.SCHEDULE_KEY}",
                    1000000000
                ).show()
            }
        }
    }

    /** Function to pull data again with a new event and schedule key*/
    fun btnRetryOnClick() {
        MainViewerActivity.UserDataPoints.contents?.remove("schedule")
        MainViewerActivity.UserDataPoints.contents?.addProperty("schedule", Constants.SCHEDULE_KEY)
        MainViewerActivity.UserDataPoints.contents?.remove("key")
        MainViewerActivity.UserDataPoints.contents?.addProperty("key", Constants.EVENT_KEY)
        MainViewerActivity.UserDataPoints.write()
        MainViewerActivity.refreshManager.start(lifecycleScope)
        lifecycleScope.launch { getData() }
    }
}
