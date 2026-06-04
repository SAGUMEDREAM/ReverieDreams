package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.armor.*;
import cc.thonly.reverie_dreams.fabric.datagen.generator.AbstractEquipmentAssetProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class EquipmentAssetProvider extends AbstractEquipmentAssetProvider {

    public EquipmentAssetProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future);
    }

    @Override
    protected void bootstrap(BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> consumer) {
        consumer.accept(EarphoneArmorMaterial.REGISTRY_KEY, createHumanoidAndHorseModel(ReverieDreams.id("earphone")));
        consumer.accept(KoishiHatArmorMaterial.REGISTRY_KEY, createHumanoidAndHorseModel(ReverieDreams.id("koishi_hat")));
        consumer.accept(LowGravityBootArmorMaterial.REGISTRY_KEY,createHumanoidAndHorseModel(ReverieDreams.id("low_gravity_boot")));
        consumer.accept(CrownOfTheUnderworldArmorMaterial.REGISTRY_KEY,createHumanoidAndHorseModel(ReverieDreams.id("crown_of_the_underworld")));
        consumer.accept(SilverArmorMaterial.REGISTRY_KEY, createHumanoidAndHorseModel(ReverieDreams.id("silver")));
        consumer.accept(MagicIceArmorMaterial.REGISTRY_KEY, createHumanoidAndHorseModel(ReverieDreams.id("magic_ice")));
        consumer.accept(MaidArmorMaterial.REGISTRY_KEY, createHumanoidAndHorseModel(ReverieDreams.id("maid")));
        consumer.accept(DreamArmorMaterial.REGISTRY_KEY, createHumanoidAndHorseModel(ReverieDreams.id("dream")));
        consumer.accept(WaterproofArmorMaterial.REGISTRY_KEY, EquipmentClientInfo.builder().addHumanoidLayers(ReverieDreams.id("waterproof"), true).addHumanoidLayers(ReverieDreams.id("waterproof_overlay"), false).addLayers(EquipmentClientInfo.LayerType.HORSE_BODY, EquipmentClientInfo.Layer.leatherDyeable(ReverieDreams.id("waterproof"), true)).build());
    }
}
