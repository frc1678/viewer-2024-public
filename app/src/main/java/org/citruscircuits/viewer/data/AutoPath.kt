package org.citruscircuits.viewer.data

import kotlinx.serialization.Serializable

/**
 * This is a data class to hold auto path data.
 * Because the data can be null from server, all the values need to be nullable too.
 */
@Suppress("PropertyName")
@Serializable
data class AutoPath(
    val match_numbers_played: String? = null,
    val start_position: String? = null,
    val has_preload: Boolean? = null,
    val intake_position_1: String? = null,
    val intake_position_2: String? = null,
    val intake_position_3: String? = null,
    val intake_position_4: String? = null,
    val intake_position_5: String? = null,
    val intake_position_6: String? = null,
    val intake_position_7: String? = null,
    val intake_position_8: String? = null,
    val score_1: String? = null,
    val score_2: String? = null,
    val score_3: String? = null,
    val score_4: String? = null,
    val score_5: String? = null,
    val score_6: String? = null,
    val score_7: String? = null,
    val score_8: String? = null,
    val score_9: String? = null,
    val score_1_successes: Int? = null,
    val score_2_successes: Int? = null,
    val score_3_successes: Int? = null,
    val score_4_successes: Int? = null,
    val score_5_successes: Int? = null,
    val score_6_successes: Int? = null,
    val score_7_successes: Int? = null,
    val score_8_successes: Int? = null,
    val score_9_successes: Int? = null,
    val num_matches_ran: Int? = null,
    val leave: Boolean? = null,
)
