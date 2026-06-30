package cc.thonly.reverie_dreams.fabric.datagen.tag;

import cc.thonly.reverie_dreams.registry.content.RDEnchantments;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.concurrent.CompletableFuture;

public class EnchantmentTagProvider extends FabricTagsProvider<Enchantment> {
    public EnchantmentTagProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
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
