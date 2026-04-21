package me.changelin.factions.raid;

import com.google.gson.reflect.TypeToken;
import me.changelin.factions.api.AutoSavable;
import me.changelin.factions.database.JsonDatabase;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class RaidDataStore implements AutoSavable {

    private static final String FILE_NAME = "raid-data.json";
    private static final Type STORE_TYPE = new TypeToken<Map<String, Integer>>() { }.getType();
    private static final int DEFAULT_OBSIDIAN_DURABILITY = 5;

    private final JsonDatabase jsonDatabase;
    private final Map<String, Integer> obsidianHealth;

    public RaidDataStore(JsonDatabase jsonDatabase) {
        this.jsonDatabase = jsonDatabase;
        this.obsidianHealth = new HashMap<>(jsonDatabase.read(FILE_NAME, STORE_TYPE, HashMap::new));
    }

    public int damageObsidian(String blockKey) {
        int remaining = obsidianHealth.getOrDefault(blockKey, DEFAULT_OBSIDIAN_DURABILITY);
        remaining = Math.max(0, remaining - 1);
        if (remaining == 0) {
            obsidianHealth.remove(blockKey);
        } else {
            obsidianHealth.put(blockKey, remaining);
        }
        return remaining;
    }

    public void clear(String blockKey) {
        obsidianHealth.remove(blockKey);
    }

    @Override
    public void save() {
        jsonDatabase.write(FILE_NAME, obsidianHealth);
    }
}
