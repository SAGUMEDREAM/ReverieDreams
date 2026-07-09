package cc.thonly.reverie_dreams.server;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.dialog.DialogPlayerManager;
import cc.thonly.reverie_dreams.registry.content.RoleCards;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.function.BiConsumer;

@SuppressWarnings({"@ALL", "unused"})
public class CustomClickActionRegistry {
    private static final Map<Identifier, BiConsumer<ServerPlayer, ServerboundCustomClickActionPacket>> REGISTRY = new Object2ObjectOpenHashMap<>(256);
    public static final Identifier STOP_DIALOG_VIDEO_KEY = ReverieDreams.id("stop/dialog_video");
    public static final Identifier ROLE_SUMMON_KEY = ReverieDreams.id("role/summon");
    public static final Identifier CHAT_KEY = ReverieDreams.id("role/chat");
    public static final Identifier PAGE_GOTO_KEY = ReverieDreams.id("page/goto");
    public static final Identifier PAGE_BACK_KEY = ReverieDreams.id("page/back");
    public static final Identifier MODIFY_CUSTOM_SKIN_KEY = ReverieDreams.id("modify_custum_skin");
    public static final Holder<BiConsumer<ServerPlayer, ServerboundCustomClickActionPacket>> ROLE_SUMMON = registerCustomAction(ROLE_SUMMON_KEY, RoleCards::handleRoleCardDialog);
    public static final Holder<BiConsumer<ServerPlayer, ServerboundCustomClickActionPacket>> STOP_DIALOG_VIDEO = registerCustomAction(STOP_DIALOG_VIDEO_KEY, DialogPlayerManager::handleStopVideoDialog);
    public static final Holder<BiConsumer<ServerPlayer, ServerboundCustomClickActionPacket>> CHAT = registerCustomAction(CHAT_KEY, ChatAIManager::handleChat);
    public static final Holder<BiConsumer<ServerPlayer, ServerboundCustomClickActionPacket>> PAGE_GOTO = registerCustomAction(PAGE_GOTO_KEY, BookPageManagerImpl::handlePageGoto);
    public static final Holder<BiConsumer<ServerPlayer, ServerboundCustomClickActionPacket>> PAGE_BACK = registerCustomAction(PAGE_BACK_KEY, BookPageManagerImpl::handlePageBack);
    public static final Holder<BiConsumer<ServerPlayer, ServerboundCustomClickActionPacket>> MODIFY_CUSTOM_SKIN = registerCustomAction(MODIFY_CUSTOM_SKIN_KEY, CustomSkinSelectorHandlers::handleAction);

    public static Holder<BiConsumer<ServerPlayer, ServerboundCustomClickActionPacket>> registerCustomAction(Identifier location, BiConsumer<ServerPlayer, ServerboundCustomClickActionPacket> consumer) {
        if (REGISTRY.containsKey(location)) {
            throw new RuntimeException("Duplicated Key %s".formatted(location));
        }
        REGISTRY.put(location, consumer);
        return Holder.direct(consumer);
    }

    public static void handle(GameProfile profile, ServerboundCustomClickActionPacket packet) {
        if (profile == null) {
            return;
        }
        MinecraftServer server = ReverieDreams.getServer();
        if (server == null) {
            return;
        }
        BiConsumer<ServerPlayer, ServerboundCustomClickActionPacket> biConsumer = REGISTRY.get(packet.id());
        if (biConsumer == null) {
            return;
        }
        ServerPlayer player = server.getPlayerList().getPlayer(profile.id());
        biConsumer.accept(player, packet);
    }

    public static void initialize() {

    }
}
