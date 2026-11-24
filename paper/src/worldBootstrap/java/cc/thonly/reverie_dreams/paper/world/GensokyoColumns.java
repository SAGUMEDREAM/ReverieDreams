package cc.thonly.reverie_dreams.paper.world;

/** Immutable source-column access; allows source arithmetic to be checked without a server. */
public interface GensokyoColumns {
    GensokyoMap map();
    int heightAt(int x, int z);
    int biomeAt(int x, int z);
    String surfaceAt(int x, int z);
}
