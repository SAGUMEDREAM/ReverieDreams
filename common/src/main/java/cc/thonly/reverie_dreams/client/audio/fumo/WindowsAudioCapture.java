package cc.thonly.reverie_dreams.client.audio.fumo;

import com.sun.jna.*;
import com.sun.jna.ptr.*;
import com.sun.jna.win32.StdCallLibrary;

import java.util.*;
import java.util.function.BooleanSupplier;

/**
 * WASAPI shared-mode capture and render-endpoint loopback, using Minecraft's JNA.
 * COM objects and buffers are owned/released by the thread that opened them.
 * Contract: https://learn.microsoft.com/windows/win32/coreaudio/loopback-recording
 */
public final class WindowsAudioCapture implements AudioCaptureBackend {
    private interface Ole32 extends StdCallLibrary {
        Ole32 INSTANCE = Native.load("ole32", Ole32.class);

        int CoInitializeEx(Pointer reserved, int flags);

        int CoCreateInstance(Pointer clsid, Pointer outer, int context, Pointer iid, PointerByReference result);

        void CoUninitialize();

        void CoTaskMemFree(Pointer memory);

        int PropVariantClear(Pointer value);
    }

    private static final Memory CLSID_ENUMERATOR = guid("BCDE0395-E52F-467C-8E3D-C4579291692E");
    private static final Memory IID_ENUMERATOR = guid("A95664D2-9614-4F35-A746-DE8DB63617E6");
    private static final Memory IID_AUDIO_CLIENT = guid("1CB9AD4C-DBFA-4C32-B178-C2F568A703B2");
    private static final Memory IID_CAPTURE_CLIENT = guid("C8ADBD64-E71E-48A0-A4DE-185C395CD317");

    private static Memory guid(String text) {
        UUID value = UUID.fromString(text);
        Memory m = new Memory(16);
        long high = value.getMostSignificantBits(), low = value.getLeastSignificantBits();
        m.setInt(0, (int) (high >>> 32));
        m.setShort(4, (short) (high >>> 16));
        m.setShort(6, (short) high);
        for (int i = 0; i < 8; i++)
            m.setByte(8 + i, (byte) (low >>> (56 - i * 8)));
        return m;
    }

    private static int invoke(Pointer object, int slot, Object... args) {
        Object[] call = new Object[args.length + 1];
        call[0] = object;
        System.arraycopy(args, 0, call, 1, args.length);
        return Function.getFunction(object.getPointer(0).getPointer((long) slot * Native.POINTER_SIZE), Function.ALT_CONVENTION).invokeInt(call);
    }

    private static void check(int hr) {
        if (hr < 0)
            throw new IllegalStateException(String.format(Locale.ROOT, "Audio device error 0x%08X", hr));
    }

    private static void release(Pointer p) {
        if (p != null)
            invoke(p, 2);
    }

    private static final class Com implements AutoCloseable {
        Com() {
            check(Ole32.INSTANCE.CoInitializeEx(null, 0));
        }

        @Override
        public void close() {
            Ole32.INSTANCE.CoUninitialize();
        }
    }

    private static Pointer enumerator() {
        PointerByReference out = new PointerByReference();
        check(Ole32.INSTANCE.CoCreateInstance(CLSID_ENUMERATOR, null, 23, IID_ENUMERATOR, out));
        return out.getValue();
    }

    private static String deviceId(Pointer device) {
        PointerByReference out = new PointerByReference();
        check(invoke(device, 5, out));
        try {
            return out.getValue().getWideString(0);
        } finally {
            Ole32.INSTANCE.CoTaskMemFree(out.getValue());
        }
    }

    private static String deviceName(Pointer device) {
        Pointer store = null;
        try (Memory property = new Memory(20); Memory value = new Memory(24); Memory nameGuid = guid("A45C254E-DF1C-4EFD-8020-67D146A850E0")) {
            PointerByReference out = new PointerByReference();
            check(invoke(device, 4, 0, out));
            store = out.getValue();
            property.write(0, nameGuid.getByteArray(0, 16), 0, 16);
            property.setInt(16, 14);
            value.clear();
            check(invoke(store, 5, property, value));
            try {
                if (value.getShort(0) == 31 && value.getPointer(8) != null)
                    return value.getPointer(8).getWideString(0);
                return deviceId(device);
            } finally {
                Ole32.INSTANCE.PropVariantClear(value);
            }
        } finally {
            release(store);
        }
    }

    @Override
    public List<AudioDevice> devices(AudioSource source) {
        try (Com ignored = new Com()) {
            Pointer enumerator = enumerator(), collection = null;
            try {
                PointerByReference out = new PointerByReference();
                check(invoke(enumerator, 3, source == AudioSource.OUTPUT ? 0 : 1, 1, out));
                collection = out.getValue();
                IntByReference count = new IntByReference();
                check(invoke(collection, 3, count));
                List<AudioDevice> result = new ArrayList<>();
                for (int i = 0; i < Math.min(count.getValue(), 256); i++) {
                    check(invoke(collection, 4, i, out));
                    Pointer device = out.getValue();
                    try {
                        result.add(new AudioDevice(deviceId(device), deviceName(device)));
                    } finally {
                        release(device);
                    }
                }
                return List.copyOf(result);
            } finally {
                release(collection);
                release(enumerator);
            }
        }
    }

    @Override
    public void capture(AudioSource source, String id, FumoBeatDetector detector, BooleanSupplier running) throws Exception {
        try (Com ignored = new Com()) {
            Pointer enumerator = enumerator(), device = null, audio = null, capture = null, mix = null;
            boolean started = false;
            try {
                PointerByReference out = new PointerByReference();
                if (id.isEmpty())
                    check(invoke(enumerator, 4, source == AudioSource.OUTPUT ? 0 : 1, 0, out));
                else
                    check(invoke(enumerator, 5, new WString(id), out));
                device = out.getValue();
                check(invoke(device, 3, IID_AUDIO_CLIENT, 23, null, out));
                audio = out.getValue();
                check(invoke(audio, 8, out));
                mix = out.getValue();
                int tag = Short.toUnsignedInt(mix.getShort(0)), bits = Short.toUnsignedInt(mix.getShort(14));
                int channels = Short.toUnsignedInt(mix.getShort(2)), rate = mix.getInt(4), align = Short.toUnsignedInt(mix.getShort(12));
                if (tag == 0xfffe) {
                    if (Short.toUnsignedInt(mix.getShort(16)) < 22)
                        throw new IllegalArgumentException("Truncated extensible audio format");
                    tag = mix.getInt(24);
                }
                if (tag != 1 && tag != 3)
                    throw new IllegalArgumentException("Unsupported device mix format");
                PcmFormat format = new PcmFormat(rate, channels, bits, align, tag == 3);
                check(invoke(audio, 3, 0, source == AudioSource.OUTPUT ? 0x20000 : 0, 1_000_000L, 0L, mix, null));
                check(invoke(audio, 14, IID_CAPTURE_CLIENT, out));
                capture = out.getValue();
                check(invoke(audio, 10));
                started = true;
                IntByReference next = new IntByReference(), frames = new IntByReference(), flags = new IntByReference();
                PointerByReference data = new PointerByReference();
                while (running.getAsBoolean() && !Thread.currentThread().isInterrupted()) {
                    check(invoke(capture, 5, next));
                    if (next.getValue() == 0) {
                        Thread.sleep(5);
                        continue;
                    }
                    check(invoke(capture, 3, data, frames, flags, null, null));
                    try {
                        int count = frames.getValue();
                        if (count < 0 || count > rate)
                            throw new IllegalStateException("Invalid audio buffer size");
                        byte[] bytes = count == 0 || (flags.getValue() & 2) != 0 ? new byte[count * align] : data.getValue().getByteArray(0, count * align);
                        detector.accept(bytes, count, format, System.nanoTime());
                    } finally {
                        check(invoke(capture, 4, frames.getValue()));
                    }
                }
            } finally {
                if (started)
                    invoke(audio, 11);
                if (mix != null)
                    Ole32.INSTANCE.CoTaskMemFree(mix);
                release(capture);
                release(audio);
                release(device);
                release(enumerator);
            }
        }
    }
}
