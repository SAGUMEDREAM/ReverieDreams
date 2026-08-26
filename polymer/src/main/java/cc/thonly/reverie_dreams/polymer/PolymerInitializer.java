package cc.thonly.reverie_dreams.polymer;

import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.data.npc.NPCRoleType;
import cc.thonly.reverie_dreams.data.npc.RoleType;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.polymer.mixin.FabricEntityDataRegistryImplAccessor;
import cc.thonly.reverie_dreams.proxy.GuidebookFactory;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.polymer.callback.PolymerEntityGetterCallback;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import cc.thonly.reverie_dreams.creative_tab.content.ItemGroupContentHelper;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.fabric.ReverieDreamsFabric;
import cc.thonly.reverie_dreams.polymer.helper.*;
import cc.thonly.reverie_dreams.registry.content.villager.RDPointOfInterestTypes;
import cc.thonly.reverie_dreams.polymer.item.PolymerTHGuideBookItem;
import cc.thonly.reverie_dreams.item.base.ColoredSpawnEggItem;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.advancements.RDCriteriaTriggers;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.effect.RDPotions;
import cc.thonly.reverie_dreams.registry.content.effect.RDStatusEffects;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiPlaceholderItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.sound.RDSoundEvents;
import com.geckolib.GeckoLibConstants;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.item.PolymerCreativeModeTabUtils;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import eu.pb4.polymer.core.api.other.PolymerPotion;
import eu.pb4.polymer.core.api.other.PolymerSoundEvent;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.extras.api.ResourcePackExtras;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jspecify.annotations.NonNull;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.Iterator;
import java.util.function.Function;

@SuppressWarnings("unused")
@Slf4j
public class PolymerInitializer {
    public static final String POLYMER_MOD_ID = "reverie_dreams_polymerify";

    public static void bootstrap() {
        PolymerInitializer.polymerify();
    }

    public static void replaceGuidebook() {
        GuidebookFactory.EVENT.register(PolymerTHGuideBookItem::new);
    }

    @SuppressWarnings("rawtypes")
    public static void polymerify() {
        PolymerEntityGetterCallback.EVENT.register(PolymerEntity::get);
        for (Holder<Block> holder : RDBlocks.HOLDERS) {
            PolymerBlockHelper.registerOverlay(holder.value());
        }
        for (Holder<DataComponentType> component : RDDataComponentTypes.COMPONENTS) {
            PolymerComponent.registerDataComponent(component.value());
        }
        for (Holder<Item> item : RDItems.LATE_POLYMERIFY_ITEM_LIST) {
            PolymerItemHelper.registerOverlay(item.value());
        }
        for (Item spawnEgg : ColoredSpawnEggItem.SPAWN_EGGS) {
            PolymerItemHelper.registerOverlay(spawnEgg);
        }
        for (Holder<Item> item : RDGuiPlaceholderItems.GUI_PLACEHOLDER_ITEM_LIST) {
            PolymerItemHelper.registerOverlay(item.value());
        }
        for (DanmakuType danmakuType : BuiltInRegistryProviders.DANMAKU_TYPE) {
            PolymerItemHelper.registerOverlay(danmakuType.getItemHolder().asItem());
        }
        for (Holder<SoundEvent> soundEvent : RDSoundEvents.SOUND_EVENTS) {
            PolymerSoundEvent.registerOverlay(soundEvent.value());
        }
        for (Holder<BlockEntityType> entity : RDBlockEntityTypes.ENTITIES) {
            PolymerBlockUtils.registerBlockEntity(entity.value());
        }
        for (Holder<MobEffect> registryEntry : RDStatusEffects.EFFECTS) {
            PolymerStatusEffectHelper.registerOverlay(registryEntry);
        }
        for (Holder<Potion> potion : RDPotions.POTIONS) {
            PolymerPotion.registerOverlay(potion, new PolymerPotion() {
                @Override
                public @NonNull Potion getPolymerReplacement(Potion potion, PacketContext context) {
                    return Potions.HEALING.value();
                }
            });
        }
        for (var entityType : RDEntityTypes.ENTITY_TYPES) {
            PolymerEntityUtils.registerType(entityType.value());
        }
        for (Holder<CriterionTrigger<?>> holder : RDCriteriaTriggers.LIST) {
            RegistrySyncUtils.setServerEntry(BuiltInRegistries.TRIGGER_TYPES, holder.value());
        }
        for (Tuple<ResourceKey<CreativeModeTab>, Function<CreativeModeTab.Builder, CreativeModeTab.Builder>> tuple : ItemGroupContentHelper.FABRIC_LATE_INIT) {
            ResourceKey<CreativeModeTab> key = tuple.getA();
            Function<CreativeModeTab.Builder, CreativeModeTab.Builder> builderFunction = tuple.getB();
            PolymerCreativeModeTabUtils.registerPolymerCreativeModeTab(key, builderFunction.apply(ItemGroupContentHelper.builder()).build());
        }
        PolymerComponent.registerDataComponent(GeckoLibConstants.STACK_ANIMATABLE_ID_COMPONENT.get());
        RegistrySyncUtils.setServerEntry(FabricEntityDataRegistryImplAccessor.getHandlerRegistry(), DanmakuProperties.SERIALIZER);
        RegistrySyncUtils.setServerEntry(FabricEntityDataRegistryImplAccessor.getHandlerRegistry(), SkinType.SERIALIZER);
        RegistrySyncUtils.setServerEntry(FabricEntityDataRegistryImplAccessor.getHandlerRegistry(), NPCRoleType.SERIALIZER);
        RegistrySyncUtils.setServerEntry(FabricEntityDataRegistryImplAccessor.getHandlerRegistry(), RoleType.SERIALIZER);
        RegistrySyncUtils.setServerEntry(FabricEntityDataRegistryImplAccessor.getHandlerRegistry(), IngredientStack.SERIALIZER);
        RegistrySyncUtils.setServerEntry(FabricEntityDataRegistryImplAccessor.getHandlerRegistry(), IngredientStack.LIST_SERIALIZER);

        RegistrySyncUtils.setServerEntry(BuiltInRegistries.RECIPE_SERIALIZER, RecipeManager.DANMAKU_DYE_RECIPE.value());
        RegistrySyncUtils.setServerEntry(BuiltInRegistries.POINT_OF_INTEREST_TYPE, BuiltInRegistries.POINT_OF_INTEREST_TYPE.getValue(RDPointOfInterestTypes.HAWKERS_KEY));
        RegistrySyncUtils.setServerEntry(BuiltInRegistries.POINT_OF_INTEREST_TYPE, BuiltInRegistries.POINT_OF_INTEREST_TYPE.getValue(RDPointOfInterestTypes.PRIEST_KEY));
        RegistrySyncUtils.setServerEntry(BuiltInRegistries.POINT_OF_INTEREST_TYPE, BuiltInRegistries.POINT_OF_INTEREST_TYPE.getValue(RDPointOfInterestTypes.MONEY_SHOP_CLERK_KEY));

        PolymerEntityHelper.bootstrap();
        PolymerVillagerProfessionHelper.bootstrap();

        Iterator<Runnable> rIterator = ReverieDreamsFabric.FABRIC_LATE_INIT.iterator();
        while (rIterator.hasNext()) {
            Runnable next = rIterator.next();
            next.run();
            rIterator.remove();
        }

        PolymerResourcePackUtils.addModAssets(ReverieDreams.MOD_ID);
        PolymerResourcePackUtils.addModAssets(POLYMER_MOD_ID);
        PolymerResourcePackUtils.markAsRequired();
        ResourcePackExtras.forDefault().addBridgedModelsFolder(
                ReverieDreams.id("block"),
                ReverieDreams.id("item"),
                ReverieDreams.id("entity"),
                ReverieDreams.id("font"),
                ReverieDreams.id("mob_effect")
        );
        ResourcePackExtras.forDefault().addBridgedModelsFolder(
                polymerifyId("block"),
                polymerifyId("item"),
                polymerifyId("entity"),
                polymerifyId("font"),
                ReverieDreams.id("mob_effect")
        );
        ResourcePackGenerator.registerEvent();
    }

    public static Identifier polymerifyId(String name) {
        return Identifier.fromNamespaceAndPath(POLYMER_MOD_ID, name);
    }
}
