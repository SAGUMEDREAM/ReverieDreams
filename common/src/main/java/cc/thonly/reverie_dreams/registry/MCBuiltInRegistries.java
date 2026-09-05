package cc.thonly.reverie_dreams.registry;

import cc.thonly.keine.api.KeineAPI;
import cc.thonly.keine.api.KeineRegistries;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import cc.thonly.reverie_dreams.util.trading.TradeSet;
import cc.thonly.reverie_dreams.util.trading.VillagerTrade;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.levelgen.feature.Feature;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class MCBuiltInRegistries {
    public static final List<DeferredDelegateRegister> REGISTERS = new ArrayList<>();
    public static final KeineRegistries KEINE_REGISTRIES = KeineAPI.getApi().getRegistries(ReverieDreams.MOD_ID);
    public static final DeferredDelegateRegister<SoundEvent> SOUND_EVENT = get(Registries.SOUND_EVENT);
    public static final DeferredDelegateRegister<DataComponentType<?>> DATA_COMPONENT_TYPE = get(Registries.DATA_COMPONENT_TYPE);
    public static final DeferredDelegateRegister<EntityType<?>> ENTITY_TYPE = get(Registries.ENTITY_TYPE);
    public static final DeferredDelegateRegister<Block> BLOCK = get(Registries.BLOCK);
    public static final DeferredDelegateRegister<Item> ITEM = get(Registries.ITEM);
    public static final DeferredDelegateRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE = get(Registries.BLOCK_ENTITY_TYPE);
    public static final DeferredDelegateRegister<MobEffect> MOB_EFFECT = get(Registries.MOB_EFFECT);
    public static final DeferredDelegateRegister<Potion> POTION = get(Registries.POTION);
    public static final DeferredDelegateRegister<PoiType> POI_TYPE = get(Registries.POINT_OF_INTEREST_TYPE);
    public static final DeferredDelegateRegister<VillagerProfession> VILLAGER_PROFESSION = get(Registries.VILLAGER_PROFESSION);
    public static final DeferredDelegateRegister<CriterionTrigger<?>> CRITERION_TRIGGER = get(Registries.TRIGGER_TYPE);
    public static final DeferredDelegateRegister<GameRule<?>> GAME_RULE = get(Registries.GAME_RULE);
    public static final DeferredDelegateRegister<CreativeModeTab> CREATIVE_MODE_TAB = get(Registries.CREATIVE_MODE_TAB);
    public static final DeferredDelegateRegister<Feature<?>> FEATURE = get(Registries.FEATURE);
    public static final DeferredDelegateRegister<RecipeSerializer<?>> RECIPE_SERIALIZER = get(Registries.RECIPE_SERIALIZER);

    public static final RegistryProvider<VillagerTrade> VILLAGER_TRADE =
            BuiltInRegistryProviders.ofEntry(BuiltInRegistryProviderKeys.VILLAGER_TRADE)
                    .codec(VillagerTrade.CODEC);
    public static final RegistryProvider<TradeSet> TRADE_SET =
            BuiltInRegistryProviders.ofEntry(BuiltInRegistryProviderKeys.TRADE_SET)
                    .codec(TradeSet.CODEC);

    public static void register() {
        for (var register : REGISTERS) {
            register.register();
        }
    }

    public static <T> DeferredDelegateRegister<T> get(ResourceKey<Registry<T>> key) {
        DeferredDelegateRegister<T> deferredRegister = DeferredDelegateRegister.create(ReverieDreams.MOD_ID, key);
        REGISTERS.add(deferredRegister);
        return deferredRegister;
    }
}
