package cc.thonly.reverie_dreams.client.audio.fumo;

/**
 * The capture engine's interleaved little-endian mix format. No audio is retained.
 */
public record PcmFormat(int sampleRate, int channels, int bits, int frameBytes, boolean floatingPoint) {
    public PcmFormat {
        if (sampleRate < 8000 || sampleRate > 384000 || channels < 1 || channels > 32
                || (floatingPoint ? bits != 32 && bits != 64 : bits != 8 && bits != 16 && bits != 24 && bits != 32)
                || frameBytes < channels * bits / 8 || frameBytes > 512)
            throw new IllegalArgumentException("Unsupported PCM format");
    }

    public float sample(byte[] bytes, int offset) {
        long value = 0;
        for (int i = 0; i < bits / 8; i++)
            value |= (bytes[offset + i] & 255L) << (i * 8);
        double decoded;
        if (floatingPoint)
            decoded = bits == 32 ? Float.intBitsToFloat((int) value) : Double.longBitsToDouble(value);
        else if (bits == 8)
            decoded = ((int) value - 128) / 128.0;
        else
            decoded = (value << (64 - bits) >> (64 - bits)) / Math.scalb(1.0, bits - 1);
        return Double.isFinite(decoded) ? (float) Math.clamp(decoded, -1, 1) : 0;
    }
}
