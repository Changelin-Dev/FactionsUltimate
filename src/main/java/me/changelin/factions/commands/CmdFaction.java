package me.changelin.factions.commands;

import me.changelin.factions.FactionsPlugin;
import me.changelin.factions.core.Faction;
import me.changelin.factions.managers.FactionManager;
import me.changelin.factions.power.PowerManager;
import me.changelin.factions.quests.MonthlyQuestService;
import me.changelin.factions.quests.MonthlyQuestState;
import me.changelin.factions.social.FactionHome;
import me.changelin.factions.social.FactionRole;
import me.changelin.factions.social.FactionUpgrade;
import me.changelin.factions.social.RelationType;
import me.changelin.factions.util.ChunkMapRenderer;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.Locale;

public class CmdFaction implements CommandExecutor {

    private final FactionsPlugin plugin;

    public CmdFaction(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cSeuls les joueurs peuvent utiliser cette commande.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "create" -> handleCreate(player, args);
            case "invite" -> handleInvite(player, args);
            case "join" -> handleJoin(player, args);
            case "claim" -> handleClaim(player);
            case "leave" -> handleLeave(player);
            case "list" -> handleList(player);
            case "power" -> handlePower(player);
            case "map" -> ChunkMapRenderer.send(player, plugin);
            case "access" -> handleAccess(player, args);
            case "deposit" -> handleDeposit(player, args);
            case "withdraw" -> handleWithdraw(player, args);
            case "upgrade" -> handleUpgrade(player);
            case "ally" -> handleRelation(player, args, RelationType.ALLY);
            case "enemy" -> handleRelation(player, args, RelationType.ENEMY);
            case "neutral" -> handleRelation(player, args, RelationType.NEUTRAL);
            case "kick" -> handleKick(player, args);
            case "role" -> handleRole(player, args);
            case "sethome" -> handleSetHome(player);
            case "home" -> handleHome(player);
            case "quest" -> handleQuest(player, args);
            default -> player.sendMessage("§cCommande inconnue. Tapez §e/f §cpour voir l'aide.");
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6--- §eAide Factions §6---");
        player.sendMessage("§e/f create <nom> §7- Creer une faction");
        player.sendMessage("§e/f invite <joueur> §7- Inviter un joueur");
        player.sendMessage("§e/f join <nom> §7- Rejoindre une faction");
        player.sendMessage("§e/f claim §7- Claim ou over-claim le chunk courant");
        player.sendMessage("§e/f access <joueur> §7- Gerer les acces du chunk");
        player.sendMessage("§e/f deposit <montant> §7- Alimenter la banque");
        player.sendMessage("§e/f withdraw <montant> §7- Retirer de la banque");
        player.sendMessage("§e/f upgrade §7- Monter le niveau de faction");
        player.sendMessage("§e/f ally|enemy|neutral <nom> §7- Relations diplomatiques");
        player.sendMessage("§e/f role <joueur> <grade> §7- Changer un grade");
        player.sendMessage("§e/f kick <joueur> §7- Expulser un membre");
        player.sendMessage("§e/f sethome /f home §7- Gestion des homes");
        player.sendMessage("§e/f map §7- Afficher la mini-carte");
        player.sendMessage("§e/f quest §7- Voir la quete mensuelle");
        player.sendMessage("§e/f quest deposit §7- Ouvrir l'entrepot virtuel");
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage : /f create <nom>");
            return;
        }

        if (plugin.getFactionManager().getFactionByPlayer(player.getUniqueId()) != null) {
            player.sendMessage("§cVous etes deja dans une faction.");
            return;
        }

        String name = args[1];
        if (plugin.getFactionManager().isNameTaken(name)) {
            player.sendMessage("§cCette faction existe deja.");
            return;
        }

        Faction faction = plugin.getFactionManager().createFaction(name, player.getUniqueId());
        if (faction == null) {
            player.sendMessage("§cImpossible de creer cette faction.");
            return;
        }

        plugin.getPowerManager().ensureProfile(player.getUniqueId());
        refreshHud();
        player.sendMessage("§aLa faction §e" + faction.getName() + " §aa ete creee.");
    }

    private void handleInvite(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage : /f invite <joueur>");
            return;
        }

        Faction faction = requireFaction(player);
        if (faction == null) {
            return;
        }

        if (!hasRole(player, faction, FactionRole.COLEADER)) {
            player.sendMessage("§cVous devez etre officier ou chef pour inviter.");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage("§cCe joueur doit etre connecte.");
            return;
        }

        if (plugin.getFactionManager().getFactionByPlayer(target.getUniqueId()) != null) {
            player.sendMessage("§cCe joueur est deja dans une faction.");
            return;
        }

        if (!plugin.getFactionManager().invitePlayer(faction, target.getUniqueId())) {
            player.sendMessage("§cImpossible d'inviter ce joueur. Limite de membres atteinte ?");
            return;
        }

        target.sendMessage("§e" + player.getName() + " §avous invite dans la faction §e" + faction.getName() + "§a.");
        target.sendMessage("§7Utilisez §e/f join " + faction.getName() + " §7pour accepter.");
        player.sendMessage("§aInvitation envoyee a §e" + target.getName() + "§a.");
    }

    private void handleJoin(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage : /f join <nom>");
            return;
        }

        if (plugin.getFactionManager().getFactionByPlayer(player.getUniqueId()) != null) {
            player.sendMessage("§cVous etes deja dans une faction.");
            return;
        }

        String factionName = args[1];
        if (plugin.getFactionManager().getFaction(factionName) == null) {
            player.sendMessage("§cCette faction n'existe pas.");
            return;
        }

        if (!plugin.getFactionManager().joinFaction(player.getUniqueId(), factionName)) {
            player.sendMessage("§cVous n'avez pas d'invitation valide ou la faction est pleine.");
            return;
        }

        plugin.getPowerManager().ensureProfile(player.getUniqueId());
        refreshHud();
        player.sendMessage("§aVous avez rejoint la faction §e" + factionName + "§a.");
    }

    private void handleClaim(Player player) {
        Faction attacker = requireFaction(player);
        if (attacker == null) {
            return;
        }

        if (!hasRole(player, attacker, FactionRole.COLEADER)) {
            player.sendMessage("§cVous devez etre officier ou chef pour claim.");
            return;
        }

        Chunk chunk = player.getLocation().getChunk();
        Faction defender = plugin.getFactionManager().getFactionAt(chunk);

        if (defender == null) {
            if (!plugin.getPowerManager().canClaimMore(attacker)) {
                player.sendMessage("§cVotre faction n'a pas assez de power pour claim davantage de chunks.");
                return;
            }

            plugin.getFactionManager().claim(attacker, chunk);
            refreshHud();
            player.sendMessage("§aChunk claim avec succes pour §e" + attacker.getName() + "§a.");
            return;
        }

        if (defender == attacker) {
            player.sendMessage("§eCe chunk appartient deja a votre faction.");
            return;
        }

        if (!plugin.getPowerManager().isUnderPowered(defender)) {
            player.sendMessage("§cCette faction n'est pas sous-power. Over-claim impossible.");
            return;
        }

        int attackerPower = plugin.getPowerManager().getFactionPower(attacker);
        int defenderPower = plugin.getPowerManager().getFactionPower(defender);
        if (attackerPower <= defenderPower) {
            player.sendMessage("§cVotre power total doit etre superieur a celui de §e" + defender.getName() + "§c.");
            return;
        }

        if (!plugin.getPowerManager().canClaimMore(attacker)) {
            player.sendMessage("§cVotre faction n'a pas assez de power disponible pour absorber ce chunk.");
            return;
        }

        plugin.getFactionManager().overClaim(attacker, defender, chunk);
        plugin.getPowerManager().applyOverclaimPenalty(attacker);
        plugin.getFactionManager().save();
        refreshHud();
        player.sendMessage("§aOver-claim reussi sur la faction §e" + defender.getName() + "§a.");
        player.sendMessage("§6Cout temporaire : §e-" + PowerManager.OVERCLAIM_COST + " power factionnel effectif.");
    }

    private void handleAccess(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage : /f access <joueur>");
            return;
        }

        Faction faction = requireFaction(player);
        if (faction == null) {
            return;
        }

        if (!player.getUniqueId().equals(faction.getOwner())) {
            player.sendMessage("§cSeul le chef peut gerer les acces du chunk.");
            return;
        }

        Chunk chunk = player.getLocation().getChunk();
        Faction owner = plugin.getFactionManager().getFactionAt(chunk);
        if (owner == null || !owner.getName().equalsIgnoreCase(faction.getName())) {
            player.sendMessage("§cVous devez etre dans un chunk claim par votre faction.");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage("§cCe joueur doit etre connecte pour ouvrir le menu.");
            return;
        }

        if (owner.isMember(target.getUniqueId())) {
            player.sendMessage("§eCe joueur est deja membre de la faction.");
            return;
        }

        plugin.getChunkAccessMenuManager().openMenu(player, target, FactionManager.toChunkId(chunk));
        player.sendMessage("§aMenu d'acces ouvert pour §e" + target.getName() + "§a.");
    }

    private void handleDeposit(Player player, String[] args) {
        Faction faction = requireFaction(player);
        if (faction == null) {
            return;
        }

        if (!plugin.getEconomyHook().isAvailable()) {
            player.sendMessage("§cVault n'est pas disponible sur ce serveur.");
            return;
        }

        double amount = parseAmount(player, args, "/f deposit <montant>");
        if (amount < 0) {
            return;
        }

        EconomyResponse response = plugin.getEconomyHook().getEconomy().withdrawPlayer(player, amount);
        if (!response.transactionSuccess()) {
            player.sendMessage("§cTransaction refusee : " + response.errorMessage);
            return;
        }

        faction.deposit(amount);
        plugin.getFactionManager().save();
        refreshHud();
        player.sendMessage("§aVous avez depose §e" + formatMoney(amount) + "§a dans la banque de faction.");
    }

    private void handleWithdraw(Player player, String[] args) {
        Faction faction = requireFaction(player);
        if (faction == null) {
            return;
        }

        if (!plugin.getEconomyHook().isAvailable()) {
            player.sendMessage("§cVault n'est pas disponible sur ce serveur.");
            return;
        }

        if (!hasRole(player, faction, FactionRole.COLEADER)) {
            player.sendMessage("§cVous devez etre officier ou chef pour retirer de la banque.");
            return;
        }

        double amount = parseAmount(player, args, "/f withdraw <montant>");
        if (amount < 0) {
            return;
        }

        if (!faction.withdraw(amount)) {
            player.sendMessage("§cLa banque de faction ne contient pas assez d'argent.");
            return;
        }

        EconomyResponse response = plugin.getEconomyHook().getEconomy().depositPlayer(player, amount);
        if (!response.transactionSuccess()) {
            faction.deposit(amount);
            player.sendMessage("§cTransaction refusee : " + response.errorMessage);
            return;
        }

        plugin.getFactionManager().save();
        refreshHud();
        player.sendMessage("§aVous avez retire §e" + formatMoney(amount) + "§a de la banque de faction.");
    }

    private void handleUpgrade(Player player) {
        Faction faction = requireFaction(player);
        if (faction == null) {
            return;
        }

        if (!player.getUniqueId().equals(faction.getOwner())) {
            player.sendMessage("§cSeul le chef peut ameliorer la faction.");
            return;
        }

        FactionUpgrade next = faction.getUpgrade().next();
        if (next == null) {
            player.sendMessage("§eVotre faction est deja au niveau maximum.");
            return;
        }

        if (faction.getBankBalance() < next.getUpgradeCost()) {
            player.sendMessage("§cIl manque §e" + formatMoney(next.getUpgradeCost() - faction.getBankBalance()) + "§c dans la banque.");
            return;
        }

        faction.withdraw(next.getUpgradeCost());
        faction.upgrade();
        plugin.getFactionManager().save();
        refreshHud();
        player.sendMessage("§aFaction amelioree au niveau §e" + faction.getUpgrade().getLevel() + "§a.");
    }

    private void handleRelation(Player player, String[] args, RelationType relationType) {
        if (args.length < 2) {
            player.sendMessage("§cUsage : /f " + relationType.name().toLowerCase(Locale.ROOT) + " <nom>");
            return;
        }

        Faction faction = requireFaction(player);
        if (faction == null) {
            return;
        }

        if (!player.getUniqueId().equals(faction.getOwner())) {
            player.sendMessage("§cSeul le chef peut gerer les relations.");
            return;
        }

        Faction target = plugin.getFactionManager().getFaction(args[1]);
        if (target == null || target == faction) {
            player.sendMessage("§cFaction cible invalide.");
            return;
        }

        plugin.getFactionManager().setRelation(faction, target, relationType);
        refreshHud();
        player.sendMessage("§aRelation avec §e" + target.getName() + " §amajoree sur §e" + relationType.getDisplayName() + "§a.");
    }

    private void handleKick(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage : /f kick <joueur>");
            return;
        }

        Faction faction = requireFaction(player);
        if (faction == null) {
            return;
        }

        if (!hasRole(player, faction, FactionRole.COLEADER)) {
            player.sendMessage("§cVous devez etre officier ou chef pour kick.");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null || !faction.isMember(target.getUniqueId())) {
            player.sendMessage("§cCe joueur n'est pas dans votre faction.");
            return;
        }

        if (target.getUniqueId().equals(faction.getOwner())) {
            player.sendMessage("§cVous ne pouvez pas expulser le chef.");
            return;
        }

        if (faction.getRole(target.getUniqueId()).atLeast(faction.getRole(player.getUniqueId()))
                && !player.getUniqueId().equals(faction.getOwner())) {
            player.sendMessage("§cVous ne pouvez pas expulser un membre de grade egal ou superieur.");
            return;
        }

        plugin.getFactionManager().kickMember(faction, target.getUniqueId());
        refreshHud();
        player.sendMessage("§aVous avez expulse §e" + target.getName() + "§a.");
        target.sendMessage("§cVous avez ete expulse de la faction §e" + faction.getName() + "§c.");
    }

    private void handleRole(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsage : /f role <joueur> <recrue|membre|officier>");
            return;
        }

        Faction faction = requireFaction(player);
        if (faction == null) {
            return;
        }

        if (!player.getUniqueId().equals(faction.getOwner())) {
            player.sendMessage("§cSeul le chef peut changer les grades.");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null || !faction.isMember(target.getUniqueId())) {
            player.sendMessage("§cCe joueur n'est pas dans votre faction.");
            return;
        }

        FactionRole role = parseRole(args[2]);
        if (role == null || role == FactionRole.LEADER) {
            player.sendMessage("§cGrade invalide. Utilisez recrue, membre ou officier.");
            return;
        }

        faction.setRole(target.getUniqueId(), role);
        plugin.getFactionManager().save();
        refreshHud();
        player.sendMessage("§a" + target.getName() + " est maintenant §e" + role.getDisplayName() + "§a.");
    }

    private void handleSetHome(Player player) {
        Faction faction = requireFaction(player);
        if (faction == null) {
            return;
        }

        if (!faction.getUpgrade().isHomeUnlocked()) {
            player.sendMessage("§cLe home se debloque au niveau 2 de faction.");
            return;
        }

        if (!hasRole(player, faction, FactionRole.COLEADER)) {
            player.sendMessage("§cVous devez etre officier ou chef pour definir le home.");
            return;
        }

        Location location = player.getLocation();
        faction.setHome(new FactionHome(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch()));
        plugin.getFactionManager().save();
        player.sendMessage("§aHome de faction defini.");
    }

    private void handleHome(Player player) {
        Faction faction = requireFaction(player);
        if (faction == null) {
            return;
        }

        if (!faction.getUpgrade().isHomeUnlocked()) {
            player.sendMessage("§cLe home se debloque au niveau 2 de faction.");
            return;
        }

        if (faction.getRole(player.getUniqueId()) == FactionRole.RECRUIT) {
            player.sendMessage("§cLes recrues ne peuvent pas utiliser le home.");
            return;
        }

        Location location = plugin.getFactionManager().toLocation(faction.getHome());
        if (location == null) {
            player.sendMessage("§cAucun home de faction n'a ete defini.");
            return;
        }

        player.teleport(location);
        player.sendMessage("§aTeleportation au home de faction.");
    }

    private void handleQuest(Player player, String[] args) {
        Faction faction = requireFaction(player);
        if (faction == null) {
            return;
        }

        MonthlyQuestService questService = plugin.getQuestService();
        MonthlyQuestState quest = questService.getCurrentQuest();

        if (args.length == 1) {
            player.sendMessage("§6--- §eQuete Mensuelle §6---");
            player.sendMessage("§7Objectif : §e" + quest.getObjective());
            player.sendMessage("§7Mois : §e" + quest.getMonthKey());
            player.sendMessage("§7Progression globale : §e" + questService.formatProgress());
            player.sendMessage("§7Vos points : §e" + String.format(Locale.US, "%.1f", questService.getContributionPoints(player.getUniqueId())));
            player.sendMessage("§7Paliers : §e25% §7/ §e60% §7/ §e100%");
            player.sendMessage("§7Utilisez §e/f quest deposit §7pour contribuer.");
            player.sendMessage("§7Utilisez §e/f quest claim <1|2|3> §7pour recuperer une box.");
            return;
        }

        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "deposit" -> plugin.getQuestListener().openDepositInventory(player);
            case "claim" -> handleQuestClaim(player, args);
            default -> player.sendMessage("§cUsage : /f quest [deposit|claim <palier>]");
        }
    }

    private void handleQuestClaim(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsage : /f quest claim <1|2|3>");
            return;
        }

        int tier;
        try {
            tier = Integer.parseInt(args[2]);
        } catch (NumberFormatException exception) {
            player.sendMessage("§cPalier invalide.");
            return;
        }

        if (!plugin.getQuestService().claimReward(player, tier)) {
            player.sendMessage("§cAucune recompense disponible pour ce palier.");
            return;
        }

        player.sendMessage("§aRecompense de quete recuperee pour le palier §e" + tier + "§a.");
    }

    private void handleLeave(Player player) {
        Faction faction = plugin.getFactionManager().getFactionByPlayer(player.getUniqueId());
        if (faction == null) {
            player.sendMessage("§cVous n'etes dans aucune faction.");
            return;
        }

        if (faction.getOwner().equals(player.getUniqueId())) {
            if (faction.getMembers().size() > 1) {
                player.sendMessage("§cTransferez le lead avant de quitter votre faction.");
                return;
            }

            plugin.getFactionManager().disbandFaction(faction);
            refreshHud();
            player.sendMessage("§eVotre faction a ete dissoute.");
            return;
        }

        plugin.getFactionManager().leaveFaction(player.getUniqueId());
        refreshHud();
        player.sendMessage("§aVous avez quitte la faction §e" + faction.getName() + "§a.");
    }

    private void handleList(Player player) {
        if (plugin.getFactionManager().getFactions().isEmpty()) {
            player.sendMessage("§7Aucune faction n'est enregistree.");
            return;
        }

        player.sendMessage("§6--- §eFactions actives §6---");
        plugin.getFactionManager().getFactions().stream()
                .sorted(Comparator.comparing(Faction::getName, String.CASE_INSENSITIVE_ORDER))
                .forEach(faction -> player.sendMessage("§e" + faction.getName()
                        + " §7- Membres: §f" + faction.getMembers().size() + "/" + faction.getUpgrade().getMaxMembers()
                        + " §7- Lands: §f" + faction.getLandCount()
                        + " §7- Power: §f" + plugin.getPowerManager().getFactionPower(faction)
                        + " §7- Banque: §f" + formatMoney(faction.getBankBalance())
                        + " §7- Niv: §f" + faction.getUpgrade().getLevel()
                        + (plugin.getPowerManager().isUnderPowered(faction) ? " §c(Sous-power)" : "")));
    }

    private void handlePower(Player player) {
        Faction faction = plugin.getFactionManager().getFactionByPlayer(player.getUniqueId());
        int current = plugin.getPowerManager().getCurrentPower(player.getUniqueId());
        int max = plugin.getPowerManager().getMaxPower(player.getUniqueId());

        player.sendMessage("§6--- §ePower §6---");
        player.sendMessage("§7Personnel : §e" + current + "§7/§e" + max);

        if (faction != null) {
            player.sendMessage("§7Faction : §e" + faction.getName());
            player.sendMessage("§7Role : §e" + faction.getRole(player.getUniqueId()).getDisplayName());
            player.sendMessage("§7Power total : §e" + plugin.getPowerManager().getFactionPower(faction));
            player.sendMessage("§7Chunks : §e" + faction.getLandCount());
            player.sendMessage("§7Niveau : §e" + faction.getUpgrade().getLevel() + " §7(x" + faction.getUpgrade().getPowerMultiplier() + ")");
            player.sendMessage("§7Banque : §e" + formatMoney(faction.getBankBalance()));
            player.sendMessage("§7Dette de conquete : §e" + faction.getOverclaimPenalty());
            player.sendMessage("§7Etat : " + (plugin.getPowerManager().isUnderPowered(faction) ? "§cSous-power" : "§aStable"));
        } else {
            player.sendMessage("§7Faction : §cAucune");
        }
    }

    private Faction requireFaction(Player player) {
        Faction faction = plugin.getFactionManager().getFactionByPlayer(player.getUniqueId());
        if (faction == null) {
            player.sendMessage("§cVous devez etre dans une faction.");
        }
        return faction;
    }

    private boolean hasRole(Player player, Faction faction, FactionRole minimum) {
        return faction.getRole(player.getUniqueId()).atLeast(minimum);
    }

    private FactionRole parseRole(String raw) {
        return switch (raw.toLowerCase(Locale.ROOT)) {
            case "officier", "coleader" -> FactionRole.COLEADER;
            case "membre", "member" -> FactionRole.MEMBER;
            case "recrue", "recruit" -> FactionRole.RECRUIT;
            default -> null;
        };
    }

    private double parseAmount(Player player, String[] args, String usage) {
        if (args.length < 2) {
            player.sendMessage("§cUsage : " + usage);
            return -1;
        }
        try {
            double amount = Double.parseDouble(args[1]);
            if (amount <= 0) {
                player.sendMessage("§cLe montant doit etre positif.");
                return -1;
            }
            return amount;
        } catch (NumberFormatException exception) {
            player.sendMessage("§cMontant invalide.");
            return -1;
        }
    }

    private String formatMoney(double amount) {
        if (plugin.getEconomyHook().isAvailable()) {
            return plugin.getEconomyHook().getEconomy().format(amount);
        }
        return String.format("%.2f$", amount);
    }

    private void refreshHud() {
        plugin.getHudManager().refreshAll();
    }
}
