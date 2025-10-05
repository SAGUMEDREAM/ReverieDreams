package cc.thonly.reverie_dreams.registry;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.damage.DanmakuDamageType;
import cc.thonly.reverie_dreams.damage.DanmakuDamageTypes;
import cc.thonly.reverie_dreams.danmaku.*;
import cc.thonly.reverie_dreams.engine.JavaScriptElement;
import cc.thonly.reverie_dreams.engine.JavaScriptManager;
import cc.thonly.reverie_dreams.entity.npc.*;
import cc.thonly.reverie_dreams.entity.skin.*;
import cc.thonly.reverie_dreams.entity.variant.YouseiVariant;
import cc.thonly.reverie_dreams.entity.variant.YouseiVariants;
import cc.thonly.reverie_dreams.fumo.Fumo;
import cc.thonly.reverie_dreams.fumo.Fumos;
import cc.thonly.reverie_dreams.item.RoleCards;
import cc.thonly.reverie_dreams.item.builder.RoleCard;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.util.Identifier;

import java.util.Map;

@Slf4j
@SuppressWarnings("unchecked")
public class RegistryManager {
    public static final Map<RegistryKey<? extends Registry<?>>, IntrinsicalRegister<?>> ROOT = new Object2ObjectLinkedOpenHashMap<>();
    public static final IntrinsicalRegister<BaseRecipeType<?>> RECIPE_TYPE = RegistryManager.<BaseRecipeType<?>>ofEntry(Touhou.id("recipe_type"));

    public static final IntrinsicalRegister<DanmakuType> DANMAKU_TYPE = RegistryManager.<DanmakuType>ofEntry(Touhou.id("danmaku_type"))
            .codec(DanmakuType.CODEC)
            .builder(DanmakuTypes::bootstrap);

    public static final IntrinsicalRegister<DanmakuShape> DANMAKU_SHAPE = RegistryManager.<DanmakuShape>ofEntry(Touhou.id("danmaku_shape"))
            .codec(DanmakuShape.CODEC)
            .builder(DanmakuShapes::bootstrap);

    public static final IntrinsicalRegister<DanmakuTrajectory> DANMAKU_TRAJECTORY = RegistryManager.<DanmakuTrajectory>ofEntry(Touhou.id("danmaku_trajectory"))
            .codec(DanmakuTrajectory.CODEC)
            .builder(DanmakuTrajectories::bootstrap);

    public static final IntrinsicalRegister<DanmakuDamageType> DANMAKU_DAMAGE_TYPE = RegistryManager.<DanmakuDamageType>ofEntry(Touhou.id("danmaku_damage_type"))
            .codec(DanmakuDamageType.CODEC)
            .defaultId(DanmakuDamageType.DEFAULT_ID)
            .builder(DanmakuDamageTypes::bootstrap);

    public static final IntrinsicalRegister<JavaScriptElement> JAVASCRIPT_ELEMENT = RegistryManager.<JavaScriptElement>ofEntry(Touhou.id("javascript_element"))
            .codec(JavaScriptElement.CODEC)
            .reloadBuilder(JavaScriptManager::reload)
            .builder(JavaScriptManager::bootstrap);

    public static final IntrinsicalRegister<NPCSkin> ROLE_SKIN = RegistryManager.<NPCSkin>ofEntry(Touhou.id("role_skin"))
            .codec(NPCSkin.UNIT_CODEC)
            .builder(RoleSkins::bootstrap, MobSkins::bootstrap);

    public static final IntrinsicalRegister<NPCSkinConfig> SKIN_CONFIG = RegistryManager.<NPCSkinConfig>ofEntry(Touhou.id("skin_config"))
            .codec(NPCSkinConfig.CODEC)
            .reloadBuilder(NPCSkinConfigs::reload)
            .builder(NPCSkinConfigs::bootstrap);

    public static final IntrinsicalRegister<NPCRole> NPC_ROLE = RegistryManager.<NPCRole>ofEntry(Touhou.id("npc_role"))
            .codec(NPCRole.CODEC)
            .builder(NPCRoles::bootstrap);

    public static final IntrinsicalRegister<RoleCard> ROLE_CARD = RegistryManager.<RoleCard>ofEntry(Touhou.id("role_card"))
            .codec(RoleCard.CODEC)
            .builder(RoleCards::bootstrap);

    public static final IntrinsicalRegister<NPCRoleInteractionEvent> ROLE_INTERACTION_EVENT = RegistryManager.<NPCRoleInteractionEvent>ofEntry(Touhou.id("interaction_event"))
            .codec(NPCRoleInteractionEvent.CODEC)
            .builder(NPCRoleInteractionEvents::bootstrap);

    public static final IntrinsicalRegister<NPCState> NPC_STATE = RegistryManager.<NPCState>ofEntry(Touhou.id("npc_state"))
            .codec(NPCState.CODEC)
            .defaultId(NPCState.DEFAULT_ID)
            .builder(NPCStates::bootstrap);

    public static final IntrinsicalRegister<NPCWorkMode> NPC_WORK_MODE = RegistryManager.<NPCWorkMode>ofEntry(Touhou.id("npc_work_mode"))
            .codec(NPCWorkMode.CODEC)
            .defaultId(NPCWorkMode.DEFAULT_ID)
            .builder(NPCWorkModes::bootstrap);

    public static final IntrinsicalRegister<Fumo> FUMO = RegistryManager.<Fumo>ofEntry(Touhou.id("fumo"))
            .codec(Fumo.CODEC)
            .builder(Fumos::bootstrap);

    public static final IntrinsicalRegister<YouseiVariant> YOUSEI_VARIANT = RegistryManager.<YouseiVariant>ofEntry(Touhou.id("yousei_variant"))
            .codec(YouseiVariant.CODEC)
            .defaultId(Touhou.id("blue"))
            .builder(YouseiVariants::bootstrap);

    public static void bootstrap() {
        for (var entry : ROOT.entrySet()) {
            IntrinsicalRegister<?> registry = entry.getValue();
            registry.build();
        }
    }

    public static <T> T registerForBuiltin(IntrinsicalRegister<T> registry, Identifier key, T value) {
        register(registry, key, value);
        registry.setBuiltin(key, value);
        return value;
    }

    public static <T> T register(IntrinsicalRegister<T> registry, Identifier key, T value) {
        registry.add(RegistryKey.of(registry.getKey(), key), value, RegistryEntryInfo.DEFAULT);
        return value;
    }

    public static <T> T set(IntrinsicalRegister<T> registry, Identifier key, T value) {
        registry.set(RegistryKey.of(registry.getKey(), key), value, RegistryEntryInfo.DEFAULT);
        return value;
    }

    public static <T> IntrinsicalRegister<T> ofEntry(Identifier identifier) {
        return ofEntry(RegistryKey.ofRegistry(identifier));
    }

    public static <T> IntrinsicalRegister<T> ofEntry(RegistryKey<? extends Registry<T>> key) {
        if (ROOT.containsKey(key)) {
            return (IntrinsicalRegister<T>) ROOT.get(key);
        }
        IntrinsicalRegister<T> intrinsicalRegister = new IntrinsicalRegister<>(key);
        ROOT.put(key, intrinsicalRegister);
        return intrinsicalRegister;
    }
}
