package cc.thonly.reverie_dreams.item.weapon;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Map;

/**
 * Vanilla sound layers give each numbered gun a distinct report, without client-only audio.
 */
public final class LunarWeaponSounds {
    public record Layer(Identifier event, float volume, float pitch) {
        public Layer {
            if (event == null || !Float.isFinite(volume) || volume <= 0 || volume > 1.5f
                    || !Float.isFinite(pitch) || pitch < .5f || pitch > 2) {
                throw new IllegalArgumentException("Invalid lunar firing sound layer");
            }
        }
    }

    private static final Map<LunarWeaponType, List<Layer>> PROFILES = Map.of(
            LunarWeaponType.RIFLE, List.of(
                    layer("entity.firework_rocket.blast", .72f, 1.65f),
                    layer("item.crossbow.shoot", .38f, 1.45f)),
            LunarWeaponType.PISTOL, List.of(
                    layer("entity.firework_rocket.blast", .50f, 1.95f),
                    layer("item.crossbow.shoot", .25f, 1.7f)),
            LunarWeaponType.SHOTGUN, List.of(
                    layer("entity.generic.explode", .75f, 1.6f),
                    layer("item.crossbow.shoot", .5f, .75f)),
            LunarWeaponType.SMG, List.of(
                    layer("block.note_block.hat", .32f, 1.15f),
                    layer("entity.firework_rocket.blast", .18f, 1.9f)),
            LunarWeaponType.SNIPER, List.of(
                    layer("entity.firework_rocket.large_blast", 1.2f, .85f),
                    layer("item.trident.throw", .3f, .65f),
                    layer("entity.firework_rocket.large_blast_far", .4f, .8f)));

    private LunarWeaponSounds() {
    }

    private static Layer layer(String name, float volume, float pitch) {
        return new Layer(Identifier.withDefaultNamespace(name), volume, pitch);
    }

    public static List<Layer> profile(LunarWeaponType type) {
        return PROFILES.get(type);
    }

    /**
     * Called once per cartridge, not once per shotgun pellet. No delayed audio tasks.
     */
    public static void play(ServerLevel world, LivingEntity owner, LunarWeaponType type) {
        if (owner.level() != world || !owner.isAlive())
            return;
        float variation = .98f + world.getRandom().nextFloat() * .04f;
        for (Layer layer : profile(type)) {
            SoundEvent event = BuiltInRegistries.SOUND_EVENT.getValue(layer.event());
            if (event != null) {
                world.playSound(null, owner.getX(), owner.getY(), owner.getZ(), event,
                        SoundSource.PLAYERS, layer.volume(), Math.min(2, layer.pitch() * variation));
            }
        }
    }
}
