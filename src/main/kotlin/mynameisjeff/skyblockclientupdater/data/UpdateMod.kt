package mynameisjeff.skyblockclientupdater.data

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class UpdateMod(
    @Contextual val file: File, val name: String, @SerialName("update_to_ids") val updateURL: String, var type: Type
) {
    enum class Type {
        UPDATING, TEMP_DISABLE, DISABLE
    }

    override fun equals(other: Any?): Boolean {
        return other is UpdateMod && other.file == file && other.name == name && other.updateURL == updateURL
    }

    override fun hashCode(): Int {
        var result = file.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + updateURL.hashCode()
        return result
    }
}