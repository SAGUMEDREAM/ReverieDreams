package cc.thonly.reverie_dreams.server;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.server.dialog.compass.GensokyoCompassDialog;
import cc.thonly.reverie_dreams.server.dialog.gap.GapDialog;
import cc.thonly.reverie_dreams.server.dialog.music.MusicSelectionDialog;
import cc.thonly.reverie_dreams.server.dialog.video.DialogPlayerManager;
import cc.thonly.reverie_dreams.gui.entity.NPCSkinGui;
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
import cc.thonly.reverie_dreams.board.BoardGames;
import cc.thonly.reverie_dreams.server.dialog.RedstoneConfigDialog;
import cc.thonly.reverie_dreams.server.dialog.RecordPlayerDialog;
import cc.thonly.reverie_dreams.workshop.WorkshopCounter;
import cc.thonly.reverie_dreams.workshop.WorkshopWriting;

@SuppressWarnings({"@ALL", "unused"})
public class CustomClickActionRegistry {
    private static final Map<Identifier, BiConsumer<ServerPlayer, ServerboundCustomClickActionPacket>> REGISTRY = new Object2ObjectOpenHashMap<>(256);
    public static final Identifier STOP_DIALOG_VIDEO_KEY = ReverieDreams.id("stop/dialog_video");
    public static final Holder<BiConsumer<ServerPlayer, ServerboundCustomClickActionPacket>> BOARD_ACTION = registerCustomAction(
            BoardGames.ACTION, BoardGames::handle);
    public static final Holder<BiConsumer<ServerPlayer, ServerboundCustomClickActionPacket>> REDSTONE_ACTION = registerCustomAction(
            RedstoneConfigDialog.ACTION, RedstoneConfigDialog::handle);
    public static final Holder<BiConsumer<ServerPlayer, ServerboundCustomClickActionPacket>> RECORD_PLAYER_ACTION = registerCustomAction(
            RecordPlayerDialog.ACTION, RecordPlayerDialog::handle);
    public static final Holder<BiConsumer<ServerPlayer, ServerboundCustomClickActionPacket>> WORKSHOP_WRITING = registerCustomAction(
            WorkshopWriting.ACTION,WorkshopWriting::handle);
    public static final Holder<BiConsumer<ServerPlayer, ServerboundCustomClickActionPacket>> WORKSHOP_COUNTER = registerCustomAction(
            WorkshopCounter.ACTION,WorkshopCounter::handle);
    public static final Identifier ROLE_SUMMON_KEY = ReverieDreams.id("role/summon");
    public static final Identifier CHAT_KEY = ReverieDreams.id("role/chat");
    public static final Identifier PAGE_GOTO_KEY = ReverieDreams.id("page/goto");
    public static final Identifier PAGE_BACK_KEY = ReverieDreams.id("page/back");
    public static final Identifier MODIFY_CUSTOM_SKIN_KEY = ReverieDreams.id("modify_custum_skin");
    public static final Identifier ROLE_CHANGE_SKIN_KEY = ReverieDreams.id("role/change_skin");
    public static final Identifier MUSIC_SELECT_KEY = ReverieDreams.id("music/select");
    public static final Identifier MUSIC_PAGE_KEY = ReverieDreams.id("music/page");
    public static final Identifier GAP_ACTION_KEY = ReverieDreams.id("gap/action");
    public static final Identifier GENSOKYO_COMPASS_ACTION_KEY = ReverieDreams.id("gensokyo_compass/action");
    public static final Holder<BiConsumer<ServerPlayer, ServerboundCustomClickActionPacket>> ROLE_SUMMON = registerCustomAction(ROLE_SUMMON_KEY, RoleCards::handleRoleCardDialog);
    public static final Holder<BiConsumer<ServerPlayer, ServerboundCustomClickActionPacket>> STOP_DIALOG_VIDEO = registerCustomAction(STOP_DIALOG_VIDEO_KEY, DialogPlayerManager::handleStopVideoDialog);
    public static final Holder<BiConsumer<ServerPlayer, ServerboundCustomClickActionPacket>> CHAT = registerCustomAction(CHAT_KEY, ChatAIManager::handleChat);
    public static final Holder<BiConsumer<ServerPlayer, ServerboundCustomClickActionPacket>> PAGE_GOTO = registerCustomAction(PAGE_GOTO_KEY, BookPageManagerImpl::handlePageGoto);
    public static final Holder<BiConsumer<ServerPlayer, ServerboundCustomClickActionPacket>> PAGE_BACK = registerCustomAction(PAGE_BACK_KEY, BookPageManagerImpl::handlePageBack);
    public static final Holder<BiConsumer<ServerPlayer, ServerboundCustomClickActionPacket>> MODIFY_CUSTOM_SKIN = registerCustomAction(MODIFY_CUSTOM_SKIN_KEY, CustomSkinSelectorHandlers::handleAction);
    public static final Holder<BiConsumer<ServerPlayer, ServerboundCustomClickActionPacket>> ROLE_CHANGE_SKIN = registerCustomAction(ROLE_CHANGE_SKIN_KEY, NPCSkinGui::handleAction);
    public static final Holder<BiConsumer<ServerPlayer, ServerboundCustomClickActionPacket>> MUSIC_SELECT = registerCustomAction(MUSIC_SELECT_KEY, MusicSelectionDialog::handleSelect);
    public static final Holder<BiConsumer<ServerPlayer, ServerboundCustomClickActionPacket>> MUSIC_PAGE = registerCustomAction(MUSIC_PAGE_KEY, MusicSelectionDialog::handlePage);
    public static final Holder<BiConsumer<ServerPlayer, ServerboundCustomClickActionPacket>> GAP_ACTION = registerCustomAction(GAP_ACTION_KEY, GapDialog::handleAction);
    public static final Holder<BiConsumer<ServerPlayer, ServerboundCustomClickActionPacket>> GENSOKYO_COMPASS_ACTION = registerCustomAction(GENSOKYO_COMPASS_ACTION_KEY, GensokyoCompassDialog::handleAction);

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
