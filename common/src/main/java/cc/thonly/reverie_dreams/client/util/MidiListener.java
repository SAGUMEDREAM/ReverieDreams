package cc.thonly.reverie_dreams.client.util;

import cc.thonly.reverie_dreams.api.client.midi.MidiListenerAPI;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import javax.sound.midi.*;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MidiListener {

    private static MidiListener _INSTANCE = null;
    private static final MidiListenerAPI _MIDI_LISTENER_API;

    static {
        _MIDI_LISTENER_API = MidiListenerAPI.get();
    }

    private boolean started = false;
    private final Map<String, DeviceWrapper> devices = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler;

    private MidiListener() {}


    public synchronized void start() {
        if (this.started) {
            log.warn("MIDI listener already started");
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "midi-hotplug-thread");
            t.setDaemon(true);
            return t;
        });

        scanDevices();

        scheduler.scheduleAtFixedRate(this::safeScan, 2, 2, TimeUnit.SECONDS);

        this.started = true;
        log.info("MIDI listener started (hotplug enabled)");
    }

    private void safeScan() {
        try {
            scanDevices();
        } catch (Exception e) {
            log.error("Error scanning MIDI devices", e);
        }
    }

    private void scanDevices() {
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();

        Set<String> current = ConcurrentHashMap.newKeySet();

        for (MidiDevice.Info info : infos) {
            String id = buildId(info);
            current.add(id);

            if (!devices.containsKey(id)) {
                connectDevice(info, id);
            }
        }

        for (String existingId : devices.keySet()) {
            if (!current.contains(existingId)) {
                disconnectDevice(existingId);
            }
        }
    }

    private String buildId(MidiDevice.Info info) {
        return info.getName() + "|" + info.getVendor() + "|" + info.getVersion();
    }

    private void connectDevice(MidiDevice.Info info, String id) {
        try {
            MidiDevice device = MidiSystem.getMidiDevice(info);

            if (device.getMaxTransmitters() == 0) return;

            device.open();

            Transmitter transmitter = device.getTransmitter();
            Receiver receiver = createReceiver();

            transmitter.setReceiver(receiver);

            devices.put(id, new DeviceWrapper(device, transmitter));

            log.info("MIDI device connected: {}", info.getName());

        } catch (Exception e) {
            log.error("Failed to connect MIDI device {}", info.getName(), e);
        }
    }

    private void disconnectDevice(String id) {
        DeviceWrapper wrapper = devices.remove(id);
        if (wrapper == null) return;

        try {
            wrapper.transmitter.close();
        } catch (Exception e) {
            log.warn("Error closing transmitter", e);
        }

        try {
            wrapper.device.close();
        } catch (Exception e) {
            log.warn("Error closing device", e);
        }

        log.info("MIDI device disconnected: {}", id);
    }

    private Receiver createReceiver() {
        return new Receiver() {
            @Override
            public void send(MidiMessage message, long timeStamp) {
                if (!(message instanceof ShortMessage sm)) return;

                int command = sm.getCommand();
                int key = sm.getData1();
                int velocity = sm.getData2();

                switch (command) {
                    case ShortMessage.NOTE_ON -> {
                        if (velocity > 0) {
                            _MIDI_LISTENER_API.onNoteOn(key, velocity);
                        } else {
                            _MIDI_LISTENER_API.onNoteOff(key);
                        }
                    }
                    case ShortMessage.NOTE_OFF -> _MIDI_LISTENER_API.onNoteOff(key);
                }
            }

            @Override
            public void close() {
                log.info("Receiver closed");
            }
        };
    }

    public synchronized void close() {
        if (!started) {
            log.warn("MIDI listener not running");
            return;
        }

        scheduler.shutdownNow();

        for (String id : devices.keySet()) {
            disconnectDevice(id);
        }

        devices.clear();

        started = false;
        _INSTANCE = null;

        log.info("MIDI listener stopped");
    }


    public static synchronized MidiListener startListener() {
        if (_INSTANCE == null) {
            _INSTANCE = new MidiListener();
            _INSTANCE.start();
        } else {
            log.warn("MIDI listener already running");
        }
        return _INSTANCE;
    }

    public static void closeListener() {
        if (_INSTANCE == null) {
            log.warn("MIDI listener is not running");
            return;
        }
        _INSTANCE.close();
    }

    public static @Nullable MidiListener getOrEmpty() {
        return _INSTANCE;
    }


    private record DeviceWrapper(MidiDevice device, Transmitter transmitter) {
    }
}