package org.citruscircuits.viewer.fragments.team_details

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.data.getTeamDataValue
import java.io.File

/**
 * Page for showing robot pictures.
 * Navigate to this page by clicking on the Picture button in the top right of Team Details.
 * The Picture button will only appear if the phone has a picture for the robot.
 * Pictures are stored in the file path:
 * storage -> self -> primary -> Android -> data -> com.example.viewer -> files
 */
class RobotPicFragment : Fragment() {
    private var teamNumber: String? = null
    private var teamName: String? = null
    private var picFileFull: File? = null
    private var picFileSide: File? = null

    /**
     * Gets the picture files from the phone's Viewer data folder.
     */
    private fun getPicFiles() {
        picFileFull = File(Constants.DOWNLOADS_FOLDER, "${teamNumber}_full_robot.jpg")
        picFileSide = File(Constants.DOWNLOADS_FOLDER, "${teamNumber}_side.jpg")
    }

    /**
     * Rotates the robot pictures to be displayed correctly.
     */
    private fun Bitmap.rotated() = Bitmap.createBitmap(
        this, 0, 0, width, height, Matrix().also { it.postRotate(90F) }, true
    )

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        // Get the team number and team name from the arguments
        teamNumber = arguments?.getString(Constants.TEAM_NUMBER, Constants.NULL_CHARACTER)
        teamName = getTeamDataValue(teamNumber!!, "team_name")
        getPicFiles()
        // List of pictures
        val picList = listOf(picFileFull, picFileSide)
        // Titles for each picture
        val picTitles = mapOf(picFileFull to "Full", picFileSide to "Side")
        setContent {
            BoxWithConstraints {
                // To adjust image padding, only change what maxWidth.value is being multiplied by
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = (15 * maxWidth.value / 392.72726).dp)
                ) {
                    // The header for the page
                    Text(
                        "Robot Pictures for $teamNumber",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(vertical = 5.dp)
                    )
                    // Displays each image
                    val state = rememberLazyListState()
                    LazyRow(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize(),
                        state = state,
                        flingBehavior = rememberSnapFlingBehavior(lazyListState = state)
                    ) {
                        items(picList) { pic ->
                            if (pic != null) {
                                if (pic.exists()) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Picture title
                                        Text(
                                            text = picTitles[pic]!!,
                                            fontSize = 15.sp,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )
                                        // Robot picture
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            Image(
                                                bitmap = BitmapFactory
                                                    .decodeFile(pic.absolutePath)
                                                    .rotated()
                                                    .asImageBitmap(),
                                                contentDescription = picTitles[pic]!!,
                                                modifier = Modifier
                                                    .fillParentMaxSize()
                                                    .padding(5.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

