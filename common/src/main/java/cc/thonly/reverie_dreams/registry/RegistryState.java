package cc.thonly.reverie_dreams.registry;

public enum RegistryState {
    LOADING,
    FINISHED,
    ;

    public boolean is(RegistryState state) {
        return state.equals(this);
    }
}
