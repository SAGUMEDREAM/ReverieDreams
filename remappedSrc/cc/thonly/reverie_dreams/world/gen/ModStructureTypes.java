package cc.thonly.reverie_dreams.world.gen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public class ModStructureTypes {

    public static void init() {

    }

    private static <S extends Structure> StructureType<S> register(String id, MapCodec<S> codec) {
        return Registry.register(BuiltInRegistries.STRUCTURE_TYPE, id, () -> codec);
    }
}
