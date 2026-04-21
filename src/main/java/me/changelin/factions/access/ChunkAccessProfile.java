package me.changelin.factions.access;

import java.util.EnumMap;
import java.util.Map;

public class ChunkAccessProfile {

    private final Map<ChunkAccessPermission, Boolean> permissions = new EnumMap<>(ChunkAccessPermission.class);

    public ChunkAccessProfile() {
        for (ChunkAccessPermission permission : ChunkAccessPermission.values()) {
            permissions.put(permission, false);
        }
    }

    public boolean has(ChunkAccessPermission permission) {
        return permissions.getOrDefault(permission, false);
    }

    public void set(ChunkAccessPermission permission, boolean allowed) {
        permissions.put(permission, allowed);
    }

    public boolean isEmpty() {
        for (Boolean value : permissions.values()) {
            if (Boolean.TRUE.equals(value)) {
                return false;
            }
        }
        return true;
    }

    public Map<ChunkAccessPermission, Boolean> asMap() {
        return permissions;
    }
}
