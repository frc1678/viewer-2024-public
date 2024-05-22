package org.citruscircuits.viewer.data

import kotlinx.serialization.json.jsonPrimitive
import org.citruscircuits.viewer.StartupActivity

/**
 * @return A string value of any alliance object in the database
 * provided the match number, alliance color, and requested field.
 * `null` if the value cannot be found.
 */
fun getAllianceInMatchObjectByKey(allianceColor: String, matchNumber: String, field: String) =
    StartupActivity.databaseReference?.aim?.get(matchNumber)?.let { if (allianceColor == "red") it.red else it.blue }
        ?.get(field)?.jsonPrimitive?.content
