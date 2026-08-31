package cc.thonly.reverie_dreams.api.dialog;

import cc.thonly.reverie_dreams.dialog.DialogFiles;
import cc.thonly.reverie_dreams.dialog.DialogPlayer;
import cc.thonly.reverie_dreams.dialog.DialogPlayerManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface DialogAPI {
    // 播放 Dialog 视频
    static DialogPlayer play(ServerPlayer player, String filename, @Nullable SoundEvent soundEvent) {
        return DialogPlayerManager.play(player, filename, soundEvent);
    }

    // 创建播放器对象
    static DialogPlayer createPlayer(ServerPlayer player, String fileId) {
        return new DialogPlayer(player, fileId);
    }

    // 创建播放器对象
    static DialogPlayer createPlayer(ServerPlayer player, DialogFiles.Entry entry) {
        return new DialogPlayer(player, entry);
    }

    // 创建播放器对象
    static DialogPlayer createPlayer(ServerPlayer player, DialogFiles.Entry entry, @Nullable SoundEvent soundEvent) {
        return new DialogPlayer(player, entry, soundEvent);
    }

    // 重载资源
    static void reload() {
        DialogPlayerManager.reload();
    }

    // 从玩家获取播放器实例
    static DialogPlayer getPlayerInstance(Player player) {
        return DialogPlayerManager.getPlayerInstance(player.getStringUUID());
    }

    // 从玩家 uuid 获取播放器实例
    static DialogPlayer getPlayerInstance(String uuid) {
        return DialogPlayerManager.getPlayerInstance(uuid);
    }
}
