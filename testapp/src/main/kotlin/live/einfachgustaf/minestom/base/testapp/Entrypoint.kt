package live.einfachgustaf.minestom.base.testapp

import live.einfachgustaf.minestom.base.MinestomBase
import live.einfachgustaf.minestom.base.testapp.commands.GiveItemStackCommand
import net.minestom.server.MinecraftServer
import java.net.InetSocketAddress

fun main() {
    val base = MinestomBase.createServer()

    MinecraftServer.getCommandManager().register(GiveItemStackCommand())

    base.start(InetSocketAddress("127.0.0.1", 25565))
}