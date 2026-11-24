package cc.thonly.reverie_dreams.client.audio.radio;

import java.util.Arrays;

/**
 * Mono 44.1 kHz PCM, independent of the operating system's audio devices.
 */
public final class RadioPcm {
    public static final int RATE = 44100;
    private long phase;

    public short[] convert(short[] source, int length, int channels, int rate) {
        if (channels < 1 || channels > 2 || rate < 8000 || rate > 96000)
            throw new IllegalArgumentException("Invalid MPEG format");
        int frames = length / channels;
        short[] output = new short[(int) Math.ceil(frames * (double) RATE / rate) + 2];
        int count = 0;
        for (; phase < (long) frames * RATE; phase += rate) {
            int i = (int) (phase / RATE), j = Math.min(i + 1, frames - 1);
            int a = mono(source, i, channels), b = mono(source, j, channels);
            output[count++] = (short) Math.round(a + (b - a) * (phase % RATE / (double) RATE));
        }
        phase -= (long) frames * RATE;
        return Arrays.copyOf(output, count);
    }

    private static int mono(short[] data, int frame, int channels) {
        return channels == 1 ? data[frame] : ((int) data[frame * 2] + data[frame * 2 + 1]) / 2;
    }
}
