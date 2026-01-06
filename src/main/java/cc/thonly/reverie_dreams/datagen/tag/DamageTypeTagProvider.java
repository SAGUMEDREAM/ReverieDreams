package cc.thonly.reverie_dreams.datagen.tag;

import cc.thonly.reverie_dreams.registry.content.RDDamageTypes;
import cc.thonly.reverie_dreams.registry.tag.RDDamageTypeTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;

import java.util.concurrent.CompletableFuture;

public class DamageTypeTagProvider extends FabricTagProvider<DamageType> {

    public DamageTypeTagProvider(FabricDataOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(packOutput, Registries.DAMAGE_TYPE, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        HolderLookup.RegistryLookup<DamageType> lookup = wrapperLookup.lookupOrThrow(Registries.DAMAGE_TYPE);

        this.tag(DamageTypeTags.NO_KNOCKBACK).add(RDDamageTypes.DANMAKU_GENERIC).add(RDDamageTypes.DANMAKU_REAL);
        this.tag(DamageTypeTags.BYPASSES_ARMOR).add(RDDamageTypes.DANMAKU_REAL);
        this.tag(DamageTypeTags.IS_PROJECTILE).add(RDDamageTypes.DANMAKU_REAL).add(RDDamageTypes.DANMAKU_REAL);
        this.tag(RDDamageTypeTags.DANMAKU_HIT).add(RDDamageTypes.DANMAKU_REAL).add(RDDamageTypes.DANMAKU_REAL);
    }
}
