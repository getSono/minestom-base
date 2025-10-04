package live.einfachgustaf.minestom.base.k8s

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

/**
 * Adapter for integrating with Open Match for matchmaking.
 * Handles receiving match assignments and reporting status.
 */
class OpenMatchAdapter(
    private val port: Int = 8080,
    private val agonesAdapter: AgonesAdapter? = null
) {
    private val logger = LoggerFactory.getLogger(OpenMatchAdapter::class.java)
    private val scope = CoroutineScope(Dispatchers.Default)
    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? = null
    private var assignmentHandler: ((MatchAssignment) -> Unit)? = null

    /**
     * Starts the HTTP server to receive match assignments from Open Match.
     */
    fun start() {
        logger.info("Starting Open Match HTTP server on port $port")
        
        server = embeddedServer(Netty, port = port) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                })
            }

            routing {
                // Health check endpoint
                get("/healthz") {
                    call.respondText("OK")
                }

                // Match assignment endpoint
                post("/assign") {
                    try {
                        val assignment = call.receive<MatchAssignment>()
                        logger.info("Received match assignment: $assignment")
                        
                        // Validate the assignment
                        if (assignment.matchId.isBlank()) {
                            logger.warn("Received invalid assignment with empty matchId")
                            call.respond(io.ktor.http.HttpStatusCode.BadRequest, 
                                mapOf("error" to "Invalid assignment: matchId is required"))
                            return@post
                        }
                        
                        // Process the assignment
                        handleAssignment(assignment)
                        
                        call.respond(io.ktor.http.HttpStatusCode.OK, 
                            mapOf("status" to "accepted", "matchId" to assignment.matchId))
                    } catch (e: Exception) {
                        logger.error("Error processing match assignment", e)
                        call.respond(io.ktor.http.HttpStatusCode.InternalServerError, 
                            mapOf("error" to "Failed to process assignment: ${e.message}"))
                    }
                }
            }
        }.start(wait = false)
        
        logger.info("Open Match HTTP server started successfully")
    }

    /**
     * Stops the HTTP server.
     */
    fun stop() {
        logger.info("Stopping Open Match HTTP server")
        server?.stop(1000, 2000)
        server = null
    }

    /**
     * Sets the handler for processing match assignments.
     *
     * @param handler A function that receives and processes match assignments
     */
    fun onAssignment(handler: (MatchAssignment) -> Unit) {
        this.assignmentHandler = handler
    }

    /**
     * Handles a match assignment by:
     * 1. Setting Agones labels/annotations with match metadata
     * 2. Invoking the custom assignment handler if set
     */
    private fun handleAssignment(assignment: MatchAssignment) {
        scope.launch {
            // Set Agones labels for observability
            agonesAdapter?.let { agones ->
                try {
                    agones.setLabel("match-id", assignment.matchId)
                    agones.setLabel("player-count", assignment.players.size.toString())
                    
                    // Set annotations for detailed metadata
                    assignment.metadata.forEach { (key, value) ->
                        agones.setAnnotation("match-$key", value)
                    }
                    
                    logger.info("Updated Agones metadata for match ${assignment.matchId}")
                } catch (e: Exception) {
                    logger.error("Failed to update Agones metadata", e)
                }
            }
            
            // Invoke custom handler
            assignmentHandler?.invoke(assignment)
        }
    }

    /**
     * Reports match status back to an external orchestration layer.
     * This is a placeholder for integration with your specific orchestration system.
     *
     * @param matchId The match ID
     * @param status The status to report
     * @param details Additional details about the status
     */
    suspend fun reportMatchStatus(matchId: String, status: MatchStatus, details: String = "") {
        logger.info("Match $matchId status: $status - $details")
        
        // Update Agones annotations with status
        agonesAdapter?.let { agones ->
            try {
                agones.setAnnotation("match-status", status.name)
                agones.setAnnotation("match-status-details", details)
            } catch (e: Exception) {
                logger.error("Failed to report match status to Agones", e)
            }
        }
        
        // TODO: Implement actual reporting to Open Match or orchestration layer
        // This would typically be an HTTP POST to your backend API
    }
}

/**
 * Represents a match assignment from Open Match.
 */
@Serializable
data class MatchAssignment(
    val matchId: String,
    val players: List<PlayerInfo> = emptyList(),
    val teams: Map<String, List<String>> = emptyMap(),
    val sessionToken: String = "",
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Represents player information in a match assignment.
 */
@Serializable
data class PlayerInfo(
    val playerId: String,
    val attributes: Map<String, String> = emptyMap()
)

/**
 * Represents the status of a match.
 */
enum class MatchStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    TIMEOUT
}
