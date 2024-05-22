package org.citruscircuits.viewer.data

import kotlinx.serialization.json.jsonPrimitive
import org.citruscircuits.viewer.StartupActivity.Companion.databaseReference

/**
 * Gets an alliance's predicted alliance data for a specific field
 * @param allianceNumber The key of the alliance
 * @param field The field of data being fetched
 */
fun getPredictedAllianceDataByKey(allianceNumber: Int, field: String) =
    databaseReference?.alliance?.get(allianceNumber.toString())?.get(field)?.jsonPrimitive?.content
