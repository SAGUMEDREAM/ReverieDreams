package cc.thonly.pak;

import java.io.IOException;

public final class PakException extends IOException {
    public PakException(String message) { super(message); }
    public PakException(String message, Throwable cause) { super(message, cause); }
}
