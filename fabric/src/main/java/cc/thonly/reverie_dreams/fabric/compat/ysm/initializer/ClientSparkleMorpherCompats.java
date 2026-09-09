package cc.thonly.reverie_dreams.fabric.compat.ysm.initializer;

import com.micaftic.morpher.client.gui.ModernPlayerModelScreen;
import com.micaftic.morpher.network.NetworkHandler;
import com.micaftic.morpher.network.message.C2SSetMaidModelPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class ClientSparkleMorpherCompats {

    public static void openScreen(int entityId) {
        Minecraft instance = Minecraft.getInstance();
        Screen parent = instance.screen;
        instance.setScreen(new ModernPlayerModelScreen(parent, (modelId, texture) -> NetworkHandler.sendToServer(new C2SSetMaidModelPacket(entityId, modelId, texture))));
    }
}
