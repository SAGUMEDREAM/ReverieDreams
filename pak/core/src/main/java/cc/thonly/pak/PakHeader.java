package cc.thonly.pak;

public record PakHeader(int plainLength, byte[] salt, byte[] nonce, byte[] authenticatedBytes) {
    public PakHeader {
        salt = salt.clone();
        nonce = nonce.clone();
        authenticatedBytes = authenticatedBytes.clone();
    }
    @Override public byte[] salt() { return salt.clone(); }
    @Override public byte[] nonce() { return nonce.clone(); }
    @Override public byte[] authenticatedBytes() { return authenticatedBytes.clone(); }
}
