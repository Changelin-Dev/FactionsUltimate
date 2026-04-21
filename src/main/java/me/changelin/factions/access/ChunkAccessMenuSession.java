package me.changelin.factions.access;

import java.util.UUID;

public class ChunkAccessMenuSession {

    private final UUID editorId;
    private final UUID targetId;
    private final String targetName;
    private final String chunkId;

    public ChunkAccessMenuSession(UUID editorId, UUID targetId, String targetName, String chunkId) {
        this.editorId = editorId;
        this.targetId = targetId;
        this.targetName = targetName;
        this.chunkId = chunkId;
    }

    public UUID getEditorId() {
        return editorId;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getChunkId() {
        return chunkId;
    }
}
