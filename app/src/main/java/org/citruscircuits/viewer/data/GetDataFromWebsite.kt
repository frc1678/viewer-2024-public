package org.citruscircuits.viewer.data

import org.citruscircuits.viewer.MainViewerActivity
import org.citruscircuits.viewer.StartupActivity
import org.citruscircuits.viewer.constants.Constants

suspend fun getDataFromWebsite() {
    // Sets databaseReference to the data in Grosbeak for the given Event Key
    StartupActivity.databaseReference = DataApi.getViewerData(Constants.EVENT_KEY)

    if (GrosbeakUrl != "https://grosbeak.citruscircuits.org") {
        StartupActivity.standStratUsernames =
            StandStratApi.getStandStratUsernames(Constants.EVENT_KEY)

        for (username in StartupActivity.standStratUsernames!!) {
            StartupActivity.standStratData[username] = StandStratApi.getStandStratData(Constants.EVENT_KEY, username)
        }
    }

    // Sets the teamList to the new team list for the competition
    // Gets this team list from grosbeak/api/team-list
    MainViewerActivity.teamList = DataApi.getTeamList(Constants.SCHEDULE_KEY).map { it }

    // Pulls the match schedule from grosbeak and then puts it in rawMatchSchedule
    val rawMatchSchedule = DataApi.getMatchSchedule(Constants.SCHEDULE_KEY)

    // For each team in the match schedule, add the team to either the redTeams list or the blueTeams list
    for (i in rawMatchSchedule.toList().sortedBy { it.first.toInt() }.toMap()) {
        val match = Match(i.key)
        for (j in i.value.teams) {
            when (j.color) {
                "red" -> match.redTeams.add(j.number)
                "blue" -> match.blueTeams.add(j.number)
            }
        }
        MainViewerActivity.matchCache[i.key] = match
    }
}
