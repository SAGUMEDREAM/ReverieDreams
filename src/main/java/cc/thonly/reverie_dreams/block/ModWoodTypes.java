package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.Touhou;
import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.type.WoodTypeBuilder;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

public interface ModWoodTypes {
    BlockSetType SPIRITUAL_BLOCK_SET_TYPE = BlockSetTypeBuilder.copyOf(BlockSetType.OAK).register(Touhou.id("spiritual"));
    WoodType SPIRITUAL_WOOD_TYPE = WoodTypeBuilder.copyOf(WoodType.OAK).register(Touhou.id("spiritual"), SPIRITUAL_BLOCK_SET_TYPE);
}
