package me.changelin.factions.access;

public enum ChunkAccessPermission {
    ACCESS_BUILD("Construction"),
    ACCESS_INTERACT("Interaction"),
    ACCESS_CONTAINER("Conteneurs");

    private final String displayName;

    ChunkAccessPermission(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
