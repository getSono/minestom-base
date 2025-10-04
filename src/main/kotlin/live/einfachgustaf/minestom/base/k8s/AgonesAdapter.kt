package live.einfachgustaf.minestom.base.k8s

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import net.minestom.server.MinecraftServer
import net.scrayos.agones.client.GrpcAgonesSdk
import org.slf4j.LoggerFactory

class AgonesAdapter(val minecraftServer: MinecraftServer) {

    private val logger = LoggerFactory.getLogger(AgonesAdapter::class.java)
    val agonesSdk = GrpcAgonesSdk()
    private val healthCheckScope = CoroutineScope(Dispatchers.Default)
    private var isHealthy = true

    fun initialize() {
        AgonesListener(agonesSdk, minecraftServer)
    }

    /**
     * Signals to Agones that the game server is ready to accept connections.
     * This should be called after the Minestom server has fully initialized.
     */
    suspend fun ready() {
        logger.info("Marking game server as ready in Agones")
        agonesSdk.ready()
    }

    /**
     * Signals to Agones that the game server is shutting down.
     * This should be called before the server stops to ensure graceful shutdown.
     */
    suspend fun shutdown() {
        logger.info("Signaling graceful shutdown to Agones")
        agonesSdk.shutdown()
    }

    /**
     * Starts periodic health checks to report the server's health status to Agones.
     * The health check runs every 5 seconds by default.
     *
     * @param intervalSeconds The interval between health checks in seconds (default: 5)
     */
    fun startHealthChecks(intervalSeconds: Long = 5) {
        logger.info("Starting Agones health checks with interval of $intervalSeconds seconds")
        healthCheckScope.launch {
            val healthFlow = flow {
                while (isActive && isHealthy) {
                    emit(Unit)
                    delay(intervalSeconds * 1000)
                }
            }
            try {
                agonesSdk.health(healthFlow)
            } catch (e: Exception) {
                logger.error("Failed to send health checks to Agones", e)
            }
        }
    }

    /**
     * Sets a label on the GameServer in Agones.
     * Labels can be used for observability and filtering.
     *
     * @param key The label key
     * @param value The label value
     */
    suspend fun setLabel(key: String, value: String) {
        logger.debug("Setting Agones label: $key=$value")
        agonesSdk.label(key, value)
    }

    /**
     * Sets an annotation on the GameServer in Agones.
     * Annotations can store metadata that is not used for filtering.
     *
     * @param key The annotation key
     * @param value The annotation value
     */
    suspend fun setAnnotation(key: String, value: String) {
        logger.debug("Setting Agones annotation: $key=$value")
        agonesSdk.annotation(key, value)
    }

    /**
     * Sets the player capacity for the GameServer.
     *
     * @param capacity The maximum number of players
     */
    suspend fun setPlayerCapacity(capacity: Long) {
        agonesSdk.alpha().playerCapacity(capacity)
    }

    /**
     * Updates the health status of the server.
     * If set to false, health checks will not be sent to Agones.
     *
     * @param healthy Whether the server is healthy
     */
    fun setHealthStatus(healthy: Boolean) {
        isHealthy = healthy
        logger.info("Server health status set to: $healthy")
    }
}