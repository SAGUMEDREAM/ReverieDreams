package cc.thonly.reverie_dreams.fabric.compat.ysm;

import cc.thonly.reverie_dreams.client.NPCScreen;
import cc.thonly.reverie_dreams.fabric.compat.ysm.network.C2SSetNPCModelPacket;
import com.micaftic.morpher.client.gui.ModernPlayerModelScreen;
import com.micaftic.morpher.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class ClientInvoker {
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
}
