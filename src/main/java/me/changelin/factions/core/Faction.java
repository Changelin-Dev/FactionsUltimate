package me.changelin.factions.core;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Faction {

    private final UUID id;          // L'ID unique (ne changera jamais)
    private String name;            // Le nom de la faction (pourra changer)
    private UUID leader;            // L'UUID du chef
    private final Set<UUID> members; // Liste des UUIDs des membres

    public Faction(UUID id, String name, UUID leader) {
        this.id = id;
        this.name = name;
        this.leader = leader;
        this.members = new HashSet<>();
        this.members.add(leader); // Le chef est le premier membre
    }

    // --- Getters ---
    public UUID getId() { return id; }
    public String getName() { return name; }
    public UUID getLeader() { return leader; }
    public Set<UUID> getMembers() { return members; }

    // --- Setters ---
    public void setName(String name) { this.name = name; }
    public void setLeader(UUID leader) { this.leader = leader; }
}