package org.citruscircuits.viewer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.customview.widget.ViewDragHelper
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_main.container
import kotlinx.android.synthetic.main.field_map_popup.view.blue_chip
import kotlinx.android.synthetic.main.field_map_popup.view.chip_group
import kotlinx.android.synthetic.main.field_map_popup.view.close_button
import kotlinx.android.synthetic.main.field_map_popup.view.field_map
import kotlinx.android.synthetic.main.field_map_popup.view.none_chip
import kotlinx.android.synthetic.main.field_map_popup.view.red_chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.data.Match
import org.citruscircuits.viewer.data.NotesApi
import org.citruscircuits.viewer.fragments.alliance_details.AllianceDetailsFragment
import org.citruscircuits.viewer.fragments.groups.GroupsFragment
//import org.citruscircuits.viewer.fragments.live_picklist.LivePicklistFragment
import org.citruscircuits.viewer.fragments.match_schedule.MatchScheduleFragment
import org.citruscircuits.viewer.fragments.pickability.PickabilityFragment
import org.citruscircuits.viewer.fragments.preferences.PreferencesFragment
import org.citruscircuits.viewer.fragments.ranking.RankingFragment
import org.citruscircuits.viewer.fragments.team_list.TeamListFragment
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.InputStream
import java.io.OutputStream

/**
 * Main activity class that handles navigation.
 */
class MainViewerActivity : ViewerActivity() {
    private lateinit var toggle: ActionBarDrawerToggle

    companion object {
        var matchCache = mutableMapOf<String, Match>()
        var teamList = listOf<String>()
        var starredMatches = mutableSetOf<String>()
        var eliminatedAlliances = mutableSetOf<String>()
        val refreshManager = RefreshManager()
        val leaderboardCache = mutableMapOf<String, Leaderboard>()
        var notesCache = mutableMapOf<String, String>()
        var mapMode = 1

        /** Update Viewer Notes locally by pulling from Grosbeak*/
        suspend fun updateNotesCache() {
            val notesList = NotesApi.getAll(Constants.EVENT_KEY)
            notesCache = notesList.toMutableMap()
            Log.d("notes", "updated notes cache")
        }
    }

    /**
     * Overrides the back button to go back to last fragment.
     * Disables the back button and returns nothing when in the startup match schedule.
     */
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.container)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawer(GravityCompat.START)
        if (supportFragmentManager.backStackEntryCount > 1) supportFragmentManager.popBackStack()
    }

    override fun onResume() {
        super.onResume()
        // Creates the files for user data points and starred matches
        UserDataPoints.read(this)
        StarredMatches.read()
        EliminatedAlliances.read()
        // Pull the set of starred matches from the downloads file viewer_starred_matches.
        val jsonStarred = StarredMatches.contents.get("starredMatches")?.asJsonArray
        if (jsonStarred != null) {
            for (starred in jsonStarred) {
                starredMatches.add(starred.asString)
            }
        }
        // Pull the set of eliminated alliances from the downloads file viewer_eliminated_alliances.
        val jsonEliminated = EliminatedAlliances.contents.get("eliminatedAlliances")?.asJsonArray
        if (jsonEliminated != null) {
            for (alliance in jsonEliminated) {
                eliminatedAlliances.add(alliance.asString)
            }
        }
    }

    /** Creates the main activity, containing the top app bar, nav drawer, and shows by default the match schedule page*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        setToolbarText(actionBar, supportActionBar)
        val drawerLayout: DrawerLayout = findViewById(R.id.container)
        val navView: NavigationView = findViewById(R.id.navigation)
        // Defaults
        navView.setCheckedItem(R.id.nav_menu_match_schedule)
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        // Creates leaderboards for each datapoint for the Rankings page
        (Constants.FIELDS_TO_BE_DISPLAYED_TEAM_DETAILS + Constants.FIELDS_TO_BE_DISPLAYED_LFM).forEach {
            if (it !in Constants.CATEGORY_NAMES) createLeaderboard(it)
        }
        // Creates a refresher that will call updateNavFooter() every so often
        refreshManager.addRefreshListener {
            Log.d("data-refresh", "Updated: ranking")
            updateNavFooter()
        }
        if (!Constants.USE_TEST_DATA) {
            lifecycleScope.launch {
                updateNotesCache()
            }
        }
        // Make the back button in the top action bar only go back one screen and not to the first screen
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Creates fragments
        val matchScheduleFragment = MatchScheduleFragment()
        val rankingFragment = RankingFragment()
        //val livePicklistFragment = LivePicklistFragment()
        val pickabilityFragment = PickabilityFragment()
        val teamListFragment = TeamListFragment()
        val allianceDetailsFragment = AllianceDetailsFragment()
        val preferencesFragment = PreferencesFragment()
        updateNavFooter()
        // default screen when the viewer starts (after pulling data) - the match schedule page
        supportFragmentManager.beginTransaction().addToBackStack(null)
            .replace(R.id.nav_host_fragment, matchScheduleFragment, "matchSchedule").commit()
        // Listener to open the nav drawer
        container.addDrawerListener(NavDrawerListener(navView, supportFragmentManager, this))
        // Set a listener for each item in the drawer
        navView.setNavigationItemSelectedListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawer(
                GravityCompat.START
            )
            when (it.itemId) {
                R.id.nav_menu_match_schedule -> {
                    MatchScheduleFragment.lastPageMatchDetails = false
                    supportFragmentManager.popBackStack(0, 0)
                    supportFragmentManager.beginTransaction().addToBackStack(null)
                        .replace(R.id.nav_host_fragment, matchScheduleFragment, "matchSchedule")
                        .commit()
                }

                R.id.nav_menu_rankings -> {
                    supportFragmentManager.beginTransaction().addToBackStack(null)
                        .replace(R.id.nav_host_fragment, rankingFragment, "rankings").commit()
                }

//                R.id.nav_menu_picklist -> {
//                    supportFragmentManager.beginTransaction().addToBackStack(null)
//                        .replace(R.id.nav_host_fragment, livePicklistFragment, "picklist").commit()
//                }

                R.id.nav_menu_pickability -> {
                    val ft = supportFragmentManager.beginTransaction()
                    if (supportFragmentManager.fragments.last().tag != "pickability") ft.addToBackStack(
                        null
                    )
                    ft.replace(R.id.nav_host_fragment, pickabilityFragment, "pickability").commit()
                }

                R.id.nav_menu_team_list -> {
                    val ft = supportFragmentManager.beginTransaction()
                    if (supportFragmentManager.fragments.last().tag != "teamList") ft.addToBackStack(
                        null
                    )
                    ft.replace(R.id.nav_host_fragment, teamListFragment, "teamlist").commit()
                }

                R.id.nav_menu_alliance_details -> {
                    val ft = supportFragmentManager.beginTransaction()
                    if (supportFragmentManager.fragments.last().tag != "allianceDetails") ft.addToBackStack(
                        null
                    )
                    ft.replace(R.id.nav_host_fragment, allianceDetailsFragment, "allianceDetails")
                        .commit()
                }

                R.id.nav_menu_groups -> {
                    val ft = supportFragmentManager.beginTransaction()
                    if (supportFragmentManager.fragments.last().tag != "groups") ft.addToBackStack(
                        null
                    )
                    ft.replace(R.id.nav_host_fragment, GroupsFragment(), "groups").commit()
                }

                R.id.nav_menu_preferences -> {
                    val ft = supportFragmentManager.beginTransaction()
                    if (supportFragmentManager.fragments.last().tag != "preferences") ft.addToBackStack(
                        null
                    )
                    ft.replace(R.id.nav_host_fragment, preferencesFragment, "preferences").commit()
                }
            }
            true
        }
        lifecycleScope.launch(Dispatchers.IO) { Groups.startListener() }
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        if (toggle.onOptionsItemSelected(item)) true else super.onOptionsItemSelected(item)

    /** Inflate the top bar, which includes the field map button */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.toolbar, menu)
        val fieldMapItem: MenuItem = menu.findItem(R.id.field_map_button)
        val fieldButton = fieldMapItem.actionView
        // Field map button
        fieldButton?.setOnClickListener {
            val popupView = View.inflate(this, R.layout.field_map_popup, null)
            val width = LinearLayout.LayoutParams.MATCH_PARENT
            val height = LinearLayout.LayoutParams.MATCH_PARENT
            val popupWindow = PopupWindow(popupView, width, height, false)
            popupWindow.showAtLocation(it, Gravity.CENTER, 0, 0)
            when (mapMode) {
                0 -> {
                    popupView.red_chip.isChecked = true
                    popupView.none_chip.isChecked = false
                    popupView.blue_chip.isChecked = false
                    popupView.field_map.setImageResource(R.drawable.field_red_map_24)
                }

                1 -> {
                    popupView.red_chip.isChecked = false
                    popupView.none_chip.isChecked = true
                    popupView.blue_chip.isChecked = false
                    popupView.field_map.setImageResource(R.drawable.field_24)
                }

                2 -> {
                    popupView.red_chip.isChecked = false
                    popupView.none_chip.isChecked = false
                    popupView.blue_chip.isChecked = true
                    popupView.field_map.setImageResource(R.drawable.field_blue_map_24)
                }
            }
            popupView.red_chip.setOnClickListener { popupView.red_chip.isChecked = true }
            popupView.blue_chip.setOnClickListener { popupView.blue_chip.isChecked = true }
            popupView.none_chip.setOnClickListener { popupView.none_chip.isChecked = true }
            popupView.chip_group.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    popupView.red_chip.id -> {
                        popupView.field_map.setImageResource(R.drawable.field_red_map_24)
                        mapMode = 0
                    }

                    popupView.none_chip.id -> {
                        popupView.field_map.setImageResource(R.drawable.field_24)
                        mapMode = 1
                    }

                    popupView.blue_chip.id -> {
                        popupView.field_map.setImageResource(R.drawable.field_blue_map_24)
                        mapMode = 2
                    }
                }
                return@setOnCheckedChangeListener
            }
            popupView.close_button.setOnClickListener { popupWindow.dismiss() }
        }
        return super.onCreateOptionsMenu(menu)
    }

    /** Nav footer that displays when the viewer was last refreshed.  */
    private fun updateNavFooter() {
        val footer = findViewById<TextView>(R.id.nav_footer)
        footer.text = if (Constants.USE_TEST_DATA) getString(R.string.test_data)
        else getString(R.string.last_updated, super.getTimeText())
    }

    /** Object to extract the datapoints of the user preferences file and write to it. */
    object UserDataPoints {
        /** Holds a user's datapoints */
        var contents: JsonObject? = null
        private var gson = Gson()

        /** User preferences file*/
        val file = File(Constants.DOWNLOADS_FOLDER, "viewer_user_data_prefs.json")

        /** Get contents from User Preferences file. This file should always exist unless a mismatch is found, in which the file will be deleted*/
        fun read(context: Context) {
            // Load defaults if no file exists
            if (!fileExists()) copyDefaults(context)
            // Try to pull from file
            try {
                contents = JsonParser.parseReader(FileReader(file)).asJsonObject
            } catch (e: Exception) {
                Log.e("UserDataPoints.read", "Failed to read user datapoints file")
            }
            // Gets user
            val user = contents?.get("selected")?.asString
            // Gets user's datapoints
            val userDataPoints = contents?.get(user)?.asJsonArray
            // If the datapoints exist, check if they match with Constants TEAM / TIM datapoints
            if (userDataPoints != null) {
                for (i in userDataPoints) {
                    if (
                        i.asString !in Constants.FIELDS_TO_BE_DISPLAYED_TEAM_DETAILS &&
                        i.asString !in Constants.FIELDS_TO_BE_DISPLAYED_MATCH_DETAILS_PLAYED &&
                        i.asString != "See Matches" && i.asString != "TEAM" && i.asString != "TIM"
                    ) {
                        file.delete()
                        // If not, that means someone forgot to update either Constants or User Datapoints defaults
                        Log.e(
                            "UserDataPoints.read",
                            "Datapoint ${i.asString} does not exist in Constants"
                        )
                        // Reset back to defaults
                        copyDefaults(context)
                        break
                    }
                }
            }
        }

        fun write() {
            val writer = FileWriter(file, false)
            gson.toJson(contents as JsonElement, writer)
            writer.close()
        }

        /** Check if user preferences file exists*/
        private fun fileExists(): Boolean = file.exists()

        /**Copies the default preferences to the User Preferences file*/
        fun copyDefaults(context: Context) {
            // Read from user preferences file
            val inputStream: InputStream = context.resources.openRawResource(R.raw.default_prefs)
            try {
                // Copies over the contents of the defaults (inputStream) to the file
                val outputStream: OutputStream = FileOutputStream(file)
                val buffer = ByteArray(1024)
                var len: Int?
                while (inputStream.read(buffer, 0, buffer.size).also { len = it } != -1) {
                    outputStream.write(buffer, 0, len!!)
                }
                inputStream.close()
                outputStream.close()
                try {
                    // Add the other default elements to the file
                    contents = JsonParser.parseReader(FileReader(file)).asJsonObject
                    contents?.remove("key")
                    contents?.addProperty("key", Constants.DEFAULT_KEY)
                    contents?.remove("schedule")
                    contents?.addProperty("schedule", Constants.DEFAULT_SCHEDULE)
                    contents?.remove("default_key")
                    contents?.addProperty("default_key", Constants.DEFAULT_KEY)
                    contents?.remove("default_schedule")
                    contents?.addProperty("default_schedule", Constants.DEFAULT_SCHEDULE)
                    write()
                } catch (e: Exception) {
                    Log.e("UserDataPoints.read", "Failed to read user datapoints file")
                }
            } catch (e: Exception) {
                Log.e("copyDefaults", "Failed to copy default preferences to file, $e")
            }
        }
    }

    /**
     * Writes file to store the starred matches on the viewer
     */
    object StarredMatches {
        var contents = JsonObject()
        private var gson = Gson()

        // Creates a list that stores all the match numbers that team 1678 is in
        val citrusMatches = matchCache.filter {
            return@filter it.value.blueTeams.contains("1678") or it.value.redTeams.contains("1678")
        }.map { return@map it.value.matchNumber }

        private val file = File(Constants.DOWNLOADS_FOLDER, "viewer_starred_matches.json")

        fun read() {
            if (!fileExists()) write()
            try {
                contents = JsonParser.parseReader(FileReader(file)).asJsonObject
            } catch (e: Exception) {
                Log.e("StarredMatches.read", "Failed to read starred matches file")
            }
        }

        private fun write() {
            val writer = FileWriter(file, false)
            gson.toJson(contents as JsonElement, writer)
            writer.close()
        }

        private fun fileExists(): Boolean = file.exists()

        /**
         * Updates the file with the currently starred matches based on the companion object starredMatches
         */
        fun input() {
            val starredJsonArray = JsonArray()
            for (starred in starredMatches) starredJsonArray.add(starred)
            contents.remove("starredMatches")
            contents.add("starredMatches", starredJsonArray)
            write()
        }

    }

    /** Object to read and write eliminated alliances data. This was not used this year. */
    object EliminatedAlliances {
        var contents = JsonObject()
        private var gson = Gson()
        private val file = File(Constants.DOWNLOADS_FOLDER, "viewer_eliminated_alliances.json")

        fun read() {
            if (!fileExists()) write()
            try {
                contents = JsonParser.parseReader(FileReader(file)).asJsonObject
            } catch (e: Exception) {
                Log.e("EliminatedAlliances.read", "Failed to read eliminated alliances file")
            }
        }

        private fun write() {
            val writer = FileWriter(file, false)
            gson.toJson(contents as JsonElement, writer)
            writer.close()
        }

        private fun fileExists(): Boolean = file.exists()

        fun input() {
            val eliminatedJsonArray = JsonArray()
            for (alliance in eliminatedAlliances) eliminatedJsonArray.add(alliance)
            contents.remove("eliminatedAlliances")
            contents.add("eliminatedAlliances", eliminatedJsonArray)
            write()
        }
    }

    /**
     * An object to read/write the starred teams file with.
     */
    object StarredTeams {
        private val gson = Gson()
        private val teams = mutableSetOf<String>()

        fun add(team: String) {
            teams.add(team)
            write()
        }

        fun remove(team: String) {
            teams.remove(team)
            write()
        }

        fun contains(team: String) = teams.contains(team)

        private val file = File(Constants.DOWNLOADS_FOLDER, "viewer_starred_teams.json")

        fun read() {
            if (!file.exists()) write()
            try {
                JsonParser.parseReader(FileReader(file)).asJsonArray.forEach { teams.add(it.asString) }
            } catch (e: Exception) {
                Log.e("StarredTeams.read", "Failed to read starred teams file")
            }
        }

        private fun write() {
            val writer = FileWriter(file, false)
            gson.toJson(teams, writer)
            writer.close()
        }
    }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

/** Class for creating listeners for all fragments (manages state changes)*/
class NavDrawerListener(
    private val navView: NavigationView,
    private val fragManager: FragmentManager,
    private val activity: Activity
) : DrawerLayout.DrawerListener {
    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
    override fun onDrawerOpened(drawerView: View) = activity.hideKeyboard()

    override fun onDrawerClosed(drawerView: View) {}
    override fun onDrawerStateChanged(newState: Int) {
        if (newState == ViewDragHelper.STATE_SETTLING) {
            when (fragManager.fragments.last().tag) {
                "matchSchedule" -> navView.setCheckedItem(R.id.nav_menu_match_schedule)
                "rankings" -> navView.setCheckedItem(R.id.nav_menu_rankings)
                "picklist" -> navView.setCheckedItem(R.id.nav_menu_picklist)
                "pickability" -> navView.setCheckedItem(R.id.nav_menu_pickability)
                "teamList" -> navView.setCheckedItem(R.id.nav_menu_team_list)
                "allianceDetails" -> navView.setCheckedItem(R.id.nav_menu_alliance_details)
                "groups" -> navView.setCheckedItem(R.id.nav_menu_groups)
                "preferences" -> navView.setCheckedItem(R.id.nav_menu_preferences)
            }
        }
    }
}