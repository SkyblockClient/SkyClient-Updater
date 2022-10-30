package mynameisjeff.skyblockclientupdater.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RepoMod(
    @SerialName("id")
    val internalId: String,
    @SerialName("old_id")
    val oldId: String? = null,
    @SerialName("file")
    val fileName: String,
    @SerialName("forge_id")
    val modId: String? = null,
    @SerialName("url")
    val updateURL: String = "https://github.com/nacrt/SkyblockClient-REPO/raw/main/files/mods/$fileName",
    @SerialName("update_to_ids")
    val updateToIds: Array<String> = arrayOf(),
    val ignored: Boolean = false,
    val hasBrokenMCModInfo: Boolean = false,
    val alwaysConsider: Boolean = false,
) {
    override fun equals(other: Any?): Boolean {
        return other is RepoMod && (other.internalId == internalId || other.oldId == internalId || other.internalId == oldId || other.oldId == oldId)
    }

    override fun hashCode(): Int {
        return internalId.hashCode()
    }
}