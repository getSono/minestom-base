package live.einfachgustaf.minestom.base.server

import kotlinx.coroutines.runBlocking
import live.einfachgustaf.minestom.base.k8s.AgonesAdapter
import live.einfachgustaf.minestom.base.k8s.OpenMatchAdapter
import net.minestom.server.MinecraftServer
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

class MinestomServer {
    private val logger = LoggerFactory.getLogger(MinestomServer::class.java)
    private var isAgonesEnabled = false
    private var isOpenMatchEnabled = false

    /**
     * Initializes the Minestom server.
     *
     * This method is called automatically when the server instance is created.
     */
    val server: MinecraftServer = MinecraftServer.init()

    /**
     * Adapter for integrating with Agones for game server management.
     * This can be used to manage server lifecycle events such as allocation, shutdown, and health checks.
     */
    val agonesAdapter = AgonesAdapter(server)

    /**
     * Adapter for integrating with Open Match for matchmaking.
     * This handles receiving match assignments and reporting status.
     */
    val openMatchAdapter: OpenMatchAdapter by lazy { 
        OpenMatchAdapter(agonesAdapter = if (isAgonesEnabled) agonesAdapter else null) 
    }

    /**
     * Starts the Minestom server at the specified address.
     * If the AGONES_SDK_GRPC_PORT environment variable is set, Agones integration will be enabled.
     *
     * @param address The address to bind the server to.
     * @param enableAgones Whether to enable Agones integration (default: auto-detect from environment)
     * @param startHealthChecks Whether to start health checks automatically (default: true)
     * @param enableOpenMatch Whether to enable Open Match integration (default: false)
     * @param openMatchPort The port for the Open Match HTTP server (default: 8080)
     */
    fun start(
        address: InetSocketAddress,
        enableAgones: Boolean = System.getenv("AGONES_SDK_GRPC_PORT") != null,
        startHealthChecks: Boolean = true,
        enableOpenMatch: Boolean = false,
        openMatchPort: Int = 8080
    ) {
        logger.info("Starting Minestom server at ${address.hostString}:${address.port}")
        
        if (enableAgones) {
            logger.info("Agones integration enabled")
            isAgonesEnabled = true
            agonesAdapter.initialize()
            
            // Start health checks if requested
            if (startHealthChecks) {
                agonesAdapter.startHealthChecks()
            }
        }

        if (enableOpenMatch) {
            logger.info("Open Match integration enabled on port $openMatchPort")
            isOpenMatchEnabled = true
            // Create adapter with proper Agones reference
            val adapter = OpenMatchAdapter(port = openMatchPort, agonesAdapter = if (isAgonesEnabled) agonesAdapter else null)
            adapter.start()
        }
        
        server.start(address)
        
        // Mark server as ready in Agones after server has started
        if (isAgonesEnabled) {
            runBlocking {
                agonesAdapter.ready()
            }
        }
    }

    /**
     * Signals shutdown to Agones.
     * Should be called before the application exits to ensure graceful shutdown.
     */
    fun signalShutdown() {
        logger.info("Signaling shutdown to Agones")
        
        if (isOpenMatchEnabled) {
            openMatchAdapter.stop()
        }
        
        if (isAgonesEnabled) {
            runBlocking {
                agonesAdapter.shutdown()
            }
        }
    }
}