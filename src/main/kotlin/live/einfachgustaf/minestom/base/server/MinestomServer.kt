package live.einfachgustaf.minestom.base.server

import live.einfachgustaf.minestom.base.k8s.AgonesAdapter
import net.minestom.server.MinecraftServer
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

class MinestomServer {
    private val logger = LoggerFactory.getLogger(MinestomServer::class.java)

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
     * Starts the Minestom server at the specified address.
     *
     * @param address The address to bind the server to.
     */
    fun start(address: InetSocketAddress) {
        logger.info("Starting Minestom server at ${address.hostString}:${address.port}")
        if (System.getenv("AGONES_SDK_GRPC_PORT") != null) {
            agonesAdapter.initialize()
        }
        server.start(address)
    }
}