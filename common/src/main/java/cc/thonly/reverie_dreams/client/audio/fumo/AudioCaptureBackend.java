package cc.thonly.reverie_dreams.client.audio.fumo;

import java.util.List;
import java.util.function.BooleanSupplier;

import com.sun.jna.Platform;

public interface AudioCaptureBackend {
    List<AudioDevice> devices(AudioSource source) throws Exception;

    void capture(AudioSource source, String id, FumoBeatDetector detector, BooleanSupplier running) throws Exception;

    static AudioCaptureBackend create() {
        return Platform.isWindows() ? new WindowsAudioCapture() : new JavaSoundAudioCapture();
    }
}
