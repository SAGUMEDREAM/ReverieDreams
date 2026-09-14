package cc.thonly.reverie_dreams.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

public interface RegistryBuilderFactory {
    void addBuilder(Identifier key, Supplier<Registry<?>> factory);
}
