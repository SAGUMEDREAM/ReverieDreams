package cc.thonly.reverie_dreams.api.entity;

public interface LockedCart {
    void reverie_dreams$lockCart();

    void reverie_dreams$releaseCart();

    boolean reverie_dreams$isCartLocked();
}
