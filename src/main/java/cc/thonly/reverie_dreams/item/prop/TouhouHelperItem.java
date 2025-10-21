package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.dialog.DialogInit;
import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.gui.recipe.RecipeTypeCategoryGui;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.server.DelayedTask;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.gui.BookGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TouhouHelperItem extends Item {

    public TouhouHelperItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient() && user instanceof ServerPlayerEntity player) {
            ItemStack itemStack = user.getStackInHand(hand);
            user.useBook(itemStack, hand);
            user.incrementStat(Stats.USED.getOrCreateStat(this));
            user.openDialog(RegistryEntry.of(DialogInit.MAIN_HELP));
            world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS);
            return ActionResult.SUCCESS_SERVER;
        }
        return ActionResult.SUCCESS;
    }

    public static boolean resolve(ItemStack book, ServerCommandSource commandSource, @Nullable PlayerEntity player) {
        WrittenBookContentComponent writtenBookContentComponent = book.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
        if (writtenBookContentComponent != null && !writtenBookContentComponent.resolved()) {
            WrittenBookContentComponent writtenBookContentComponent2 = writtenBookContentComponent.resolve(commandSource, player);
            if (writtenBookContentComponent2 != null) {
                book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, writtenBookContentComponent2);
                return true;
            }
            book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, writtenBookContentComponent.asResolved());
        }
        return false;
    }

}
