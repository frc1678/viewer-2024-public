package org.citruscircuits.viewer.data

val teamNumberRegex = Regex("(\\d*)(\\w*)")

/**
 * Returns the team name of a given team number
 * @param teamNumber The given team number
 */
fun getTeamName(teamNumber: String): String? {
    val groups = teamNumberRegex.find(teamNumber)?.groups ?: return null
    val parsedTeamNumber = if (groups.size > 1) groups[1]?.value else null
    val parsedLetter = if (groups.size > 2) groups[2]?.value else null
    return parsedTeamNumber?.let { realTeamNumber ->
        getTeamDataValue(realTeamNumber, "team_name").let { teamName ->
            if (teamName == "UNKNOWN NAME") return null
            buildString {
                append(teamName)
                if (!parsedLetter.isNullOrEmpty()) append(" Robot $parsedLetter")
            }
        }
    }
}
