package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.dialog.DialogInit;
import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.gui.recipe.RecipeTypeCategoryGui;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.server.DelayedTask;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.gui.BookGui;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class TouhouHelperItem extends Item {

    public TouhouHelperItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (!world.isClientSide() && user instanceof ServerPlayer player) {
            ItemStack itemStack = user.getItemInHand(hand);
            user.openItemGui(itemStack, hand);
            user.awardStat(Stats.ITEM_USED.get(this));
            user.openDialog(Holder.direct(DialogInit.MAIN_HELP));
            world.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    public static boolean resolve(ItemStack book, CommandSourceStack commandSource, @Nullable Player player) {
        WrittenBookContent writtenBookContentComponent = book.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (writtenBookContentComponent != null && !writtenBookContentComponent.resolved()) {
            WrittenBookContent writtenBookContentComponent2 = writtenBookContentComponent.resolve(commandSource, player);
            if (writtenBookContentComponent2 != null) {
                book.set(DataComponents.WRITTEN_BOOK_CONTENT, writtenBookContentComponent2);
                return true;
            }
            book.set(DataComponents.WRITTEN_BOOK_CONTENT, writtenBookContentComponent.markResolved());
        }
        return false;
    }

}
