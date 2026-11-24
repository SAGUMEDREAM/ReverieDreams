package cc.thonly.reverie_dreams.paper.danmaku;

import cc.thonly.reverie_dreams.paper.util.Ids;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.tags.DamageTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.damage.DamageEffect;
import org.bukkit.damage.DamageScaling;
import org.bukkit.damage.DamageType;
import org.bukkit.damage.DeathMessageType;

import java.util.List;

/** Paper74 registry lifecycle API; deliberately has no CraftEngine or Minecraft class references. */
@SuppressWarnings("UnstableApiUsage")
public final class DanmakuBootstrap {
    public static final TypedKey<DamageType> GENERIC = TypedKey.create(RegistryKey.DAMAGE_TYPE, Ids.MOD_ID + ":danmaku_generic");
    public static final TypedKey<DamageType> REAL = TypedKey.create(RegistryKey.DAMAGE_TYPE, Ids.MOD_ID + ":danmaku_real");
    private DanmakuBootstrap() {}
    public static void configure(BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(RegistryEvents.DAMAGE_TYPE.compose().newHandler(event -> {
            for (var key : List.of(GENERIC, REAL)) event.registry().register(key, builder -> builder.messageId("danmaku")
                    .exhaustion(.1f).damageScaling(DamageScaling.NEVER).damageEffect(DamageEffect.HURT).deathMessageType(DeathMessageType.DEFAULT));
        }));
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.DAMAGE_TYPE).newHandler(event -> {
            var registrar = event.registrar();
            registrar.addToTag(DamageTypeTagKeys.IS_PROJECTILE, List.of(GENERIC, REAL));
            registrar.addToTag(DamageTypeTagKeys.NO_KNOCKBACK, List.of(GENERIC, REAL));
            registrar.addToTag(DamageTypeTagKeys.BYPASSES_ARMOR, List.of(REAL));
            registrar.addToTag(TagKey.create(RegistryKey.DAMAGE_TYPE, Ids.MOD_ID + ":danmaku_hit"), List.of(GENERIC, REAL));
        }));
    }
}
