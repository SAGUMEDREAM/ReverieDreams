package cc.thonly.reverie_dreams.fabric.gametest;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.Wagyu;
import cc.thonly.reverie_dreams.item.prop.ExorcismPaperItem;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.tag.RDEntityTypeTags;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestType;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public final class EntityAdditionsGameTest {
    @GameTest(maxTicks = 40)
    public void tengu_take_exorcism_damage_but_tamed_allies_do_not(GameTestHelper helper) {
        var level = helper.getLevel();
        var white = helper.spawn(RDEntityTypes.WHITE_WOLF_TENGU.value(), new BlockPos(2, 2, 2));
        var crow = helper.spawn(RDEntityTypes.CROW_TENGU.value(), new BlockPos(3, 2, 2));
        var tame = helper.spawn(RDEntityTypes.WHITE_WOLF_TENGU.value(), new BlockPos(2, 2, 3));
        var human = helper.spawn(RDEntityTypes.HUMAN.value(), new BlockPos(3, 2, 3));
        var player = helper.makeMockServerPlayerInLevel();
        tame.setOwner(player);

        helper.assertTrue(white.is(RDEntityTypeTags.YOKAI) && crow.is(RDEntityTypeTags.YOKAI),
                "both tengu must be in the data tag shared by exorcism, enchantment and advancement");
        float whiteHealth = white.getHealth(), crowHealth = crow.getHealth();
        float tameHealth = tame.getHealth(), humanHealth = human.getHealth();
        var paper = RDItems.EXORCISM_PAPER.createStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, paper);
        helper.assertTrue(paper.interactLivingEntity(player, white, InteractionHand.MAIN_HAND).consumesAction(),
                "direct paper interaction should succeed");
        helper.assertTrue(white.getHealth() < whiteHealth && white.hasEffect(MobEffects.WEAKNESS),
                "direct paper must deal damage and apply weakness");
        float afterDirect = white.getHealth();
        ExorcismPaperItem.purifyArea(level, white.position(), player, white);
        helper.assertTrue(crow.getHealth() < crowHealth && crow.hasEffect(MobEffects.GLOWING),
                "the surrounding crow must take area exorcism");
        helper.assertTrue(white.getHealth() == afterDirect, "the direct target must be excluded from the area damage");
        helper.assertTrue(tame.getHealth() == tameHealth && human.getHealth() == humanHealth,
                "a tamed tengu and a human must not be affected by exorcism");
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void wagyu_breeds_drops_its_ingredient_and_is_rare_in_taiga(GameTestHelper helper) {
        var level = helper.getLevel();
        var first = helper.spawn(RDEntityTypes.WAGYU.value(), new BlockPos(2, 2, 2));
        var second = helper.spawn(RDEntityTypes.WAGYU.value(), new BlockPos(3, 2, 2));
        var ordinary = helper.spawn(EntityType.COW, new BlockPos(2, 2, 4));
        first.setInLoveTime(600);
        second.setInLoveTime(600);
        ordinary.setInLoveTime(600);
        helper.assertTrue(first.canMate(second) && !first.canMate(ordinary), "wagyu breeds with its own kind");
        first.spawnChildFromBreeding(level, second);
        var calves = level.getEntitiesOfClass(Wagyu.class, first.getBoundingBox().inflate(4), Wagyu::isBaby);
        helper.assertTrue(calves.size() == 1 && calves.getFirst().getType() == RDEntityTypes.WAGYU.value(),
                "breeding must add a wagyu calf to the world");
        var calf = calves.getFirst();
        helper.assertTrue(first.getVariant().unwrapKey().orElseThrow().identifier().equals(ReverieDreams.id("wagyu")),
                "wagyu must use its own cow variant");
        helper.assertTrue(calf.getVariant().equals(first.getVariant()), "offspring must keep the wagyu variant");

        var table = level.getServer().reloadableRegistries().getLootTable(
                ResourceKey.create(Registries.LOOT_TABLE, ReverieDreams.id("entities/wagyu")));
        first.igniteForSeconds(3);
        var parameters = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, first)
                .withParameter(LootContextParams.ORIGIN, first.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, first.damageSources().onFire())
                .create(LootContextParamSets.ENTITY);
        boolean leather = false;
        for (int seed = 1; seed <= 16; seed++) {
            var drops = table.getRandomItems(parameters, seed);
            helper.assertTrue(drops.stream().anyMatch(stack -> stack.is(RDIngredientItems.WAGYU_BEEF.asItem())),
                    "each meat roll must drop the existing wagyu ingredient, including fire kills");
            helper.assertTrue(drops.stream().noneMatch(stack -> stack.is(Items.BEEF) || stack.is(Items.COOKED_BEEF)),
                    "the table must not produce regular beef");
            leather |= drops.stream().anyMatch(stack -> stack.is(Items.LEATHER));
        }
        helper.assertTrue(leather, "the leather pool must be present");

        var biomes = level.registryAccess().lookupOrThrow(Registries.BIOME);
        for (var key : java.util.List.of(Biomes.TAIGA, Biomes.SNOWY_TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA,
                Biomes.OLD_GROWTH_SPRUCE_TAIGA, GensokyoForestBootstrap.biome(GensokyoForestType.TAIGA))) {
            var biome = biomes.getOrThrow(key).value();
            int wagyuWeight = weight(biome, RDEntityTypes.WAGYU.value());
            helper.assertTrue(wagyuWeight > 0 && wagyuWeight < weight(biome, EntityType.COW),
                    "wagyu must appear in " + key.identifier() + " with lower weight than ordinary cows");
        }
        helper.assertTrue(weight(biomes.getOrThrow(Biomes.PLAINS).value(), RDEntityTypes.WAGYU.value()) == 0,
                "wagyu must not appear in the plains spawn table");
        helper.succeed();
    }

    private static int weight(Biome biome, EntityType<?> type) {
        return biome.getMobSettings().getMobs(MobCategory.CREATURE).unwrap().stream()
                .filter(entry -> entry.value().type() == type).mapToInt(entry -> entry.weight()).sum();
    }
}
