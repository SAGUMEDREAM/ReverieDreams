package cc.thonly.reverie_dreams.api.client.midi.impl;

import cc.thonly.reverie_dreams.api.client.midi.MidiListenerAPI;
import cc.thonly.reverie_dreams.api.client.midi.MidiListenerContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MidiListenerAPIImpl implements MidiListenerAPI {
    private final List<MidiListenerContext> contexts = new CopyOnWriteArrayList<>();
    private static final MidiListenerAPIImpl INSTANCE = new MidiListenerAPIImpl();

    public static MidiListenerAPIImpl instance() {
        return INSTANCE;
    }

    @Override
    public void register(@NotNull MidiListenerContext context) {
        this.contexts.add(context);
    }

    @Override
    public void unregister(@NotNull MidiListenerContext context) {
        this.contexts.remove(context);
    }

    @Override
    public void onNoteOn(int key, int velocity) {
        for (MidiListenerContext ctx : this.contexts) {
            ctx.press(key, velocity);
        }
    }

    @Override
    public void onNoteOff(int key) {
        for (MidiListenerContext ctx : this.contexts) {
            ctx.release(key);
        }
    }
}
