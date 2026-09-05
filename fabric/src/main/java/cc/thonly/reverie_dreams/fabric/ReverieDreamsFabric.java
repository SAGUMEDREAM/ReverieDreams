package cc.thonly.reverie_dreams.fabric;

import cc.thonly.keine.fabric.FabricKeine;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.ReverieDreamsPluginLoader;
import cc.thonly.reverie_dreams.api.ReverieDreamsPlugin;
import cc.thonly.reverie_dreams.api.plugin.callback.ReverieDreamsExtensionEvents;
import cc.thonly.reverie_dreams.api.registry.*;
import cc.thonly.reverie_dreams.command.CommandInit;
import cc.thonly.reverie_dreams.creative_tab.content.BaseCreativeTab;
import cc.thonly.reverie_dreams.fabric.api.ReverieDreamsPolymerBridge;
import cc.thonly.reverie_dreams.fabric.compat.ReverieDreamsFabricCompats;
import cc.thonly.reverie_dreams.registry.DeferredDelegateRegister;
import cc.thonly.reverie_dreams.registry.MCBuiltInRegistries;
import cc.thonly.reverie_dreams.registry.content.villager.RDVillagerTrades;
import cc.thonly.reverie_dreams.util.PlatformContext;
import cc.thonly.reverie_dreams.world.gen.RDBuiltinBiomes;
import cc.thonly.reverie_dreams.world.gen.RDBuiltinConfigurationCarvers;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricTrackedDataRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;

import java.util.*;
import java.util.List;

@Slf4j
public class ReverieDreamsFabric implements ModInitializer {
    public static final List<Runnable> FABRIC_LATE_INIT = new ArrayList<>();

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void onInitialize() {
        this.setupEarly();
        this.checkApiLoaded();
        if (PlatformContext.hasPolymer()) {
            ReverieDreamsPolymerBridge.tryPreloadPolymer();
        }
        ReverieDreams.initialize(() -> {
            for (DeferredDelegateRegister<?> delegateRegister : MCBuiltInRegistries.REGISTERS) {
                ResourceKey<? extends Registry<?>> key = delegateRegister.getKey();

                for (DeferredDelegateRegister.Entry<?> entry : delegateRegister.entries()) {
                    Identifier id = entry.getRegistryId();

                    Registry registry = BuiltInRegistries.REGISTRY.getValue((ResourceKey) key);
                    if (registry == null) {
                        continue;
                    }
                    Object value = entry.supplier().get();

                    Holder.Reference reference = Registry.registerForHolder(
                            registry,
                            id,
                            value
                    );

                    entry.bind(reference);
                }
            }
            for (NetworkManager.ClientboundEntry<?> entry : NetworkManager.CLIENTBOUNDS) {
                PayloadTypeRegistry.playS2C().register(
                        (CustomPacketPayload.Type) entry.type(),
                        (StreamCodec) entry.codec()
                );

                ClientPlayNetworking.registerGlobalReceiver(
                        (CustomPacketPayload.Type) entry.type(),
                        (payload, context) -> {
                            ((NetworkManager.ClientboundEntry) entry).handler().accept(
                                    context.player(),
                                    payload
                            );
                        }
                );
            }
            for (NetworkManager.ServerboundEntry<?> entry : NetworkManager.SERVERBOUNDS) {
                PayloadTypeRegistry.playC2S().register(
                        (CustomPacketPayload.Type) entry.type(),
                        (StreamCodec) entry.codec()
                );

                ServerPlayNetworking.registerGlobalReceiver(
                        (CustomPacketPayload.Type) entry.type(),
                        (payload, context) -> {
                            ((NetworkManager.ServerboundEntry) entry).handler().accept(
                                    context.player(),
                                    payload
                            );
                        }
                );
            }
            RDVillagerTrades.initialize();
            BiomeModifications.addCarver(BiomeSelectors.includeByKey(RDBuiltinBiomes.THE_MOON), RDBuiltinConfigurationCarvers.MOON_CAVE);
            ItemGroupEvents.MODIFY_ENTRIES_ALL.register(BaseCreativeTab::busInvoker);
            AliasManager.execute(Registries.ITEM, map -> map.forEach(BuiltInRegistries.ITEM::addAlias));
            AliasManager.execute(Registries.BLOCK, map -> map.forEach(BuiltInRegistries.BLOCK::addAlias));
            AliasManager.execute(Registries.ENTITY_TYPE, map -> map.forEach(BuiltInRegistries.ENTITY_TYPE::addAlias));
            AliasManager.execute(Registries.DATA_COMPONENT_TYPE, map -> map.forEach(BuiltInRegistries.DATA_COMPONENT_TYPE::addAlias));
//            Placeholders.registerCommon(ReverieDreams.id("version"), (ctx, args) -> PlaceholderResult.value(PlatformContext.VERSION.get()));
        });
    }

    @SuppressWarnings({"DataFlowIssue", "unchecked", "rawtypes"})
    public static void finishRegister() {
        for (EntityAttributeRegistry.Entry<?> entry : EntityAttributeRegistry.ENTRIES) {
            FabricDefaultAttributeRegistry.register(
                    entry.entityType().get(),
                    entry.function().apply()
            );
        }
        for (SpawnPlacementsRegistry.Entry<?> entry : SpawnPlacementsRegistry.ENTRIES) {
            SpawnPlacements.register((EntityType) entry.type().get(), entry.spawnPlacement(), entry.heightmapType(), entry.spawnPredicate());
        }
        EntityDataSerializerProviders.get().forEach(FabricTrackedDataRegistry::register);
        ReverieDreamsFabricCompats.initialize();
        ReverieDreams.COMMON_LATE_INIT.forEach(Runnable::run);
        ReverieDreams.COMMON_LATE_INIT.clear();
        ReverieDreams.BUS_LATE_INIT.forEach(Runnable::run);
        ReverieDreams.BUS_LATE_INIT.clear();
        CommandRegistrationCallback.EVENT.register(CommandInit::registerCommand);
        ReverieDreamsPolymerBridge.tryPolymerify();
        ReverieDreamsPluginLoader.run();
    }

    public void checkApiLoaded() {
        FabricKeine.loadApiImpl();
        ReverieDreamsExtensionEvents.SCAN_EVENT.register(this::loadPlugins);
    }

    public List<ReverieDreamsPlugin> loadPlugins() {
        return FabricLoader.getInstance()
                .getEntrypoints("reverie_dreams:extension", ReverieDreamsPlugin.class);
    }

    private void setupEarly() {
        List<String> launchArgs = Arrays.stream(FabricLoader.getInstance().getLaunchArguments(true)).toList();
        for (String arg : launchArgs) {
            if (arg.contains("--output") || arg.contains("--input") || arg.contains("--mod") || arg.contains("--all")) {
                PlatformContext.IS_DATAGEN_MODE = true;
                break;
            }
        }
    }

}
