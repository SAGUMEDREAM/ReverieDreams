package cc.thonly.reverie_dreams.client;

/**
 * Client-side evidence that the initial position and a lit chunk were actually received.
 */
public interface GensokyoLoadTrackerAccess {
    void reverie_dreams$receivedPosition();

    void reverie_dreams$receivedLitChunk(int x, int z);
}
