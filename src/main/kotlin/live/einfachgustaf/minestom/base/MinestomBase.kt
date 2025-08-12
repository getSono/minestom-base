package live.einfachgustaf.minestom.base

import live.einfachgustaf.minestom.base.server.MinestomServer

object MinestomBase {

    /**
     * Creates a new Minestom server instance.
     *
     * @return A new instance of [live.einfachgustaf.minestom.base.server.MinestomServer].
     */
    fun createServer(): MinestomServer = MinestomServer()
}