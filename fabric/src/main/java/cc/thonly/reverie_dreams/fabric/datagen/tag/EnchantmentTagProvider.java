package cc.thonly.reverie_dreams.fabric.datagen.tag;

import cc.thonly.reverie_dreams.registry.content.RDEnchantments;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.concurrent.CompletableFuture;

public class EnchantmentTagProvider extends FabricTagProvider<Enchantment> {
    public EnchantmentTagProvider(FabricDataOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(packOutput, Registries.ENCHANTMENT, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        HolderLookup.RegistryLookup<Enchantment> lookup = wrapperLookup.lookupOrThrow(Registries.ENCHANTMENT);

        this.builder(EnchantmentTags.IN_ENCHANTING_TABLE)
                .add(RDEnchantments.EXTERMINATION)
                .add(RDEnchantments.MOON_DAMAGE)
                .add(RDEnchantments.DANMAKU_PROTECTION)
                .add(RDEnchantments.POWERFUL)
                .add(RDEnchantments.FROZEN)
                .add(RDEnchantments.CHARGE)
        ;
    }
}
