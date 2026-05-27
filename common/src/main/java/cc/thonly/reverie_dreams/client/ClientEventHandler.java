package cc.thonly.reverie_dreams.client;

import cc.thonly.reverie_dreams.client.component.ClientPlayerComponentManager;
import cc.thonly.reverie_dreams.client.util.MidiListener;
import cc.thonly.reverie_dreams.networking.payload.HelloPacket;
import cc.thonly.reverie_dreams.networking.payload.PlayerJoinVersionPacket;
import cc.thonly.reverie_dreams.util.PlatformContext;
import net.blay09.mods.balm.Balm;
import net.minecraft.client.Minecraft;

public class ClientEventHandler {
    public static void onPlayerConnectedToServer(Minecraft client) {
        ClientPlayerComponentManager.clearConnection();
        Balm.networking().sendToServer(new HelloPacket());
        Balm.networking().sendToServer(new PlayerJoinVersionPacket(PlatformContext.VERSION.get()));
    }

    public static void onInitMidiDevice(Minecraft client) {
        MidiListener.startListener();
    }

    public static void onStopMidiDevice(Minecraft client) {
        MidiListener.closeListener();
    }
}
