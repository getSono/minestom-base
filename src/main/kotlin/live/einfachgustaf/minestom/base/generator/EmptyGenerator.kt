package live.einfachgustaf.minestom.base.generator

import net.minestom.server.instance.generator.GenerationUnit
import net.minestom.server.instance.generator.Generator

class EmptyGenerator: Generator {

    override fun generate(unit: GenerationUnit?) = Unit
}