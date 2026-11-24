package cc.thonly.reverie_dreams.neoforge.compat.ysm;

import cc.thonly.reverie_dreams.client.gui.NPCActionWheelScreen;
import cc.thonly.reverie_dreams.compat.ysm.NPCAnimationActions;
import cc.thonly.reverie_dreams.compat.ysm.NPCModelSelection;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.neoforge.integration.sparkle.NPCActionCatalog;
import com.micaftic.morpher.client.ClientModelManager;
import com.micaftic.morpher.client.gui.ModernPlayerModelScreen;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ClientInvoker {
    private ClientInvoker() {
    }

    public static void bootstrap() {
        boolean clientCatalog = NPCModelSelection.usesSparkleClientCatalog();
        NPCModelSelection.setClientPicker(target -> openScreen(target, clientCatalog), clientCatalog);
        NPCAnimationActions.setClientOpener(ClientInvoker::openActionWheel);
    }

    private static void openActionWheel(NPCAnimationActions.OpenWheel packet) {
        Minecraft client = Minecraft.getInstance();
        var target = packet.target();
        if (client.level == null || !(client.level.getEntity(target.entityId()) instanceof NPCSimpleEntity npc)
                || !target.entityUuid().equals(npc.getUUID())
                || !NetworkManager.canServerReceive(NPCAnimationActions.SelectAction.ID)) {
            NPCAnimationActions.reportWheel(packet, false);
            return;
        }
        String model = npc.reverie_dreams$getModelId();
        var assembly = ClientModelManager.getModelContext(model).orElse(null);
        if (assembly == null || assembly.getAnimationBundle() == null) {
            NPCAnimationActions.reportWheel(packet, false);
            client.gui.setOverlayMessage(Component.translatableWithFallback("gui.npc.actions.unavailable",
                    "This companion's model is unavailable or still loading."), false);
            return;
        }
        client.setScreen(new NPCActionWheelScreen(client.screen, npc.getName(), NPCActionCatalog.entries(assembly), packet, model));
    }

    private static void openScreen(NPCModelSelection.OpenPicker target, boolean clientCatalog) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null
                || !(client.level.getEntity(target.entityId()) instanceof NPCSimpleEntity npc)
                || !target.entityUuid().equals(npc.getUUID())
                || !NetworkManager.canServerReceive(NPCModelSelection.SelectModel.ID)) {
            return;
        }

        Screen parent = client.screen;
        ModernPlayerModelScreen screen = new ModernPlayerModelScreen(parent, (model, texture) -> {
            String textureId = texture == null ? "" : texture;
            if (!NPCModelSelection.isValidChoice(model, textureId) || (clientCatalog && !supportsClientModel(model))) {
                client.gui.setOverlayMessage(Component.translatableWithFallback(
                        "message.reverie_dreams.npc.model.local_required",
                        "Choose an available local YSM or Blockbench model for this companion."), false);
                client.setScreen(parent);
                return;
            }
            NetworkManager.sendToServer(new NPCModelSelection.SelectModel(target, model, textureId));
            if (clientCatalog) {
                client.setScreen(parent);
            }
        }, "reverie_dreams:npc");
        screen.setRememberPlayerSelection(false);
        client.setScreen(screen);
    }

    private static boolean supportsClientModel(String model) {
        return !ClientModelManager.hasCloudMemorySource(model) && !ClientModelManager.MODEL_LIBRARY.isPeer(model)
                && ClientModelManager.getModelContext(model).filter(assembly -> assembly.getAnimationBundle() != null).isPresent();
    }
}
