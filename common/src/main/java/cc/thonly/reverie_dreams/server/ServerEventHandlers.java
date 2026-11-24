package cc.thonly.reverie_dreams.server;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.dialog.DialogVideoAPI;
import cc.thonly.reverie_dreams.api.player.PlayerComponentManager;
import cc.thonly.reverie_dreams.api.player.PlayerInputManagerAccess;
import cc.thonly.reverie_dreams.util.GrazeHandler;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.LegacySpellcardRuntime;
import cc.thonly.reverie_dreams.data.danmaku.stg.runtime.DanmakuDirector;
import cc.thonly.reverie_dreams.item.template.RoleCardItem;
import cc.thonly.reverie_dreams.item.prop.TimeStopClock;
import cc.thonly.reverie_dreams.networking.ServerNetworkingHandlers;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.PlayerComponentRegistry;
import cc.thonly.reverie_dreams.server.dialog.video.DialogPlayerManager;
import cc.thonly.reverie_dreams.api.player.PlayerComponentKey;
import cc.thonly.reverie_dreams.util.PlatformContext;
import cc.thonly.reverie_dreams.util.nbs.NotaUtils;
import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import cc.thonly.reverie_dreams.world.gensokyo.gen.GensokyoMapInstaller;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.Level;

import java.net.URI;
import java.util.List;
import cc.thonly.reverie_dreams.ai.AIRequestContext;
import cc.thonly.reverie_dreams.server.dialog.RedstoneConfigDialog;
import cc.thonly.reverie_dreams.server.dialog.RecordPlayerDialog;

@SuppressWarnings({"rawtypes", "unchecked", "ConstantValue", "resource"})
public class ServerEventHandlers {
    public static void onPlayerJoinByCreateComponent(ServerPlayer player) {
        PlayerComponentManager componentManager = PlayerComponentManager.serverAccess();
        for (PlayerComponentKey<?> key : PlayerComponentRegistry.components()) {
            componentManager.getOrCreatePlayerComponent(player, (Class) key.type());
        }
    }

    public static void onPlayerJoinBySync(ServerPlayer player) {
        BuiltInRegistryProviders.startSyncRegistry(List.of(player));
        RecipeManager.startSyncRecipe(List.of(player));
    }

    /**
     * 玩家真正进入 Gensokyo 后再分批安装结构，不能放在 SERVER_STARTED 里阻塞首次创建存档的加载界面。
     */
    public static void onPlayerJoinByInstallGensokyo(ServerPlayer player) {
        if (player.level().dimension().equals(RDBuiltinLevels.getGensokyo())) {
            requestGensokyoInstall(player);
        }
    }

    public static void onPlayerChangeDimension(ServerPlayer player, ResourceKey<Level> from, ResourceKey<Level> to) {
        if (to.equals(RDBuiltinLevels.getGensokyo())) {
            requestGensokyoInstall(player);
        }
    }

    private static void requestGensokyoInstall(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        if (server != null) {
            GensokyoMapInstaller.install(server);
        }
    }

    /**
     * 事件实现差异时的兜底：只要玩家已经在 Gensokyo，下一 tick 就会请求安装。
     * 安装器本身是幂等的，已经在队列中时不会重复创建。
     */
    public static void onServerTickInstallGensokyo(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.level().dimension().equals(RDBuiltinLevels.getGensokyo())) {
                GensokyoMapInstaller.install(server);
                return;
            }
        }
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
        SessionManager.clear(player.getUUID());
        RoleCardItem.clearUsingDataFor(player);
        TimeStopClock.releaseIfOwner(player);
    }

    public static void onPlayerDisconnectionByRemoveModClient(ServerPlayer player) {
        ServerNetworkingHandlers.PLAYER_WITH_MOD.remove(player);
        ServerNetworkingHandlers.PLAYER_SIDE_VERSION.remove(player);
    }

    public static void onServerStarted(MinecraftServer server) {
        DanmakuDirector.cancelAll();
        LegacySpellcardRuntime.cancelAll();
        PlayerInputManagerAccess polymerAccess = PlayerInputManagerAccess.polymerAccess();
        polymerAccess.reload();
        PlayerInputManagerAccess inputManager = PlayerInputManagerAccess.access();
        inputManager.reload();
        DialogVideoAPI.reload();
        SessionManager.clear();
        RoleCardItem.USING_DATA_MAP.clear();
        DialogPlayerManager.PLAYER_INSTANCES.clear();
        DialogPlayerManager.FILENAME2UID.clear();
        DialogPlayerManager.LOADED.clear();
        TimeStopClock.reset();
        RemoteSignalManager.access().reloadAll(server);
        TimeStopClock.TASK = null;
        TimeStopClock.ACTIVE_OWNER = null;
    }

    /**
     * 服务器停止时清空静态弹幕调度状态，避免跨存档/跨服务器实例持有旧 ServerLevel。
     */
    public static void onServerStopping(MinecraftServer server) {
        AIRequestContext.cancelForServer(server);
        GrazeHandler.clear();
        LegacySpellcardRuntime.cancelAll();
        DanmakuDirector.cancelAll();
        DialogPlayerManager.PLAYER_INSTANCES.clear();
        DialogPlayerManager.FILENAME2UID.clear();
        DialogPlayerManager.LOADED.clear();
        SessionManager.clear();
        RoleCardItem.USING_DATA_MAP.clear();
        TimeStopClock.release(server);
        NotaUtils.clearCaches();
        DelayedTask.clear(server);
        RemoteSignalManager.access().clear();
        RedstoneConfigDialog.clear();
        RecordPlayerDialog.clear();
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
