package cc.thonly.reverie_dreams.registry;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.*;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuShape;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.SpellCardFrameConfig;
import cc.thonly.reverie_dreams.data.npc.*;
import cc.thonly.reverie_dreams.data.skin.CustomType;
import cc.thonly.reverie_dreams.data.skin.SkinConfig;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.engine.JavaScriptElement;
import cc.thonly.reverie_dreams.entity.skill.Skill;
import cc.thonly.reverie_dreams.entity.variant.*;
import cc.thonly.reverie_dreams.item.base.RoleCard;
import cc.thonly.reverie_dreams.util.trading.TradeSet;
import cc.thonly.reverie_dreams.util.trading.VillagerTrade;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import cc.thonly.reverie_dreams.data.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.data.npc.NPCState;
import cc.thonly.reverie_dreams.data.npc.NPCRoleType;
import cc.thonly.reverie_dreams.data.npc.NPCSimpleRoleType;

@SuppressWarnings("SpellCheckingInspection")
public class BuiltInRegistryProviderKeys {
    public static final ResourceKey<Registry<DanmakuType>> DANMAKU_TYPE = create("danmaku_type");
    public static final ResourceKey<Registry<DanmakuShape>> DANMAKU_SHAPE = create("danmaku_shape");
    public static final ResourceKey<Registry<DanmakuTrajectory>> DANMAKU_TRAJECTORY = create("danmaku_trajectory");
    public static final ResourceKey<Registry<SpellCardFrameConfig>> DANMAKU_CONFIG = create("danmaku_config");
    public static final ResourceKey<Registry<JavaScriptElement>> JAVASCRIPT_ELEMENT = create("javascript_element");
    public static final ResourceKey<Registry<SkinType>> SKIN_TYPE = create("skin_type");
    public static final ResourceKey<Registry<SkinConfig>> SKIN_CONFIG = create("skin_config");
    public static final ResourceKey<Registry<CustomType>> CUSTOM_SKIN_TYPE = create("custom_skin_type");
    public static final ResourceKey<Registry<NPCRoleType>> NPC_ROLE_TYPE = create("npc_role");
    public static final ResourceKey<Registry<NPCSimpleRoleType>> NPC_SIMPLE_ROLE = create("npc_simple_role");
    public static final ResourceKey<Registry<NPCMenuType>> NPC_MENU_TYPE = create("npc_menu_type");
    public static final ResourceKey<Registry<RoleCard>> ROLE_CARD = create("role_card");
    public static final ResourceKey<Registry<Skill<?>>> SKILL = create("skill");
    public static final ResourceKey<Registry<NPCLikeInteractionEvent>> NPCLIKE_INTERACTION_EVENT = create("interaction_event");
    public static final ResourceKey<Registry<NPCState>> NPC_STATE = create("npc_state");
    public static final ResourceKey<Registry<NPCWorkMode>> NPC_WORK_MODE = create("npc_work_mode");
    public static final ResourceKey<Registry<FumoType>> FUMO = create("fumo");
    public static final ResourceKey<Registry<YouseiVariant>> YOUSEI_VARIANT = create("yousei_variant");
    public static final ResourceKey<Registry<RabbitUnitVariant>> RABBIT_UNIT_VARIANT = create("rabbit_unit_variant");
    public static final ResourceKey<Registry<OniVariant>> ONI_VARIANT = create("oni_variant");
    public static final ResourceKey<Registry<FoodProperty>> FOOD_PROPERTY = create("food_property");
    public static final ResourceKey<Registry<BeverageProperty>> BEVERAGE_PROPERTY = create("beverage_property");
    public static final ResourceKey<Registry<CraftingConflict>> CRAFTING_CONFLICT = create("crafting_conflict");
    public static final ResourceKey<Registry<Customer>> CUSTOMER = create("customer");
    public static final ResourceKey<Registry<SkinType>> SKIN_TYPE_MERGED = create("skin_type");
    public static final ResourceKey<Registry<RoleType>> ROLE_TYPE_MERGED = create("role_type");

    public static final ResourceKey<Registry<VillagerTrade>> VILLAGER_TRADE = create("villager_trade");
    public static final ResourceKey<Registry<TradeSet>> TRADE_SET = create("trade_set");

    public static <T> ResourceKey<Registry<T>> create(String name) {
        return create(ReverieDreams.id(name));
    }

    public static <T> ResourceKey<Registry<T>> create(Identifier id) {
        return ResourceKey.createRegistryKey(id);
    }

}
