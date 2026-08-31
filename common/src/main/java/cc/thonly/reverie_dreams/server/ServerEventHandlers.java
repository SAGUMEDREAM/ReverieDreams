package cc.thonly.reverie_dreams.server;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.dialog.DialogAPI;
import cc.thonly.reverie_dreams.api.player.PlayerComponentManager;
import cc.thonly.reverie_dreams.api.player.PlayerInputManagerAccess;
import cc.thonly.reverie_dreams.entity.ai.goal.work.NPCFindBlockGoal;
import cc.thonly.reverie_dreams.networking.ServerNetworkingHandlers;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.PlayerComponentRegistry;
import cc.thonly.reverie_dreams.server.player.PlayerComponent;
import cc.thonly.reverie_dreams.server.player.PlayerComponentInitializer;
import cc.thonly.reverie_dreams.util.PlatformContext;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.net.URI;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"rawtypes"})
public class ServerEventHandlers {
    public static void onPlayerJoinByCreateComponent(ServerPlayer player) {
        PlayerComponentManager componentManager = PlayerComponentManager.serverAccess();
        for (Map.Entry<Class<PlayerComponent<? extends PlayerComponent>>, PlayerComponentInitializer<?>> mapEntry : PlayerComponentRegistry.getComponents()) {
            Class<PlayerComponent<? extends PlayerComponent>> key = mapEntry.getKey();
            componentManager.getOrCreatePlayerComponent(player, key);
        }
    }

    public static void onPlayerJoinBySync(ServerPlayer player) {
        BuiltInRegistryProviders.startSyncRegistry(List.of(player));
        RecipeManager.startSyncRecipe(List.of(player));
    }

    public static void onPlayerJoinByModUpdateCheck(ServerPlayer player) {
        if (!ReverieDreams.config().checkUpdate) {
            return;
        }
        if (!player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            return;
        }
        if (PlatformContext.LATEST_VERSION == null) {
            return;
        }
        MutableComponent mutableText = Component.empty();
        mutableText.append(Component.translatable("message.reverie_dreams.update", PlatformContext.LATEST_VERSION));
        mutableText.append(" §r[");
        mutableText.append(Component.translatable("item.action.click.left").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(URI.create("https://modrinth.com/mod/gensokyo-reverie-of-lost-dreams")))));
        mutableText.append("§r]");
        player.sendSystemMessage(mutableText, false);
    }

    public static void onPlayerDisconnectionBySavingComponent(ServerPlayer player) {
        PlayerComponentManager playerComponentManager = PlayerComponentManager.serverAccess();
        playerComponentManager.saveAll();
    }

    public static void onPlayerDisconnectionByRemoveModClient(ServerPlayer player) {
        ServerNetworkingHandlers.PLAYER_WITH_MOD.remove(player);
        ServerNetworkingHandlers.PLAYER_SIDE_VERSION.remove(player);
    }

    public static void onServerStarted(MinecraftServer server) {
        PlayerInputManagerAccess polymerAccess = PlayerInputManagerAccess.polymerAccess();
        polymerAccess.reload();
        PlayerInputManagerAccess inputManager = PlayerInputManagerAccess.access();
        inputManager.reload();
        NPCFindBlockGoal.EXCLUSIONS.clear();
        DialogAPI.reload();
        SessionManager.clear();
        RemoteSignalManager.access().reloadAll(server);
    }

    public static void onServerSavingAfter(MinecraftServer server, boolean flush, boolean force) {
        PlayerComponentManager componentManager = PlayerComponentManager.serverAccess();
        componentManager.saveAll();
        RemoteSignalManager.access().saveAll(server);
    }

    public static void onServerReloading(MinecraftServer server, ReloadableServerResources resources) {
        server.execute(() -> {
        });
    }

    public static void onServerReloaded(MinecraftServer server) {
        server.execute(() -> {
            PlayerComponentManager playerComponentManager = PlayerComponentManager.serverAccess();
            playerComponentManager.onLoad(server);
            BuiltInRegistryProviders.startSyncRegistry(server.getPlayerList().getPlayers());
            RecipeManager.startSyncRecipe(server.getPlayerList().getPlayers());
        });
    }

}
