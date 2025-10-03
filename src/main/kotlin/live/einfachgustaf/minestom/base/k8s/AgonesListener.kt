package live.einfachgustaf.minestom.base.k8s

import com.github.shynixn.mccoroutine.minestom.addSuspendingListener
import net.minestom.server.MinecraftServer
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.scrayos.agones.client.AgonesSdk

class AgonesListener(private val agonesSdk: AgonesSdk, minecraftServer: MinecraftServer) {

    init {
        val globalEventHandler = MinecraftServer.getGlobalEventHandler()

        globalEventHandler.addSuspendingListener(minecraftServer, AsyncPlayerConfigurationEvent::class.java) {
            agonesSdk.alpha().playerConnect(
                it.player.uuid.toString()
            )
        }

        globalEventHandler.addSuspendingListener(minecraftServer, PlayerDisconnectEvent::class.java) {
            agonesSdk.alpha().playerDisconnect(
                it.player.uuid.toString()
            )
        }
    }
}