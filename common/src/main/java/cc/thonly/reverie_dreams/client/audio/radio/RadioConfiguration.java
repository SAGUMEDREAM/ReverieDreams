package cc.thonly.reverie_dreams.client.audio.radio;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

import java.util.LinkedHashMap;
import java.util.Map;

@Config(name = "reverie_dreams-radio-client")
public final class RadioConfiguration implements ConfigData {
    public boolean enabled = false;
    public boolean occlusion = true;
    /**
     * Local per-URL PCM bookmark; live streams and downloaded audio are never saved.
     */
    public Map<String, Long> positions = new LinkedHashMap<>();
}
