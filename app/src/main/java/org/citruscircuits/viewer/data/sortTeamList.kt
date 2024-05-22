package org.citruscircuits.viewer.data

/**
 * Used to correctly sort team lists that include team numbers with letters in them
 */
fun sortTeamList(list: List<String>): List<String> {
    val regex = "(?<=\\d)(?=\\D)".toRegex()
    val tempList = mutableListOf<List<String>>()
    for (teamNumber in list) {
        // splits the team number between digits and letters into a list, then adds it to tempList,
        // adds a blank string to the list in case the team number does not have letters
        tempList.add(teamNumber.split(regex) + "")
    }
    // sorts tempList alphabetically by the 2nd element of each list,
    // then sorts it numerically by the 1st element of each list
    tempList.sortBy { it[1] }
    tempList.sortBy { it[0].toInt() }
    val newTeamList = mutableListOf<String>()
    for (team in tempList) {
        // recombines team into a team number, then adds it to the newTeamList
        newTeamList.add(team[0] + team[1])
    }
    return newTeamList
}
