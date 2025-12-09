package cc.thonly.reverie_dreams.datagen.tag;

import cc.thonly.reverie_dreams.registry.content.RDEnchantments;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.concurrent.CompletableFuture;

public class EnchantmentTagProvider extends KeyTagProvider<Enchantment> {
    public EnchantmentTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(packOutput, Registries.ENCHANTMENT, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        HolderLookup.RegistryLookup<Enchantment> lookup = wrapperLookup.lookupOrThrow(Registries.ENCHANTMENT);

        this.tag(EnchantmentTags.IN_ENCHANTING_TABLE)
                .add(RDEnchantments.EXTERMINATION)
                .add(RDEnchantments.MOON_DAMAGE)
                .add(RDEnchantments.DANMAKU_PROTECTION)
                .add(RDEnchantments.POWERFUL)
        ;
    }
}
