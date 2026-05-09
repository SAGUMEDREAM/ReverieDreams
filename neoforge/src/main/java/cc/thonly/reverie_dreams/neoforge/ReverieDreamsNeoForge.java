package cc.thonly.reverie_dreams.neoforge;

import cc.thonly.keine.neoforge.NeoForgeKeine;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.command.CommandInit;
import cc.thonly.reverie_dreams.creative_tab.content.BaseCreativeTab;
import cc.thonly.reverie_dreams.neoforge.compat.ReverieDreamsNeoForgeCompats;
import cc.thonly.reverie_dreams.neoforge.impl.NeoRegistryImpl;
import com.mojang.brigadier.CommandDispatcher;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.neoforge.platform.runtime.NeoForgeLoadContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(ReverieDreams.MOD_ID)
@SuppressWarnings({"unchecked", "rawtypes"})
public class ReverieDreamsNeoForge {
    public ReverieDreamsNeoForge(ModContainer modContainer, IEventBus modEventBus) {
        NeoForgeKeine.loadApiImpl();
        final var context = new NeoForgeLoadContext(modContainer, modEventBus);
        setupApi();
        Balm.initializeMod(ReverieDreams.MOD_ID, context, registrars -> {
            ReverieDreams.initialize(registrars, () -> {
                ReverieDreams.ENTITY_DATA_SERIALIZER_REGISTRY.forEach((identifier, entityDataSerializer) -> {
                    Registry.register(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, identifier, entityDataSerializer);
                });
            });
        });
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onCreativeTabEvent);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    public void setupApi() {
        ReverieDreams.REGISTRY_GETTER = resourceKey -> new NeoRegistryImpl<>((ResourceKey<? extends Registry<Object>>) resourceKey) {
        };
        ReverieDreams.REGISTRY_SHADOWER = (resourceKey, handler) -> new NeoRegistryImpl(resourceKey, handler) {
        };
    }

    @SubscribeEvent
    public void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ReverieDreams.LATE_INIT.forEach(Runnable::run);
            ReverieDreams.LATE_INIT.clear();
            ReverieDreamsNeoForgeCompats.initialize();
        });
    }

    @SubscribeEvent
    public void onCreativeTabEvent(BuildCreativeModeTabContentsEvent event) {
        CreativeModeTab tab = event.getTab();
        BaseCreativeTab.busInvoker(tab, event);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandBuildContext buildContext = event.getBuildContext();
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        CommandInit.initialize(dispatcher, buildContext);
    }

}
