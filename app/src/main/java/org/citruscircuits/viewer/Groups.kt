package org.citruscircuits.viewer

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.citruscircuits.viewer.constants.Constants
import java.io.File

/**
 * Manager for groups of teams.
 */
object Groups {
    /**
     * The colors to highlight groups in.
     */
    val colors = listOf(Color.Yellow, Color.Green, Color.Cyan, Color.Magenta)

    /**
     * The number of groups there are.
     */
    val groupCount = colors.size

    /**
     * The file on the device in which groups are stored.
     */
    private val file = File(Constants.DOWNLOADS_FOLDER, "viewer-groups.json")

    /**
     * The state of the groups. Each inner list is a group. The number of groups should always match [groupCount].
     */
    val groups = MutableStateFlow<List<List<String>>>(List(groupCount) { emptyList() })

    /**
     * Reads from the groups file and writes any changes to groups to the groups file.
     */
    suspend fun startListener() {
        // read from file or create a new list if there is no file
        groups.value = if (file.exists()) Json.decodeFromString(file.readText()) else List(groupCount) { emptyList() }
        // write to file on state changes
        groups.collect { state ->
            file.writeText(Json.encodeToString(state))
        }
    }
}
