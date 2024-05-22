package org.citruscircuits.viewer.fragments.team_details

import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntSize
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.data.AutoPath

/**
 * Coordinates for the colored rectangles on the intake positions.
 *
 * Keys are intake positions, values are coordinates as [Offset]s.
 */
val intakeOffsets = mapOf(
    "spike_1" to Offset(295f, 140f),
    "spike_2" to Offset(295f, 300f),
    "spike_3" to Offset(295f, 460f),
    "center_1" to Offset(880f, 80f),
    "center_2" to Offset(880f, 270f),
    "center_3" to Offset(880f, 460f),
    "center_4" to Offset(880f, 650f),
    "center_5" to Offset(880f, 840f),
    "other" to Offset(420f, 840f)
)

/**
 * Coordinates for the text on the intake positions.
 *
 * Keys are intake positions, values are coordinates as [Offset]s.
 */
val intakeTextOffsets = mapOf(
    "spike_1" to Offset(300f, 210f),
    "spike_2" to Offset(300f, 370f),
    "spike_3" to Offset(300f, 530f),
    "center_1" to Offset(885f, 160f),
    "center_2" to Offset(885f, 340f),
    "center_3" to Offset(885f, 530f),
    "center_4" to Offset(885f, 720f),
    "center_5" to Offset(885f, 910f),
    "other" to Offset(425f, 910f)
)

/**
 * Coordinates for the text on each scoring position.
 *
 * Keys are score positions, values are coordinates as [Offset]s.
 */
val scorePosTextOffsets = mapOf(
    "amp" to Offset(190f, 95f),
    "speaker" to Offset(4f, 365f),
    "ferry" to Offset(475f, 95f)
)

/**
 * Coordinates for the colored rectangles covering scoring positions.
 *
 * Keys are scoring positions, values are pairs of [Offset]s for the top left coordinates of each
 * rectangle to the [Size] of each rectangle.
 */
val scorePosOffsets = mapOf(
    "amp" to (Offset(160f, 45f) to Size(150f, 55f)),
    "speaker" to (Offset(3f, 287f)to Size(120f,120f)),
    "ferry" to (Offset(395f, 45f)to Size(250f,55f)),
)

/**
 * Coordinates for the colored rectangles covering start positions.
 *
 * Keys are start positions, values are pairs of [Offset]s for the top left coordinates of each
 * rectangle to the [Size] of each rectangle.
 */
val startPosOffsets = mapOf(
    "1" to (Offset(30f, 630f) to Size(205f, 190f)),
    "2" to (Offset(30f, 440f) to Size(205f, 190f)),
    "3" to (Offset(130f, 255f) to Size(105f, 190f)),
    "4" to (Offset(30f, 100f) to Size(205f, 160f))
)

/**
 * Coordinates for the top text on start positions.
 *
 * Keys are start positions, values are coordinates as [Offset]s.
 */
val startPosTextOffsets = mapOf(
    "1" to Offset(90f,740f),
    "2" to Offset(90f, 550f),
    "3" to Offset(140f, 365f),
    "4" to Offset(90f, 200f)
)

/**
 * The map showing the given [AutoPath].
 *
 * @param autoPath The [AutoPath] object giving the data for this Auto Path.
 * @param modifier Modifier for the display
 */
@Composable
fun AutoPath(autoPath: AutoPath, modifier: Modifier = Modifier) {
    // List of all possible intakes
    val allIntakeList = listOf(
        autoPath.intake_position_1,
        autoPath.intake_position_2,
        autoPath.intake_position_3,
        autoPath.intake_position_4,
        autoPath.intake_position_5,
        autoPath.intake_position_6,
        autoPath.intake_position_7,
        autoPath.intake_position_8
    )
    // List of all possible scores
    val allScoreList = listOf(
        autoPath.score_1,
        autoPath.score_2,
        autoPath.score_3,
        autoPath.score_4,
        autoPath.score_5,
        autoPath.score_6,
        autoPath.score_7,
        autoPath.score_8,
        autoPath.score_9
    )
    // List of all score success values
    val allScoreSuccessesList = mutableListOf(
        autoPath.score_1_successes,
        autoPath.score_2_successes,
        autoPath.score_3_successes,
        autoPath.score_4_successes,
        autoPath.score_5_successes,
        autoPath.score_6_successes,
        autoPath.score_7_successes,
        autoPath.score_8_successes,
        autoPath.score_9_successes
    )
    // Get the image of the map
    val fieldMapImage = ImageBitmap.imageResource(id = R.drawable.field_map_auto_paths)
    // Canvas to show the field map and all the things on it
    var boxsize by remember { mutableStateOf(IntSize.Zero) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates -> boxsize = coordinates.size }
    ) {
        Image(fieldMapImage, "", modifier = Modifier.fillMaxWidth())
        Canvas(modifier = Modifier.fillMaxWidth()) {
            if (autoPath.start_position != "0") {
                // Path showing the order that they intook and scored gamepieces in
                var path = Path()
                // The Path starts at the start position
                path.moveTo(
                    (startPosOffsets[autoPath.start_position]!!.first.x * boxsize.width / 1000) +
                        ((startPosOffsets[autoPath.start_position]!!.second.width / 2f) * boxsize.width / 1000),
                    (startPosOffsets[autoPath.start_position]!!.first.y * boxsize.height / 1000) +
                        ((startPosOffsets[autoPath.start_position]!!.second.height / 2f) * boxsize.height / 1000)
                )
                var i = 1f
                // Adds lines to the Path until the auto stops intaking gamepieces
                for (intake in allIntakeList) {
                    if (intake != "none") {
                        path.lineTo(
                            (intakeOffsets[intake]!!.x * boxsize.width / 1000) + (50f * boxsize.width / 1000),
                            (intakeOffsets[intake]!!.y * boxsize.height / 1000) + (50f * boxsize.height / 1000)
                        )
                        drawPath(path, Color(14, 99, 31), style = Stroke(width = 25f), alpha = i)
                        path = Path()
                        path.moveTo(
                            (intakeOffsets[intake]!!.x * boxsize.width / 1000) + (50f * boxsize.width / 1000),
                            (intakeOffsets[intake]!!.y * boxsize.height / 1000) + (50f * boxsize.height / 1000)
                        )
                        i *= 0.75f
                    } else break
                }
                // Draws the Path
                drawPath(path, Color(14, 99, 31), style = Stroke(width = 25f), alpha = i)
            }

            // Draw the starting position rectangle
            if (autoPath.start_position != "0") {
                drawRect(
                    // Set color based on success rate of scoring that piece
                    // Orange if they had a preloaded gamepiece and did not even attempt to score it
                    color = if (autoPath.has_preload == true && autoPath.score_1 == "none") Color(255, 140, 0)
                    // Green if the success rate of scoring the gamepiece is greater than 2/3
                    else if (autoPath.has_preload == true && (((autoPath.score_1_successes!!.toFloat()) / (autoPath.num_matches_ran!!.toFloat())) >= (2f / 3f))) {
                        Color.Green
                    // Yellow if between 2/3 and 1/3
                    } else if (autoPath.has_preload == true && (((autoPath.score_1_successes!!.toFloat()) / (autoPath.num_matches_ran!!.toFloat())) >= (1f / 3f))) {
                        Color.Yellow
                    // Red if less than 1/3
                    } else if (autoPath.has_preload == true) Color.Red
                    // If no preload, defaults color to gray
                    else Color.Gray,
                    topLeft = Offset(
                        startPosOffsets[autoPath.start_position]!!.first.x * boxsize.width / 1000,
                        startPosOffsets[autoPath.start_position]!!.first.y * boxsize.height / 1000
                    ),
                    size = Size(
                        startPosOffsets[autoPath.start_position]!!.second.width * boxsize.width / 1000,
                        startPosOffsets[autoPath.start_position]!!.second.height * boxsize.height / 1000
                    ),
                )
            }

            // If preloaded, score_1 is scoring the preload
            if (autoPath.has_preload == true) {
                // Draw the text for the success-to-attempt ratio of scoring preload
                if (autoPath.score_1 != "none") {
                    drawText(
                        text = "${autoPath.score_1_successes}/${autoPath.num_matches_ran}",
                        offset = Offset(
                            startPosTextOffsets[autoPath.start_position]!!.x * boxsize.width / 1000,
                            startPosTextOffsets[autoPath.start_position]!!.y * boxsize.height / 1000
                        ),
                        size = 50f * boxsize.width / 1000
                    )
                }

                // Only show if it exists
                if (autoPath.intake_position_1 != null && autoPath.score_2 != null && autoPath.intake_position_1 != "none") {
                    drawGamepiece(
                        autoPath.num_matches_ran,
                        autoPath.intake_position_1,
                        autoPath.score_2,
                        autoPath.score_2_successes,
                        boxsize
                    )
                }

                // Only show if it exists
                if (autoPath.intake_position_2 != null && autoPath.score_3 != null && autoPath.intake_position_2 != "none") {
                    drawGamepiece(
                        autoPath.num_matches_ran,
                        autoPath.intake_position_2,
                        autoPath.score_3,
                        autoPath.score_3_successes,
                        boxsize
                    )
                }

                // Only show if it exists
                if (autoPath.intake_position_3 != null && autoPath.score_4 != null && autoPath.intake_position_3 != "none") {
                    drawGamepiece(
                        autoPath.num_matches_ran,
                        autoPath.intake_position_3,
                        autoPath.score_4,
                        autoPath.score_4_successes,
                        boxsize
                    )
                }

                // Only show if it exists
                if (autoPath.intake_position_4 != null && autoPath.score_5 != null && autoPath.intake_position_4 != "none") {
                    drawGamepiece(
                        autoPath.num_matches_ran,
                        autoPath.intake_position_4,
                        autoPath.score_5,
                        autoPath.score_5_successes,
                        boxsize
                    )
                }

                // Only show if it exists
                if (autoPath.intake_position_5 != null && autoPath.score_6 != null && autoPath.intake_position_5 != "none") {
                    drawGamepiece(
                        autoPath.num_matches_ran,
                        autoPath.intake_position_5,
                        autoPath.score_6,
                        autoPath.score_6_successes,
                        boxsize
                    )
                }

                // Only show if it exists
                if (autoPath.intake_position_6 != null && autoPath.score_7 != null && autoPath.intake_position_6 != "none") {
                    drawGamepiece(
                        autoPath.num_matches_ran,
                        autoPath.intake_position_6,
                        autoPath.score_7,
                        autoPath.score_7_successes,
                        boxsize
                    )
                }

                // Only show if it exists
                if (autoPath.intake_position_7 != null && autoPath.score_8 != null && autoPath.intake_position_7 != "none") {
                    drawGamepiece(
                        autoPath.num_matches_ran,
                        autoPath.intake_position_7,
                        autoPath.score_8,
                        autoPath.score_8_successes,
                        boxsize
                    )
                }

                // Only show if it exists
                if (autoPath.intake_position_8 != null && autoPath.score_9 != null && autoPath.intake_position_8 != "none") {
                    drawGamepiece(
                        autoPath.num_matches_ran,
                        autoPath.intake_position_8,
                        autoPath.score_9,
                        autoPath.score_9_successes,
                        boxsize
                    )
                }
            } else {
                // Score #s correspond to intake #s when there is no preload
                // Only show if it exists
                if (autoPath.intake_position_1 != null && autoPath.score_1 != null && autoPath.intake_position_1 != "none") {
                    drawGamepiece(
                        autoPath.num_matches_ran,
                        autoPath.intake_position_1,
                        autoPath.score_1,
                        autoPath.score_1_successes,
                        boxsize
                    )
                }

                // Only show if it exists
                if (autoPath.intake_position_2 != null && autoPath.score_2 != null && autoPath.intake_position_2 != "none") {
                    drawGamepiece(
                        autoPath.num_matches_ran,
                        autoPath.intake_position_2,
                        autoPath.score_2,
                        autoPath.score_2_successes,
                        boxsize
                    )
                }

                // Only show if it exists
                if (autoPath.intake_position_3 != null && autoPath.score_3 != null && autoPath.intake_position_3 != "none") {
                    drawGamepiece(
                        autoPath.num_matches_ran,
                        autoPath.intake_position_3,
                        autoPath.score_3,
                        autoPath.score_3_successes,
                        boxsize
                    )
                }

                // Only show if it exists
                if (autoPath.intake_position_4 != null && autoPath.score_4 != null && autoPath.intake_position_4 != "none") {
                    drawGamepiece(
                        autoPath.num_matches_ran,
                        autoPath.intake_position_4,
                        autoPath.score_4,
                        autoPath.score_4_successes,
                        boxsize
                    )
                }

                // Only show if it exists
                if (autoPath.intake_position_5 != null && autoPath.score_5 != null && autoPath.intake_position_5 != "none") {
                    drawGamepiece(
                        autoPath.num_matches_ran,
                        autoPath.intake_position_5,
                        autoPath.score_5,
                        autoPath.score_5_successes,
                        boxsize
                    )
                }

                // Only show if it exists
                if (autoPath.intake_position_6 != null && autoPath.score_6 != null && autoPath.intake_position_6 != "none") {
                    drawGamepiece(
                        autoPath.num_matches_ran,
                        autoPath.intake_position_6,
                        autoPath.score_6,
                        autoPath.score_6_successes,
                        boxsize
                    )
                }

                // Only show if it exists
                if (autoPath.intake_position_7 != null && autoPath.score_7 != null && autoPath.intake_position_7 != "none") {
                    drawGamepiece(
                        autoPath.num_matches_ran,
                        autoPath.intake_position_7,
                        autoPath.score_7,
                        autoPath.score_7_successes,
                        boxsize
                    )
                }

                // Only show if it exists
                if (autoPath.intake_position_8 != null && autoPath.score_8 != null && autoPath.intake_position_8 != "none") {
                    drawGamepiece(
                        autoPath.num_matches_ran,
                        autoPath.intake_position_8,
                        autoPath.score_8,
                        autoPath.score_8_successes,
                        boxsize
                    )
                }
            }

            var scoreAmpAttempts = 0
            var scoreAmpSuccesses = 0
            var scoreSpeakerAttempts = 0
            var scoreSpeakerSuccesses = 0
            var ferryAttempts = 0
            var ferrySuccesses = 0

            /*
            Takes the amount of times the team scores in the amp & speaker and how many times
            it successfully scores in those locations and stores them in the corresponding lists
             */
            var score = 0
            for (item in allScoreList) {
                if (item != null) {
                    if (item.toString() == "amp" || item.toString() == "fail_amp") {
                        scoreAmpAttempts++
                        // Ensures values aren't null before adding them to the success list
                        if (allScoreSuccessesList[score] != null) {
                            scoreAmpSuccesses += allScoreSuccessesList[score]!!
                        }
                    } else if (item.toString() == "speaker" || item.toString() == "fail_speaker") {
                        scoreSpeakerAttempts++
                        // Ensures values aren't null before adding them to the success list
                        if (allScoreSuccessesList[score] != null) {
                            scoreSpeakerSuccesses += allScoreSuccessesList[score]!!
                        }
                    } else if (item.toString() == "ferry" || item.toString() == "fail_ferry") {
                        ferryAttempts++
                        // Ensures values aren't null before adding them to the success list
                        if (allScoreSuccessesList[score] != null) {
                            ferrySuccesses += allScoreSuccessesList[score]!!
                        }
                    }
                }
                score++
            }

            // Values that are displayed in the success-to-attempt ratio text over the speaker & amp
            val score_amp_attempts = scoreAmpAttempts * autoPath.num_matches_ran!!
            val score_speaker_attempts = scoreSpeakerAttempts * autoPath.num_matches_ran
            val ferry_attempts = ferryAttempts * autoPath.num_matches_ran

            // Draw box over amp position
            drawRoundRect(
                // Sets color based on success rate of scoring in the amp
                // Light gray if they didn't even try to score there
                color = if (score_amp_attempts == 0) Color.LightGray
                // Green if the success rate is greater than 2/3
                else if ((scoreAmpSuccesses.toFloat() / score_amp_attempts.toFloat()) >= (2f / 3f)) Color.Green
                // Yellow if between 2/3 and 1/3
                else if ((scoreAmpSuccesses.toFloat() / score_amp_attempts.toFloat()) >= (1f / 3f)) Color.Yellow
                // Red if less than 1/3
                else Color.Red,
                topLeft = Offset(
                    scorePosOffsets["amp"]!!.first.x * boxsize.width / 1000,
                    scorePosOffsets["amp"]!!.first.y * boxsize.height / 1000
                ),
                size = Size(
                    scorePosOffsets["amp"]!!.second.width * boxsize.width / 1000,
                    scorePosOffsets["amp"]!!.second.height * boxsize.height / 1000
                ),
                cornerRadius = CornerRadius(10f * boxsize.width / 1000),
            )
            // Draw the text for the success-to-attempt ratio of scoring in the amp
            if (score_amp_attempts != 0) {
                drawText(
                    text = "$scoreAmpSuccesses/$score_amp_attempts",
                    offset = Offset(
                        scorePosTextOffsets["amp"]!!.x * boxsize.width / 1000,
                        scorePosTextOffsets["amp"]!!.y * boxsize.height / 1000
                    ),
                    size = 60f * boxsize.width / 1000
                )
            }

            // Draw box over speaker position
            drawRoundRect(
                // Sets color based on success rate of scoring in the amp
                // Light gray if they didn't even try to score there
                color = if (score_speaker_attempts == 0) Color.LightGray
                // Green if the success rate is greater than 2/3
                else if ((scoreSpeakerSuccesses.toFloat() / score_speaker_attempts.toFloat()) >= (2f / 3f)) Color.Green
                // Yellow if between 2/3 and 1/3
                else if ((scoreSpeakerSuccesses.toFloat() / score_speaker_attempts.toFloat()) >= (1f / 3f)) Color.Yellow
                // Red if less than 1/3
                else Color.Red,
                topLeft = Offset(
                    scorePosOffsets["speaker"]!!.first.x * boxsize.width / 1000,
                    scorePosOffsets["speaker"]!!.first.y * boxsize.height / 1000
                ),
                size = Size(
                    scorePosOffsets["speaker"]!!.second.width * boxsize.width / 1000,
                    scorePosOffsets["speaker"]!!.second.height * boxsize.height / 1000
                ),
                cornerRadius = CornerRadius(10f * boxsize.width / 1000),
            )
            // Draw the text for the success-to-attempt ratio of scoring in the speaker
            if (score_speaker_attempts != 0) {
                drawText(
                    text = "$scoreSpeakerSuccesses/$score_speaker_attempts",
                    offset = Offset(
                        scorePosTextOffsets["speaker"]!!.x * boxsize.width / 1000,
                        scorePosTextOffsets["speaker"]!!.y * boxsize.height / 1000
                    ),
                    size = 40f * boxsize.width / 1000
                )
            }

            // Draw box for ferrying
            drawRoundRect(
                // Sets color based on success rate of ferrying
                // Light gray if they didn't even try to ferry
                color = if (ferry_attempts == 0) Color.LightGray
                // Green if the success rate is greater than 2/3
                else if ((ferrySuccesses.toFloat() / ferry_attempts.toFloat()) >= (2f / 3f)) Color.Green
                // Yellow if between 2/3 and 1/3
                else if ((ferrySuccesses.toFloat() / ferry_attempts.toFloat()) >= (1f / 3f)) Color.Yellow
                // Red if less than 1/3
                else Color.Red,
                topLeft = Offset(
                    scorePosOffsets["ferry"]!!.first.x * boxsize.width / 1000,
                    scorePosOffsets["ferry"]!!.first.y * boxsize.height / 1000
                ),
                size = Size(
                    scorePosOffsets["ferry"]!!.second.width * boxsize.width / 1000,
                    scorePosOffsets["ferry"]!!.second.height * boxsize.height / 1000
                ),
                cornerRadius = CornerRadius(10f * boxsize.width / 1000),
            )
            // Draw the text for the success-to-attempt ratio of ferrying
            if (ferry_attempts != 0) {
                drawText(
                    text = "$ferrySuccesses/$ferry_attempts",
                    offset = Offset(
                        scorePosTextOffsets["ferry"]!!.x * boxsize.width / 1000,
                        scorePosTextOffsets["ferry"]!!.y * boxsize.height / 1000
                    ),
                    size = 40f * boxsize.width / 1000
                )
            }
        }
    }
}

/**
 * Helper for drawing text in a [Canvas].
 *
 * @param text The text to draw.
 * @param offset The coordinates to draw the text at.
 * @param size The size of the text.
 */
fun DrawScope.drawText(text: String, offset: Offset, size: Float) = drawIntoCanvas {
    it.nativeCanvas.drawText(text, offset.x, offset.y, Paint().apply { textSize = size })
}

/**
 * Helper for drawing the status of a gamepiece in a [Canvas].
 *
 * @param numMatchesRan The number of matches the auto was ran in.
 * @param intakePosition Where the team intook the gamepiece.
 * @param scoreTarget Where the team scored the gamepiece.
 * @param scoreSuccesses The number of times the team succeeded in scoring the gamepiece.
 */
fun DrawScope.drawGamepiece(
    numMatchesRan: Int?, intakePosition: String?, scoreTarget: String?, scoreSuccesses: Int?, boxsize: IntSize
) = drawIntoCanvas {
    // Draw the rectangle for the gamepiece, color depends on success-to-attempt ratio
    drawRoundRect(
        // Orange if they intook the gamepiece, but didn't even attempt to score it
        color = if (scoreTarget == "none") Color(255, 140, 0)
        // Green if the success rate is greater than 2/3
        else if (((scoreSuccesses!!.toFloat()) / (numMatchesRan!!.toFloat())) >= (2f / 3f)) Color.Green
        // Yellow if between 2/3 and 1/3
        else if (((scoreSuccesses.toFloat()) / (numMatchesRan.toFloat())) >= (1f / 3f)) Color.Yellow
        // Red if less than 1/3
        else Color.Red,
        topLeft = Offset(
            intakeOffsets[intakePosition]!!.x * boxsize.width / 1000,
            intakeOffsets[intakePosition]!!.y * boxsize.height / 1000
        ),
        size = Size(100f * boxsize.width / 1000, 100f * boxsize.height / 1000),
        cornerRadius = CornerRadius(10f * boxsize.width / 1000),
    )
    // Draw the text for the success-to-attempt ratio of scoring this piece after the intake
    if (scoreTarget != "none") {
        drawText(
            text = "${scoreSuccesses}/${numMatchesRan}",
            offset = Offset(
                intakeTextOffsets[intakePosition]!!.x * boxsize.width / 1000,
                intakeTextOffsets[intakePosition]!!.y * boxsize.height / 1000
            ),
            size = 60f * boxsize.width / 1000
        )
    }
}