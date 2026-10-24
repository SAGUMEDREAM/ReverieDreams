package cc.thonly.reverie_dreams.api.registry.tag;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluid;

public record TagRegistration<T>(ResourceKey<Registry<T>> registryKey) {
   public static final TagRegistration<Item> ITEM_TAG = new TagRegistration<>(Registries.ITEM);
   public static final TagRegistration<Block> BLOCK_TAG = new TagRegistration<>(Registries.BLOCK);
   public static final TagRegistration<Biome> BIOME_TAG = new TagRegistration<>(Registries.BIOME);
   public static final TagRegistration<Structure> STRUCTURE_TAG = new TagRegistration<>(Registries.STRUCTURE);
   public static final TagRegistration<Fluid> FLUID_TAG = new TagRegistration<>(Registries.FLUID);
   public static final TagRegistration<EntityType<?>> ENTITY_TYPE_TAG = new TagRegistration<>(Registries.ENTITY_TYPE);
   public static final TagRegistration<Enchantment> ENCHANTMENT_TAG = new TagRegistration<>(Registries.ENCHANTMENT);

   public TagKey<T> registerFabric(String tagId) {
      return TagKey.create(this.registryKey, Identifier.fromNamespaceAndPath("fabric", tagId));
   }

   public TagKey<T> registerC(String tagId) {
      return TagKey.create(this.registryKey, Identifier.fromNamespaceAndPath("c", tagId));
   }
}
