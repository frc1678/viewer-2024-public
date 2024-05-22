package org.citruscircuits.viewer.fragments.match_schedule

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.citruscircuits.viewer.Groups
import org.citruscircuits.viewer.MainViewerActivity
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.data.Match
import org.citruscircuits.viewer.data.getAllianceInMatchObjectByKey

/**
 * Filters to be applied to the match schedule page.
 *
 * @param readable A human-readable name for the filter.
 */
enum class MatchScheduleFilter(val readable: String) {
    ALL("All matches"), OUR("Our matches"), STARRED("Starred matches")
}

/**
 * Page displaying the match schedule.
 *
 * @param getMatchSchedule Method for getting the match schedule.
 * @param initialSearch Initial value to populate search with.
 * @param onOpenMatchDetails Callback to open the match details for the given match number.
 * @param onOpenTeamDetails Callback to open the team details for the given team number.
 */
@Composable
fun MatchSchedulePage(
    getMatchSchedule: (filter: MatchScheduleFilter, search: String) -> Map<String, Match>,
    initialSearch: String = "",
    onOpenMatchDetails: (String) -> Unit,
    onOpenTeamDetails: (String) -> Unit,
    onOpen: (Map<String, Match>, LazyListState) -> Unit
) {
    Surface {
        Column(modifier = Modifier.fillMaxSize()) {
            // current filter option
            var filter by rememberSaveable { mutableStateOf(MatchScheduleFilter.ALL) }
            // current search query
            var search by rememberSaveable { mutableStateOf(initialSearch) }
            // get match schedule
            // update match schedule if filter/search updates
            val matchSchedule by remember(filter, search) { mutableStateOf(getMatchSchedule(filter, search)) }
            // filter/search menu
            Column {
                FilterSearchMenu(search, { search = it }, filter, { filter = it }, onOpenTeamDetails)
                HorizontalDivider()
            }
            val state = rememberLazyListState()
            // main list
            MatchSchedule(matchSchedule, onOpenMatchDetails, onSearchTeam = { search = it }, listState = state)
            onOpen(matchSchedule, state)
        }
    }
}

/**
 * Menu for editing filter/search options.
 *
 * @param search The current search query.
 * @param setSearch Setter for [search].
 * @param filter The current filter option.
 * @param setFilter Setter for [filter].
 * @param onOpenTeamDetails Callback to open the team details for the given team number.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun FilterSearchMenu(
    search: String,
    setSearch: (String) -> Unit,
    filter: MatchScheduleFilter,
    setFilter: (MatchScheduleFilter) -> Unit,
    onOpenTeamDetails: (String) -> Unit
) {
    // whether the menu is expanded
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    // animate opening/closing
    Surface(modifier = Modifier.animateContentSize()) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { searchOpen = !searchOpen }
                    .padding(8.dp)
            ) {
                // title
                Icon(Icons.AutoMirrored.Filled.ManageSearch, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Filter and search")
                Spacer(modifier = Modifier.width(8.dp))
                // show badge if filter/search is active
                val chosenOptions =
                    (if (search.isNotEmpty()) 1 else 0) + (if (filter != MatchScheduleFilter.ALL) 1 else 0)
                if (chosenOptions != 0) {
                    Badge { Text(chosenOptions.toString()) }
                }
                Spacer(modifier = Modifier.weight(1f))
                // dropdown icon
                if (searchOpen) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                } else {
                    Icon(Icons.AutoMirrored.Filled.ArrowLeft, contentDescription = null)
                }
            }
            if (searchOpen) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(8.dp)) {
                    // search box
                    OutlinedTextField(
                        value = search,
                        onValueChange = setSearch,
                        label = { Text("Search") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        // clear button
                        trailingIcon = {
                            if (search.isNotEmpty()) {
                                IconButton(onClick = { setSearch("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = null)
                                }
                            }
                        },
                        // make keyboard show search action instead of enter
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        // open team details on keyboard search action
                        keyboardActions = KeyboardActions(onSearch = { onOpenTeamDetails(search) }),
                        modifier = Modifier.fillMaxWidth()
                    )
                    // whether dropdown is shown
                    var expanded by rememberSaveable { mutableStateOf(false) }
                    // filter dropdown
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        // show the current filter
                        OutlinedTextField(
                            value = filter.readable, onValueChange = {}, label = { Text("Filter") },
                            readOnly = true,
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        // dropdown menu
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            // show item for each filter
                            MatchScheduleFilter.values().forEach {
                                DropdownMenuItem(
                                    text = { Text(it.readable) },
                                    onClick = {
                                        setFilter(it)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    // actions
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // clear button
                        OutlinedButton(
                            onClick = {
                                // clear filter/search
                                setFilter(MatchScheduleFilter.ALL)
                                setSearch("")
                                // close the menu
                                searchOpen = false
                            }
                        ) {
                            Icon(Icons.Default.ClearAll, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clear options")
                        }
                        // done button
                        OutlinedButton(onClick = { searchOpen = false }) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Done")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Lazy list showing the matches.
 *
 * @param matchSchedule The matches to show.
 * @param onOpenMatchDetails Callback to open the match details for the given match number.
 * @param onSearchTeam Callback to set the search query to the given team number.
 */
@Composable
private fun MatchSchedule(
    matchSchedule: Map<String, Match>,
    onOpenMatchDetails: (String) -> Unit,
    onSearchTeam: (String) -> Unit,
    listState: LazyListState
) {
    LazyColumn(state = listState) {
        // show an item for each match
        itemsIndexed(matchSchedule.values.toList()) { index, match ->
            Column(modifier = Modifier.fillMaxWidth()) {
                // divider
                if (index != 0) HorizontalDivider()
                // show match
                Match(
                    match, onOpenMatchDetails = { onOpenMatchDetails(match.matchNumber) }, onSearchTeam = onSearchTeam
                )
            }
        }
    }
}

/**
 * List item displaying details of a match in the match schedule.
 *
 * @param match The match to show details for.
 * @param onOpenMatchDetails Callback to open the match details for this match.
 * @param onSearchTeam Callback to set the search query to the given team number.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Match(match: Match, onOpenMatchDetails: () -> Unit, onSearchTeam: (String) -> Unit) {
    // check whether there is data available
    val hasActualData =
        getAllianceInMatchObjectByKey(Constants.RED, match.matchNumber, "has_actual_data").toBoolean()
            && getAllianceInMatchObjectByKey(Constants.BLUE, match.matchNumber, "has_actual_data").toBoolean()
    // get red's match score, actual or predicted
    val redScore = getAllianceInMatchObjectByKey(
        Constants.RED, match.matchNumber, if (hasActualData) "actual_score" else "predicted_score"
    )?.toFloatOrNull()
    // get blue's match score, actual or predicted
    val blueScore = getAllianceInMatchObjectByKey(
        Constants.BLUE, match.matchNumber, if (hasActualData) "actual_score" else "predicted_score"
    )?.toFloatOrNull()
    // check whether red/blue won (both should be false if tied)
    val redWon = hasActualData && (redScore ?: 0f) > (blueScore ?: 0f)
    val blueWon = hasActualData && (blueScore ?: 0f) > (redScore ?: 0f)
    // state to force recomposition if the match is starred
    // TODO remove when starred matches is changed to be observable
    var starredMatchesUpdater by remember { mutableStateOf(false) }
    // get starred matches
    val starredMatches = remember(starredMatchesUpdater) { MainViewerActivity.starredMatches }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(IntrinsicSize.Max)
            .combinedClickable(
                // toggle starred on long click
                onLongClick = {
                    if (match.matchNumber in starredMatches) {
                        MainViewerActivity.starredMatches.remove(match.matchNumber)
                    } else {
                        MainViewerActivity.starredMatches.add(match.matchNumber)
                    }
                    MainViewerActivity.StarredMatches.input()
                    starredMatchesUpdater = !starredMatchesUpdater
                },
                onClick = onOpenMatchDetails
            )
            // set background color depending on how many starred teams there are
            .background(
                when ((match.redTeams + match.blueTeams).count { MainViewerActivity.StarredTeams.contains(it) }) {
                    0 -> colorResource(id = R.color.Highlight_0)
                    1 -> colorResource(id = R.color.Highlight_1)
                    else -> colorResource(id = R.color.Highlight_2)
                }
            )
            .padding(8.dp)
    ) {
        // left display
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
            // star icon if the match is starred
            Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                if (match.matchNumber in starredMatches) {
                    Icon(Icons.Default.Star, contentDescription = null)
                }
            }
            // match number
            Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                Text(match.matchNumber, style = MaterialTheme.typography.bodyLarge)
            }
            // icon for match status
            Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                if (hasActualData) Icon(Icons.Default.Check, contentDescription = null)
                else Icon(Icons.Default.AccessTime, contentDescription = null)
            }
        }
        // teams
        Column(
            modifier = Modifier
                .weight(4f)
                .fillMaxHeight()
        ) {
            // red
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    // background color red
                    .background(color = colorResource(id = R.color.Red))
                    // black border if red won
                    .then(if (redWon) Modifier.border(3.dp, Color.Black) else Modifier)
            ) {
                // show teams
                match.redTeams.forEach {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            // search team on long click
                            .combinedClickable(onLongClick = { onSearchTeam(it) }, onClick = onOpenMatchDetails)
                    ) {
                        // get highlight color if team is in a group
                        val groups by Groups.groups.collectAsStateWithLifecycle()
                        val highlight = groups
                            .indexOfFirst { group -> it in group }
                            .takeIf { it > -1 }
                            ?.let { Groups.colors[it] }
                        // team number
                        // bold if red won
                        Text(
                            it,
                            color = if (highlight == null) Color.White else Color.Black,
                            fontWeight = if (redWon) FontWeight.Bold else null,
                            modifier = Modifier
                                .background(highlight ?: Color.Transparent)
                                .padding(2.dp)
                        )
                    }
                }
            }
            // blue
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    // background color blue
                    .background(color = colorResource(id = R.color.Blue))
                    // black border if blue won
                    .then(if (blueWon) Modifier.border(3.dp, Color.Black) else Modifier)
            ) {
                // show teams
                match.blueTeams.forEach {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            // search team on long click
                            .combinedClickable(onLongClick = { onSearchTeam(it) }, onClick = onOpenMatchDetails)
                    ) {
                        // get highlight color if team is in a group
                        val groups by Groups.groups.collectAsStateWithLifecycle()
                        val highlight = groups
                            .indexOfFirst { group -> it in group }
                            .takeIf { it > -1 }
                            ?.let { Groups.colors[it] }
                        // team number
                        // bold if blue won
                        Text(
                            it,
                            color = if (highlight == null) Color.White else Color.Black,
                            fontWeight = if (blueWon) FontWeight.Bold else null,
                            modifier = Modifier
                                .background(highlight ?: Color.Transparent)
                                .padding(2.dp)
                        )
                    }
                }
            }
        }
        // scores/RPs
        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
        ) {
            // dividers to form a 'grid'
            HorizontalDivider(modifier = Modifier.align(Alignment.Center))
            VerticalDivider(modifier = Modifier.align(Alignment.Center))
            Column(modifier = Modifier.fillMaxSize()) {
                // red
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // red score
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                    ) {
                        val score = redScore?.let { (if (hasActualData) "%.0f" else "%.1f").format(it) }
                        Text(
                            score ?: Constants.NULL_PREDICTED_SCORE_CHARACTER,
                            fontWeight = if (redWon) FontWeight.Bold else null
                        )
                    }
                    // red RPs
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                    ) {
                        val rp1 = getAllianceInMatchObjectByKey(
                            Constants.RED, match.matchNumber, if (hasActualData) "actual_rp1" else "predicted_rp1"
                        )?.toDoubleOrNull()?.let { it > Constants.PREDICTED_RANKING_POINT_QUALIFICATION } ?: false
                        val rp2 = getAllianceInMatchObjectByKey(
                            Constants.RED, match.matchNumber, if (hasActualData) "actual_rp2" else "predicted_rp2"
                        )?.toDoubleOrNull()?.let { it > Constants.PREDICTED_RANKING_POINT_QUALIFICATION } ?: false
                        // show icons if RPs were achieved
                        Box(modifier = Modifier.weight(1f)) {
                            if (rp1) Image(painterResource(id = R.drawable.ic_melody), contentDescription = null)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            if (rp2) Image(painterResource(id = R.drawable.ic_ensemble), contentDescription = null)
                        }
                    }
                }
                // blue
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // blue score
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                    ) {
                        val score = blueScore?.let { (if (hasActualData) "%.0f" else "%.1f").format(it) }
                        Text(
                            score ?: Constants.NULL_PREDICTED_SCORE_CHARACTER,
                            fontWeight = if (blueWon) FontWeight.Bold else null
                        )
                    }
                    // blue RPs
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                    ) {
                        val rp1 = getAllianceInMatchObjectByKey(
                            Constants.BLUE, match.matchNumber, if (hasActualData) "actual_rp1" else "predicted_rp1"
                        )?.toDoubleOrNull()?.let { it > Constants.PREDICTED_RANKING_POINT_QUALIFICATION } ?: false
                        val rp2 = getAllianceInMatchObjectByKey(
                            Constants.BLUE, match.matchNumber, if (hasActualData) "actual_rp2" else "predicted_rp2"
                        )?.toDoubleOrNull()?.let { it > Constants.PREDICTED_RANKING_POINT_QUALIFICATION } ?: false
                        // show icons if RPs were achieved
                        Box(modifier = Modifier.weight(1f)) {
                            if (rp1) Image(painterResource(id = R.drawable.ic_melody), contentDescription = null)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            if (rp2) Image(painterResource(id = R.drawable.ic_ensemble), contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}
