package cc.thonly.reverie_dreams.client.audio.fumo;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

/**
 * Registered only by the client initializer; the server never opens an audio device.
 */
@Config(name = "reverie_dreams-fumo-client")
public final class FumoAudioConfiguration implements ConfigData {
    public boolean enabled = false;
    public AudioSource source = AudioSource.OUTPUT;
    /**
     * Empty means the OS default endpoint of the selected source type.
     */
    public String deviceId = "";

    @Override
    public void validatePostLoad() {
        if (source == null)
            source = AudioSource.OUTPUT;
        if (deviceId == null || deviceId.length() > 4096)
            deviceId = "";
    }
}
