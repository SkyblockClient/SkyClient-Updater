package mynameisjeff.skyblockclientupdater

import cc.polyfrost.oneconfig.utils.commands.CommandManager
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.serializersModuleOf
import mynameisjeff.skyblockclientupdater.command.Command
import mynameisjeff.skyblockclientupdater.config.Config
import mynameisjeff.skyblockclientupdater.data.FileSerializer
import mynameisjeff.skyblockclientupdater.utils.UnionX509TrustManager
import net.minecraft.client.Minecraft
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import java.awt.Color
import java.security.KeyStore
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import kotlin.coroutines.CoroutineContext


/**
 * ktor code taken from Skytils under GNU Affero General Public License v3.0
 * Modified
 * https://github.com/Skytils/SkytilsMod/blob/1.x/LICENSE
 */
@Mod(
    name = "@NAME@",
    version = SkyClientUpdater.VERSION,
    modid = "@ID@",
    clientSideOnly = true,
    modLanguageAdapter = "cc.polyfrost.oneconfig.utils.KotlinLanguageAdapter"
) object SkyClientUpdater : CoroutineScope {
    const val VERSION = "@VER"

    val accentColor = Color(67, 184, 0)

    val mc: Minecraft
    get() = Minecraft.getMinecraft()

    val json = Json {
        serializersModule = serializersModuleOf(FileSerializer())
        ignoreUnknownKeys = true
    }

    @JvmField
    val threadPool = Executors.newFixedThreadPool(10) as ThreadPoolExecutor

    @JvmField
    val dispatcher = threadPool.asCoroutineDispatcher()

    val IO = object : CoroutineScope {
        override val coroutineContext = Dispatchers.IO + SupervisorJob() + CoroutineName("SkyClientUpdater IO")
    }

    override val coroutineContext: CoroutineContext = dispatcher + SupervisorJob() + CoroutineName("SkyClientUpdater")

    val client = HttpClient(CIO) {
        install(ContentEncoding) {
            deflate(1.0F)
            gzip(0.9F)
            identity(0.1F)
        }
        install(ContentNegotiation) {
            json(json)
            json(json, ContentType.Text.Plain)
        }
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)
            exponentialDelay()
        }
        install(HttpTimeout)
        install(UserAgent) {
            agent = "SkyblockClient-Updater/$VERSION"
        }

        engine {
            endpoint {
                connectTimeout = 10000
                keepAliveTime = 5000
                requestTimeout = 10000
                socketTimeout = 10000
            }
            https {
                val backingManager = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
                    init(null as KeyStore?)
                }.trustManagers.first { it is X509TrustManager } as X509TrustManager

                val ourManager = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
                    UpdateChecker::class.java.getResourceAsStream("/certs.jks").use {
                        val ourKs = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
                            load(it, "skytilsontop".toCharArray())
                        }
                        init(ourKs)
                    }
                }.trustManagers.first { it is X509TrustManager } as X509TrustManager

                trustManager = UnionX509TrustManager(backingManager, ourManager)
            }
        }
    }

    @Mod.EventHandler
    fun on(event: FMLPreInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(EventListener())
        MinecraftForge.EVENT_BUS.register(UpdateChecker.INSTANCE)
        CommandManager.register(Command)
        Config.preload()

        UpdateChecker.INSTANCE.downloadHelperTask()
        UpdateChecker.INSTANCE.getValidModFiles()
        runBlocking {
            UpdateChecker.INSTANCE.updateLatestCommitId()
            UpdateChecker.INSTANCE.getLatestMods()
        }
        UpdateChecker.INSTANCE.getUpdateCandidates()
    }

}
