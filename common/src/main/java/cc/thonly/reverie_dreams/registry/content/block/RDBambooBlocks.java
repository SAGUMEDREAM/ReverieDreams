package cc.thonly.reverie_dreams.registry.content.block;

import cc.thonly.reverie_dreams.block.nature.LargeBambooBlock;
import cc.thonly.reverie_dreams.block.nature.LargeBambooShootBlock;
import cc.thonly.reverie_dreams.registry.delegate.BlockDelegate;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import net.minecraft.util.Unit;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.List;

public final class RDBambooBlocks {
    public static final BlockDelegate LARGE_BAMBOO=RDBlocks.registerSimpleBlock("large_bamboo",LargeBambooBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.BAMBOO).offsetType(BlockBehaviour.OffsetType.NONE).randomTicks(),item("tooltip.reverie_dreams.large_bamboo"));
    public static final BlockDelegate LARGE_BAMBOO_SHOOT=RDBlocks.registerSimpleBlock("large_bamboo_shoot",LargeBambooShootBlock::new,
            RDPlantBlocks.createPlantSettings().offsetType(BlockBehaviour.OffsetType.NONE).randomTicks(),()->item("tooltip.reverie_dreams.large_bamboo_shoot")
                    .component(RDDataComponentTypes.INGREDIENT_ITEM_TYPE.value(),Unit.INSTANCE)
                    .food(new FoodProperties(2, 1.2f, false)));
    public static final List<BlockDelegate> ALL=List.of(LARGE_BAMBOO,LARGE_BAMBOO_SHOOT);
    private static Item.Properties item(String key){return new Item.Properties().component(DataComponents.LORE,new ItemLore(List.of(Component.translatable(key))));}
    public static void initialize(){}
    private RDBambooBlocks(){}
}
