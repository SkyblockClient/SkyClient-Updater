package mynameisjeff.skyblockclientupdater.data

import kotlinx.serialization.Serializable

@Serializable
data class GitHubCommit(val sha: String)