package live.einfachgustaf.minestom.base.server

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
     * Starts the Minestom server at the specified address.
     *
     * @param address The address to bind the server to.
     */
    fun start(address: InetSocketAddress) {
        logger.info("Starting Minestom server at ${address.hostString}:${address.port}")
        server.start(address)
    }
}