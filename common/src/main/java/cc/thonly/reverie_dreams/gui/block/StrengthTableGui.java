package cc.thonly.reverie_dreams.gui.block;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.entity.StrengthenTableBlockEntity;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.gui.GuiCommon;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.item.template.SpellCardTemplateItem;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.StrengthTableRecipe;
import cc.thonly.reverie_dreams.recipe.type.StrengthTableRecipeType;
import cc.thonly.reverie_dreams.recipe.type.StrengthTableRecipeType.UpgradeStatus;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerFactory;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerKeys;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Locale;

@Getter
public class StrengthTableGui extends SimpleGui implements GuiCommon {
    public static final String[][] GRID = new String[][]{
            {"X", "X", "I", "I", "X", "X", "I", "X", "X"}
    };
    StrengthenTableBlockEntity blockEntity;
    GuiElementBuilder output;

    public StrengthTableGui(ServerPlayer player, StrengthenTableBlockEntity blockEntity, boolean manipulatePlayerSlots) {
        super(MenuType.GENERIC_9x1, player, manipulatePlayerSlots);
        this.blockEntity = blockEntity;
        this.init();
    }

    public void init() {
        this.setTitle(
                Component.empty()
                        .append(Component.translatable("space.-8"))
                        .append(Component.literal("\ub004")
                                .withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)
                                        .withFont(new FontDescription.Resource(ReverieDreams.id("reverie_dreams")))))
                        .append(Component.translatable("space.-168"))
                        .append(Component.translatable(RDBlocks.STRENGTH_TABLE.asBlock().getDescriptionId()).withStyle(ChatFormatting.WHITE))
        );
        this.setSlot(2, new Slot(this.blockEntity.getInventory(), 0, 0, 0));
        this.setSlot(3, new Slot(this.blockEntity.getInventory(), 1, 0, 0));
        this.refreshOutput();
    }

    @Override
    public void onTick() {
        super.onTick();
        if (!this.hasTable()) {
            this.close();
            return;
        }
        this.refreshOutput();
    }

    private boolean hasTable() {
        if (this.blockEntity == null || this.blockEntity.getLevel() == null) return false;
        var level = this.blockEntity.getLevel();
        var pos = this.blockEntity.getBlockPos();
        return level.getBlockEntity(pos) == this.blockEntity
                && level.getBlockState(pos).is(RDBlocks.STRENGTH_TABLE.asBlock());
    }

    private void refreshOutput() {
        ItemStack mainStack = this.blockEntity.getInventory().getItem(0).copy();
        ItemStack offStack = this.blockEntity.getInventory().getItem(1).copy();
        var type = (StrengthTableRecipeType) RecipeManager.STRENGTH_TABLE;
        var upgrade = type.evaluateUpgrade(new IngredientStack(mainStack), new IngredientStack(offStack));
        if (upgrade.status() == UpgradeStatus.READY) {
            ItemStack result = upgrade.output().getLazyStack().copy();
            this.output = new GuiElementBuilder(result)
                    .addLoreLine(Component.translatableWithFallback("gui.reverie_dreams.strength.take", "Click to apply the upgrade"))
                    .setCallback((index, click, action, basedGui) -> this.clickOutput());
            addChanges(this.output, mainStack, offStack, result);
        } else {
            this.output = new GuiElementBuilder(upgrade.status() == UpgradeStatus.INVALID ? Items.BARRIER : Items.PAPER)
                    .setName(switch (upgrade.status()) {
                        case MISSING_SUBJECT -> Component.translatableWithFallback("gui.reverie_dreams.strength.subject", "Place a danmaku item");
                        case MISSING_MATERIAL -> Component.translatableWithFallback("gui.reverie_dreams.strength.material", "Add an upgrade material");
                        case INVALID -> Component.translatableWithFallback("gui.reverie_dreams.strength.invalid", "These items cannot be combined");
                        case LIMIT -> Component.translatableWithFallback("gui.reverie_dreams.strength.limit", "This upgrade exceeds the limit");
                        case READY -> throw new IllegalStateException("Upgrade has a result");
                    })
                    .setCallback(() -> SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f));
        }
        this.setSlot(6, this.output);
    }

    private static void addChanges(GuiElementBuilder builder, ItemStack mainStack, ItemStack material, ItemStack result) {
        var type = RDDataComponentTypes.DANMAKU_PROPERTIES.value();
        var before = mainStack.getOrDefault(type, DanmakuProperties.ofDefault());
        var after = result.getOrDefault(type, DanmakuProperties.ofDefault());
        if (before.actualSpeed() != after.actualSpeed()) {
            builder.addLoreLine(Component.translatableWithFallback("gui.reverie_dreams.strength.speed", "Speed: %s → %s",
                    number(before.actualSpeed()), number(after.actualSpeed())));
        }
        if (before.count() != after.count()) {
            builder.addLoreLine(Component.translatableWithFallback("gui.reverie_dreams.strength.count", "Projectiles: %s → %s",
                    before.count(), after.count()));
        }
        if (before.damage() != after.damage()) {
            builder.addLoreLine(Component.translatableWithFallback("gui.reverie_dreams.strength.damage", "Damage: %s → %s",
                    number(before.damage()), number(after.damage())));
        }
        if (material.getItem() instanceof SpellCardTemplateItem) {
            builder.addLoreLine(Component.translatableWithFallback("gui.reverie_dreams.strength.template", "Applies the supplied spell-card template"));
        }
    }

    private static String number(float value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    public void clickOutput() {
        if (!this.hasTable()) {
            return;
        }
        SimpleContainer inventory = this.blockEntity.getInventory();
        ItemStack mainStack = inventory.getItem(0);
        ItemStack offStack = inventory.getItem(1);
        IngredientStack mainSlot = new IngredientStack(mainStack);
        IngredientStack offSlot = new IngredientStack(offStack);
        List<StrengthTableRecipe> entries = RecipeManager.STRENGTH_TABLE.getMatches(List.of(mainSlot, offSlot));
        if (entries.isEmpty()) {
            this.refreshOutput();
            return;
        }
        StrengthTableRecipe entry = entries.getFirst();
        if (entry != null) {
            IngredientStack resultItem = entry.getOutput();

            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack itemStack = inventory.getItem(i);
                itemStack.shrink(1);
            }
            ItemStack itemStack = resultItem.build().copy();
//            itemStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(this.inputText));
            this.player.getInventory().placeItemBackInInventory(itemStack);
            this.blockEntity.setChanged();
            this.refreshOutput();
            SimpleTriggerFactory.create(SimpleTriggerKeys.DANMAKU_UPGRADE).trigger(this.player);
            SoundEventPlayUtils.playUISound(this.player, SoundEvents.ANVIL_USE, 1.0f, 1.0f);
        } else {
            this.output.setItem(Items.AIR);
        }
    }

    @Override
    public void onOpen() {
        super.onOpen();
    }

    @Override
    public void onPlayerClose(boolean success) {
        super.onPlayerClose(success);
        this.blockEntity.setChanged();
    }
}
