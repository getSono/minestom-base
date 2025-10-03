package live.einfachgustaf.minestom.base.k8s

import net.minestom.server.MinecraftServer
import net.scrayos.agones.client.GrpcAgonesSdk

class AgonesAdapter(val minecraftServer: MinecraftServer) {

    val agonesSdk = GrpcAgonesSdk()

    fun initialize() {
        AgonesListener(agonesSdk, minecraftServer)
    }

    suspend fun setPlayerCapacity(capacity: Long) {
        agonesSdk.alpha().playerCapacity(capacity)
    }
}