package cc.thonly.reverie_dreams.gui.block;

import cc.thonly.reverie_dreams.gui.container.InfiniteContainerGui;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import cc.thonly.reverie_dreams.util.item.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class CupboardGui extends InfiniteContainerGui {

    public CupboardGui(ServerPlayer player, BlockState state, Level world, BlockPos pos) {
        super(player, state, world, pos);
    }

    @Override
    public Block getSupportBlock() {
        return RDBlocks.CUPBOARD.asBlock();
    }

    @Override
    public boolean canInsertItem(ItemStack itemStack) {
        ItemUtils.updateItemStackTag(itemStack);
        return itemStack.has(RDDataComponentTypes.FOOD_PROPERTIES.value())
                || itemStack.has(RDDataComponentTypes.BEVERAGE_PROPERTIES.value())
                || itemStack.is(RDItemTags.FOOD)
                || itemStack.is(RDItemTags.BEVERAGE)
                || itemStack.is(RDItemTags.INGREDIENT)
                ;
    }

}
