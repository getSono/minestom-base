package live.einfachgustaf.minestom.base.testapp.commands

import net.kyori.adventure.text.Component
import net.minestom.server.command.builder.Command
import net.minestom.server.component.DataComponents
import net.minestom.server.entity.EntityCreature
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player


class SpawnTextDisplayCommand: Command("spawntextdisplay") {

    init {
        setDefaultExecutor { sender, _ ->
            if (sender !is Player) {
                sender.sendMessage("This command can only be used by players.")
                return@setDefaultExecutor
            }

            /*
            val textDisplay = textDisplay(sender.instance) {
                text = Component.text("TextDisplay")
                position = sender.position.add(0.0, 2.0, 0.0) // Spawn it 2 blocks above the player
            }
             */

            val instance = sender.instance
            val position = sender.position
            val entity = EntityCreature(EntityType.TEXT_DISPLAY)

            entity.set(DataComponents.CUSTOM_NAME, Component.text("TextDisplay"))

            entity.setInstance(instance, position) // actually spawning a horse

            sender.sendMessage("Text display spawned!")
        }
    }
}