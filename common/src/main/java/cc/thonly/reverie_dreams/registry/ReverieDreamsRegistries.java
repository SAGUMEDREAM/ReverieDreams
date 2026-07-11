package cc.thonly.reverie_dreams.registry;

import cc.thonly.keine.api.KeineAPI;
import cc.thonly.keine.api.KeineRegistries;
import cc.thonly.reverie_dreams.ReverieDreams;
import com.google.common.base.Suppliers;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrarManager;
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
import java.util.function.Supplier;

@SuppressWarnings("rawtypes")
public class ReverieDreamsRegistries {
    public static final List<DeferredRegister> REGISTERS = new ArrayList<>();
    public static final KeineRegistries KEINE_REGISTRIES = KeineAPI.getApi().getRegistries(ReverieDreams.MOD_ID);
    public static final Supplier<RegistrarManager> REGISTRIES = Suppliers.memoize(() -> RegistrarManager.get(ReverieDreams.MOD_ID));
    public static final DeferredRegister<SoundEvent> SOUND_EVENT = get(Registries.SOUND_EVENT);
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPE = get(Registries.DATA_COMPONENT_TYPE);
    public static final DeferredRegister<Item> ITEM = get(Registries.ITEM);
    public static final DeferredRegister<Block> BLOCK = get(Registries.BLOCK);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPE = get(Registries.ENTITY_TYPE);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE = get(Registries.BLOCK_ENTITY_TYPE);
    public static final DeferredRegister<MobEffect> MOB_EFFECT = get(Registries.MOB_EFFECT);
    public static final DeferredRegister<Potion> POTION = get(Registries.POTION);
    public static final DeferredRegister<PoiType> POI_TYPE = get(Registries.POINT_OF_INTEREST_TYPE);
    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSION = get(Registries.VILLAGER_PROFESSION);
    public static final DeferredRegister<CriterionTrigger<?>> CRITERION_TRIGGER = get(Registries.TRIGGER_TYPE);
    public static final DeferredRegister<GameRule<?>> GAME_RULE = get(Registries.GAME_RULE);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = get(Registries.CREATIVE_MODE_TAB);
    public static final DeferredRegister<Feature<?>> FEATURE = get(Registries.FEATURE);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER = get(Registries.RECIPE_SERIALIZER);

    public static void register() {
        for (var register : REGISTERS) {
            register.register();
        }
    }

    public static <T> DeferredRegister<T> get(ResourceKey<Registry<T>> key) {
        DeferredRegister<T> deferredRegister = DeferredRegister.create(ReverieDreams.MOD_ID, key);
        REGISTERS.add(deferredRegister);
        return deferredRegister;
    }
}
