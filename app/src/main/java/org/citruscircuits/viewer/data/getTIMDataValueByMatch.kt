package org.citruscircuits.viewer.data

import kotlinx.serialization.json.jsonPrimitive
import org.citruscircuits.viewer.StartupActivity
import org.citruscircuits.viewer.constants.Constants

/**
 * Gets a single requested [TIM data][Constants.FIELDS_TO_BE_DISPLAYED_MATCH_DETAILS_PLAYED] value
 * given the [matchNumber], [teamNumber], and data [field].
 *
 * @param matchNumber The match number to search for.
 * @param teamNumber The team number to search for in the match.
 * @param field The field to get the value of.
 * @return The value of the requested [field].
 */
fun getTIMDataValueByMatch(matchNumber: String, teamNumber: String, field: String) =
    // Returns the field value for the given Team, Match, and Field requested
    if (field in Constants.STAND_STRAT_TIM_DATA) getStandStrategistTIMDataByKey(teamNumber, matchNumber, field)
    else StartupActivity.databaseReference?.tim?.get(matchNumber)?.get(teamNumber)?.get(field)?.jsonPrimitive?.content
        ?.replace("null", Constants.NULL_CHARACTER)?.replaceFirstChar { it.uppercase() }
        ?.replace("True", "T")?.replace("False", "F")

