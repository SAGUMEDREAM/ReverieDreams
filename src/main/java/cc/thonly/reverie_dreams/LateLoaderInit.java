package cc.thonly.reverie_dreams;

import cc.thonly.polymer.*;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.entity.villager.RDPointOfInterestTypes;
import cc.thonly.reverie_dreams.item.base.SpawnEggItem;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.content.advancements.RDCriteriaTriggers;
import cc.thonly.reverie_dreams.registry.content.effect.RDPotions;
import cc.thonly.reverie_dreams.registry.content.effect.RDStatusEffects;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.other.PolymerSoundEvent;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.extras.api.ResourcePackExtras;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LateLoaderInit implements ModInitializer {
    public static final List<Runnable> LATE_INIT = new ArrayList<>();
    public static final String POLYMER_MOD_ID = "reverie_dreams_polymerify";

    @Override
    public void onInitialize() {
        this.polymerify();
    }

    public void polymerify() {
        for (Item item : RDItems.SIMPLE_LIST) {
            PolymerItemHelper.registerOverlay(item);
        }
        for (Item spawnEgg : SpawnEggItem.SPAWN_EGGS) {
            PolymerItemHelper.registerOverlay(spawnEgg);
        }
        for (Item item : RDGuiItems.GUI_ITEM_LIST) {
            PolymerItemHelper.registerOverlay(item);
        }
        for (DanmakuType danmakuType : RegistryHandlers.DANMAKU_TYPE) {
            PolymerItemHelper.registerOverlay(danmakuType.getItem());
        }
        for (SoundEvent soundEvent : SoundEventInit.SOUND_EVENTS) {
            PolymerSoundEvent.registerOverlay(soundEvent);
            RegistrySyncUtils.setServerEntry(BuiltInRegistries.SOUND_EVENT, soundEvent);
        }
        for (Holder<MobEffect> registryEntry : RDStatusEffects.EFFECTS) {
            PolymerStatusEffectHelper.registerOverlay(registryEntry);
        }
        for (Potion potion : RDPotions.POTIONS) {
            RegistrySyncUtils.setServerEntry(BuiltInRegistries.POTION, potion);
        }
        for (EntityType<?> entityType : RDEntityTypes.ENTITY_TYPES) {
            PolymerEntityUtils.registerType(entityType);
        }
        for (CriterionTrigger<?> trigger : RDCriteriaTriggers.LIST) {
            RegistrySyncUtils.setServerEntry(BuiltInRegistries.TRIGGER_TYPES, trigger);
        }
        RegistrySyncUtils.setServerEntry(BuiltInRegistries.POINT_OF_INTEREST_TYPE, BuiltInRegistries.POINT_OF_INTEREST_TYPE.getValue(RDPointOfInterestTypes.HAWKERS));
        RegistrySyncUtils.setServerEntry(BuiltInRegistries.POINT_OF_INTEREST_TYPE, BuiltInRegistries.POINT_OF_INTEREST_TYPE.getValue(RDPointOfInterestTypes.PRIEST));
        RegistrySyncUtils.setServerEntry(BuiltInRegistries.POINT_OF_INTEREST_TYPE, BuiltInRegistries.POINT_OF_INTEREST_TYPE.getValue(RDPointOfInterestTypes.MONEY_SHOP_CLERK));

        PolymerEntityHelper.bootstrap();
        PolymerVillagerProfessionHelper.bootstrap();

        Iterator<Runnable> rIterator = LATE_INIT.iterator();
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
                ReverieDreams.id("font")
        );
        ResourcePackExtras.forDefault().addBridgedModelsFolder(
                id("block"),
                id("item"),
                id("entity"),
                id("font")
        );
        ResourcePackGenerator.registerEvent();
    }

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(POLYMER_MOD_ID, name);
    }
}
