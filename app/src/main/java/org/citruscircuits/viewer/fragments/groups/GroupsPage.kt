package org.citruscircuits.viewer.fragments.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.citruscircuits.viewer.Groups
import org.citruscircuits.viewer.MainViewerActivity.Companion.teamList

/**
 * Page for displaying and editing groups of teams.
 */
@Composable
fun GroupsPage() {
    // get list of groups
    val groups by Groups.groups.collectAsStateWithLifecycle()
    var dialogOpen by rememberSaveable { mutableStateOf(false)}

    // background
    Surface {
        // scrollable
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // title
            item {
                Box(
                    // centered
                    contentAlignment = Alignment.Center, modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text("Groups", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(16.dp))
                }
            }
            // item per group
            items(Groups.groupCount) { groupIndex ->
                HorizontalDivider()
                Column(modifier = Modifier.fillMaxWidth()) {
                    // group name
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            // highlight with the group's color
                            .background(color = Groups.colors[groupIndex])
                            .padding(8.dp)
                    ) {
                        // add 1 to index to get group number
                        Text("Group ${groupIndex + 1}", style = MaterialTheme.typography.titleLarge)
                    }
                    // check if there are teams
                    if (groups[groupIndex].isNotEmpty()) {
                        // loop over teams
                        groups[groupIndex].forEachIndexed { teamIndex, team ->
                            HorizontalDivider()
                            // show team
                            ListItem(
                                headlineContent = { Text(team) },
                                trailingContent = {
                                    // delete button
                                    IconButton(
                                        onClick = {
                                            Groups.groups.value = groups.toMutableList().mapIndexed { index, group ->
                                                if (index == groupIndex) {
                                                    group.toMutableList().apply { removeAt(teamIndex) }
                                                } else group
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null)
                                    }
                                }
                            )
                        }
                    } else {
                        HorizontalDivider()
                        // placeholder text
                        ListItem(headlineContent = { Text("No teams in this group yet.") })
                    }
                    HorizontalDivider()
                    // whether to show the team number entry UI
                    var addingTeam by rememberSaveable { mutableStateOf(false) }
                    if (addingTeam) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(8.dp)) {
                            // current team number input
                            var input by rememberSaveable { mutableStateOf("") }

                            // Adds a team to a group if the team number is in the team list
                            fun addTeam() {
                                if (teamList.contains(input)) {
                                    Groups.groups.value = groups.toMutableList()
                                        .mapIndexed { index, group -> if (index == groupIndex) group + input else group }
                                } else {
                                    dialogOpen = true
                                }
                            }

                            // text field + suggestions
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // text field
                                OutlinedTextField(
                                    value = input,
                                    onValueChange = { if (it.isDigitsOnly()) input = it },
                                    label = { Text("Enter team number") },
                                    leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                                    trailingIcon = {
                                        // clear button
                                        if (input.isNotEmpty()) IconButton(onClick = { input = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = null)
                                        }
                                    },
                                    // use number keyboard and show done button
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                                    ),
                                    // make done button add the team
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            addTeam()
                                            addingTeam = false
                                        }
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                // only show suggestions if something is typed
                                if (input.isNotEmpty()) {
                                    // get suggestions from team list
                                    val suggestions =
                                        teamList.filter { input in it }.filterNot { input == it }
                                            .sortedBy { it.toIntOrNull() }.take(3)
                                    // if there's suggestions, show them in a card
                                    if (suggestions.isNotEmpty()) {
                                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                                            suggestions.forEach { team ->
                                                ListItem(
                                                    headlineContent = { Text(team) },
                                                    modifier = Modifier.clickable { input = team }
                                                )
                                                HorizontalDivider()
                                            }
                                        }
                                    } else {
                                        // placeholder
                                        Text("No suggestions")
                                    }
                                }
                            }
                            // cancel/add buttons
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // cancel button
                                OutlinedButton(onClick = { addingTeam = false }) {
                                    Icon(Icons.Default.Clear, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Cancel")
                                }
                                // add button
                                Button(
                                    onClick = {
                                        addTeam()
                                        addingTeam = false
                                    }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add")
                                }
                            }
                        }
                    } else {
                        // button to open team number entry UI
                        ListItem(
                            headlineContent = { Text("Add team") },
                            leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                            modifier = Modifier.clickable { addingTeam = true }
                        )
                    }
                }
            }
        }
        when {
            dialogOpen ->
                CreateAlertDialog { dialogOpen = false }
        }
    }
}

/**
 * Creates an alert dialog that tells the user they've input an invalid team number
 * @param onDismissRequest Function that sets dialogOpen to false when the user closes the dialog
 */
@Composable
fun CreateAlertDialog(onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = "Invalid Team Number",
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}