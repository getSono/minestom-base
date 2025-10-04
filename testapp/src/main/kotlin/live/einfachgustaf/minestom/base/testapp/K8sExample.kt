package live.einfachgustaf.minestom.base.testapp

import kotlinx.coroutines.runBlocking
import live.einfachgustaf.minestom.base.MinestomBase
import live.einfachgustaf.minestom.base.k8s.MatchStatus
import live.einfachgustaf.minestom.base.testapp.commands.GiveItemStackCommand
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.extras.MojangAuth
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.block.Block
import java.net.InetSocketAddress

/**
 * Example entrypoint demonstrating Kubernetes integration with Agones and Open Match.
 * 
 * This example shows how to:
 * - Enable Agones integration for game server management
 * - Enable Open Match integration for matchmaking
 * - Handle match assignments
 * - Set labels and annotations on the GameServer
 * - Report match status
 * 
 * To use this with Agones, ensure the AGONES_SDK_GRPC_PORT environment variable is set.
 */
fun main() {
    val base = MinestomBase.createServer()

    // Enable Mojang authentication (optional)
    // MojangAuth.init()

    MinecraftServer.getCommandManager().register(GiveItemStackCommand())

    // Create test instance
    val instanceContainer = MinecraftServer.getInstanceManager().createInstanceContainer().apply {
        setGenerator { instance ->
            instance.modifier().fillHeight(0, 40, Block.GRASS_BLOCK)
        }
    }

    instanceContainer.setChunkSupplier(::LightingChunk)

    val globalEventHandler = MinecraftServer.getGlobalEventHandler()

    // Track active players for match status reporting
    var activePlayers = 0

    globalEventHandler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
        val player = event.player
        event.spawningInstance = instanceContainer
        player.respawnPoint = Pos(0.0, 42.0, 0.0)
        activePlayers++
    }

    globalEventHandler.addListener(PlayerDisconnectEvent::class.java) { event ->
        activePlayers--
        
        // Report match completion when all players leave
        if (activePlayers == 0) {
            runBlocking {
                base.openMatchAdapter.reportMatchStatus(
                    matchId = "current-match",
                    status = MatchStatus.COMPLETED,
                    details = "All players disconnected"
                )
            }
        }
    }

    // Set up Open Match assignment handler
    base.openMatchAdapter.onAssignment { assignment ->
        println("=== Match Assignment Received ===")
        println("Match ID: ${assignment.matchId}")
        println("Players: ${assignment.players.size}")
        println("Teams: ${assignment.teams}")
        println("Session Token: ${assignment.sessionToken}")
        println("Metadata: ${assignment.metadata}")
        println("================================")

        // Example: Set custom Agones labels based on match data
        runBlocking {
            assignment.metadata["game-mode"]?.let { gameMode ->
                base.agonesAdapter.setLabel("game-mode", gameMode)
            }
            
            // Report match as in progress
            base.openMatchAdapter.reportMatchStatus(
                matchId = assignment.matchId,
                status = MatchStatus.IN_PROGRESS,
                details = "Match started with ${assignment.players.size} players"
            )
        }
    }

    // Start the server with K8s integration
    base.start(
        address = InetSocketAddress("0.0.0.0", 25565),
        enableAgones = true,          // Enable Agones (auto-detects AGONES_SDK_GRPC_PORT)
        startHealthChecks = true,      // Start automatic health checks
        enableOpenMatch = true,        // Enable Open Match HTTP endpoint
        openMatchPort = 8080           // Port for /assign endpoint
    )

    println("Server started with Kubernetes integration!")
    println("- Agones: ${if (System.getenv("AGONES_SDK_GRPC_PORT") != null) "enabled" else "disabled (no AGONES_SDK_GRPC_PORT)"}")
    println("- Open Match: enabled on port 8080")
    println("- Minecraft server: 0.0.0.0:25565")
    println()
    println("Test the /assign endpoint with:")
    println("""  curl -X POST http://localhost:8080/assign -H "Content-Type: application/json" -d '{"matchId":"test-123","players":[{"playerId":"player1"}]}'""")

    // Add shutdown hook to signal graceful shutdown to Agones
    Runtime.getRuntime().addShutdownHook(Thread {
        println("Shutting down gracefully...")
        base.signalShutdown()
    })
}
