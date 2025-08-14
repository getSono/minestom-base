package live.einfachgustaf.minestom.base.testapp

import live.einfachgustaf.minestom.base.MinestomBase
import live.einfachgustaf.minestom.base.testapp.commands.GiveItemStackCommand
import live.einfachgustaf.minestom.base.testapp.commands.SpawnTextDisplayCommand
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.extras.MojangAuth
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.block.Block
import java.net.InetSocketAddress


fun main() {
    val base = MinestomBase.createServer()

    MojangAuth.init()

    listOf(
        GiveItemStackCommand(),
        SpawnTextDisplayCommand()
    ).forEach {
        MinecraftServer.getCommandManager().register(it)
    }

    // test instance
    val instanceContainer = MinecraftServer.getInstanceManager().createInstanceContainer().apply {
        // grass layer
        setGenerator { instance ->
            instance.modifier().fillHeight(0, 40, Block.GRASS_BLOCK)
        }
    }

    instanceContainer.setChunkSupplier(::LightingChunk)

    val globalEventHandler = MinecraftServer.getGlobalEventHandler()

    globalEventHandler.addListener(AsyncPlayerConfigurationEvent::class.java, { event ->
        val player = event.player
        event.spawningInstance = instanceContainer
        player.respawnPoint = Pos(0.0, 42.0, 0.0)
    })

    base.start(InetSocketAddress("127.0.0.1", 25565))
}