package org.citruscircuits.viewer.fragments.team_list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import org.citruscircuits.viewer.MainViewerActivity
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.data.getTeamName
import org.citruscircuits.viewer.data.sortTeamList

/**
 * Page displaying all teams, sorted numerically.
 *
 * @param onOpenTeamDetails Callback to open the team details page for the given team.
 * @param onOpenNotes Callback to open the notes page for the given team.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TeamListPage(onOpenTeamDetails: (team: String) -> Unit, onOpenNotes: (team: String) -> Unit) {
    // get team list sorted
    val teams = sortTeamList(MainViewerActivity.teamList)
    // background
    Surface {
        Column(modifier = Modifier.fillMaxSize()) {
            // title
            Box(
                // centered
                contentAlignment = Alignment.Center, modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
            ) {
                Text("Team List", style = MaterialTheme.typography.headlineLarge)
            }
            HorizontalDivider()
            // main list
            LazyColumn {
                itemsIndexed(teams) { index, team ->
                    // state to force recomposition if the team is starred
                    // TODO remove when starred teams is changed to be observable
                    var starredUpdater by remember { mutableStateOf(false) }
                    // whether the team is starred
                    val starred = remember(starredUpdater) { MainViewerActivity.StarredTeams.contains(team) }
                    Column {
                        if (index != 0) HorizontalDivider()
                        ListItem(
                            // team number
                            headlineContent = { Text(team) },
                            // team name
                            supportingContent = { Text(getTeamName(team) ?: Constants.NULL_CHARACTER) },
                            // star button
                            trailingContent = {
                                IconButton(
                                    onClick = {
                                        // change whether the team is in the starred teams list
                                        if (MainViewerActivity.StarredTeams.contains(team)) {
                                            MainViewerActivity.StarredTeams.remove(team)
                                        } else {
                                            MainViewerActivity.StarredTeams.add(team)
                                        }
                                        starredUpdater = !starredUpdater
                                    }
                                ) {
                                    // star icon
                                    if (starred) Icon(Icons.Default.Star, null, tint = colorResource(R.color.Gold))
                                    // dot
                                    else Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .size(4.dp)
                                            .background(LocalContentColor.current)
                                    )
                                }
                            },
                            // navigate to other pages
                            modifier = Modifier.combinedClickable(
                                onClick = { onOpenTeamDetails(team) },
                                onLongClick = { onOpenNotes(team) }
                            )
                        )
                    }
                }
            }
        }
    }
}
