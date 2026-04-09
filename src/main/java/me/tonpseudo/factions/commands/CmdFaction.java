package me.changelin.factions.commands;

import me.changelin.factions.FactionsPlugin;
import me.changelin.factions.core.Faction;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CmdFaction implements CommandExecutor {

    private final FactionsPlugin plugin;

    public CmdFaction(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // 1. Vérifier si c'est un joueur
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cSeuls les joueurs peuvent utiliser cette commande.");
            return true;
        }

        // 2. Vérifier s'il y a des arguments (ex: /f create <nom>)
        if (args.length == 0) {
            player.sendMessage("§cUsage: /f create <nom>");
            return true;
        }

        // 3. Logique du "create"
        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 2) {
                player.sendMessage("§cTu dois spécifier un nom pour ta faction.");
                return true;
            }

            String factionName = args[1];

            // Vérifier si le nom est déjà pris
            if (plugin.getFactionManager().exists(factionName)) {
                player.sendMessage("§cCe nom de faction est déjà utilisé !");
                return true;
            }

            // Vérifier la longueur du nom (ex: entre 3 et 16 caractères)
            if (factionName.length() < 3 || factionName.length() > 16) {
                player.sendMessage("§cLe nom doit faire entre 3 et 16 caractères.");
                return true;
            }

            // Création de la faction
            Faction faction = plugin.getFactionManager().createFaction(factionName, player);
            player.sendMessage("§aLa faction §6" + faction.getName() + " §aa été créée avec succès !");
            return true;
        }

        return false;
    }
}