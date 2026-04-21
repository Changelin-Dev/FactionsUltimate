package me.changelin.factions.database;

import com.google.gson.reflect.TypeToken;
import me.changelin.factions.access.ChunkAccessPermission;
import me.changelin.factions.access.ChunkAccessProfile;
import me.changelin.factions.api.AutoSavable;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChunkPermissionStore implements AutoSavable {

    private static final String FILE_NAME = "chunk-permissions.json";
    private static final Type STORE_TYPE = new TypeToken<Map<String, Map<String, Map<String, Boolean>>>>() { }.getType();

    private final JsonDatabase jsonDatabase;
    private final Map<String, Map<String, Map<String, Boolean>>> permissionsByChunk;

    public ChunkPermissionStore(JsonDatabase jsonDatabase) {
        this.jsonDatabase = jsonDatabase;
        this.permissionsByChunk = new HashMap<>(jsonDatabase.read(FILE_NAME, STORE_TYPE, HashMap::new));
    }

    public ChunkAccessProfile getProfile(String chunkId, UUID playerId) {
        Map<String, Map<String, Boolean>> players = permissionsByChunk.computeIfAbsent(chunkId, ignored -> new HashMap<>());
        Map<String, Boolean> raw = players.computeIfAbsent(playerId.toString(), ignored -> new EnumMapWrapper().create());

        ChunkAccessProfile profile = new ChunkAccessProfile();
        for (ChunkAccessPermission permission : ChunkAccessPermission.values()) {
            profile.set(permission, raw.getOrDefault(permission.name(), false));
        }
        return profile;
    }

    public void setPermission(String chunkId, UUID playerId, ChunkAccessPermission permission, boolean allowed) {
        Map<String, Map<String, Boolean>> players = permissionsByChunk.computeIfAbsent(chunkId, ignored -> new HashMap<>());
        Map<String, Boolean> raw = players.computeIfAbsent(playerId.toString(), ignored -> new EnumMapWrapper().create());
        raw.put(permission.name(), allowed);
        cleanup(chunkId, playerId);
    }

    public boolean hasPermission(String chunkId, UUID playerId, ChunkAccessPermission permission) {
        Map<String, Map<String, Boolean>> players = permissionsByChunk.get(chunkId);
        if (players == null) {
            return false;
        }

        Map<String, Boolean> raw = players.get(playerId.toString());
        return raw != null && raw.getOrDefault(permission.name(), false);
    }

    private void cleanup(String chunkId, UUID playerId) {
        Map<String, Map<String, Boolean>> players = permissionsByChunk.get(chunkId);
        if (players == null) {
            return;
        }

        Map<String, Boolean> raw = players.get(playerId.toString());
        if (raw == null) {
            return;
        }

        boolean hasAny = false;
        for (Boolean value : raw.values()) {
            if (Boolean.TRUE.equals(value)) {
                hasAny = true;
                break;
            }
        }

        if (!hasAny) {
            players.remove(playerId.toString());
        }

        if (players.isEmpty()) {
            permissionsByChunk.remove(chunkId);
        }
    }

    @Override
    public void save() {
        jsonDatabase.write(FILE_NAME, permissionsByChunk);
    }

    private static final class EnumMapWrapper {
        Map<String, Boolean> create() {
            Map<String, Boolean> defaults = new HashMap<>();
            for (ChunkAccessPermission permission : ChunkAccessPermission.values()) {
                defaults.put(permission.name(), false);
            }
            return defaults;
        }
    }
}
