package cc.thonly.reverie_dreams.item;

import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.gui.recipe.RecipeTypeCategoryGui;
import cc.thonly.reverie_dreams.item.base.BasicPolymerItem;
import cc.thonly.reverie_dreams.server.DelayedTask;
import cc.thonly.reverie_dreams.dialog.DialogInit;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.gui.BookGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
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
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class TouhouHelperItem extends BasicPolymerItem {
    public TouhouHelperItem(String path, Settings settings) {
        super(path,
                settings.maxCount(1)
                        .rarity(Rarity.EPIC)
                        .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
                ,
                Items.KNOWLEDGE_BOOK
        );
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

    public static class HelperGui extends BookGui {

        public HelperGui(ServerPlayerEntity player) {
            super(player, getDefaultBookElement());
            this.init();
        }

        public void init() {
            this.player.playSoundToPlayer(SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }

        public static BookElementBuilder getDefaultBookElement() {
            BookElementBuilder builder = new BookElementBuilder();
            // 1
            builder.addPage(
                    Text.empty().append("欢迎游玩Gensokyo: Reverie of Lost Dreams，本书将介绍模组的游玩指南"),
                    Text.empty(),
                    Text.empty().append("导航目录："),
                    Text.empty().append("> [祭坛摆放]").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.ChangePage(3))),
                    Text.empty().append("> [弹幕合成]").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.ChangePage(4))),
                    Text.empty().append("> [Fumo制作]").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.ChangePage(6))),
                    Text.empty().append("> [角色养成]").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.ChangePage(7))),
                    Text.empty().append("").setStyle(Style.EMPTY),
                    Text.empty().append("配方管理器：/touhou recipe").setStyle(Style.EMPTY)
            );
            // 2
            builder.addPage(Text.empty());
            // 3
            builder.addPage(
                    Text.literal("祭坛摆放："),
                    Text.empty().append("§b⏹§b⏹§b⏹§b⏹§c⏹§b⏹§b⏹§b⏹§b⏹"),
                    Text.empty().append("§b⏹§c⏹§b⏹§b⏹§b⏹§b⏹§b⏹§c⏹§b⏹"),
                    Text.empty().append("§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹"),
                    Text.empty().append("§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹"),
                    Text.empty().append("§c⏹§b⏹§b⏹§b⏹§e⏹§b⏹§b⏹§b⏹§c⏹"),
                    Text.empty().append("§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹"),
                    Text.empty().append("§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹"),
                    Text.empty().append("§b⏹§c⏹§b⏹§b⏹§b⏹§b⏹§b⏹§c⏹§b⏹"),
                    Text.empty().append("§b⏹§b⏹§b⏹§b⏹§c⏹§b⏹§b⏹§b⏹§b⏹"),
                    Text.empty().append("§b⏹：").append(Text.translatable(Items.AIR.getTranslationKey())),
                    Text.empty().append("§c⏹：").append(Text.translatable(ModBlocks.SPIRITUAL.strippedLog().getTranslationKey())),
                    Text.empty().append("§e⏹：").append(Text.translatable(ModBlocks.GENSOKYO_ALTAR.getTranslationKey())),
                    Text.empty().append("注意：").append("§c⏹§r一共摆放三层")
            );
            // 4
            builder.addPage(
                    Text.empty().append("弹幕工作台："),
                    Text.empty().append("弹幕工作台是一个合成弹幕的工作方块，里面共有5个槽位可以摆放物品合成，合成类型为有序合成"),
                    Text.empty(),
                    Text.empty().append("合成配方："),
                    Text.empty().append("§c⏹§c⏹§c⏹"),
                    Text.empty().append("§c⏹§e⏹§c⏹"),
                    Text.empty().append("§c⏹§c⏹§c⏹"),
                    Text.empty().append("§e⏹").append(Text.translatable(Items.CRAFTING_TABLE.getTranslationKey())),
                    Text.empty().append("§c⏹").append(Text.translatable(Items.REDSTONE.getTranslationKey()))
            );
            // 5
            builder.addPage(
                    Text.empty().append("弹幕工作台："),
                    Text.empty().append("通用弹幕配方："),
                    Text.empty().append("§a⏹ -> ").append("任意颜料"),
                    Text.empty().append("§b⏹ -> ").append(Text.translatable(Items.FIREWORK_STAR.getTranslationKey())),
                    Text.empty().append("§c⏹ -> ").append(Text.translatable(ModItems.POWER.getTranslationKey())),
                    Text.empty().append("§d⏹ -> ").append(Text.translatable(ModItems.POINT.getTranslationKey())),
                    Text.empty().append("§e⏹ -> ")
            );
            // 6
            builder.addPage(
                    Text.empty().append("Fumo是指由日本株式会社Gift出品的一系列东方Project角色的布制玩偶，因其商品名带有“ふもふも（fumofumo）”字样而得名。"),
                    Text.empty().append(Text.translatable(ModItems.FUMO_LICENSE.getTranslationKey())).append("+").append(EntityType.VILLAGER.getTranslationKey()).append("=>").append(ModEntities.FUMO_SELLER_VILLAGER.getTranslationKey()),
                    Text.empty().append("提示：每一天Fumo商人都会刷新新的Fumo，不要拖太久买哦!")
            );
            // 7
            builder.addPage(
                    Text.empty().append("角色是指本Mod中的随从角色，可以使用蛋糕驯服，拥有背包，功能类似女仆"),
                    Text.empty().append("角色可通过合成角色卡概率随机获取一种东方Project角色"),
                    Text.empty().append("角色死亡后会变成").append(Text.translatable(ModItems.ROLE_ARCHIVE.getTranslationKey())).append("，需要在祭坛摆放一圈").append(Items.DIAMOND.getTranslationKey()).append("x4才能复活你的角色")
            );

            builder.signed();
            return builder;
        }

        @Override
        public boolean onCommand(String command) {
            if (command.equals("/touhou ui_relay_recipe")) {
                this.close();
                DelayedTask.createFromSecond(this.player.getServer(), 0.1f, () -> RecipeTypeCategoryGui.create(this.player));
            }
            return false;
        }

        @Override
        public void onPreviousPageButton() {
            super.onPreviousPageButton();
            this.player.playSoundToPlayer(SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }

        @Override
        public void onNextPageButton() {
            super.onNextPageButton();
            this.player.playSoundToPlayer(SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }

        @Override
        public void onTakeBookButton() {
            super.onTakeBookButton();
            this.close();
        }

    }
}
