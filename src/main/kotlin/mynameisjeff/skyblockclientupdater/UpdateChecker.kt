package mynameisjeff.skyblockclientupdater

import com.google.gson.JsonParser
import gg.essential.api.EssentialAPI
import gg.essential.api.utils.Multithreading
import gg.essential.api.utils.WebUtil
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.serializer
import mynameisjeff.skyblockclientupdater.SkyClientUpdater.json
import mynameisjeff.skyblockclientupdater.SkyClientUpdater.mc
import mynameisjeff.skyblockclientupdater.config.Config
import mynameisjeff.skyblockclientupdater.data.LocalMod
import mynameisjeff.skyblockclientupdater.data.MCMod
import mynameisjeff.skyblockclientupdater.data.RepoMod
import mynameisjeff.skyblockclientupdater.data.UpdateMod
import mynameisjeff.skyblockclientupdater.gui.screens.ModUpdateScreen
import mynameisjeff.skyblockclientupdater.utils.TickTask
import mynameisjeff.skyblockclientupdater.utils.readTextAndClose
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.util.Util
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.client.FMLClientHandler
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.ModContainer
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.util.jar.JarFile

/**
 * Taken from Skytils under GNU Affero General Public License v3.0
 * Modified
 * https://github.com/Skytils/SkytilsMod/blob/1.x/LICENSE
 */
class UpdateChecker {
    companion object {
        var INSTANCE = UpdateChecker()
        private set

        private val logger = LogManager.getLogger("SkyClientUpdater (UpdateChecker)")

        val installedMods = arrayListOf<LocalMod>()

        val taskDir = File(File(mc.mcDataDir, "skyclientupdater"), "files")
        val ignoredJson = File(taskDir, "ignored.json")

        val needsDelete = hashSetOf<Pair<File, String>>()

        private var addedShutdownHook = false

        lateinit var deleteTask: File

        fun reset() {
            MinecraftForge.EVENT_BUS.unregister(INSTANCE)
            INSTANCE = UpdateChecker()
            MinecraftForge.EVENT_BUS.register(INSTANCE)
            INSTANCE.updateLatestCommitId()
            INSTANCE.getLatestMods()
            INSTANCE.getUpdateCandidates()
        }
    }

    val latestMods = hashSetOf<RepoMod>()
    val needsUpdate = hashSetOf<UpdateMod>()

    val ignored = arrayListOf<UpdateMod>()

    var latestCommitId = "main"
    private var ignoreUpdates = false

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onGuiOpened(event: GuiOpenEvent) {
        if (event.gui !is GuiMainMenu || ignoreUpdates || needsUpdate.isEmpty()) return
        TickTask(2) {
            EssentialAPI.getGuiUtil().openScreen(ModUpdateScreen(needsUpdate))
        }
    }

    fun ignoreUpdates() {
        ignoreUpdates = true
    }

    fun updateLatestCommitId() {
        latestCommitId = try {
            val commits = JsonParser().parse(
                WebUtil.fetchString(
                    "https://api.github.com/repos/${
                        System.getProperty(
                            "scu.repo",
                            "SkyblockClient/SkyblockClient-REPO"
                        )
                    }/commits"
                ) ?: throw NullPointerException()
            ).asJsonArray
            commits[0].asJsonObject["sha"].asString
        } catch (ex: Throwable) {
            logger.error("Failed to fetch latest commit ID.", ex)
            "main"
        }
    }

    fun deleteFileOnShutdown(oldFile: File, newFile: String) {
        if (!addedShutdownHook) {
            addedShutdownHook = true
            Runtime.getRuntime().addShutdownHook(Thread {
                try {
                    logger.info("Attempting to apply SkyClient updates.")
                    val os = Util.getOSType()
                    logger.info("SCU - DEBUG STEP GET OS COMPLETED")

                    logger.info("Copying updated jars to mods.")
                    val directory = File(File(mc.mcDataDir, "skyclientupdater"), "updates")
                    val modDir = File(mc.mcDataDir, "mods")
                    for (item in needsDelete) {
                        try {

                            val newJar = File(directory, item.second)
                            logger.info("Copying ${item.second} to mod folder")
                            val newLocation = File(modDir, item.second)
                            logger.info("SCU - step 1 finished")
                            newLocation.createNewFile()
                            logger.info("SCU - step 2 finished")
                            newJar.copyTo(newLocation, true)
                            logger.info("SCU - step 3 finished")
                        } catch (exc: Exception) {
                            logger.info("ERROR: ")
                            logger.info(exc.cause)
                            logger.info(exc.message)
                            logger.info(exc.stackTrace)
                        }
                    }
                    logger.info("SCU - DEBUG STEP 5")
                    logger.info("SCU - DEBUG STEP 6")
                    if ((os == Util.EnumOS.OSX || os == Util.EnumOS.LINUX) && needsDelete.removeAll { it.first.delete() } && needsDelete.isEmpty()) {
                        logger.info("Successfully deleted all files normally.")
                        return@Thread
                    }

                    logger.info("Running delete task")
                    if (deleteTask.path == "invalid") {
                        logger.info("Task doesn't exist")
                        Desktop.getDesktop().open(File(mc.mcDataDir, "mods"))
                        return@Thread
                    }
                    logger.info("SCU - DEBUG STEP 7")
                    val runtime = getJavaRuntime()
                    logger.info("Using runtime $runtime")
                    if (os == Util.EnumOS.OSX) {
                        val sipStatus = Runtime.getRuntime().exec("csrutil status")
                        sipStatus.waitFor()
                        if (!sipStatus.inputStream.readTextAndClose().contains("System Integrity Protection status: disabled.")) {
                            logger.info("SIP is NOT disabled, opening Finder.")
                            Desktop.getDesktop().open(File(mc.mcDataDir, "mods"))
                            return@Thread
                        }
                    }
                    val list = arrayListOf<String>(runtime, "-jar", deleteTask.absolutePath)
                    list.addAll(needsDelete.map { it.first.absolutePath })
                    logger.info(list.joinToString(" "))
                    Runtime.getRuntime().exec(list.toTypedArray())
                    logger.info("Successfully applied SkyClient mod update.")
                } catch (ex: Throwable) {
                    logger.error("Failed to apply SkyClient mod Update.", ex)
                }
            })
        }
        needsDelete.add(oldFile to newFile)
    }

    fun getValidModFiles() {
        val modDir = File(mc.mcDataDir, "mods")
        if (!modDir.isDirectory && !modDir.mkdirs()) logger.warn("Mods directory not found.").also { return }
        val modFiles = (modDir.listFiles() ?: return).toMutableList()

        val subModDir = File(modDir, Loader.MC_VERSION)
        if (subModDir.isDirectory) {
            val versionModFiles = subModDir.listFiles()
            if (versionModFiles != null) modFiles.addAll(versionModFiles)
        }

        val modList = ArrayList(Loader.instance().modList)
        FMLClientHandler.instance().addSpecialModEntries(modList)
        installedMods.addAll(modFiles.filter { it.isFile && it.extension == "jar" }.map {
            LocalMod(it, getModIds(modList, it))
        })
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun getModIds(modList: List<ModContainer>, file: File): MutableSet<String> {
        val list = hashSetOf<String>()
        runCatching {
            modList.filter { it.source == file }.mapTo(list) { it.modId }
            JarFile(file).use { jarFile ->
                val mcModInfo = json.decodeFromStream<List<MCMod>>(jarFile.getInputStream(jarFile.getJarEntry("mcmod.info") ?: return@runCatching null) ?: return@runCatching null)
                mcModInfo.mapTo(list) { it.modId }
            }
        }.onFailure { it.printStackTrace() }
        return list
    }

    fun getLatestMods() {
        try {
            if (Config.enableBeta) {
                latestMods.addAll(json.decodeFromString<List<RepoMod>>(WebUtil.fetchString("https://cdn.jsdelivr.net/gh/SkyblockClient/SkyblockClient-REPO@$latestCommitId/files/mods_beta.json") ?: run { Config.enableBeta = false; Config.markDirty(); Config.writeData(); throw UnsupportedOperationException("Beta mods not available, disabling...") }).filter { !it.ignored })
            }
            latestMods.addAll(json.decodeFromString<List<RepoMod>>(WebUtil.fetchString("https://cdn.jsdelivr.net/gh/SkyblockClient/SkyblockClient-REPO@$latestCommitId/files/mods.json") ?: throw NullPointerException()).filter { !it.ignored })
        } catch (ex: Throwable) {
            logger.error("Failed to load mod files.", ex)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun getUpdateCandidates() {
        val checkedMods = ArrayList<String>()

        // update to mod id loop
        for (localMod in installedMods) {
            for (repoMod in latestMods) {
                if(checkModId(localMod, repoMod) && repoMod.updateToIds.isNotEmpty()) {
                    for (updateToId in repoMod.updateToIds) {
                        // mark the mod as updated
                        checkedMods.add(localMod.file.name)
                        // get the update to mod
                        val updateToRepoMod = getRepoModFromID(latestMods, updateToId)
                        if (updateToRepoMod != null) {
                            needsUpdate.add(UpdateMod(localMod.file, updateToRepoMod.fileName, updateToRepoMod.updateURL, UpdateMod.Type.UPDATING))
                        }
                    }
                }
            }
        }

        val localModsList = installedMods.filter { latestMods.none { m -> m.fileName == it.file.name } }
        val repoModList = latestMods.filter { installedMods.none { m -> m.file.name == it.fileName } }


        // mod id checking loop
        loopMods@ for (localMod in localModsList.toTypedArray()) {
            if ((localMod.modIds.isNotEmpty() && checkedMods.contains(localMod.modIds.first().toString())) || localMod.matched)
                continue@loopMods
            val fileName = localMod.file.name
            for (repoMod in repoModList) {
                if (checkModId(localMod, repoMod) )
                {
                    checkedMods.add(localMod.file.name)
                    if (checkNeedsUpdate(repoMod.fileName, fileName)) {
                        if (repoMod.fileName == "OverflowAnimations-1.1.0.jar") { //todo i should generalize this lol
                            needsUpdate.add(UpdateMod(localMod.file, repoMod.fileName, repoMod.updateURL, UpdateMod.Type.UPDATING))
                            val sk1er = repoModList.find { it.modId == "sk1er_old_animations" }
                            if (sk1er != null) {
                                needsUpdate.add(UpdateMod(localMod.file, sk1er.fileName, sk1er.updateURL, UpdateMod.Type.UPDATING))
                            }
                            localMod.matched = true
                            continue@loopMods
                        }
                        if (needsUpdate.add(UpdateMod(localMod.file, repoMod.fileName, repoMod.updateURL, UpdateMod.Type.UPDATING))) {
                            localMod.matched = true
                        }
                        continue@loopMods
                    }
                }
            }
        }

        // file name checking loop
        loopMods@ for (localMod in localModsList) {
            if ((localMod.modIds.isNotEmpty() && checkedMods.contains(localMod.modIds.first().toString())) || localMod.matched)
                continue@loopMods
            val fileName = localMod.file.name
            for (repoMod in repoModList) {
                if (checkMatch(repoMod.fileName, fileName))
                {
                    checkedMods.add(localMod.file.name)
                    if (checkNeedsUpdate(repoMod.fileName, fileName))
                    {
                        if (needsUpdate.add(UpdateMod(localMod.file, repoMod.fileName, repoMod.updateURL, UpdateMod.Type.UPDATING))) {
                            localMod.matched = true
                        }
                        continue@loopMods
                    }
                }
            }
        }
        try {
            if (!ignoredJson.exists()) {
                ignoredJson.createNewFile()
                ignoredJson.writeText("[]")
            } else {
                ignoredJson.inputStream().use { inputStream ->
                    ignored.addAll(json.decodeFromStream<List<UpdateMod>>(inputStream))
                    ignored.forEach { removed ->
                        needsUpdate.removeIf { removed == it }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                ignoredJson.delete()
                ignoredJson.createNewFile()
                ignoredJson.writeText("[]")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun writeIgnoredJson() {
        ignored.removeIf { it.type != UpdateMod.Type.DISABLE }
        ignoredJson.writeText(json.encodeToString(ListSerializer(json.serializersModule.serializer()), ignored))
    }

    private fun getRepoModFromID(repoModList: HashSet<RepoMod>, modId: String): RepoMod? {
        loopMods@ for (repoMod in repoModList) {
            if(repoMod.modId == modId) {
                return  repoMod
            }
        }
        return null
    }

    private fun checkModId(localMod: LocalMod, repoMod: RepoMod): Boolean {
        if (repoMod.alwaysConsider) return true

        if (localMod.modIds.isEmpty() || repoMod.modId == null) return false

        return localMod.modIds.contains(repoMod.modId)
    }

    private fun checkMatch(expected: String, received: String): Boolean {
        val exempt = charArrayOf('_', '-', '+', ' ', '.')

        val e = expected.lowercase().toCharArray().dropWhile { it == '!' }.filter { !exempt.contains(it) }
        val r = received.lowercase().toCharArray().dropWhile { it == '!' }.filter { !exempt.contains(it) }
        if (e.joinToString().take(4) != r.joinToString().take(4)) return false
        val distance = StringUtils.getLevenshteinDistance(e.joinToString(""), r.joinToString(""))
        if (distance !in 1..7) return false
        return true
    }

    private fun checkNeedsUpdate(expected: String, received: String): Boolean {
        val exempt = charArrayOf('_', '-', '+', ' ', '.')
        val whitespace = charArrayOf('_', ' ', '.', '+')

        val e = expected.lowercase().toCharArray().dropWhile { it == '!' }.filter { !exempt.contains(it) }
        val r = received.lowercase().toCharArray().dropWhile { it == '!' }.filter { !exempt.contains(it) }

        val ec = e.filterIndexed { index, c -> c != r.getOrNull(index) }
        val rc = r.filterIndexed { index, c -> c != e.getOrNull(index) }

        if (listOf(ec, rc).flatten().all { it.isDigit() || whitespace.contains(it) }) {
            val ed = ec.dropWhile { !it.isDigit() }.takeWhile { it.isDigit() }.joinToString("").toIntOrNull() ?: 0
            val rd = rc.dropWhile { !it.isDigit() }.takeWhile { it.isDigit() }.joinToString("").toIntOrNull() ?: 0
            return ed > rd
        }
        return true
    }

    fun downloadHelperTask() {
        logger.info("Checking for SkyClientUpdater delete task...")
        val url =
            "https://github.com/Polyfrost/Deleter/releases/download/v1.8/Deleter-1.8.jar"
        taskDir.mkdirs()
        val taskFile = File(taskDir, url.substringAfterLast("/"))
        if (!taskFile.exists()) {
            Multithreading.runAsync {
                logger.info("Downloading SkyClientUpdater delete task.")
                deleteTask = try {
                    WebUtil.downloadToFile(url, taskFile, "SkyblockClient-Updater/${SkyClientUpdater.VERSION}")
                    logger.info("SkyClientUpdater delete task successfully downloaded!")
                    taskFile
                } catch (e: Exception) {
                    e.printStackTrace()
                    logger.info("Downloading SkyClientUpdater delete task failed!")
                    File("invalid")
                }
            }
        } else {
            deleteTask = taskFile
            logger.info("SkyClientUpdater delete task found")
        }
    }

    /**
     * @link https://stackoverflow.com/a/47925649
     */
    @Throws(IOException::class)
    fun getJavaRuntime(): String {
        val os = System.getProperty("os.name")
        val java = "${System.getProperty("java.home")}${File.separator}bin${File.separator}${
            if (os != null && os.lowercase().startsWith("windows")) "java.exe" else "java"
        }"
        if (!File(java).isFile) throw IOException("Unable to find suitable java runtime at $java")
        return java
    }
}
