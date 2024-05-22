package org.citruscircuits.viewer

import org.citruscircuits.viewer.data.Match

/**
 * @param teamNumbers the list of team numbers to search. If you pass in an empty list then it will return all matches. If you pass in 1-2 items it will only return matches with those team numbers. If you pass in more than 2 items it will break.
 * @param starred If true, only return matches that have been starred.
 */
fun getMatchSchedule(teamNumbers: List<String> = listOf(), starred: Boolean = false): Map<String, Match> {
    if (starred) {
        val starredMatches = mutableMapOf<String, Match>()
        val searchedMatches = mutableMapOf<String, Match>()
        val returnedMatches = mutableMapOf<String, Match>()
        for (i in MainViewerActivity.matchCache) {
            if (MainViewerActivity.starredMatches.contains(i.value.matchNumber)) {
                starredMatches[i.key] = i.value
            }
        }
        if (teamNumbers.isNotEmpty()) {
            for (i in MainViewerActivity.matchCache) {
                if (teamNumbers.size == 1) {
                    if ((teamNumbers[0] in i.value.redTeams) or (teamNumbers[0] in i.value.blueTeams)) {
                        searchedMatches[i.key] = i.value
                    }
                } else if (((teamNumbers[0] in i.value.redTeams) or (teamNumbers[0] in i.value.blueTeams)) and ((teamNumbers[1] in i.value.redTeams) or (teamNumbers[1] in i.value.blueTeams))) {
                    searchedMatches[i.key] = i.value
                }
            }
            for (i in starredMatches) {
                for (x in searchedMatches) {
                    if (i == x) returnedMatches[x.key] = x.value
                }
            }
            return returnedMatches.toSortedMap(compareBy { it.toInt() })
        } else {
            return starredMatches.toSortedMap(compareBy { it.toInt() })
        }
    } else if (teamNumbers.isNotEmpty()) {
        val tempMatches = mutableMapOf<String, Match>()
        for (i in MainViewerActivity.matchCache) {
            if (teamNumbers.size == 1) {
                if ((teamNumbers[0] in i.value.redTeams) or (teamNumbers[0] in i.value.blueTeams)) {
                    tempMatches[i.key] = i.value
                }
            } else if (((teamNumbers[0] in i.value.redTeams) or (teamNumbers[0] in i.value.blueTeams)) and ((teamNumbers[1] in i.value.redTeams) or (teamNumbers[1] in i.value.blueTeams))) {
                tempMatches[i.key] = i.value
            }
        }
        return tempMatches.toSortedMap(compareBy { it.toInt() })
    }
    return MainViewerActivity.matchCache.toSortedMap(compareBy { it.toInt() })
}
