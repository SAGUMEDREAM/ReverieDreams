package cc.thonly.reverie_dreams.server;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.entity.type.ChatAIEntity;
import cc.thonly.reverie_dreams.openai.AIMessage;
import cc.thonly.reverie_dreams.server.dialog.ActionBuilder;
import cc.thonly.reverie_dreams.server.dialog.DialogBuilder;
import cc.thonly.reverie_dreams.server.dialog.DialogEntry;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.Input;
import net.minecraft.server.dialog.action.Action;
import net.minecraft.server.dialog.input.TextInput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("unchecked")
public class ChatAIManager {

    @SuppressWarnings("rawtypes")
    public static void handleChat(ServerPlayer player, ServerboundCustomClickActionPacket packet) {
        MinecraftServer server = ReverieDreams.getServer();
        if (server == null) {
//            System.out.println(0);
            return;
        }
        Optional<Tag> payload = packet.payload();
        if (payload.isEmpty()) {
//            System.out.println(1);
            return;
        }
        Tag element = payload.get();
        if (!(element instanceof CompoundTag compound)) {
//            System.out.println(2);
            return;
        }
        String player_uuid = compound.getStringOr("player_uuid", "");
        String entity_uuid = compound.getStringOr("entity_uuid", "");
        String userInput = compound.getStringOr("user_input", "");
        if (userInput.isEmpty() || player_uuid.isEmpty() || entity_uuid.isEmpty()) {
//            System.out.println(3);
            return;
        }
        ServerPlayer serverPlayer = server.getPlayerList().getPlayer(UUID.fromString(player_uuid));
        if (player == null || player.hasDisconnected()) {
//            System.out.println(4);
            return;
        }
        if (serverPlayer==null) {
            return;
        }
        if (!serverPlayer.equals(player) || serverPlayer.hasDisconnected()) {
            return;
        }
        ChatAIEntity chatAIEntity = null;
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(UUID.fromString(entity_uuid));
            if (!(entity instanceof ChatAIEntity aiEntity)) {
                continue;
            }
//            System.out.println(5);
            chatAIEntity = aiEntity;
            break;
        }
        if (chatAIEntity == null) {
//            System.out.println(6);
            return;
        }
//        System.out.println(7);
        chatAIEntity.send(player, chatAIEntity.encapsulateUserInputContent(player, userInput));
    }

    public static <T extends Entity> ChatAIEntity<T> of(T entity) {
        if (entity instanceof ChatAIEntity<?> chatAIEntity) {
            return (ChatAIEntity<T>) chatAIEntity;
        }
        return null;
    }

    public static <T extends Entity> Holder<Dialog> buildDialog(ServerPlayer player, ChatAIEntity<T> aiEntity) {
        List<AIMessage> chatHistory = aiEntity.getChatHistory(player);
        if (!(aiEntity instanceof Entity entity)) {
            return null;
        }
        DialogEntry dialogEntry = DialogBuilder.builder(builder -> {
            builder.common(common -> {
                common.title(entity.getDisplayName());
                for (AIMessage message : chatHistory) {
                    String role = message.getRole();
                    String content = message.getContent();
                    boolean isUser = role.equalsIgnoreCase("user");
                    boolean isAssistant = role.equalsIgnoreCase("assistant");
                    boolean isSystem = role.equalsIgnoreCase("system");
                    if (isSystem) {
                        continue;
                    }
                    MutableComponent component = Component.empty();
                    if (isUser) {
                        component.append(" ");
                    } else if (isAssistant) {
                        component.append("[").append(entity.getDisplayName()).append("] ");
                    }
                    component.append(content);
                    common.addTextBody(component, 245);
                }
                if (chatHistory.size() <= 1) {
                    common.addTextBody(" ");
                }
                common.addTextBody(Component.translatable("gui.reverie_dreams.input_request"));
                common.input((inputs, inputFactory) -> {
                    Input user_input = inputFactory.textInput("user_input", 200, Component.empty(), true, "", 256, new TextInput.MultilineOptions(Optional.of(64), Optional.of(64)));
                    inputs.add(user_input);
                });
                common.setCanCloseWithEscape(true);
                common.setPause(true);
            });
            builder.actions(actions -> {
                ActionBuilder actionBuilder = actions.actionBuilder();
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putString("player_uuid", player.getStringUUID());
                compoundTag.putString("entity_uuid", entity.getStringUUID());
                Action sendMsg = actionBuilder.customAll(CustomClickActionRegistry.CHAT_KEY, compoundTag);
                actions.addButton(Component.translatable("gui.reverie_dreams.send"), 180, Optional.of(sendMsg));
            });
            builder.exitAction(Component.translatable("gui.reverie_dreams.close"), 180, Optional.empty());
        }).get().buildOrThrow();
        return dialogEntry.get();
    }

    public static String fixMojibakeIfNeeded(String mojibake) {
        if (mojibake == null || mojibake.isEmpty()) {
            return mojibake;
        }

        byte[] bytes = mojibake.getBytes(StandardCharsets.ISO_8859_1);

        String gbkDecoded = new String(bytes, Charset.forName("GBK"));

        if (isLikelyChineseText(gbkDecoded)) {
            return gbkDecoded;
        }

        // 如果不像是中文，尝试用 UTF-8 解码并检查是否明显乱码
        String utf8Decoded = new String(bytes, StandardCharsets.UTF_8);
        if (isStillMojibake(utf8Decoded)) {
            // 依然乱码，可能是其他情况，返回原串
            return mojibake;
        }

        return utf8Decoded;
    }

    public static boolean isLikelyChineseText(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        if (text.contains("�")) {
            return false;
        }

        int chineseCount = 0;
        int totalCheck = Math.min(text.length(), 100);

        for (int i = 0; i < totalCheck; i++) {
            char c = text.charAt(i);
            if ((c >= 0x4E00 && c <= 0x9FFF) || (c >= 0x3400 && c <= 0x4DBF)) {
                chineseCount++;
            }
        }

        return (double) chineseCount / totalCheck > 0.3;
    }

    public static boolean isStillMojibake(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        return text.contains("�") || hasManyControlChars(text);
    }

    public static boolean hasManyControlChars(String text) {
        int controlCount = 0;
        int len = Math.min(text.length(), 100);
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (c < 0x20 && c != '\n' && c != '\r' && c != '\t') {
                controlCount++;
            }
        }
        return (double) controlCount / len > 0.2;
    }

    public static String apiUrl() {
        return ReverieDreams.config().apiUrl;
    }

    public static String apiKey() {
        return ReverieDreams.config().apiKey;
    }

    public static String chatType() {
        return ReverieDreams.config().chatType;
    }

    public static Object model() {
        return ReverieDreams.config().model;
    }
}
