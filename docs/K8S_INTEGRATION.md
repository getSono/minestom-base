# Kubernetes Integration

This document describes how to use the Kubernetes (Agones + Open Match) integration features in Minestom Base.

## Features

### Agones Integration

Agones is a Kubernetes controller for managing game servers. The integration provides:

- **Lifecycle Management**: Signal when the server is ready, shutdown gracefully
- **Health Checks**: Automatic health reporting to Agones
- **Metadata**: Set labels and annotations on the GameServer
- **Player Tracking**: Automatically track player connections/disconnections

### Open Match Integration

Open Match is an open-source matchmaking framework. The integration provides:

- **Match Assignment Endpoint**: HTTP endpoint (`/assign`) to receive match assignments
- **Match Validation**: Validate incoming match assignments
- **Status Reporting**: Report match status via Agones annotations
- **Custom Handlers**: Process match assignments with custom logic

## Usage

### Basic Setup

```kotlin
import live.einfachgustaf.minestom.base.MinestomBase
import java.net.InetSocketAddress

fun main() {
    val server = MinestomBase.createServer()
    
    server.start(
        address = InetSocketAddress("0.0.0.0", 25565),
        enableAgones = true,          // Enable Agones integration
        startHealthChecks = true,      // Start automatic health checks
        enableOpenMatch = true,        // Enable Open Match integration
        openMatchPort = 8080           // Port for /assign endpoint
    )
}
```

### Agones Features

#### Automatic Detection

Agones integration is automatically enabled when the `AGONES_SDK_GRPC_PORT` environment variable is set (which Agones does automatically).

```kotlin
// Agones will be enabled automatically in a Kubernetes/Agones environment
server.start(InetSocketAddress("0.0.0.0", 25565))
```

#### Manual Control

You can also explicitly control Agones integration:

```kotlin
server.start(
    address = InetSocketAddress("0.0.0.0", 25565),
    enableAgones = true,           // Force enable
    startHealthChecks = true       // Enable health checks
)
```

#### Setting Labels and Annotations

```kotlin
import kotlinx.coroutines.runBlocking

runBlocking {
    // Labels are used for filtering and selection
    server.agonesAdapter.setLabel("game-mode", "battle-royale")
    server.agonesAdapter.setLabel("map", "desert")
    
    // Annotations store additional metadata
    server.agonesAdapter.setAnnotation("match-id", "abc-123")
    server.agonesAdapter.setAnnotation("tournament-id", "summer-2024")
}
```

#### Setting Player Capacity

```kotlin
runBlocking {
    server.agonesAdapter.setPlayerCapacity(100)
}
```

#### Manual Health Status Control

```kotlin
// Mark server as unhealthy (stops health checks)
server.agonesAdapter.setHealthStatus(false)

// Mark server as healthy again
server.agonesAdapter.setHealthStatus(true)
```

#### Graceful Shutdown

```kotlin
// Signal shutdown to Agones before stopping
server.signalShutdown()

// Add a shutdown hook for automatic cleanup
Runtime.getRuntime().addShutdownHook(Thread {
    server.signalShutdown()
})
```

### Open Match Features

#### Handle Match Assignments

```kotlin
import live.einfachgustaf.minestom.base.k8s.MatchStatus

server.openMatchAdapter.onAssignment { assignment ->
    println("Match ${assignment.matchId} received!")
    println("Players: ${assignment.players.size}")
    
    // Process the assignment
    assignment.players.forEach { player ->
        println("Player ${player.playerId}")
    }
    
    // Set Agones labels
    runBlocking {
        server.agonesAdapter.setLabel("match-id", assignment.matchId)
    }
}
```

#### Report Match Status

```kotlin
import kotlinx.coroutines.runBlocking

runBlocking {
    server.openMatchAdapter.reportMatchStatus(
        matchId = "match-123",
        status = MatchStatus.IN_PROGRESS,
        details = "Game started"
    )
}
```

#### Match Assignment Format

The `/assign` endpoint expects JSON in the following format:

```json
{
  "matchId": "match-123",
  "players": [
    {
      "playerId": "player-1",
      "attributes": {
        "skill": "1500",
        "region": "us-west"
      }
    }
  ],
  "teams": {
    "red": ["player-1", "player-2"],
    "blue": ["player-3", "player-4"]
  },
  "sessionToken": "abc123xyz",
  "metadata": {
    "game-mode": "capture-the-flag",
    "map": "desert"
  }
}
```

Test it with curl:

```bash
curl -X POST http://localhost:8080/assign \
  -H "Content-Type: application/json" \
  -d '{
    "matchId": "test-123",
    "players": [{"playerId": "player1"}],
    "metadata": {"game-mode": "deathmatch"}
  }'
```

## Kubernetes Deployment

### GameServer Manifest

Example Agones GameServer manifest:

```yaml
apiVersion: agones.dev/v1
kind: GameServer
metadata:
  name: minestom-server
spec:
  ports:
    - name: default
      portPolicy: Dynamic
      containerPort: 25565
      protocol: TCP
    - name: openmatch
      portPolicy: Static
      containerPort: 8080
      protocol: TCP
  health:
    disabled: false
    initialDelaySeconds: 5
    periodSeconds: 5
  template:
    spec:
      containers:
        - name: minestom
          image: your-registry/minestom-server:latest
          env:
            - name: AGONES_SDK_GRPC_PORT
              value: "9357"
          resources:
            requests:
              memory: "2Gi"
              cpu: "1000m"
            limits:
              memory: "4Gi"
              cpu: "2000m"
```

### Fleet Configuration

Example Agones Fleet for auto-scaling:

```yaml
apiVersion: agones.dev/v1
kind: Fleet
metadata:
  name: minestom-fleet
spec:
  replicas: 2
  template:
    metadata:
      labels:
        app: minestom
    spec:
      ports:
        - name: default
          portPolicy: Dynamic
          containerPort: 25565
          protocol: TCP
        - name: openmatch
          portPolicy: Static
          containerPort: 8080
          protocol: TCP
      health:
        disabled: false
        initialDelaySeconds: 5
        periodSeconds: 5
      template:
        spec:
          containers:
            - name: minestom
              image: your-registry/minestom-server:latest
              env:
                - name: AGONES_SDK_GRPC_PORT
                  value: "9357"
```

## Examples

See `testapp/src/main/kotlin/live/einfachgustaf/minestom/base/testapp/K8sExample.kt` for a complete example demonstrating all features.

## Architecture

```
┌─────────────────────────────────────────────┐
│         Minestom Server                     │
│  ┌─────────────────────────────────────┐   │
│  │     MinestomServer                   │   │
│  │  ┌────────────┐  ┌───────────────┐  │   │
│  │  │  Agones    │  │  OpenMatch    │  │   │
│  │  │  Adapter   │  │   Adapter     │  │   │
│  │  └─────┬──────┘  └───────┬───────┘  │   │
│  └────────┼─────────────────┼──────────┘   │
│           │                 │              │
└───────────┼─────────────────┼──────────────┘
            │                 │
            ▼                 ▼
    ┌───────────────┐  ┌──────────────┐
    │  Agones SDK   │  │  HTTP Server │
    │   (gRPC)      │  │  (port 8080) │
    └───────┬───────┘  └──────┬───────┘
            │                 │
            ▼                 ▼
    ┌───────────────┐  ┌──────────────┐
    │   Agones      │  │  Open Match  │
    │  Controller   │  │  Director    │
    └───────────────┘  └──────────────┘
```

## Status Codes

### Match Status

- `PENDING`: Match assignment received but not started
- `IN_PROGRESS`: Match is currently active
- `COMPLETED`: Match finished successfully
- `FAILED`: Match failed or encountered an error
- `TIMEOUT`: Match timed out waiting for players

## Best Practices

1. **Always use health checks** in production environments
2. **Set meaningful labels** for filtering and observability
3. **Handle shutdown gracefully** by calling `signalShutdown()`
4. **Validate match assignments** before processing
5. **Report match status regularly** for observability
6. **Use player capacity** to help Agones with allocation decisions
7. **Set appropriate resource limits** in your Kubernetes manifests

## Troubleshooting

### Agones not connecting

- Ensure `AGONES_SDK_GRPC_PORT` is set (usually `9357`)
- Check that the Agones sidecar is running in the pod
- Verify network policies allow gRPC communication

### Health checks failing

- Check server logs for errors
- Verify the server is actually healthy
- Ensure health check interval isn't too aggressive

### Match assignments not received

- Verify the HTTP server is running on the correct port
- Check that port 8080 is accessible
- Test the endpoint with curl locally first
- Review Open Match Director logs for errors

## References

- [Agones Documentation](https://agones.dev/site/)
- [Open Match Documentation](https://open-match.dev/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
