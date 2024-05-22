package org.citruscircuits.viewer.data

import kotlinx.serialization.json.jsonPrimitive
import org.citruscircuits.viewer.StartupActivity
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.getMatchSchedule

/**
 * Gets the value of the given [TIM [field]][Constants.FIELDS_TO_BE_DISPLAYED_MATCH_DETAILS_PLAYED]
 * in the database for the given [teamNumber].
 *
 * @param teamNumber The team number for the TIM value.
 * @param field The field to get the value for.
 * @return The value of the requested [field]
 */
fun getTIMDataValue(teamNumber: String, field: String): Map<String, String?> {
    val matchNumList = getMatchSchedule(listOf(teamNumber)).keys.sortedBy { it.toIntOrNull() }
    val result = mutableMapOf<String, String?>()
    for (matchNumber in matchNumList) {
        if (field in Constants.STAND_STRAT_TIM_DATA) result[matchNumber] = getStandStrategistTIMDataByKey(teamNumber, matchNumber, field)?.replaceFirstChar { it.uppercase() }
        else result[matchNumber] = StartupActivity.databaseReference?.tim?.get(matchNumber)?.get(teamNumber)
            ?.get(field)?.jsonPrimitive?.content?.replaceFirstChar { it.uppercase() }
            ?.replace("True", "T")?.replace("False", "F")
    }
    return result
}
