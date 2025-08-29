package cc.thonly.reverie_dreams;

import cc.thonly.polymer.PolymerEntityHelper;
import cc.thonly.polymer.PolymerStatusEffectHelper;
import cc.thonly.polymer.ResourcePackGenerator;
import cc.thonly.reverie_dreams.effect.ModStatusEffects;
import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.entity.npc.NPCRole;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.extras.api.ResourcePackExtras;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class LateLoaderInit implements ModInitializer {
    public static final List<Runnable> LATE_INIT = new ArrayList<>();
    public static final String POLYMER_MOD_ID = "reverie_dreams_polymerify";

    public static final RegistryKey<ItemGroup> ROLE_SPAWN_EGG_ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, Touhou.id("item_group_role_spawn_egg"));
    public static final RegistryKey<ItemGroup> SPAWN_EGG_ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, Touhou.id("item_group_spawn_egg"));
    
    public static final ItemGroup ITEM_GROUP_SPAWN_EGG = PolymerItemGroupUtils.builder()
            .icon(() -> new ItemStack(ModItems.SPAWN_EGG))
            .displayName(Text.translatable("item_group.touhou.spawn_egg"))
            .build();
    public static final ItemGroup ITEM_GROUP_NPC_SPAWN_EGG = PolymerItemGroupUtils.builder()
            .icon(() -> new ItemStack(ModItems.SPAWN_EGG))
            .displayName(Text.translatable("item_group.touhou.role.spawn_egg"))
            .build();

    @Override
    public void onInitialize() {
        PolymerItemGroupUtils.registerPolymerItemGroup(SPAWN_EGG_ITEM_GROUP_KEY, ITEM_GROUP_SPAWN_EGG);
        PolymerItemGroupUtils.registerPolymerItemGroup(ROLE_SPAWN_EGG_ITEM_GROUP_KEY, ITEM_GROUP_NPC_SPAWN_EGG);
        ItemGroupEvents.modifyEntriesEvent(SPAWN_EGG_ITEM_GROUP_KEY).register(itemGroup -> {
            for (Item item : ModEntities.getSpawnEggItemView()) {
                itemGroup.add(item);
            }
        });
        ItemGroupEvents.modifyEntriesEvent(ROLE_SPAWN_EGG_ITEM_GROUP_KEY).register(itemGroup -> {
            Collection<NPCRole> roles = RegistryManager.NPC_ROLE.values();
            for (NPCRole role : roles) {
                Item egg = role.getEgg();
                itemGroup.add(egg.getDefaultStack());
            }
        });
        for (RegistryEntry<StatusEffect> registryEntry : ModStatusEffects.REVERIE_DREAMS_EFFECTS) {
            PolymerStatusEffectHelper.registerOverlay(registryEntry);
        }
        PolymerEntityHelper.bootstrap();

        LATE_INIT.forEach(new Consumer<Runnable>() {
            @Override
            public void accept(Runnable runnable) {
                try {
                    runnable.run();
                } catch (Exception ignored) {

                }
            }
        });
        LATE_INIT.clear();
        PolymerResourcePackUtils.addModAssets(Touhou.MOD_ID);
        PolymerResourcePackUtils.addModAssets(POLYMER_MOD_ID);
        PolymerResourcePackUtils.markAsRequired();
        ResourcePackExtras.forDefault().addBridgedModelsFolder(
                Touhou.id("block"),
                Touhou.id("item"),
                Touhou.id("entity"),
                Touhou.id("font")
        );
        ResourcePackExtras.forDefault().addBridgedModelsFolder(
                id("block"),
                id("item"),
                id("entity"),
                id("font")
        );
        ResourcePackGenerator.setup();
    }

    public static Identifier id(String name) {
        return Identifier.of(POLYMER_MOD_ID, name);
    }
}
