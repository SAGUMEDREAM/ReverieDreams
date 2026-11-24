package cc.thonly.reverie_dreams.world.gensokyo.adventure;

import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.item.AncientIronEquipment;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDBeverageItems;
import cc.thonly.reverie_dreams.registry.content.item.RDCuisineItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.*;

import java.util.function.BiConsumer;

public final class ScarletChestLoot {
    private ScarletChestLoot() {
    }

    public static ResourceKey<LootTable> key(String tier) {
        return ResourceKey.create(Registries.LOOT_TABLE, Identifier.parse("reverie_dreams:chest/gensokyo/scarlet/" + tier));
    }

    public static void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output, HolderLookup.Provider registry) {
        output.accept(key("supply"), base(3, 7)
                .withPool(once()
                        .add(stack(Items.BREAD, 3, 6).setWeight(2))
                        .add(stack(Items.BAKED_POTATO, 3, 6))
                        .add(stack(Items.COOKED_CHICKEN, 2, 4))
                        .add(stack(RDCuisineItems.SCARLET_DEVILS_CAKE, 1, 2)))
                .withPool(once()
                        .add(stack(Items.TORCH, 8, 14).setWeight(2))
                        .add(stack(Items.LADDER, 6, 10))
                        .add(stack(Items.ARROW, 6, 12))
                        .add(stack(RDBeverageItems.SCARLET_DEVIL_MANSION_BLACK_TEA, 1, 2))));

        output.accept(key("guard"), base(8, 16)
                .withPool(once()
                        .add(stack(RDItems.FAIRY_DUST, 2, 5))
                        .add(stack(Items.IRON_INGOT, 2, 4))
                        .add(stack(RDItems.EXORCISM_PAPER, 3, 6))
                        .add(stack(RDItems.BOMB_FRAGMENT, 1, 2)))
                .withPool(once()
                        .add(LootItem.lootTableItem(Items.SHIELD))
                        .add(LootItem.lootTableItem(Items.CROSSBOW))
                        .add(LootItem.lootTableItem(AncientIronEquipment.SWORD))
                        .add(LootItem.lootTableItem(AncientIronEquipment.BOOTS))
                        .add(stack(Items.SPECTRAL_ARROW, 6, 10))));

        output.accept(key("secret"), base(12, 24)
                .withPool(once().add(enchantedBook(registry, 12, 18)))
                .withPool(once()
                        .add(LootItem.lootTableItem(AncientIronEquipment.SPEAR))
                        .add(LootItem.lootTableItem(AncientIronEquipment.PICKAXE))
                        .add(stack(RDItems.UPGRADED_HEALTH_FRAGMENT, 1, 2))
                        .add(stack(RDItems.POWER, 4, 6))
                        .add(stack(Items.DIAMOND, 1, 2))));

        output.accept(key("heart"), base(24, 36)
                .withPool(once().add(LootItem.lootTableItem(RDItems.UPGRADED_HEALTH)))
                .withPool(once().add(enchantedBook(registry, 20, 28)))
                .withPool(once().add(stack(RDItems.BOMB_FRAGMENT, 3, 5)))
                .withPool(once()
                        .add(stack(RDItems.FAIRY_DUST, 6, 10))
                        .add(stack(RDItems.POWER, 6, 10))
                        .add(stack(Items.DIAMOND, 2, 3))));

        output.accept(key("library"), LootTable.lootTable().setParamSet(LootContextParamSets.CHEST)
                .withPool(once()
                        .add(researchBook(registry, Enchantments.FIRE_PROTECTION))
                        .add(researchBook(registry, Enchantments.RESPIRATION))
                        .add(researchBook(registry, Enchantments.UNBREAKING))
                        .add(researchBook(registry, Enchantments.PROTECTION)))
                .withPool(once()
                        .add(elementalBlueprint(DanmakuTypes.FIREBALL))
                        .add(elementalBlueprint(DanmakuTypes.BUBBLE))
                        .add(elementalBlueprint(DanmakuTypes.LEAF)))
                .withPool(once().add(stack(RDItems.DANMAKU_CORE, 1, 2))));
    }

    private static LootTable.Builder base(int min, int max) {
        return LootTable.lootTable().setParamSet(LootContextParamSets.CHEST).withPool(once().add(stack(RDItems.COPPER_COIN, min, max)));
    }

    private static LootPool.Builder once() {
        return LootPool.lootPool().setRolls(ConstantValue.exactly(1));
    }

    private static LootPoolSingletonContainer.Builder<?> enchantedBook(HolderLookup.Provider registry, int min, int max) {
        return LootItem.lootTableItem(Items.BOOK).apply(EnchantWithLevelsFunction.enchantWithLevels(registry, UniformGenerator.between(min, max)));
    }

    private static LootPoolSingletonContainer.Builder<?> researchBook(HolderLookup.Provider registry, ResourceKey<Enchantment> enchantment) {
        var entry = registry.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment);
        return LootItem.lootTableItem(Items.BOOK)
                .apply(new SetEnchantmentsFunction.Builder().withEnchantment(entry, ConstantValue.exactly(2)));
    }

    private static LootPoolSingletonContainer.Builder<?> elementalBlueprint(DanmakuType type) {
        return LootItem.lootTableItem(RDItems.DANMAKU_SHAPE_CREATOR)
                .apply(SetComponentsFunction.setComponent(RDDataComponentTypes.DANMAKU_SHAPE.value(), IngredientStack.of(type.getItemHolder())));
    }

    private static LootPoolSingletonContainer.Builder<?> stack(ItemLike item, int min, int max) {
        return LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));
    }
}
