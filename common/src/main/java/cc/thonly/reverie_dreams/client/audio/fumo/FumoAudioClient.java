package cc.thonly.reverie_dreams.client.audio.fumo;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.client.Minecraft;

public final class FumoAudioClient {
    private static final FumoAudioController CONTROLLER = new FumoAudioController();
    private static boolean initialized;

    private FumoAudioClient() {
    }

    public static void initialize() {
        if (initialized)
            return;
        AutoConfig.register(FumoAudioConfiguration.class, GsonConfigSerializer::new);
        initialized = true;
    }

    public static FumoAudioConfiguration config() {
        return AutoConfig.getConfigHolder(FumoAudioConfiguration.class).getConfig();
    }

    public static FumoAudioController controller() {
        return CONTROLLER;
    }

    public static void save() {
        config().validatePostLoad();
        AutoConfig.getConfigHolder(FumoAudioConfiguration.class).save();
        tick(Minecraft.getInstance());
    }

    public static void tick(Minecraft client) {
        if (!initialized)
            return;
        FumoAudioConfiguration config = config();
        CONTROLLER.configure(config.enabled && client.level != null, config.source, config.deviceId);
    }

    public static float pulse() {
        return CONTROLLER.pulse(System.nanoTime());
    }

    public static void disconnected() {
        if (initialized)
            CONTROLLER.configure(false, config().source, config().deviceId);
    }

    public static void shutdown() {
        CONTROLLER.close();
    }
}
