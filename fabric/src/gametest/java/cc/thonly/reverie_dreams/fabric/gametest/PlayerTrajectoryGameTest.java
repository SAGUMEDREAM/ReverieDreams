package cc.thonly.reverie_dreams.fabric.gametest;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuSpawner;
import cc.thonly.reverie_dreams.data.danmaku.trajectory.PlayerTrajectory;
import cc.thonly.reverie_dreams.data.danmaku.trajectory.PlayerTrajectoryType;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.item.danmaku.DanmakuItem;
import cc.thonly.reverie_dreams.loot.RDLootModifies;
import cc.thonly.reverie_dreams.recipe.type.GensokyoAltarRecipeType;
import cc.thonly.reverie_dreams.recipe.type.StrengthTableRecipeType;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTemplates;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Locale;

import static cc.thonly.reverie_dreams.data.danmaku.trajectory.PlayerTrajectoryType.*;

public final class PlayerTrajectoryGameTest {
    @GameTest(structure = "reverie_dreams:player_trajectory_test_range", padding = 7, maxTicks = 55)
    public void acquisition_and_strengthening(GameTestHelper helper) {
        var view = DanmakuTemplates.getRegistryView();
        var itemView = DanmakuTemplates.getRegistryItemStackView();
        helper.assertTrue(view.keySet().equals(itemView.keySet()) && view.size() == 16
                && !view.containsKey(ReverieDreams.id("fan")), "exactly sixteen public profiles");
        int workbench = 0, altar = 0, loot = 0;
        for (var type : PlayerTrajectoryType.values()) {
            helper.assertTrue(BuiltInRegistryProviders.DANMAKU_TRAJECTORY.getValue(type.id()) instanceof PlayerTrajectory,
                    "runtime registry " + type.id());
            var weapon = StrengthTableRecipeType.getInstance().tryGetOutput(
                    IngredientStack.of(DanmakuTypes.BALL.getItemHolder()),
                    IngredientStack.of(itemView.get(type.id()))).build();
            helper.assertTrue(weapon.get(RDDataComponentTypes.DANMAKU_PROPERTIES.value()).templateId().equals(type.id()),
                    "strengthening result " + type.id());
            var id = ReverieDreams.id("danmaku_trajectory/" + type.id().getPath());
            if (helper.getLevel().getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, id)).isPresent()) {
                workbench++;
            }
            var recipe = GensokyoAltarRecipeType.getInstance().getRegistryView().get(ReverieDreams.id("danmaku_trajectory-" + type.id().getPath()));
            if (recipe != null) {
                helper.assertTrue(recipe.getOutput().build().get(RDDataComponentTypes.DANMAKU_PROPERTIES.value())
                        .templateId().equals(type.id()), "altar output " + type.id());
                altar++;
            }
            if (type.lootTable() != null) {
                LootTable.Builder table = LootTable.lootTable();
                RDLootModifies.registerTemplateDrops(type.lootTable(), table::withPool, true);
                LootParams context = new LootParams.Builder(helper.getLevel())
                        .withParameter(LootContextParams.ORIGIN, helper.absoluteVec(new Vec3(2, 2, 2)))
                        .create(LootContextParamSets.CHEST);
                int found = 0;
                var result = table.build();
                for (int seed = 101; seed < 128; seed++) {
                    for (var stack : result.getRandomItems(context, seed * 521L)) {
                        helper.assertTrue(type.id().equals(stack.get(RDDataComponentTypes.DANMAKU_PROPERTIES.value()).templateId()),
                                "targeted chest " + type.id());
                        found++;
                    }
                }
                helper.assertTrue(found > 0, "loot not reachable " + type.id());
                loot++;
            }
        }
        helper.assertTrue(workbench == 4 && altar == 7 && loot == 5,
                "four workbench, seven altar and five chest paths, got " + workbench + "," + altar + "," + loot);
        pass(helper, "acquisition and strengthening");
    }

    @GameTest(structure = "reverie_dreams:player_trajectory_test_range", padding = 7, maxTicks = 35)
    public void player_item_use_and_scaled_origin(GameTestHelper h) {
        var level = h.getLevel();
        var player = h.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = false;
        player.setPos(h.absoluteVec(new Vec3(12.5, 2, 1.5)));
        player.getAttribute(Attributes.SCALE).setBaseValue(1.6);
        player.setXRot(0);
        player.setYRot(0);
        var stack = DanmakuTypes.BALL.getItemHolder().createStack();
        var component = RDDataComponentTypes.DANMAKU_PROPERTIES.value();
        stack.set(component, stack.get(component).withTemplateId(BEAT_BURST.id()).withSpeed(1).withDamage(8));
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var item = (DanmakuItem) stack.getItem();
        h.assertTrue(item.use(level, player, InteractionHand.MAIN_HAND).consumesAction(), "normal right-click succeeds");
        var shot = one(h, player);
        h.assertTrue(Math.abs(shot.getY() - player.getY(0.55)) < 0.01 && shot.getY() < player.getEyeY(),
                "uses actual scaled body height");
        h.assertTrue(stack.getDamageValue() == 1 && player.getCooldowns().isOnCooldown(stack), "one durability and cooldown, damage=" + stack.getDamageValue() + " cooldown=" + player.getCooldowns().isOnCooldown(stack));
        h.assertTrue(!item.use(level, player, InteractionHand.MAIN_HAND).consumesAction(), "cooldown blocks a second cast");
        h.runAtTickTime(8, () -> {
            h.assertTrue(shots(h, player).size() == 3 && stack.getDamageValue() == 1, "remaining shots consume no additional durability");
            pass(h, "player use and scaled origin");
        });
    }
    @GameTest(structure = "reverie_dreams:player_trajectory_test_range", padding = 7, maxTicks = 35)
    public void ramp_shot(GameTestHelper h) {
        var owner = shooter(h, RAMP_SHOT);
        var shot = one(h, owner);
        double start = shot.getDanmakuMotion().velocity().length();
        h.runAtTickTime(5, () -> {
            h.assertTrue(start < 0.5 && shot.currentSpeed() > start + 0.20 && shot.currentSpeed() < 1.22,
                    "bounded acceleration");
            pass(h, RAMP_SHOT);
        });
    }

    @GameTest(structure = "reverie_dreams:player_trajectory_test_range", padding = 7, maxTicks = 35)
    public void lob_shot(GameTestHelper h) {
        var owner = shooter(h, LOB_SHOT);
        var shot = one(h, owner);
        double y = shot.getY();
        h.runAtTickTime(4, () -> h.assertTrue(shot.getY() > y + .7, "arc rises"));
        h.runAtTickTime(12, () -> {
            h.assertTrue(shot.getDeltaMovement().y < -.1, "arc drops " + state(shot));
            pass(h, LOB_SHOT);
        });
    }

    @GameTest(structure = "reverie_dreams:player_trajectory_test_range", padding = 7, maxTicks = 35)
    public void beat_burst(GameTestHelper h) {
        repeat(h, BEAT_BURST);
    }

    @GameTest(structure = "reverie_dreams:player_trajectory_test_range", padding = 7, maxTicks = 35)
    public void lane_sweep(GameTestHelper h) {
        repeat(h, LANE_SWEEP);
    }

    @GameTest(structure = "reverie_dreams:player_trajectory_test_range", padding = 7, maxTicks = 35)
    public void delayed_shots_cancel_on_swap(GameTestHelper h) {
        var owner = shooter(h, BEAT_BURST);
        one(h, owner);
        owner.setItemInHand(InteractionHand.MAIN_HAND, net.minecraft.world.item.ItemStack.EMPTY);
        h.runAtTickTime(9, () -> {
            h.assertTrue(shots(h, owner).size() == 1, "swapping cancels the two outstanding shots, actual=" + shots(h, owner).size());
            pass(h, "delayed swap");
        });
    }

    private void repeat(GameTestHelper h, PlayerTrajectoryType type) {
        var owner = shooter(h, type);
        one(h, owner);
        owner.setYRot(90);
        h.runAtTickTime(7, () -> {
            var rounds = shots(h, owner);
            h.assertTrue(rounds.size() == 3, "three timed rounds " + type + " actual=" + rounds.size() + " " + rounds.stream().map(PlayerTrajectoryGameTest::state).toList());
            float damage = (float) rounds.stream().mapToDouble(d -> d.getDanmakuProperties().damage()).sum();
            h.assertTrue(Math.abs(damage - 8) < .01, "shared damage allocation");
            var components = rounds.stream().mapToDouble(d -> d.getDanmakuMotion().velocity().x).sorted().toArray();
            if (type == LANE_SWEEP) {
                h.assertTrue(components[0] < -.1 && components[2] > .1, "sweep lanes diverge");
            } else {
                h.assertTrue(Math.abs(components[0]) < .01 && Math.abs(components[2]) < .01, "fixed aim");
            }
            pass(h, type);
        });
    }

    @GameTest(structure = "reverie_dreams:player_trajectory_test_range", padding = 7, maxTicks = 35)
    public void range_convergence(GameTestHelper h) {
        var owner = shooter(h, RANGE_CONVERGENCE);
        var pair = shots(h, owner);
        h.assertTrue(pair.size() == 2, "two convergence bullets");
        h.assertTrue(pair.stream().allMatch(d -> (d.getX() - owner.getX()) * d.getDanmakuMotion().velocity().x < 0
                && d.getDanmakuProperties().damage() == 4), "both aim inward with half damage");
        h.runAtTickTime(10, () -> {
            h.assertTrue(pair.stream().allMatch(d -> Math.abs(d.getX() - owner.getX()) < .28), "near the fixed intersection");
            pass(h, RANGE_CONVERGENCE);
        });
    }

    @GameTest(structure = "reverie_dreams:player_trajectory_test_range", padding = 7, maxTicks = 35)
    public void weave_shot(GameTestHelper h) {
        wave(h, WEAVE_SHOT);
    }

    @GameTest(structure = "reverie_dreams:player_trajectory_test_range", padding = 7, maxTicks = 35)
    public void corkscrew_shot(GameTestHelper h) {
        wave(h, CORKSCREW_SHOT);
    }

    private void wave(GameTestHelper h, PlayerTrajectoryType type) {
        var owner = shooter(h, type);
        var shot = one(h, owner);
        double x = shot.getX(), y = shot.getY();
        h.runAtTickTime(4, () -> {
            h.assertTrue(shot.getX() > x + .15, "first wave lobe");
            if (type == CORKSCREW_SHOT) {
                h.assertTrue(shot.getY() > y + .12, "corkscrew also changes height");
            }
        });
        h.runAtTickTime(10, () -> {
            h.assertTrue(shot.getX() < x - .10, "opposite wave lobe");
            pass(h, type);
        });
    }

    @GameTest(structure = "reverie_dreams:player_trajectory_test_range", padding = 7, maxTicks = 45)
    public void return_arc(GameTestHelper h) {
        var owner = shooter(h, RETURN_ARC);
        var shot = one(h, owner);
        h.runAtTickTime(5, () -> h.assertTrue(shot.getDeltaMovement().z > .3, "outbound"));
        h.runAtTickTime(20, () -> {
            h.assertTrue(shot.getDeltaMovement().z < -.3 && Math.abs(shot.getX() - owner.getX()) > .8,
                    "return leg has its own lane " + state(shot));
            pass(h, RETURN_ARC);
        });
    }

    @GameTest(structure = "reverie_dreams:player_trajectory_test_range", padding = 7, maxTicks = 35)
    public void wall_ricochet(GameTestHelper h) {
        h.setBlock(12, 3, 4, Blocks.STONE);
        h.setBlock(12, 3, 0, Blocks.STONE);
        var owner = shooter(h, WALL_RICOCHET);
        var shot = one(h, owner);
        h.runAtTickTime(5, () -> {
            h.assertTrue(shot.isAlive() && shot.getDeltaMovement().z < -.35
                    && shot.getDanmakuProperties().damage() < 8, "actual solid wall reflects once with damping");
        });
        h.runAtTickTime(11, () -> {
            h.assertTrue(shot.isRemoved(), "second wall must not reflect again");
            pass(h, WALL_RICOCHET);
        });
    }

    @GameTest(structure = "reverie_dreams:player_trajectory_test_range", padding = 7, maxTicks = 35)
    public void needle_pierce(GameTestHelper h) {
        var first = enemy(h, new Vec3(12.5, 2, 3.8));
        var second = enemy(h, new Vec3(12.5, 2, 5.6));
        var owner = shooter(h, NEEDLE_PIERCE);
        var shot = one(h, owner);
        h.runAtTickTime(9, () -> {
            h.assertTrue(first.getHealth() < first.getMaxHealth() && second.getHealth() < second.getMaxHealth()
                    && shot.isRemoved(), "two different enemies receive real damage");
            pass(h, NEEDLE_PIERCE);
        });
    }

    @GameTest(structure = "reverie_dreams:player_trajectory_test_range", padding = 7, maxTicks = 35)
    public void star_fork(GameTestHelper h) {
        var owner = shooter(h, STAR_FORK);
        var parent = one(h, owner);
        h.runAtTickTime(10, () -> {
            var children = shots(h, owner);
            h.assertTrue(parent.isRemoved() && children.size() == 2, "parent consumed by two children, parent=" + state(parent) + " children=" + children.stream().map(PlayerTrajectoryGameTest::state).toList());
            h.assertTrue(children.stream().allMatch(child -> child.getDanmakuProperties().damage() == 4), "child damage shared");
        });
        h.runAtTickTime(17, () -> {
            h.assertTrue(shots(h, owner).size() == 2, "children never split again: " + shots(h, owner).stream().map(PlayerTrajectoryGameTest::state).toList());
            pass(h, STAR_FORK);
        });
    }

    @GameTest(structure = "reverie_dreams:player_trajectory_test_range", padding = 7, maxTicks = 35)
    public void blocked_fork_has_no_children(GameTestHelper h) {
        h.setBlock(12, 3, 4, Blocks.STONE);
        var owner = shooter(h, STAR_FORK);
        h.runAtTickTime(12, () -> {
            h.assertTrue(shots(h, owner).isEmpty(), "a blocked parent cannot spawn children behind a wall");
            pass(h, "blocked fork");
        });
    }

    @GameTest(structure = "reverie_dreams:player_trajectory_test_range", padding = 7, maxTicks = 35)
    public void plunge_shot(GameTestHelper h) {
        var owner = shooter(h, PLUNGE_SHOT);
        var shot = one(h, owner);
        h.runAtTickTime(5, () -> h.assertTrue(shot.getDeltaMovement().y > .4, "visible ascent"));
        h.runAtTickTime(11, () -> {
            h.assertTrue(shot.getDeltaMovement().y < -.7, "steep descent " + state(shot));
            pass(h, PLUNGE_SHOT);
        });
    }

    @GameTest(structure = "reverie_dreams:player_trajectory_test_range", padding = 7, maxTicks = 45)
    public void ground_skimmer(GameTestHelper h) {
        floor(h, 12);
        var owner = shooter(h, GROUND_SKIMMER);
        var shot = one(h, owner);
        h.runAtTickTime(12, () -> {
            double ground = h.absoluteVec(new Vec3(12.5, 2.0, 1.5)).y;
            h.assertTrue(shot.isAlive() && Math.abs(shot.getY() - ground - .19) < .13
                    && shot.getDeltaMovement().z > .3, "slides just above actual block floor " + state(shot) + " floor=" + ground);
        });
        h.runAtTickTime(24, () -> {
            h.assertTrue(shot.isRemoved(), "terminates at the drop");
            pass(h, GROUND_SKIMMER);
        });
    }

    @GameTest(structure = "reverie_dreams:player_trajectory_test_range", padding = 7, maxTicks = 45)
    public void proximity_seed(GameTestHelper h) {
        floor(h, 9);
        var owner = shooter(h, PROXIMITY_SEED);
        var previous = one(h, owner);
        fire(owner);
        h.assertTrue(previous.isRemoved(), "second cast clears the first seed");
        var seed = one(h, owner);
        h.runAtTickTime(11, () -> {
            h.assertTrue(seed.isAlive() && seed.getDeltaMovement().lengthSqr() < 1.0E-6,
                    "seed remains settled while the area is empty: " + state(seed));
            var target = enemy(h, new Vec3(13.75, 2, 4.8));
            h.runAtTickTime(21, () -> {
                h.assertTrue(seed.isRemoved() && target.getHealth() <= target.getMaxHealth() - 2,
                        "on-ground seed triggers once and strikes " + state(seed) + " health=" + target.getHealth());
                pass(h, PROXIMITY_SEED);
            });
        });
    }

    @GameTest(structure = "reverie_dreams:player_trajectory_test_range", padding = 7, maxTicks = 35)
    public void short_guidance(GameTestHelper h) {
        enemy(h, new Vec3(13.48, 2, 7.4));
        var owner = shooter(h, SHORT_GUIDANCE);
        var shot = one(h, owner);
        h.runAtTickTime(4, () -> {
            h.assertTrue(shot.isAlive() && shot.currentDirection().x > .02 && shot.currentDirection().x < .35,
                    "bounded correction toward eligible hostile target");
            pass(h, SHORT_GUIDANCE);
        });
    }

    @GameTest(structure = "reverie_dreams:player_trajectory_test_range", padding = 7, maxTicks = 35)
    public void intercept_shot(GameTestHelper h) {
        var enemy = enemy(h, new Vec3(14.5, 2, 5.5));
        var owner = shooter(h, INTERCEPT_SHOT);
        var shot = one(h, owner);
        var position = shot.position();
        var incoming = DanmakuSpawner.spawn(h.getLevel(), enemy, position.add(0, 0, 3), 0, 180,
                DanmakuBulletSpec.of(DanmakuTypes.BALL.getId(), .04f).withInterceptable(true), 1);
        var protectedShot = DanmakuSpawner.spawn(h.getLevel(), enemy, position.add(0, 0, 1.5), 0, 180,
                DanmakuBulletSpec.of(DanmakuTypes.BALL.getId(), .04f), 1);
        h.assertTrue(incoming != null && protectedShot != null && incoming.isTrajectoryInterceptable()
                && !protectedShot.isTrajectoryInterceptable(), "explicit opt-in");
        h.runAtTickTime(6, () -> {
            h.assertTrue(shot.isRemoved() && incoming.isRemoved() && protectedShot.isAlive(),
                    "one-for-one interception and default protection");
            pass(h, INTERCEPT_SHOT);
        });
    }

    private static LivingEntity shooter(GameTestHelper helper, PlayerTrajectoryType type) {
        var caster = helper.spawn(EntityType.ARMOR_STAND, new Vec3(12.5, 2, 1.5));
        caster.setNoGravity(true);
        caster.setYRot(0);
        caster.setXRot(0);
        var stack = DanmakuTypes.BALL.getItemHolder().createStack();
        var component = RDDataComponentTypes.DANMAKU_PROPERTIES.value();
        stack.set(component, stack.get(component).withTemplateId(type.id()).withSpeed(1f).withDamage(8f));
        caster.setItemInHand(InteractionHand.MAIN_HAND, stack);
        fire(caster);
        return caster;
    }

    private static void fire(LivingEntity owner) {
        var held = owner.getItemInHand(InteractionHand.MAIN_HAND);
        ((DanmakuItem) held.getItem()).shoot((net.minecraft.server.level.ServerLevel) owner.level(), owner, InteractionHand.MAIN_HAND);
    }

    private static LivingEntity enemy(GameTestHelper helper, Vec3 point) {
        var enemy = helper.spawnWithNoFreeWill(EntityType.HUSK, point);
        enemy.setNoGravity(true);
        return enemy;
    }

    private static List<DanmakuEntity> shots(GameTestHelper helper, LivingEntity owner) {
        return helper.getLevel().getEntitiesOfClass(DanmakuEntity.class, AABB.ofSize(owner.position(), 56, 56, 56),
                bullet -> bullet.getOwner() == owner);
    }

    private static DanmakuEntity one(GameTestHelper helper, LivingEntity owner) {
        var bullets = shots(helper, owner);
        helper.assertTrue(bullets.size() == 1, "one bullet expected for this test, got " + bullets.size());
        return bullets.getFirst();
    }

    private static void floor(GameTestHelper helper, int through) {
        for (int z = 0; z <= through; z++) {
            for (int x = 10; x <= 15; x++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            }
        }
    }

    private static String state(DanmakuEntity shot) {
        return "age=" + shot.tickCount + " pos=" + shot.position() + " vel=" + shot.getDeltaMovement()
                + " removed=" + shot.getRemovalReason();
    }
    private static void pass(GameTestHelper helper, PlayerTrajectoryType type) {
        pass(helper, type.name().toLowerCase(Locale.ROOT));
    }

    private static void pass(GameTestHelper helper, String name) {
        System.out.println("PLAYER TRAJECTORY PASSED: " + name);
        helper.succeed();
    }
}
