package me.changelin.factions.core;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Faction {
    private String name;
    private UUID owner;
    private List<UUID> members;
    private List<String> claims;

    public Faction(String name, UUID owner) {
        this.name = name;
        this.owner = owner;
        this.members = new ArrayList<>();
        this.members.add(owner); 
        this.claims = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public UUID getOwner() {
        return owner;
    }

    public List<UUID> getMembers() {
        return members;
    }

    public List<String> getClaims() {
        return claims;
    }

    public void addClaim(String chunkId) {
        if (!claims.contains(chunkId)) {
            claims.add(chunkId);
        }
    }
}