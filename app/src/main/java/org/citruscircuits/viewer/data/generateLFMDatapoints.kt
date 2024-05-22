package org.citruscircuits.viewer.data

import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.constants.Translations

/**
 * @return The list of data points to be displayed in LFM team details based on the list of data points it is called on.
 */
fun List<String>.generateLFMDatapointList(): List<String> {
    val lfmDatapointList = mutableListOf<String>()
    for (dataPoint in this) {
        if ("sd_" !in dataPoint) {
            when (dataPoint) {
                // If the data point is in both team details and LFM team details, add it as is
                in Constants.TEAM_AND_LFM_SHARED_DATA_POINTS -> lfmDatapointList.add(dataPoint)
                // If the data point is a header, add the LFM version
                in Constants.CATEGORY_NAMES -> Translations.TEAM_TO_LFM_HEADERS[dataPoint]?.let {
                    lfmDatapointList.add(it)
                }
                // If the data point is not in the list of data points that are in team details but not in LFM team details,
                // add the data point, except with lfm_ at the beginning of the data point name
                !in Constants.TEAM_DATA_POINTS_NOT_IN_LFM -> lfmDatapointList.add("lfm_$dataPoint")
            }
        }
    }
    return lfmDatapointList
}

/**
 * @return The list it is called on, with LFM counterparts of data points added on.
 */
fun List<String>.addLFMCounterparts(): List<String> {
    val newList = mutableListOf<String>()
    for (dataPoint in this) {
        newList.add(dataPoint)
        if ("sd_" !in dataPoint) {
            newList.add("lfm_$dataPoint")
        }
    }
    return newList
}

/**
 * @return The map it is called on, with LFM counterparts of data points added as keys
 * and values being the corresponding values.
 */
fun <T> Map<String, T>.addLFMCounterparts(): Map<String, T> {
    val newMap = mutableMapOf<String, T>()
    forEach { (datapoint, value) ->
        newMap[datapoint] = value
        if ("sd_" !in datapoint) {
            newMap["lfm_$datapoint"] = value
        }
    }
    return newMap
}
