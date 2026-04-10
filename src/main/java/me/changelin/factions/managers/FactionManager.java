package me.changelin.factions.managers;

import me.changelin.factions.core.Faction;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FactionManager {

    // On stocke les factions par leur UUID pour un accès ultra-rapide
    private final Map<UUID, Faction> factions = new HashMap<>();
    
    // On garde aussi une map Nom -> UUID pour vérifier si un nom est déjà pris
    private final Map<String, UUID> factionNames = new HashMap<>();

    /**
     * Crée une nouvelle faction
     * @param name Le nom de la faction
     * @param leader Le joueur qui crée la faction
     * @return L'objet Faction créé
     */
    public Faction createFaction(String name, Player leader) {
        UUID factionId = UUID.randomUUID();
        Faction faction = new Faction(factionId, name, leader.getUniqueId());

        // On enregistre dans nos Maps
        factions.put(factionId, faction);
        factionNames.put(name.toLowerCase(), factionId);

        return faction;
    }

    /**
     * Vérifie si un nom de faction existe déjà (insensible à la casse)
     */
    public boolean exists(String name) {
        return factionNames.containsKey(name.toLowerCase());
    }

    // Récupérer une faction par son nom
    public Faction getFactionByName(String name) {
        UUID id = factionNames.get(name.toLowerCase());
        return id != null ? factions.get(id) : null;
    }

    // Récupérer toutes les factions (pour les listes ou sauvegardes)
    public Map<UUID, Faction> getFactions() {
        return factions;
    }
}