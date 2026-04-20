package me.changelin.factions.commands;

import me.changelin.factions.FactionsPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdFaction implements CommandExecutor {

    private final FactionsPlugin plugin;

    public CmdFaction(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSeuls les joueurs peuvent utiliser cette commande.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        // Système de sous-commandes
        switch (args[0].toLowerCase()) {
            case "create":
                handleCreate(player, args);
                break;
            case "invite":
                handleInvite(player, args);
                break;
            case "join":
                handleJoin(player, args);
                break;
            case "claim":
                handleClaim(player, args);
                break;
            case "leave":
                handleLeave(player, args);
                break;
            case "list":
                handleList(player);
                break;
            default:
                player.sendMessage("§cCommande inconnue. Tape /f pour voir l'aide.");
                break;
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6--- §eAide Factions §6---");
        player.sendMessage("§e/f create <nom> §7- Créer une faction");
        player.sendMessage("§e/f invite <pseudo> §7- Inviter un joueur");
        player.sendMessage("§e/f join <nom> §7- Rejoindre une faction");
        player.sendMessage("§e/f claim §7- Revendiquer un terrain");
        player.sendMessage("§e/f leave §7- Quitter la faction");
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /f create <nom>");
            return;
        }

        String name = args[1];
    
        if (plugin.getFactionManager().getFactions().containsKey(name.toLowerCase())) {
            player.sendMessage("§cCette faction existe déjà !");
            return;
        }

        // Ici on crée la faction via le manager
        plugin.getFactionManager().createFaction(name, player.getUniqueId());
        player.sendMessage("§aLa faction §e" + name + " §aa été créée !");
    }

    private void handleInvite(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /f invite <pseudo>");
            return;
        }
        player.sendMessage("§aInvitation envoyée à §e" + args[1]);
    }

    // On complétera les autres méthodes au fur et à mesure
    private void handleJoin(Player p, String[] a) { p.sendMessage("§7Fonctionnalité Join bientôt dispo."); }
    private void handleClaim(Player p, String[] a) { p.sendMessage("§7Fonctionnalité Claim bientôt dispo."); }
    private void handleLeave(Player p, String[] a) { p.sendMessage("§7Fonctionnalité Leave bientôt dispo."); }
    private void handleList(Player p) { p.sendMessage("§7Liste des factions bientôt dispo."); }
}