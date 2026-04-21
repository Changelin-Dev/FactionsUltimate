package me.changelin.factions;

import me.changelin.factions.access.ChunkAccessMenuManager;
import me.changelin.factions.commands.CmdFaction;
import me.changelin.factions.database.ChunkPermissionStore;
import me.changelin.factions.database.JsonDatabase;
import me.changelin.factions.economy.EconomyHook;
import me.changelin.factions.hud.HudManager;
import me.changelin.factions.listeners.ChunkAccessListener;
import me.changelin.factions.listeners.FactionListener;
import me.changelin.factions.listeners.QuestListener;
import me.changelin.factions.listeners.RaidListener;
import me.changelin.factions.listeners.SocialListener;
import me.changelin.factions.managers.FactionManager;
import me.changelin.factions.placeholders.FactionsPlaceholderExpansion;
import me.changelin.factions.power.PowerListener;
import me.changelin.factions.power.PowerManager;
import me.changelin.factions.quests.MonthlyQuestService;
import me.changelin.factions.quests.QuestBossBarManager;
import me.changelin.factions.raid.RaidDataStore;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class FactionsPlugin extends JavaPlugin {

    private static final long TEN_MINUTES_TICKS = 20L * 60L * 10L;
    private static final long HUD_REFRESH_TICKS = 20L * 5L;

    private static FactionsPlugin instance;

    private FactionManager factionManager;
    private PowerManager powerManager;
    private MonthlyQuestService questService;
    private ChunkPermissionStore chunkPermissionStore;
    private RaidDataStore raidDataStore;
    private ChunkAccessMenuManager chunkAccessMenuManager;
    private ChunkAccessListener chunkAccessListener;
    private HudManager hudManager;
    private EconomyHook economyHook;
    private QuestBossBarManager questBossBarManager;
    private QuestListener questListener;

    @Override
    public void onEnable() {
        instance = this;

        JsonDatabase jsonDatabase = new JsonDatabase(this);
        this.factionManager = new FactionManager(this);
        this.powerManager = new PowerManager(this, jsonDatabase);
        this.questService = new MonthlyQuestService(this, jsonDatabase);
        this.chunkPermissionStore = new ChunkPermissionStore(jsonDatabase);
        this.raidDataStore = new RaidDataStore(jsonDatabase);
        this.chunkAccessMenuManager = new ChunkAccessMenuManager(this);
        this.chunkAccessListener = new ChunkAccessListener(this);
        this.hudManager = new HudManager(this);
        this.economyHook = new EconomyHook(this);
        this.questBossBarManager = new QuestBossBarManager(this);
        this.questListener = new QuestListener(this);

        if (getCommand("f") != null) {
            getCommand("f").setExecutor(new CmdFaction(this));
        } else {
            getLogger().severe("La commande /f est absente du plugin.yml.");
        }

        Bukkit.getPluginManager().registerEvents(new FactionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PowerListener(powerManager), this);
        Bukkit.getPluginManager().registerEvents(chunkAccessListener, this);
        Bukkit.getPluginManager().registerEvents(new RaidListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SocialListener(this), this);
        Bukkit.getPluginManager().registerEvents(questListener, this);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            powerManager.rewardOnlinePlayers(Bukkit.getOnlinePlayers());
            questService.ensureCurrentMonth();
        }, TEN_MINUTES_TICKS, TEN_MINUTES_TICKS);
        Bukkit.getScheduler().runTaskTimer(this, this::saveAllData, TEN_MINUTES_TICKS, TEN_MINUTES_TICKS);
        Bukkit.getScheduler().runTaskTimer(this, hudManager::refreshAll, 40L, HUD_REFRESH_TICKS);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new FactionsPlaceholderExpansion(this).register();
            getLogger().info("Hook PlaceholderAPI active.");
        } else {
            getLogger().info("PlaceholderAPI non detecte, placeholders desactives.");
        }

        if (economyHook.setup()) {
            getLogger().info("Hook Vault economie active.");
        } else {
            getLogger().warning("Vault non detecte ou aucun provider economie actif. Banque de faction indisponible.");
        }

        Bukkit.getScheduler().runTask(this, hudManager::refreshAll);
        getLogger().info("FactionsUltimate v1.5 active avec quetes mensuelles, HUD, relations, grades et economie.");
    }

    @Override
    public void onDisable() {
        saveAllData();
    }

    public void saveAllData() {
        factionManager.save();
        powerManager.save();
        questService.save();
        chunkPermissionStore.save();
        raidDataStore.save();
    }

    public static FactionsPlugin getInstance() {
        return instance;
    }

    public FactionManager getFactionManager() {
        return factionManager;
    }

    public PowerManager getPowerManager() {
        return powerManager;
    }

    public MonthlyQuestService getQuestService() {
        return questService;
    }

    public ChunkPermissionStore getChunkPermissionStore() {
        return chunkPermissionStore;
    }

    public RaidDataStore getRaidDataStore() {
        return raidDataStore;
    }

    public ChunkAccessMenuManager getChunkAccessMenuManager() {
        return chunkAccessMenuManager;
    }

    public ChunkAccessListener getChunkAccessListener() {
        return chunkAccessListener;
    }

    public HudManager getHudManager() {
        return hudManager;
    }

    public EconomyHook getEconomyHook() {
        return economyHook;
    }

    public QuestBossBarManager getQuestBossBarManager() {
        return questBossBarManager;
    }

    public QuestListener getQuestListener() {
        return questListener;
    }
}
