package live.einfachgustaf.minestom.base.utils

import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

/**
 * Creates an [ItemStack] with the specified [material] and applies the given [block] configuration.
 *
 * @param material The material type of the item stack.
 * @param block A lambda to configure the item stack properties.
 * @return A configured [ItemStack].
 */
inline fun itemStack(material: Material, block: ItemStack.Builder.() -> Unit = {}): ItemStack {
    return ItemStack.builder(material).apply(block).build()
}