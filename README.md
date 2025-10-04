# Minestom Base
A base for Minestom servers, providing useful utilities and features to speed up development.

## Features

### ✅ Kubernetes Integration
- **Agones SDK**: Lifecycle management, health checks, metadata, player tracking
- **Open Match**: Matchmaking with HTTP endpoint for match assignments
- See [K8s Integration Guide](docs/K8S_INTEGRATION.md) for full documentation

### Utilities
- ItemStack builder DSL
- Empty world generator
- McCoroutines integration

## Quick Start

### Basic Server
```kotlin
import live.einfachgustaf.minestom.base.MinestomBase
import java.net.InetSocketAddress

fun main() {
    val server = MinestomBase.createServer()
    server.start(InetSocketAddress("0.0.0.0", 25565))
}
```

### With Kubernetes Integration
```kotlin
val server = MinestomBase.createServer()
server.start(
    address = InetSocketAddress("0.0.0.0", 25565),
    enableAgones = true,
    enableOpenMatch = true
)
```

## TODO
- [ ] NPCs via juliarn/npc-lib
- [ ] Map displays via image files
- [ ] ktor-like dsl for creating Minecraft servers

## Setup project
### Windows
```shell
mkdir run/
./gradlew.bat build
```

### Linux/macOS
```shell
make setup
```
## Reinit project
### Windows
```shell
rm -rf run/
mkdir run/
./gradlew.bat build
```
### Linux/macOS
```shell
make reinit
```