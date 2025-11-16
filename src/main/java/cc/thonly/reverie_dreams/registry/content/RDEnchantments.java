package cc.thonly.reverie_dreams.registry.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.enchantment.DanmakuProtectionEnchantmentEffect;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.tag.RDDamageTypeTags;
import cc.thonly.reverie_dreams.registry.tag.RDEntityTypeTags;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.TimeCheck;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RDEnchantments {
    public static final Set<ResourceKey<Enchantment>> KEYS = new LinkedHashSet<>();
    public static final ResourceKey<Enchantment> EXTERMINATION = getOrCreateKey("extermination");
    public static final ResourceKey<Enchantment> MOON_DAMAGE = getOrCreateKey("moon_damage");
    public static final ResourceKey<Enchantment> DANMAKU_PROTECTION = getOrCreateKey("danmaku_protection");

    public static void bootstrap(BootstrapContext<Enchantment> context) {
        HolderGetter<Item> itemLookup = context.lookup(Registries.ITEM);
        HolderGetter<EntityType<?>> entityTypeLookup = context.lookup(Registries.ENTITY_TYPE);

        registerEnchantment(
                context,
                EXTERMINATION,
                Enchantment.enchantment(
                                Enchantment.definition(
                                        itemLookup.getOrThrow(ItemTags.SHARP_WEAPON_ENCHANTABLE),
                                        10,
                                        4,
                                        Enchantment.dynamicCost(1, 10),
                                        Enchantment.dynamicCost(21, 7),
                                        5,
                                        EquipmentSlotGroup.HAND
                                )
                        )
                        .withEffect(
                                EnchantmentEffectComponents.DAMAGE,
                                new AddValue(LevelBasedValue.perLevel(1.0f, 1.5f)),
                                LootItemEntityPropertyCondition.hasProperties(
                                        LootContext.EntityTarget.THIS,
                                        EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(entityTypeLookup, RDEntityTypeTags.YOKAI))
                                )
                        )
        );
        registerEnchantment(
                context,
                MOON_DAMAGE,
                Enchantment.enchantment(
                                Enchantment.definition(
                                        itemLookup.getOrThrow(ItemTags.SHARP_WEAPON_ENCHANTABLE),
                                        10,
                                        3,
                                        Enchantment.dynamicCost(1, 10),
                                        Enchantment.dynamicCost(21, 15),
                                        5,
                                        EquipmentSlotGroup.HAND
                                )
                        )
        );
        registerEnchantment(
                context,
                DANMAKU_PROTECTION,
                Enchantment.enchantment(
                                Enchantment.definition(
                                        itemLookup.getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                                        10,
                                        5,
                                        Enchantment.dynamicCost(1, 10),
                                        Enchantment.dynamicCost(1, 15),
                                        5,
                                        EquipmentSlotGroup.ARMOR
                                )
                        )
                        .withEffect(
                                EnchantmentEffectComponents.DAMAGE_PROTECTION,
                                new AddValue(LevelBasedValue.perLevel(2.0f)),
                                DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(RDDamageTypeTags.DANMAKU_HIT)))
                        )
        );

    }

    public static <T extends EnchantmentEntityEffect> MapCodec<T> registerEnchantment(ResourceKey<Enchantment> key, MapCodec<T> codec) {
        return Registry.register(BuiltInRegistries.ENCHANTMENT_ENTITY_EFFECT_TYPE, toEffectKey(key), codec);
    }

    public static ResourceKey<Enchantment> getOrCreateKey(String name) {
        ResourceKey<Enchantment> resourceKey = ResourceKey.create(Registries.ENCHANTMENT, ReverieDreams.id(name));
        KEYS.add(resourceKey);
        return resourceKey;
    }

    public static ResourceKey<MapCodec<? extends EnchantmentEntityEffect>> toEffectKey(ResourceKey<Enchantment> key) {
        return ResourceKey.create(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, key.location());
    }

    public static List<ItemStack> getEnchantmentBook(HolderLookup.Provider registryAccess, ResourceKey<Enchantment> key) {
        HolderLookup.RegistryLookup<Enchantment> lookup = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
        Holder.Reference<Enchantment> holder = lookup.getOrThrow(key);
        Enchantment enchantment = holder.value();
        List<ItemStack> list = new ArrayList<>();
        for (int i = enchantment.getMinLevel(); i < enchantment.getMaxLevel(); i++) {
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            mutable.set(holder, i);
            book.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
            list.add(book);
        }
        return list;
    }

    public static void registerEnchantments() {

    }

    public static Holder.Reference<Enchantment> registerEnchantment(BootstrapContext<Enchantment> context, ResourceKey<Enchantment> key, Enchantment.Builder builder) {
        return context.register(key, builder.build(key.location()));
    }
}
