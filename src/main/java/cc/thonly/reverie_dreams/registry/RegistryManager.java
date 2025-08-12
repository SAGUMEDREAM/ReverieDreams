package cc.thonly.reverie_dreams.registry;

import cc.thonly.mystias_izakaya.component.FoodProperty;
import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.engine.JavaScriptElement;
import cc.thonly.reverie_dreams.engine.JavaScriptManager;
import cc.thonly.reverie_dreams.entity.npc.*;
import cc.thonly.reverie_dreams.fumo.Fumo;
import cc.thonly.reverie_dreams.fumo.Fumos;
import cc.thonly.reverie_dreams.damage.DanmakuDamageType;
import cc.thonly.reverie_dreams.damage.DanmakuDamageTypes;
import cc.thonly.reverie_dreams.danmaku.DanmakuTrajectories;
import cc.thonly.reverie_dreams.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.entity.skin.MobSkins;
import cc.thonly.reverie_dreams.entity.skin.RoleSkin;
import cc.thonly.reverie_dreams.entity.skin.RoleSkins;
import cc.thonly.reverie_dreams.entity.variant.YouseiVariant;
import cc.thonly.reverie_dreams.entity.variant.YouseiVariants;
import cc.thonly.reverie_dreams.item.RoleCard;
import cc.thonly.reverie_dreams.item.RoleCards;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@SuppressWarnings("unchecked")
public class RegistryManager {
    public static final Map<Identifier, StandaloneRegistry<?>> REGISTRIES = new Object2ObjectLinkedOpenHashMap<>();
    public static final StandaloneRegistry<DanmakuType> DANMAKU_TYPE = ofEntry(DanmakuType.class, Touhou.id("danmaku_type"))
            .codec(DanmakuType.CODEC)
            .build(DanmakuTypes::bootstrap);
    public static final StandaloneRegistry<DanmakuTrajectory> DANMAKU_TRAJECTORY = ofEntry(DanmakuTrajectory.class, Touhou.id("danmaku_trajectory"))
            .codec(DanmakuTrajectory.CODEC)
            .build(DanmakuTrajectories::bootstrap);
    public static final StandaloneRegistry<DanmakuDamageType> DANMAKU_DAMAGE_TYPE = ofEntry(DanmakuDamageType.class, Touhou.id("danmaku_damage_type"))
            .codec(DanmakuDamageType.CODEC)
            .build(DanmakuDamageTypes::bootstrap);
    public static final StandaloneRegistry<JavaScriptElement> JAVASCRIPT_ELEMENT = ofEntry(JavaScriptElement.class, Touhou.id("javascript_element"))
            .codec(JavaScriptElement.CODEC)
            .reloadable(JavaScriptManager::reload)
            .build(JavaScriptManager::bootstrap);
    public static final StandaloneRegistry<RoleSkin> ROLE_SKIN = ofEntry(RoleSkin.class, Touhou.id("role_skin"))
            .codec(RoleSkin.CODEC)
            .build(RoleSkins::bootstrap, MobSkins::bootstrap);
    public static final StandaloneRegistry<NPCRole> NPC_ROLE = ofEntry(NPCRole.class, Touhou.id("npc_role"))
            .codec(NPCRole.CODEC)
            .build(NPCRoles::bootstrap);
    public static final StandaloneRegistry<RoleCard> ROLE_CARD = ofEntry(RoleCard.class, Touhou.id("role_card"))
            .codec(RoleCard.CODEC)
            .build(RoleCards::bootstrap);
    public static final StandaloneRegistry<NPCState> NPC_STATE = ofEntry(NPCState.class, Touhou.id("npc_state"))
            .codec(NPCState.CODEC)
            .build(NPCStates::bootstrap);
    public static final StandaloneRegistry<NPCWorkMode> NPC_WORK_MODE = ofEntry(NPCWorkMode.class, Touhou.id("npc_work_mode"))
            .codec(NPCWorkMode.CODEC)
            .build(NPCWorkModes::bootstrap);
    public static final StandaloneRegistry<Fumo> FUMO = ofEntry(Fumo.class, Touhou.id("fumo"))
            .codec(Fumo.CODEC)
            .build(Fumos::bootstrap);
    public static final StandaloneRegistry<YouseiVariant> YOUSEI_VARIANT = ofEntry(YouseiVariant.class, Touhou.id("yousei_variant"))
            .codec(YouseiVariant.CODEC)
            .defaultEntry(() -> YouseiVariants.BLUE)
            .build(YouseiVariants::bootstrap);

    public static void bootstrap() {
        for (var entry : REGISTRIES.entrySet()) {
            StandaloneRegistry<?> standaloneRegistry = entry.getValue();
            standaloneRegistry.apply();
        }
    }

    public static <T extends RegistrableObject<T>> T register(StandaloneRegistry<T> registry, Identifier key, T value) {
        return registry.add(key, value);
    }

    public static <T extends RegistrableObject<T>> T registerFinal(StandaloneRegistry<T> registry, Identifier key, Supplier<T> builder) {
        return registerFinal(registry, key, builder.get());
    }

    public static <T extends RegistrableObject<T>> T registerFinal(StandaloneRegistry<T> registry, Identifier key, T value) {
        T added = registry.add(key, value);
        Map<Identifier, T> fir = registry.getFinalIdToEntry();
        fir.put(key, value);
        return added;
    }

    public static <T extends RegistrableObject<T>> Map<Identifier, T> registerAll(StandaloneRegistry<T> registry, Map<Identifier, T> idToEntry) {
        return registry.add(idToEntry);
    }

    public static <T extends RegistrableObject<T>> T set(StandaloneRegistry<T> registry, Identifier key, T value) {
        return registry.set(key, value);
    }

    public static <T extends RegistrableObject<T>> StandaloneRegistry<T> ofEntry(Class<T> type, Identifier key) {
        StandaloneRegistry<?> rawEntry = REGISTRIES.get(key);
        if (rawEntry != null) {
            return (StandaloneRegistry<T>) rawEntry;
        } else {
            StandaloneRegistry<T> newEntry = new StandaloneRegistry<>(key);
            REGISTRIES.put(key, newEntry);
            return newEntry;
        }
    }

    public static <T extends RegistrableObject<T>> StandaloneRegistry<T> getRegistry(Class<T> type, Identifier key) {
        return (StandaloneRegistry<T>) REGISTRIES.get(key);
    }

}
