package me.changelin.factions.core;

import me.changelin.factions.social.FactionHome;
import me.changelin.factions.social.FactionRole;
import me.changelin.factions.social.FactionUpgrade;
import me.changelin.factions.social.RelationType;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Faction {
    private final String name;
    private UUID owner;
    private final Set<UUID> members;
    private final Set<String> claims;
    private final Map<UUID, FactionRole> roles;
    private final Map<String, RelationType> relations;
    private int overclaimPenalty;
    private double bankBalance;
    private int upgradeLevel;
    private FactionHome home;

    public Faction(String name, UUID owner) {
        this(name, owner, Collections.emptySet(), Collections.emptySet(), 0, 0.0, 1,
                Collections.emptyMap(), Collections.emptyMap(), null);
    }

    public Faction(String name, UUID owner, Collection<UUID> members, Collection<String> claims, int overclaimPenalty,
                   double bankBalance, int upgradeLevel, Map<UUID, FactionRole> roles,
                   Map<String, RelationType> relations, FactionHome home) {
        this.name = name;
        this.owner = owner;
        this.members = new LinkedHashSet<>();
        this.members.add(owner);
        this.members.addAll(members);
        this.claims = new LinkedHashSet<>(claims);
        this.overclaimPenalty = Math.max(0, overclaimPenalty);
        this.bankBalance = Math.max(0.0, bankBalance);
        this.upgradeLevel = Math.max(1, upgradeLevel);
        this.roles = new HashMap<>();
        this.relations = new HashMap<>();
        this.home = home;

        for (UUID memberId : this.members) {
            this.roles.put(memberId, FactionRole.RECRUIT);
        }
        this.roles.putAll(roles);
        this.roles.put(owner, FactionRole.LEADER);
        this.relations.putAll(relations);
    }

    public String getName() {
        return name;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        this.members.add(owner);
        this.roles.put(owner, FactionRole.LEADER);
    }

    public Set<UUID> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    public Set<String> getClaims() {
        return Collections.unmodifiableSet(claims);
    }

    public boolean isMember(UUID playerId) {
        return members.contains(playerId);
    }

    public void addMember(UUID playerId) {
        members.add(playerId);
        roles.putIfAbsent(playerId, FactionRole.RECRUIT);
    }

    public void removeMember(UUID playerId) {
        members.remove(playerId);
        roles.remove(playerId);
    }

    public int getLandCount() {
        return claims.size();
    }

    public void addClaim(String chunkId) {
        claims.add(chunkId);
    }

    public void removeClaim(String chunkId) {
        claims.remove(chunkId);
    }

    public int getOverclaimPenalty() {
        return overclaimPenalty;
    }

    public void addOverclaimPenalty(int amount) {
        overclaimPenalty += Math.max(0, amount);
    }

    public void reduceOverclaimPenalty(int amount) {
        overclaimPenalty = Math.max(0, overclaimPenalty - Math.max(0, amount));
    }

    public double getBankBalance() {
        return bankBalance;
    }

    public void deposit(double amount) {
        bankBalance += Math.max(0.0, amount);
    }

    public boolean withdraw(double amount) {
        if (amount < 0 || bankBalance < amount) {
            return false;
        }
        bankBalance -= amount;
        return true;
    }

    public FactionUpgrade getUpgrade() {
        return FactionUpgrade.fromLevel(upgradeLevel);
    }

    public void upgrade() {
        FactionUpgrade next = getUpgrade().next();
        if (next != null) {
            upgradeLevel = next.getLevel();
        }
    }

    public FactionRole getRole(UUID playerId) {
        if (owner.equals(playerId)) {
            return FactionRole.LEADER;
        }
        return roles.getOrDefault(playerId, FactionRole.RECRUIT);
    }

    public void setRole(UUID playerId, FactionRole role) {
        if (isMember(playerId)) {
            roles.put(playerId, role);
        }
    }

    public Map<UUID, FactionRole> getRoles() {
        return Collections.unmodifiableMap(roles);
    }

    public RelationType getRelation(String factionName) {
        return relations.getOrDefault(factionName.toLowerCase(), RelationType.NEUTRAL);
    }

    public void setRelation(String factionName, RelationType relationType) {
        relations.put(factionName.toLowerCase(), relationType);
    }

    public Map<String, RelationType> getRelations() {
        return Collections.unmodifiableMap(relations);
    }

    public FactionHome getHome() {
        return home;
    }

    public void setHome(FactionHome home) {
        this.home = home;
    }
}
