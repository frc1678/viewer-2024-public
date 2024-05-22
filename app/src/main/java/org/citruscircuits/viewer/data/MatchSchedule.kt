package org.citruscircuits.viewer.data

import kotlinx.serialization.Serializable

@Serializable
data class MatchScheduleMatchTeam(val color: String, val number: String)

@Serializable
data class MatchScheduleMatch(val teams: List<MatchScheduleMatchTeam>)
