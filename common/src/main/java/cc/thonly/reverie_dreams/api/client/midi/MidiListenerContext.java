package cc.thonly.reverie_dreams.api.client.midi;

public interface MidiListenerContext {
    void press(int key, int velocity);

    void release(int key);
}
