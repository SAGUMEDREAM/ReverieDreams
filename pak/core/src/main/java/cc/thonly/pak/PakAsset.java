package cc.thonly.pak;

import java.util.Objects;

/** A logical owner-scoped resource and the native key slot that authenticates it. */
public record PakAsset(String owner, String resource, String keyId) {
    public PakAsset {
        Objects.requireNonNull(owner);
        Objects.requireNonNull(resource);
        Objects.requireNonNull(keyId);
        if (owner.length() > 64 || !owner.matches("[a-z0-9_.-]+")) {
            throw new IllegalArgumentException("Invalid PAK owner");
        }
        if (keyId.length() > 64 || !keyId.matches("[a-zA-Z0-9_.-]+")) {
            throw new IllegalArgumentException("Invalid PAK key ID");
        }
        if (resource.length() > 1024 || !resource.startsWith(owner + ":")) {
            throw new IllegalArgumentException("Invalid PAK logical resource");
        }
        String path = resource.substring(owner.length() + 1);
        if (path.isEmpty() || !path.matches("[a-z0-9_./-]+")) {
            throw new IllegalArgumentException("Invalid PAK logical resource path");
        }
        for (String segment : path.split("/", -1)) {
            if (segment.isEmpty() || segment.equals(".") || segment.equals("..")) {
                throw new IllegalArgumentException("Invalid PAK path segment");
            }
        }
    }
}
