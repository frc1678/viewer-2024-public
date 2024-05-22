package org.citruscircuits.viewer.data

import org.citruscircuits.viewer.constants.Constants

/**
 * Gets the value of the given [Team [field]][Constants.FIELDS_TO_BE_DISPLAYED_TEAM_DETAILS] in
 * the database for the given [teamNumber].
 *
 * @param teamNumber The team number
 * @param field The datapoint name
 * @return The value of the requested [field]. [Constants.NULL_CHARACTER] if the value cannot be
 * found
 */
fun getTeamDataValue(teamNumber: String, field: String): String {
    val obj = if (field in Constants.STAND_STRAT_TEAM_DATA) getStandStrategistTeamDataByKey(teamNumber, field)
        else if (field in Constants.SECOND_PICKABILITY_FIELDS) getPickabilityDataValue(teamNumber, field)
        else getTeamObjectByKey(teamNumber, field)
    try {
        if (obj != null && obj != "null" && obj != "") {
            return if (field in Constants.PERCENT_DATA) {
                obj.toFloat().times(100.0).toString()
            } else {
                // Removes extra brackets and apostrophes (for Mode data points)
                // Adds space after comma to separate list items
                obj
                    .replace("[]", "N").replace("]", "")
                    .replace("[", "").replace("\'", "")
                    .replace(",", ", ").replace("\"", "")
                    .replaceFirstChar { it.uppercase() }
            }
        }
    } catch (_: Exception) { }
    return Constants.NULL_CHARACTER
}
