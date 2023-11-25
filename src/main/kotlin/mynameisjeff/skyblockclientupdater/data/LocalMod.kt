package mynameisjeff.skyblockclientupdater.data

import java.io.File

data class LocalMod(
    val file: File,
    var modIds: MutableSet<String>,
    var matched: Boolean = false
)
