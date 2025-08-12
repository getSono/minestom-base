package live.einfachgustaf.minestom.base.testapp.commands

import live.einfachgustaf.minestom.base.utils.itemStack
import net.kyori.adventure.text.Component
import net.minestom.server.command.builder.Command
import net.minestom.server.entity.Player
import net.minestom.server.item.Material

class GiveItemStackCommand: Command("giveitemstack") {

    init {
        setDefaultExecutor { sender, _ ->
            if (sender !is Player) {
                sender.sendMessage("This command can only be used by players.")
                return@setDefaultExecutor
            }

            // Example of giving an item stack to the player
            val stack = itemStack(Material.DIAMOND) {
                amount(5) // Set the number of diamonds to 5
                customName(Component.text("Shiny Diamonds")) // Set a custom name for the item
                lore(listOf(Component.text("These diamonds are shiny!"))) // Set a lore for the item
            }
        }
    }
}