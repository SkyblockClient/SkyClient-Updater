package mynameisjeff.skyblockclientupdater.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RepoMod(
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
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RepoMod

        if (fileName != other.fileName) return false
        if (modId != other.modId) return false
        if (updateURL != other.updateURL) return false
        if (!updateToIds.contentEquals(other.updateToIds)) return false
        if (ignored != other.ignored) return false
        if (hasBrokenMCModInfo != other.hasBrokenMCModInfo) return false
        if (alwaysConsider != other.alwaysConsider) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + (modId?.hashCode() ?: 0)
        result = 31 * result + updateURL.hashCode()
        result = 31 * result + updateToIds.contentHashCode()
        result = 31 * result + ignored.hashCode()
        result = 31 * result + hasBrokenMCModInfo.hashCode()
        result = 31 * result + alwaysConsider.hashCode()
        return result
    }
}
