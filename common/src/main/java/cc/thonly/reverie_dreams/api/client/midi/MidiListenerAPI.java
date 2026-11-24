package cc.thonly.reverie_dreams.api.client.midi;

public interface MidiListenerAPI {

    void register(MidiListenerContext context);

    void unregister(MidiListenerContext context);

    void onNoteOn(int key, int velocity);

    void onNoteOff(int key);

    static MidiListenerAPI get() {
        MidiListenerAPI implementation = Holder.implementation;
        if (implementation == null) {
            throw new IllegalStateException("MIDI listener API implementation is not registered");
        }
        return implementation;
    }

    static void setImplementation(MidiListenerAPI implementation) {
        Holder.implementation = implementation;
    }

    final class Holder {
        private static volatile MidiListenerAPI implementation;
    }
}
