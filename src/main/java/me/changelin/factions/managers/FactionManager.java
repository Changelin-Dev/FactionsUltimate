package me.changelin.factions.managers;

import me.changelin.factions.FactionsPlugin;
import me.changelin.factions.api.FactionService;
import me.changelin.factions.core.Faction;
import me.changelin.factions.social.FactionHome;
import me.changelin.factions.social.FactionRole;
import me.changelin.factions.social.RelationType;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class FactionManager implements FactionService {

    private final FactionsPlugin plugin;
    private final Map<String, Faction> factions = new HashMap<>();
    private final Map<UUID, String> playerFactionIndex = new HashMap<>();
    private final Map<String, String> claimIndex = new HashMap<>();
    private final Map<UUID, String> pendingInvites = new HashMap<>();
    private File file;
    private FileConfiguration config;

    public FactionManager(FactionsPlugin plugin) {
        this.plugin = plugin;
        setupConfig();
        loadFactions();
    }

    private void setupConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        file = new File(plugin.getDataFolder(), "factions.yml");
        if (!file.exists()) {
            plugin.saveResource("factions.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public Faction createFaction(String name, UUID owner) {
        if (isNameTaken(name) || getFactionByPlayer(owner) != null) {
            return null;
        }

        Faction faction = new Faction(name, owner);
        factions.put(normalize(name), faction);
        rebuildIndexes();
        save();
        return faction;
    }

    @Override
    public void save() {
        config.set("factions", null);
        for (Faction faction : factions.values()) {
            String basePath = "factions." + faction.getName();
            config.set(basePath + ".owner", faction.getOwner().toString());
            config.set(basePath + ".members", stringifyMembers(faction.getMembers()));
            config.set(basePath + ".claims", new ArrayList<>(faction.getClaims()));
            config.set(basePath + ".overclaim-penalty", faction.getOverclaimPenalty());
            config.set(basePath + ".bank-balance", faction.getBankBalance());
            config.set(basePath + ".upgrade-level", faction.getUpgrade().getLevel());
            config.set(basePath + ".roles", stringifyRoles(faction.getRoles()));
            config.set(basePath + ".relations", stringifyRelations(faction.getRelations()));
            saveHome(basePath, faction.getHome());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFactions() {
        factions.clear();
        if (config.getConfigurationSection("factions") == null) {
            return;
        }

        for (String name : config.getConfigurationSection("factions").getKeys(false)) {
            String basePath = "factions." + name;
            String ownerValue = config.getString(basePath + ".owner");
            if (ownerValue == null) {
                plugin.getLogger().warning("Faction " + name + " ignoree : owner manquant.");
                continue;
            }

            UUID owner;
            try {
                owner = UUID.fromString(ownerValue);
            } catch (IllegalArgumentException exception) {
                plugin.getLogger().warning("Faction " + name + " ignoree : owner invalide.");
                continue;
            }

            List<UUID> members = parseMembers(name);
            List<String> claims = config.getStringList(basePath + ".claims");
            int penalty = Math.max(0, config.getInt(basePath + ".overclaim-penalty", 0));
            double bankBalance = Math.max(0.0, config.getDouble(basePath + ".bank-balance", 0.0));
            int upgradeLevel = Math.max(1, config.getInt(basePath + ".upgrade-level", 1));
            Map<UUID, FactionRole> roles = parseRoles(basePath + ".roles");
            Map<String, RelationType> relations = parseRelations(basePath + ".relations");
            FactionHome home = parseHome(basePath + ".home");

            Faction faction = new Faction(name, owner, members, claims, penalty, bankBalance, upgradeLevel, roles, relations, home);
            factions.put(normalize(name), faction);
        }
        rebuildIndexes();
    }

    @Override
    public Faction getFactionAt(Chunk chunk) {
        String ownerName = claimIndex.get(toChunkId(chunk));
        return ownerName != null ? factions.get(ownerName) : null;
    }

    @Override
    public Collection<Faction> getFactions() {
        return Collections.unmodifiableCollection(factions.values());
    }

    @Override
    public Faction getFaction(String name) {
        return name == null ? null : factions.get(normalize(name));
    }

    @Override
    public Faction getFactionByPlayer(UUID playerId) {
        String factionName = playerFactionIndex.get(playerId);
        return factionName != null ? factions.get(factionName) : null;
    }

    public RelationType getRelationBetween(Faction source, Faction target) {
        if (source == null || target == null) {
            return RelationType.NEUTRAL;
        }
        if (source.getName().equalsIgnoreCase(target.getName())) {
            return RelationType.ALLY;
        }
        return source.getRelation(normalize(target.getName()));
    }

    public boolean isNameTaken(String name) {
        return getFaction(name) != null;
    }

    public boolean invitePlayer(Faction faction, UUID targetId) {
        if (faction == null || faction.getMembers().size() >= faction.getUpgrade().getMaxMembers()) {
            return false;
        }

        pendingInvites.put(targetId, normalize(faction.getName()));
        return true;
    }

    public boolean hasInvite(UUID playerId, String factionName) {
        return factionName != null && normalize(factionName).equals(pendingInvites.get(playerId));
    }

    public boolean joinFaction(UUID playerId, String factionName) {
        Faction faction = getFaction(factionName);
        if (faction == null
                || getFactionByPlayer(playerId) != null
                || !hasInvite(playerId, factionName)
                || faction.getMembers().size() >= faction.getUpgrade().getMaxMembers()) {
            return false;
        }

        faction.addMember(playerId);
        faction.setRole(playerId, FactionRole.RECRUIT);
        pendingInvites.remove(playerId);
        rebuildIndexes();
        save();
        return true;
    }

    public boolean leaveFaction(UUID playerId) {
        Faction faction = getFactionByPlayer(playerId);
        if (faction == null || faction.getOwner().equals(playerId)) {
            return false;
        }

        faction.removeMember(playerId);
        rebuildIndexes();
        save();
        return true;
    }

    public boolean kickMember(Faction faction, UUID targetId) {
        if (faction == null || !faction.isMember(targetId) || faction.getOwner().equals(targetId)) {
            return false;
        }

        faction.removeMember(targetId);
        rebuildIndexes();
        save();
        return true;
    }

    public boolean disbandFaction(Faction faction) {
        if (faction == null) {
            return false;
        }

        pendingInvites.entrySet().removeIf(entry -> normalize(faction.getName()).equals(entry.getValue()));
        for (Faction otherFaction : factions.values()) {
            if (!otherFaction.getName().equalsIgnoreCase(faction.getName())) {
                otherFaction.setRelation(faction.getName(), RelationType.NEUTRAL);
            }
        }
        factions.remove(normalize(faction.getName()));
        rebuildIndexes();
        save();
        return true;
    }

    public boolean claim(Faction faction, Chunk chunk) {
        if (faction == null || getFactionAt(chunk) != null) {
            return false;
        }

        String chunkId = toChunkId(chunk);
        faction.addClaim(chunkId);
        claimIndex.put(chunkId, normalize(faction.getName()));
        save();
        return true;
    }

    public boolean overClaim(Faction attacker, Faction defender, Chunk chunk) {
        if (attacker == null || defender == null || attacker == defender) {
            return false;
        }

        String chunkId = toChunkId(chunk);
        if (!defender.getClaims().contains(chunkId)) {
            return false;
        }

        defender.removeClaim(chunkId);
        attacker.addClaim(chunkId);
        claimIndex.put(chunkId, normalize(attacker.getName()));
        save();
        return true;
    }

    public void setRelation(Faction source, Faction target, RelationType relationType) {
        if (source == null || target == null || source == target) {
            return;
        }

        source.setRelation(target.getName(), relationType);
        target.setRelation(source.getName(), relationType);
        save();
    }

    public static String toChunkId(Chunk chunk) {
        return chunk.getWorld().getName() + ";" + chunk.getX() + ";" + chunk.getZ();
    }

    public static String toBlockKey(org.bukkit.block.Block block) {
        return block.getWorld().getName() + ";" + block.getX() + ";" + block.getY() + ";" + block.getZ();
    }

    public Location toLocation(FactionHome home) {
        if (home == null) {
            return null;
        }

        World world = Bukkit.getWorld(home.getWorld());
        if (world == null) {
            return null;
        }
        return new Location(world, home.getX(), home.getY(), home.getZ(), home.getYaw(), home.getPitch());
    }

    private List<String> stringifyMembers(Collection<UUID> members) {
        List<String> serialized = new ArrayList<>();
        for (UUID memberId : members) {
            serialized.add(memberId.toString());
        }
        return serialized;
    }

    private Map<String, String> stringifyRoles(Map<UUID, FactionRole> roles) {
        Map<String, String> serialized = new HashMap<>();
        for (Map.Entry<UUID, FactionRole> entry : roles.entrySet()) {
            serialized.put(entry.getKey().toString(), entry.getValue().name());
        }
        return serialized;
    }

    private Map<String, String> stringifyRelations(Map<String, RelationType> relations) {
        Map<String, String> serialized = new HashMap<>();
        for (Map.Entry<String, RelationType> entry : relations.entrySet()) {
            serialized.put(entry.getKey(), entry.getValue().name());
        }
        return serialized;
    }

    private void saveHome(String basePath, FactionHome home) {
        if (home == null) {
            config.set(basePath + ".home", null);
            return;
        }

        config.set(basePath + ".home.world", home.getWorld());
        config.set(basePath + ".home.x", home.getX());
        config.set(basePath + ".home.y", home.getY());
        config.set(basePath + ".home.z", home.getZ());
        config.set(basePath + ".home.yaw", home.getYaw());
        config.set(basePath + ".home.pitch", home.getPitch());
    }

    private Map<UUID, FactionRole> parseRoles(String path) {
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) {
            return Collections.emptyMap();
        }

        Map<UUID, FactionRole> roles = new HashMap<>();
        for (String key : section.getKeys(false)) {
            try {
                UUID memberId = UUID.fromString(key);
                FactionRole role = FactionRole.valueOf(section.getString(key, FactionRole.RECRUIT.name()));
                roles.put(memberId, role);
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Role invalide ignore a " + path + " pour " + key);
            }
        }
        return roles;
    }

    private Map<String, RelationType> parseRelations(String path) {
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) {
            return Collections.emptyMap();
        }

        Map<String, RelationType> relations = new HashMap<>();
        for (String key : section.getKeys(false)) {
            try {
                RelationType relationType = RelationType.valueOf(section.getString(key, RelationType.NEUTRAL.name()));
                relations.put(normalize(key), relationType);
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Relation invalide ignoree a " + path + " pour " + key);
            }
        }
        return relations;
    }

    private FactionHome parseHome(String path) {
        if (!config.contains(path + ".world")) {
            return null;
        }
        return new FactionHome(
                config.getString(path + ".world", ""),
                config.getDouble(path + ".x"),
                config.getDouble(path + ".y"),
                config.getDouble(path + ".z"),
                (float) config.getDouble(path + ".yaw"),
                (float) config.getDouble(path + ".pitch")
        );
    }

    private List<UUID> parseMembers(String factionName) {
        List<String> rawMembers = config.getStringList("factions." + factionName + ".members");
        if (rawMembers.isEmpty()) {
            String legacyValue = config.getString("faction." + factionName + ".member");
            rawMembers = parseLegacyList(legacyValue);
        }

        List<UUID> members = new ArrayList<>();
        for (String rawMember : rawMembers) {
            try {
                members.add(UUID.fromString(rawMember));
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("UUID membre invalide ignore pour la faction " + factionName + " : " + rawMember);
            }
        }
        return members;
    }

    private List<String> parseLegacyList(String legacyValue) {
        if (legacyValue == null || legacyValue.length() < 2) {
            return Collections.emptyList();
        }

        String trimmed = legacyValue.substring(1, legacyValue.length() - 1).trim();
        if (trimmed.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> values = new ArrayList<>();
        for (String part : trimmed.split(",")) {
            values.add(part.trim());
        }
        return values;
    }

    private void rebuildIndexes() {
        playerFactionIndex.clear();
        claimIndex.clear();

        for (Faction faction : factions.values()) {
            for (UUID memberId : faction.getMembers()) {
                playerFactionIndex.put(memberId, normalize(faction.getName()));
            }

            for (String chunkId : faction.getClaims()) {
                claimIndex.put(chunkId, normalize(faction.getName()));
            }
        }
    }

    private String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
