package cc.thonly.reverie_dreams.fabric.compat.ysm.initializer;

import cc.thonly.reverie_dreams.client.NPCScreen;
import cc.thonly.reverie_dreams.client.networking.ClientNetworkingHandlers;
import cc.thonly.reverie_dreams.data.npc.NPCMenuType;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.fabric.compat.ysm.network.C2SSetNPCModelPacket;
import cc.thonly.reverie_dreams.fabric.compat.ysm.network.C2SYsmScreenPacket;
import cc.thonly.reverie_dreams.registry.content.NPCMenuTypes;
import cc.thonly.reverie_dreams.util.YsmHolder;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import com.micaftic.morpher.client.gui.ModernPlayerModelScreen;
import com.micaftic.morpher.core.api.network.PacketDirection;
import com.micaftic.morpher.core.api.network.YSMChannel;
import com.micaftic.morpher.network.NetworkHandler;
import dev.architectury.networking.NetworkManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Items;

import java.lang.reflect.Field;

@SuppressWarnings({"SameParameterValue"})
@Slf4j
public class SparkleMorpherCompatImpl {
    static final int packet_offset = 64;

    public static void bootstrap() {
        try {
            YsmHolder.setInitialized();
            NetworkManager.registerReceiver(NetworkManager.Side.S2C, C2SYsmScreenPacket.PACKET_ID, C2SYsmScreenPacket.CODEC, (packet, context) -> {
                ClientNetworkingHandlers.safeHandleClient(() -> openScreen(packet.entityId()));
            });
            YSMChannel.register(getPacket_offset(24), C2SSetNPCModelPacket.class, C2SSetNPCModelPacket::encode, C2SSetNPCModelPacket::decode, C2SSetNPCModelPacket::handle, PacketDirection.PLAY_TO_SERVER);
            NPCMenuTypes.MODIFY_MODEL = NPCMenuTypes.registerMenuType("modify_model",
                    new NPCMenuType()
                            .factory((player, npc, currentGui) -> {
                                NPCSimpleEntity simple = (NPCSimpleEntity) npc;

                                GuiElementBuilder builder = new GuiElementBuilder();
                                builder.setItem(Items.PLAYER_HEAD);
                                builder.setItemName(Component.translatable("gui.npc.info.model"));
                                builder.setCallback((i, clickType, containerInput, slotBasedGui) -> {
                                    SoundEventPlayUtils.playUISound(
                                            player,
                                            SoundEvents.UI_BUTTON_CLICK.value(),
                                            1.0f,
                                            1.0f
                                    );
                                    if (currentGui != null) {
                                        currentGui.close();
                                    }
                                    NetworkManager.sendToPlayer(player, new C2SYsmScreenPacket(npc.getId()));
                                    npc.setPaused(false);
                                });
                                return builder;
                            })

            );
        } catch (Exception e) {
            log.error("Error: ", e);
        }
    }

    static int getPacket_offset(int discriminator) {
        int i = packet_offset + discriminator;
        if (i > 256) {
            throw new IllegalArgumentException("%s > 256".formatted(i));
        }
        return i;
    }

    @SuppressWarnings("DataFlowIssue")
    public static void openScreen(int entityId) {
        Minecraft instance = Minecraft.getInstance();
        Screen parent = instance.screen;
//        resetMorpherModelScreenState();
        ModernPlayerModelScreen screen = new ModernPlayerModelScreen(parent, (modelId, texture) -> NetworkHandler.sendToServer(new C2SSetNPCModelPacket(entityId, modelId, texture))) {
            @Override
            public void onClose() {
                super.onClose();
//                resetMorpherModelScreenState();
            }
        };
        NPCScreen npcScreen = (NPCScreen) screen;
        npcScreen.reverie_dreams$setApplyForNPC(true);
        instance.setScreen(screen);
    }

    private static void resetMorpherModelScreenState() {
        try {
            Field stateField = ModernPlayerModelScreen.class.getDeclaredField("STATE");
            stateField.setAccessible(true);

            Object state = stateField.get(null);
            if (state == null) {
                return;
            }

            clearField(state, "selectedModelId", "");
            clearField(state, "selectedTextureId", "");

            clearField(state, "currentPath", "");
            clearField(state, "selectedResourceUrl", "");
            clearField(state, "selectedTaskId", "");

            clearField(state, "modelSearchText", "");
            clearField(state, "resourceSearchText", "");
            clearField(state, "siteEditText", "");
            clearField(state, "categoryEditText", "");

            clearField(state, "multiSelectMode", false);
            clearField(state, "resourceMultiSelectMode", false);
            clearField(state, "compactPreviewExpanded", false);

            clearField(state, "modelScroll", 0);
            clearField(state, "resourceScroll", 0);
            clearField(state, "settingsScroll", 0);
            clearField(state, "sitesScroll", 0);
            clearField(state, "categoryScroll", 0);

            clearField(state, "resourceLoaded", false);
            clearField(state, "resourceLoading", false);
            clearField(state, "resourceRequestId", 0);

            clearField(state, "status", "");

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Failed to reset Morpher ModernPlayerModelScreen STATE", e
            );
        }
    }

    private static void clearField(Object target, String fieldName, Object value)
            throws ReflectiveOperationException {

        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);

        if (field.getType() == boolean.class) {
            field.setBoolean(target, (Boolean) value);
        } else if (field.getType() == int.class) {
            field.setInt(target, (Integer) value);
        } else {
            field.set(target, value);
        }
    }
}
