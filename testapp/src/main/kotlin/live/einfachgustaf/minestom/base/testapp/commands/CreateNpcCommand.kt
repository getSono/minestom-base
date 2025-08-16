package live.einfachgustaf.minestom.base.testapp.commands

import com.github.juliarn.npclib.api.profile.Profile
import com.github.juliarn.npclib.minestom.util.MinestomUtil
import live.einfachgustaf.minestom.base.testapp.TestApp.npcPlatform
import net.minestom.server.command.builder.Command

class CreateNpcCommand: Command("createnpc") {

    init {
        // Define the command's behavior here
        setDefaultExecutor { sender, _ ->

            if (sender !is net.minestom.server.entity.Player) {
                sender.sendMessage("This command can only be used by players.")
                return@setDefaultExecutor
            }

            val position =
                MinestomUtil.positionFromMinestom(sender.position, sender.instance)

            npcPlatform.newNpcBuilder()
                .position(position)
                .profile(Profile.resolved(sender.username, sender.uuid))
                .apply {
                    val npc = buildAndTrack()
                    npc.shouldIncludePlayer(sender)
                }

            sender.sendMessage("NPC created successfully!")
        }
    }
}