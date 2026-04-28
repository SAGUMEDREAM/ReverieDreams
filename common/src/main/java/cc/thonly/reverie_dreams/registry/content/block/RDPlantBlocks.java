package cc.thonly.reverie_dreams.registry.content.block;

import net.blay09.mods.balm.world.level.block.BalmBlockRegistrar;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class RDPlantBlocks {

    public static void initialize(BalmBlockRegistrar registrar) {

    }

    public static BlockBehaviour.Properties createPlantSettings() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY);
    }
}
