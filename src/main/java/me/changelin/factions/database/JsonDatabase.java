package me.changelin.factions.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.changelin.factions.FactionsPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.function.Supplier;

public class JsonDatabase {

    private final FactionsPlugin plugin;
    private final Gson gson;

    public JsonDatabase(FactionsPlugin plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public <T> T read(String fileName, Type type, Supplier<T> fallback) {
        File file = resolve(fileName);
        if (!file.exists()) {
            return fallback.get();
        }

        try (FileReader reader = new FileReader(file)) {
            T value = gson.fromJson(reader, type);
            return value != null ? value : fallback.get();
        } catch (IOException exception) {
            plugin.getLogger().warning("Impossible de lire " + fileName + ", valeur par defaut utilisee.");
            return fallback.get();
        }
    }

    public void write(String fileName, Object payload) {
        File file = resolve(fileName);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(payload, writer);
        } catch (IOException exception) {
            plugin.getLogger().severe("Impossible d'ecrire " + fileName + " : " + exception.getMessage());
        }
    }

    private File resolve(String fileName) {
        return new File(plugin.getDataFolder(), fileName);
    }
}
