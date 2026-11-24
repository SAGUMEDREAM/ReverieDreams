package cc.thonly.reverie_dreams.registry.content.block;

import cc.thonly.reverie_dreams.block.nature.*;
import cc.thonly.reverie_dreams.registry.delegate.BlockDelegate;
import net.minecraft.world.level.block.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import java.util.List;

public final class RDBambooForestBlocks {
    public static final BlockDelegate LEAF_CLUSTER=RDBlocks.registerSimpleBlock("bamboo_leaf_cluster",BambooLeafClusterBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(.2F).noCollision().noOcclusion().sound(SoundType.GRASS));
    public static final BlockDelegate LEAF_BUSH=RDBlocks.registerSimpleBlock("bamboo_leaf_bush",BambooLeafBushBlock::new,RDPlantBlocks.createPlantSettings());
    public static final BlockDelegate LEAF_LITTER=RDBlocks.registerSimpleBlock("bamboo_leaf_litter",LeafLitterBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.LEAF_LITTER));
    public static final BlockDelegate DEBRIS=RDBlocks.registerSimpleBlock("bamboo_debris",BambooDebrisBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(.3F).noOcclusion().sound(SoundType.BAMBOO_WOOD),
            new Item.Properties().component(DataComponents.LORE,new ItemLore(List.of(Component.translatable("tooltip.reverie_dreams.bamboo_debris")))));
    public static final List<BlockDelegate> ALL=List.of(LEAF_CLUSTER,LEAF_BUSH,LEAF_LITTER,DEBRIS);
    public static void initialize(){}
    private RDBambooForestBlocks(){}
}
