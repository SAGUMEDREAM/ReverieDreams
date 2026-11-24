package cc.thonly.reverie_dreams.loot;

import cc.thonly.reverie_dreams.api.entity.type.YouseiType;
import cc.thonly.reverie_dreams.entity.Goblin;
import cc.thonly.reverie_dreams.util.item.ItemUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.gamerules.GameRules;

/**
 * Shared by native clients and Polymer. Called after committed death loot.
 */
public final class CoinDrops {
    private CoinDrops() {
    }

    public static boolean allowsSource(LivingEntity victim, DamageSource source) {
        Entity attacker = source.getEntity();
        if (attacker == null && source.getDirectEntity() instanceof Projectile projectile) {
            attacker = projectile.getOwner();
        }
        if (attacker instanceof Player)
            return true;
        if (attacker instanceof LivingEntity)
            return false;

        // Mob knockback into hazards is not a pure environmental death.
        LivingEntity previous = victim.getLastHurtByMob();
        return previous == null || previous instanceof Player
                || victim.tickCount - victim.getLastHurtByMobTimestamp() > 100;
    }

    public static CoinLootTier tier(LivingEntity victim) {
        if (victim instanceof Player || victim.isBaby()
                || victim instanceof TamableAnimal pet && pet.isTame())
            return CoinLootTier.NONE;
        var id = BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType());
        String path = id.getPath();
        if (id.getNamespace().equals("reverie_dreams")) {
            if (victim instanceof YouseiType || victim instanceof Goblin)
                return CoinLootTier.FAIRY;
            return switch (path) {
                case "oni", "crow_tengu", "white_wolf_tengu", "rabbit_unit" -> CoinLootTier.ELITE;
                case "hairball" -> CoinLootTier.SMALL;
                default -> CoinLootTier.NONE;
            };
        }
        if (victim.getType().getCategory() != MobCategory.MONSTER)
            return CoinLootTier.NONE;
        // Reward only the last generation of a slime's split tree.
        if (victim instanceof Slime slime)
            return slime.getSize() == 1 ? CoinLootTier.SMALL : CoinLootTier.NONE;
        if (!id.getNamespace().equals("minecraft"))
            return CoinLootTier.COMMON;
        return switch (path) {
            case "silverfish", "endermite" -> CoinLootTier.SMALL;
            case "wither" -> CoinLootTier.WITHER;
            case "warden" -> CoinLootTier.WARDEN;
            case "wither_skeleton", "blaze", "witch", "evoker", "vindicator", "ravager",
                 "piglin_brute", "guardian", "elder_guardian", "shulker", "breeze" -> CoinLootTier.ELITE;
            default -> CoinLootTier.COMMON;
        };
    }

    public static void drop(ServerLevel level, LivingEntity victim, DamageSource source) {
        if (!level.getGameRules().get(GameRules.MOB_DROPS) || !allowsSource(victim, source))
            return;
        int value = tier(victim).roll(level.getRandom());
        for (var stack : ItemUtils.calculateCoins(value))
            victim.spawnAtLocation(level, stack);
    }
}
