package live.einfachgustaf.minestom.base

import com.github.juliarn.npclib.api.NpcActionController
import com.github.juliarn.npclib.api.Platform as NpcLibPlatform
import com.github.juliarn.npclib.minestom.MinestomPlatform as NpcLibMinestomPlatform
import live.einfachgustaf.minestom.base.server.MinestomServer
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.item.ItemStack
import java.util.function.Consumer

object MinestomBase {

    /**
     * Creates a new Minestom server instance.
     *
     * @return A new instance of [live.einfachgustaf.minestom.base.server.MinestomServer].
     */
    fun createServer(): MinestomServer = MinestomServer()

    /**
     * Creates a new NPC library platform for Minestom.
     *
     * @return A new instance of [NpcLibPlatform] configured for Minestom.
     */
    fun createNpcLibPlatform(debug: Boolean = false, actionController: Consumer<NpcActionController.Builder>): NpcLibPlatform<Instance?, Player?, ItemStack?, in Any> {
        return NpcLibMinestomPlatform.minestomNpcPlatformBuilder()
            .debug(debug)
            .extension(this)
            .actionController(actionController)
            .build()
    }
}