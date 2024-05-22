package org.citruscircuits.viewer.fragments.preferences

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.citruscircuits.viewer.MainViewerActivity
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.constants.Constants
import java.util.Locale

/**
 * Page for editing app preferences.
 *
 * @param onOpenUserPreferences Callback to navigate to the user preferences page.
 * @param onRestart Callback to restart the app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesPage(onOpenUserPreferences: () -> Unit, onRestart: () -> Unit) {
    // background
    Surface {
        // stack vertically
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                // scrollable
                .verticalScroll(rememberScrollState())
        ) {
            // header
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // title
                Text("Preferences", style = MaterialTheme.typography.headlineLarge)
                // subtitle with version number
                Text(
                    stringResource(id = R.string.tv_version_num, Constants.VERSION_NUM),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            // user selection + edit button
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                // whether the dropdown is expanded
                var expanded by rememberSaveable { mutableStateOf(false) }
                // username list
                val users = stringArrayResource(R.array.user_array)
                // currently selected name
                var name by rememberSaveable {
                    mutableStateOf(
                        (MainViewerActivity.UserDataPoints.contents?.get("selected")?.asString ?: users.first())
                            .lowercase(Locale.getDefault()).replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                            }
                    )
                }
                // update selected user on change
                LaunchedEffect(name) {
                    with(MainViewerActivity.UserDataPoints) {
                        contents?.remove("selected")
                        contents?.addProperty("selected", name.uppercase(Locale.getDefault()))
                        write()
                    }
                }
                // dropdown box
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    // text field showing current username
                    OutlinedTextField(
                        value = name,
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        label = { Text("Username") },
                        modifier = Modifier.menuAnchor()
                    )
                    // dropdown
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        users.forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    name = it
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                // edit button
                OutlinedButton(onClick = onOpenUserPreferences) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit")
                }
            }
            // state to force recomposition if the match is starred
            // TODO remove when starred matches is changed to be observable
            var starredMatchesUpdater by remember { mutableStateOf(false) }
            // get starred matches
            val starredMatches = remember(starredMatchesUpdater) { MainViewerActivity.starredMatches }
            // button to star/unstar our matches
            OutlinedButton(
                onClick = {
                    if (starredMatches.containsAll(MainViewerActivity.StarredMatches.citrusMatches)) {
                        MainViewerActivity.starredMatches -= MainViewerActivity.StarredMatches.citrusMatches.toSet()
                    } else {
                        MainViewerActivity.starredMatches += MainViewerActivity.StarredMatches.citrusMatches
                    }
                    MainViewerActivity.StarredMatches.input()
                    starredMatchesUpdater = !starredMatchesUpdater
                }
            ) {
                Icon(Icons.Default.Stars, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (starredMatches.containsAll(MainViewerActivity.StarredMatches.citrusMatches)) {
                        "Unstar our matches"
                    } else "Star our matches"
                )
            }
            // event key input
            var eventKey by rememberSaveable { mutableStateOf(Constants.EVENT_KEY) }
            OutlinedTextField(
                value = eventKey,
                onValueChange = { eventKey = it },
                label = { Text("Edit event key") },
                placeholder = { Text(Constants.DEFAULT_KEY) },
                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                singleLine = true
            )
            // schedule key input
            var scheduleKey by rememberSaveable { mutableStateOf(Constants.SCHEDULE_KEY) }
            OutlinedTextField(
                value = scheduleKey,
                onValueChange = { scheduleKey = it },
                label = { Text("Edit schedule key") },
                placeholder = { Text(Constants.DEFAULT_SCHEDULE) },
                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                singleLine = true
            )
            // submit keys button
            OutlinedButton(
                onClick = {
                    with(MainViewerActivity.UserDataPoints) {
                        contents?.remove("key")
                        contents?.addProperty("key", eventKey.ifEmpty { Constants.DEFAULT_KEY })
                        contents?.remove("schedule")
                        contents?.addProperty("schedule", scheduleKey.ifEmpty { Constants.DEFAULT_SCHEDULE })
                        write()
                    }
                    onRestart()
                }
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Submit keys")
            }
        }
    }
}
