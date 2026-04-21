package me.changelin.factions.api;

import me.changelin.factions.core.Faction;
import org.bukkit.Chunk;

import java.util.Collection;
import java.util.UUID;

public interface FactionService extends AutoSavable {
    Faction createFaction(String name, UUID owner);

    Faction getFaction(String name);

    Faction getFactionByPlayer(UUID playerId);

    Faction getFactionAt(Chunk chunk);

    Collection<Faction> getFactions();
}
