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
    val updateURL: String = "https://github.com/SkyblockClient/SkyblockClient-REPO/raw/main/files/mods/$fileName",
    @SerialName("update_to_ids")
    val updateToIds: Array<String> = arrayOf(),
    @SerialName("name_detection")
    val nameDetection: Boolean = true,
    @SerialName("update_ids_detection")
    val updateIdsDetection: Boolean = true,
    val ignored: Boolean = false,
    @SerialName("ignore_on_scu_137_above")
    val ignoredNew: Boolean = false,
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