package cc.thonly.reverie_dreams;

import cc.thonly.reverie_dreams.client.registry.RDBlockEntityRenderers;
import cc.thonly.reverie_dreams.client.registry.RDBlockRenderTypes;
import cc.thonly.reverie_dreams.client.registry.RDEntityRenderers;
import cc.thonly.reverie_dreams.client.util.PhotoScreenshotHelper;
import cc.thonly.reverie_dreams.item.prop.TenguCameraItem;
import cc.thonly.reverie_dreams.mixin.accessor.EntityAccessor;
import cc.thonly.reverie_dreams.networking.payload.*;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import cc.thonly.reverie_dreams.registry.impl.RegistrySyncer;
import cc.thonly.reverie_dreams.util.PlatformContext;
import cc.thonly.reverie_dreams.util.item.ItemUtils;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.Pair;
import lombok.extern.slf4j.Slf4j;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.client.BalmClientRegistrars;
import net.blay09.mods.balm.client.platform.event.callback.ClientLifecycleCallback;
import net.blay09.mods.balm.network.BalmNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class ReverieDreamsClient {
    public static final Logger LOGGER = LoggerFactory.getLogger(ReverieDreams.MOD_ID);

    public static void initialize(BalmClientRegistrars registrars, Runnable lateInit) {
        registrars.blockRenderTypes(RDBlockRenderTypes::initialize);
        registrars.blockEntityRenderers(RDBlockEntityRenderers::initialize);
        registrars.entityRenderers(RDEntityRenderers::initialize);
        ReverieDreamsClient.initializeClientEvent(registrars);
        ReverieDreamsClient.initializeClientHooks(registrars);
        ReverieDreams.LATE_INIT_CLIENT.forEach(Runnable::run);
        ReverieDreams.LATE_INIT_CLIENT.clear();
        lateInit.run();
    }

    public static void initializeClientEvent(BalmClientRegistrars registrars) {
        ClientLifecycleCallback.ConnectedToServer.EVENT.register(client -> {
            Balm.networking().sendToServer(new HelloPacket());
            Balm.networking().sendToServer(new CSVersionPacket(PlatformContext.VERSION.get()));
        });
    }

    public static void initializeClientHooks(BalmClientRegistrars registrars) {

    }

    public static Logger logger() {
        return LOGGER;
    }
}
