package org.citruscircuits.viewer.data

import kotlinx.serialization.json.jsonPrimitive
import org.citruscircuits.viewer.StartupActivity

/**
 * @return A string value of any team data value in the database provided the team number and requested field.
 * `null` if the value cannot be found.
 */
fun getTeamObjectByKey(teamNumber: String, field: String) =
    StartupActivity.databaseReference?.team?.get(teamNumber)?.get(field)?.jsonPrimitive?.content
