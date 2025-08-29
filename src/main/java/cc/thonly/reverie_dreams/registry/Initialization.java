package cc.thonly.reverie_dreams.registry;

public interface Initialization<T> {
    void bootstrap(IntrinsicalRegister<T> registry);
}
