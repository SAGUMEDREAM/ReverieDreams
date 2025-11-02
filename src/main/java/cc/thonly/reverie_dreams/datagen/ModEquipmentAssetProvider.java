package cc.thonly.reverie_dreams.datagen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.armor.*;
import cc.thonly.reverie_dreams.datagen.generator.EquipmentAssetProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ModEquipmentAssetProvider extends EquipmentAssetProvider {

    public ModEquipmentAssetProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future);
    }

    @Override
    protected void bootstrap(BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> consumer) {
        consumer.accept(EarphoneArmorMaterial.REGISTRY_KEY, createHumanoidAndHorseModel(ReverieDreams.id("earphone")));
        consumer.accept(KoishiHatArmorMaterial.REGISTRY_KEY, createHumanoidAndHorseModel(ReverieDreams.id("koishi_hat")));
        consumer.accept(SilverArmorMaterial.REGISTRY_KEY, createHumanoidAndHorseModel(ReverieDreams.id("silver")));
        consumer.accept(MagicIceArmorMaterial.REGISTRY_KEY, createHumanoidAndHorseModel(ReverieDreams.id("magic_ice")));
        consumer.accept(MaidArmorMaterial.REGISTRY_KEY, createHumanoidAndHorseModel(ReverieDreams.id("maid")));
        consumer.accept(DreamArmorMaterial.REGISTRY_KEY, createHumanoidAndHorseModel(ReverieDreams.id("dream")));
    }
}
