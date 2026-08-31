package cc.thonly.reverie_dreams.api.client.midi;

import cc.thonly.reverie_dreams.api.client.midi.impl.MidiListenerAPIImpl;
import cc.thonly.reverie_dreams.client.util.MidiListener;
import org.jspecify.annotations.Nullable;

public interface MidiListenerAPI {

    void register(MidiListenerContext context);

    void unregister(MidiListenerContext context);

    void onNoteOn(int key, int velocity);

    void onNoteOff(int key);

    default @Nullable MidiListener getListener() {
        return MidiListener.getOrEmpty();
    }

    static MidiListenerAPI get() {
        return MidiListenerAPIImpl.instance();
    }
}
