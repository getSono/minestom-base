import live.einfachgustaf.minestom.base.MinestomBase
import java.net.InetSocketAddress

fun main() {
    MinestomBase.createServer().start(InetSocketAddress("127.0.0.1", 25565))
}