package cc.thonly.reverie_dreams.client;

import cc.thonly.reverie_dreams.client.component.ClientPlayerComponentManager;
import cc.thonly.reverie_dreams.client.util.MidiListener;
import cc.thonly.reverie_dreams.networking.payload.HelloPacket;
import cc.thonly.reverie_dreams.networking.payload.PlayerJoinVersionPacket;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.util.PlatformContext;
import net.blay09.mods.balm.Balm;
import net.minecraft.client.player.LocalPlayer;

public class ClientEventHandler {
    public static void onPlayerConnectedToServer(LocalPlayer player) {
        ClientPlayerComponentManager.clearConnection();
        RecipeManager.RECIPE_TYPES.forEach((id, recipeType) -> recipeType.setAcceptNetworking(false));
        Balm.networking().sendToServer(new HelloPacket());
        Balm.networking().sendToServer(new PlayerJoinVersionPacket(PlatformContext.VERSION.get()));
    }

    public static void onInitMidiDevice(LocalPlayer player) {
        MidiListener.startListener();
    }

    public static void onStopMidiDevice(LocalPlayer player) {
        MidiListener.closeListener();
    }
}
