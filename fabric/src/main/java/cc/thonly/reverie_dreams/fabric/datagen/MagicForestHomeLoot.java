package cc.thonly.reverie_dreams.fabric.datagen;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootTable;
import java.util.List;
import java.util.function.BiConsumer;

final class MagicForestHomeLoot {
    private record Gift(String id, String name, int x, int y, int z, ItemLike first, ItemLike second, ItemLike third) {}
    private record Home(String id, List<Gift> gifts) {
        String path() { return "gensokyo/" + id; }
        ResourceKey<LootTable> key(Gift g) { return ChestLootTableProvider.getKey(path()+"/"+g.id); }
    }
    private static final List<Home> HOMES = List.of(
        new Home("kirisame_residence", List.of(
            new Gift("field_samples","魔理沙的采集箱",9,2,20,Items.RED_MUSHROOM,Items.BROWN_MUSHROOM,Items.GLASS_BOTTLE),
            new Gift("brew_stock","蘑菇炼制材料",14,2,20,Items.FERMENTED_SPIDER_EYE,Items.SUGAR,Items.GLOWSTONE_DUST),
            new Gift("experiment_notes","魔法实验笔记",22,8,22,Items.BOOK,Items.PAPER,Items.INK_SAC),
            new Gift("star_experiments","星弹实验材料",24,8,18,Items.FIREWORK_STAR,Items.GLOWSTONE_DUST,Items.GUNPOWDER),
            new Gift("optical_parts","聚光实验零件",24,8,27,Items.AMETHYST_SHARD,Items.COPPER_INGOT,Items.GLASS))),
        new Home("margatroid_residence", List.of(
            new Gift("doll_fabric","人偶裁衣材料",36,2,28,Items.WHITE_WOOL,Items.STRING,Items.BLUE_DYE),
            new Gift("doll_joints","人偶关节零件",42,2,16,Items.COPPER_INGOT,Items.IRON_NUGGET,Items.STICK),
            new Gift("doll_paint","人偶上色材料",48,3,26,Items.RED_DYE,Items.BLUE_DYE,Items.BRUSH),
            new Gift("thread_spools","操偶丝线",48,3,21,Items.STRING,Items.LEAD,Items.SPIDER_EYE),
            new Gift("patterns","人偶工房样纸",40,3,42,Items.PAPER,Items.BOOK,Items.INK_SAC)))
    );

    static void generateLoot(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> out) {
        for (Home home : HOMES) for (Gift g : home.gifts)
            out.accept(home.key(g), LootTable.lootTable()
                    .withPool(ChestLootTableProvider.supplies(g.first,1,3))
                    .withPool(ChestLootTableProvider.supplies(g.second,1,3))
                    .withPool(ChestLootTableProvider.supplies(g.third,1,2)));
    }

}
