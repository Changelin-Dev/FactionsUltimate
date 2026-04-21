package me.changelin.factions.managers;

import me.changelin.factions.FactionsPlugin;
import me.changelin.factions.core.Faction;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FactionManager {

    private final FactionsPlugin plugin;
    private final Map<String, Faction> factions = new HashMap<>();
    private File file;
    private FileConfiguration config;

    public FactionManager(FactionsPlugin plugin) {
        this.plugin = plugin;
        setupConfig();
        loadFactions();
    }

    private void setupConfig() {
        file = new File(plugin.getDataFolder(), "factions.yml");
        if (!file.exists()) {
            plugin.saveResource("factions.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void createFaction(String name, UUID owner) {
        if (factions.containsKey(name.toLowerCase())) return;
        
        Faction faction = new Faction(name, owner);
        factions.put(name.toLowerCase(), faction);
        saveFactions();
    }

    public void saveFactions() {
        for (Faction faction : factions.values()) {
            config.set("factions." + faction.getName() + ".owner", faction.getOwner().toString());
            config.set("faction." + faction.getName() + ".member", faction.getMembers().toString());
            config.set("factions." + faction.getName() + ".claims", faction.getClaims());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFactions() {
        if (config.getConfigurationSection("factions") == null) return;
    
        for (String name : config.getConfigurationSection("factions").getKeys(false)) {
            UUID owner = UUID.fromString(config.getString("factions." + name + ".owner"));
            
            Faction faction = new Faction(name, owner); 
        
            if (config.contains("factions." + name + ".claims")) {
                List<String> loadedClaims = config.getStringList("factions." + name + ".claims");
                for (String c : loadedClaims) {
                    faction.addClaim(c); 
                }
            }
            factions.put(name.toLowerCase(), faction);
        }
    }

    public Faction getOwnerAt(org.bukkit.Chunk chunk) {
        
        String chunkId = chunk.getWorld().getName() + ";" + chunk.getX() + ";" + chunk.getZ();
    
        for (Faction faction : factions.values()) {
            if (faction.getClaims().contains(chunkId)) {
                return faction;
            }
        }
        return null; 
    }

    public Map<String, Faction> getFactions() {
        return factions;
    }
}