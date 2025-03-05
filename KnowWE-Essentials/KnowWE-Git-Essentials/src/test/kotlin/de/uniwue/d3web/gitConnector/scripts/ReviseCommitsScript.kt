package de.uniwue.d3web.gitConnector.scripts

import de.uniwue.d3web.gitConnector.impl.mixed.JGitBackedGitConnector


fun main() {
    val path = "/Users/mkrug/Konap/Wikis/Wiki_KM"

    val gitConnector = JGitBackedGitConnector.fromPath(path)

    val commitsMaster = gitConnector.listCommitsForBranch("master")
    val commitsMaintenance = gitConnector.listCommitsForBranch("maintenance")

    val mapMaintenance =commitsMaintenance.groupBy { gitConnector.userDataFor(it).message }

}