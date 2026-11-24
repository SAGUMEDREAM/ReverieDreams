package cc.thonly.pak;

/** Shared PAK2 wire limits, independent of the author's Java encryption implementation. */
public final class PakLimits {
    public static final int MAX_PLAINTEXT_BYTES = 4 * 1024 * 1024;
    public static final int MAX_PNG_BYTES = MAX_PLAINTEXT_BYTES;
    public static final int HEADER_BYTES = 44;
    public static final int AUTH_TAG_BYTES = 16;
    public static final int MAX_FILE_BYTES = HEADER_BYTES + MAX_PLAINTEXT_BYTES + AUTH_TAG_BYTES;
    private PakLimits() {}
}
