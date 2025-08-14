package live.einfachgustaf.minestom.base.display


import net.kyori.adventure.text.Component
import net.minestom.server.component.DataComponents
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.metadata.display.TextDisplayMeta
import net.minestom.server.instance.Instance

class TextDisplayBuilder(private val instance: Instance) {
    var text: Component = Component.empty()
    var position: Pos = Pos(0.0, 0.0, 0.0)

    fun build(): Entity {
        val entity = Entity(EntityType.TEXT_DISPLAY)
        entity.editEntityMeta(TextDisplayMeta::class.java) {
            it.text = text
        }
        return entity
    }
}

fun textDisplay(
    instance: Instance,
    init: TextDisplayBuilder.() -> Unit
): TextDisplayBuilder {
    val builder = TextDisplayBuilder(instance)
    builder.init()
    builder.build().let {
        it.setInstance(instance, builder.position)
        it.spawn()
    }
    return builder
}