package cc.thonly.reverie_dreams.datagen;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.armor.*;
import cc.thonly.reverie_dreams.datagen.generator.EquipmentAssetProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ModEquipmentAssetProvider extends EquipmentAssetProvider {

    public ModEquipmentAssetProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> future) {
        super(output, future);
    }

    @Override
    protected void bootstrap(BiConsumer<RegistryKey<EquipmentAsset>, EquipmentModel> consumer) {
        consumer.accept(EarphoneArmorMaterial.REGISTRY_KEY, createHumanoidAndHorseModel(Touhou.id("earphone")));
        consumer.accept(KoishiHatArmorMaterial.REGISTRY_KEY, createHumanoidAndHorseModel(Touhou.id("koishi_hat")));
        consumer.accept(SilverArmorMaterial.REGISTRY_KEY, createHumanoidAndHorseModel(Touhou.id("silver")));
        consumer.accept(MagicIceArmorMaterial.REGISTRY_KEY, createHumanoidAndHorseModel(Touhou.id("magic_ice")));
        consumer.accept(MaidArmorMaterial.REGISTRY_KEY, createHumanoidAndHorseModel(Touhou.id("maid")));
        consumer.accept(DreamArmorMaterial.REGISTRY_KEY, createHumanoidAndHorseModel(Touhou.id("dream")));
    }
}
