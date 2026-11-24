package cc.thonly.reverie_dreams.client.audio.fumo;

import javax.sound.sampled.*;
import java.util.*;
import java.util.function.BooleanSupplier;

/**
 * Portable microphone/virtual-input fallback. Output loopback is explicitly Windows-only.
 */
final class JavaSoundAudioCapture implements AudioCaptureBackend {
    private static String id(Mixer.Info info) {
        return String.join("\u001f", info.getName(), info.getVendor(), info.getDescription(), info.getVersion());
    }

    @Override
    public List<AudioDevice> devices(AudioSource source) {
        if (source == AudioSource.OUTPUT)
            return List.of();
        List<AudioDevice> result = new ArrayList<>();
        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            if (AudioSystem.getMixer(info).getTargetLineInfo().length > 0)
                result.add(new AudioDevice(id(info), info.getName()));
        }
        return List.copyOf(result);
    }

    @Override
    public void capture(AudioSource source, String deviceId, FumoBeatDetector detector, BooleanSupplier running) throws Exception {
        if (source == AudioSource.OUTPUT)
            throw new UnsupportedOperationException("Output loopback requires Windows WASAPI");
        AudioFormat format = new AudioFormat(48000, 16, 1, true, false);
        DataLine.Info lineInfo = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine line;
        if (deviceId.isEmpty())
            line = (TargetDataLine) AudioSystem.getLine(lineInfo);
        else {
            Mixer.Info selected = Arrays.stream(AudioSystem.getMixerInfo()).filter(i -> id(i).equals(deviceId)).findFirst().orElseThrow();
            line = (TargetDataLine) AudioSystem.getMixer(selected).getLine(lineInfo);
        }
        try {
            line.open(format, 9600);
            line.start();
            PcmFormat pcm = new PcmFormat(48000, 1, 16, 2, false);
            byte[] bytes = new byte[4096];
            while (running.getAsBoolean() && !Thread.currentThread().isInterrupted()) {
                int available = Math.min(line.available(), bytes.length) / 2 * 2;
                if (available == 0) {
                    Thread.sleep(5);
                    continue;
                }
                int read = line.read(bytes, 0, available);
                detector.accept(bytes, read / 2, pcm, System.nanoTime());
            }
        } finally {
            line.stop();
            line.close();
        }
    }
}
