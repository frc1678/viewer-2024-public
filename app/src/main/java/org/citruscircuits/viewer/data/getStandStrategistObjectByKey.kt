package org.citruscircuits.viewer.data

import co.yml.charts.common.extensions.isNotNull
import org.citruscircuits.viewer.StartupActivity
import org.citruscircuits.viewer.constants.Constants

/**
 * Gets the value of a given [Stand Strategist Notes [field]][Constants.STAND_STRAT_NOTES_DATA]
 * for a given [teamNumber] and [username].
 *
 * @param username The username
 * @param teamNumber The team number
 * @param field The datapoint name
 * @return The value of the requested [field]. `null` if the value cannot be found.
 */
fun getStandStrategistNotesByKey(username: String, teamNumber: String, field: String) =
    StartupActivity.standStratData[username]?.teamData
        ?.get(teamNumber)?.get(field)
        .toString().replace("\"", "")

/**
 * Gets the value of a given [Stand Strategist Team [field]][Constants.STAND_STRAT_TEAM_DATA]
 * for a given [teamNumber].
 *
 * @param teamNumber The team number
 * @param field The datapoint name
 * @return The value of the requested [field]. `null` if the value cannot be found.
 */
fun getStandStrategistTeamDataByKey(teamNumber: String, field: String): String? {
    if (StartupActivity.standStratUsernames!!.isNotEmpty().isNotNull()) {
        when (field) {
            "shoot_specific_area_only" -> {
                /*
                For each username, gets the value of shoot_specific_area_only for the team,
                checks it against currentValue (starts as the first value found), if it is different
                and not null or empty returns N/A, otherwise sets currentValue to it if currentValue is
                null or empty. Returns N/A if there are any differences or not applicable, or null
                if no data.
                 */
                var value = StartupActivity.standStratUsernames!!.firstOrNull().let {
                    StartupActivity.standStratData[it]
                }?.teamData?.get(teamNumber)?.get(field).toString().replace("\"", "")
                for (username in StartupActivity.standStratUsernames!!) {
                    val data = StartupActivity.standStratData[username]?.teamData
                        ?.get(teamNumber)?.get(field)
                        .toString().replace("\"", "")
//                    Log.e("StandStratShoot Data checking", "$username, $teamNumber, $data")
//                    Log.e("StandStratShoot Data value (before)","$username, $teamNumber, $value")
                    if (value == "null" || value == "") value = data
                    else if (data != value && !(data == "null" || data == "")) return "N/A"
//                    Log.e("StandStratShoot Data value (after)", "$username, $teamNumber, $value")
                }
                return if (value == "" || value == "null") "N/A" else value.toString()
            }

            "avg_defense_rating" -> {
                var value: String = Constants.NULL_CHARACTER
                for (username in StartupActivity.standStratUsernames!!) {
                    val data = StartupActivity.standStratData[username]?.teamData
                        ?.get(teamNumber)?.get(field)
                        .toString().replace("\"", "")
//                    Log.e("StandStrat", data)
                    if (data != "null") value = data
                }
                return value
            }
        }
    }
    return null
}

/**
 * Gets the value of a given [Stand Strategist TIM [field]][Constants.STAND_STRAT_TIM_DATA] for
 * a given [teamNumber] and [matchNumber].
 *
 * @param teamNumber The team number
 * @param matchNumber The match number
 * @param field The datapoint name
 * @return The value of the requested [field]. `null` if the value cannot be found.
 */
fun getStandStrategistTIMDataByKey(
    teamNumber: String,
    matchNumber: String,
    field: String
): String? {
    // Gets the first non-blank value for the given datapoint given the team number and match number
    if (StartupActivity.standStratUsernames!!.isNotEmpty().isNotNull()) {
        for (username in StartupActivity.standStratUsernames!!) {
            val value = StartupActivity.standStratData[username]?.timData
                ?.get(matchNumber)?.get(teamNumber)?.get(field)
                .toString().replace("\"", "")
                .replace("null", Constants.NULL_CHARACTER)
                .replaceFirstChar { it.uppercase() }
//            Log.e("StandStratTIM Data", "$username, $matchNumber, $teamNumber, $field, value")
            /*if (value == "-1") return "N/A" else */if (value != "" && value != Constants.NULL_CHARACTER) return value
        }
    }
    return Constants.NULL_CHARACTER
}
