package cc.thonly.reverie_dreams.registry;

public interface OwnerBinding<T> {
    public void setOwner(IntrinsicalRegister<T> register);
    public IntrinsicalRegister<T> getOwner();

}
