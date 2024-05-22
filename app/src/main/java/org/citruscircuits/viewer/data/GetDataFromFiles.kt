package org.citruscircuits.viewer.data

import android.content.res.Resources
import android.util.Log
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.citruscircuits.viewer.MainViewerActivity
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.StartupActivity.Companion.databaseReference
import org.citruscircuits.viewer.StartupActivity.Companion.standStratData
import java.io.InputStream

/**
 * Function to get data from test data files.
 */
fun loadTestData(resources: Resources) {
    databaseReference =
        Json.decodeFromString<DataApi.ViewerData>(readStream(resources.openRawResource(R.raw.test_data)))
    val rawMatchSchedule = Json.decodeFromString<MutableMap<String, MatchScheduleMatch>>(
        readStream(resources.openRawResource(R.raw.test_match_schedule)),
    )

    standStratData["Nathan"] =
        Json.decodeFromString<StandStratApi.StandStratData>(readStream(resources.openRawResource(R.raw.test_stand_strat)))

    for (i in rawMatchSchedule) {
        val match = Match(i.key)
        for (j in i.value.teams) {
            when (j.color) {
                "red" -> match.redTeams.add(j.number)
                "blue" -> match.blueTeams.add(j.number)
            }
        }
        Log.e("parsedmap", match.toString())
        MainViewerActivity.matchCache[i.key] = match
    }
    MainViewerActivity.matchCache =
        MainViewerActivity.matchCache.toList().sortedBy { (_, v) -> v.matchNumber.toInt() }.toMap().toMutableMap()

    MainViewerActivity.teamList =
        Json.decodeFromString<List<Int>>(readStream(resources.openRawResource(R.raw.test_team_list)))
            .map { it.toString() }
}

fun readStream(inputStream: InputStream) = inputStream.bufferedReader().use { it.readText() }
