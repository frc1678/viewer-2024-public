package org.citruscircuits.viewer.data

/**
 * @param teamNumber The team to return a value for
 * @param field The data field to return a value for
 */
fun getPickabilityDataValue(teamNumber: String, field: String): String {
    var max: Float
    var value: String
    var pickabilityDatapoint: String
    val defense =
        getTeamDataValue(teamNumber, "defensive_rating_second_pickability").toFloatOrNull() ?: 0f
    val proxy =
        getTeamDataValue(teamNumber, "defense_proxy_second_pickability").toFloatOrNull() ?: 0f
    val score = getTeamDataValue(teamNumber, "scoring_second_pickability").toFloatOrNull() ?: 0f
    val ferry = getTeamDataValue(teamNumber, "ferrying_second_pickability").toFloatOrNull() ?: 0f
    if (field == "second_defensive_pickability") {
        if (defense > proxy) {
            pickabilityDatapoint = "Def Rating"
            max = defense
        } else if (proxy > defense) {
            pickabilityDatapoint = "Def Proxy"
            max = proxy
        } else {
            pickabilityDatapoint = "?"
            max = defense
        }
    } else if (field == "second_scoring_pickability") {
        if (score > ferry) {
            pickabilityDatapoint = "Scoring"
            max = score
        } else if (ferry > score) {
            pickabilityDatapoint = "Ferrying"
            max = ferry
        } else {
            pickabilityDatapoint = "?"
            max = score
        }
    } else {
        if (defense > proxy && defense > score && defense > ferry) {
            pickabilityDatapoint = "Def Rating"
            max = defense
        } else if (proxy > defense && proxy > score && proxy > ferry) {
            pickabilityDatapoint = "Def Proxy"
            max = proxy
        } else if (score > defense && score > proxy && score > ferry) {
            pickabilityDatapoint = "Scoring"
            max = score
        } else if (ferry > defense && ferry > score && ferry > proxy) {
            pickabilityDatapoint = "Ferrying"
            max = ferry
        } else {
            pickabilityDatapoint = "?"
            max = defense
        }
    }
    value = "$pickabilityDatapoint&$max"
    return value
}