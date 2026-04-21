package me.changelin.factions.power;

import com.google.gson.reflect.TypeToken;
import me.changelin.factions.FactionsPlugin;
import me.changelin.factions.api.PowerService;
import me.changelin.factions.core.Faction;
import me.changelin.factions.database.JsonDatabase;
import org.bukkit.entity.Player;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PowerManager implements PowerService {

    public static final int OVERCLAIM_COST = 5;

    private static final String FILE_NAME = "power.json";
    private static final Type STORE_TYPE = new TypeToken<Map<UUID, PowerProfile>>() { }.getType();
    private static final int DEFAULT_MAX_POWER = 10;
    private static final int PASSIVE_GAIN = 1;
    private static final int DEATH_LOSS = 2;

    private final FactionsPlugin plugin;
    private final JsonDatabase jsonDatabase;
    private final Map<UUID, PowerProfile> profiles;

    public PowerManager(FactionsPlugin plugin, JsonDatabase jsonDatabase) {
        this.plugin = plugin;
        this.jsonDatabase = jsonDatabase;
        this.profiles = new HashMap<>(jsonDatabase.read(FILE_NAME, STORE_TYPE, HashMap::new));
    }

    public void ensureProfile(UUID playerId) {
        profiles.computeIfAbsent(playerId, ignored -> new PowerProfile(DEFAULT_MAX_POWER, DEFAULT_MAX_POWER));
    }

    public void increaseMaxPower(UUID playerId, int amount) {
        ensureProfile(playerId);
        profiles.get(playerId).increaseMaxPower(amount);
    }

    @Override
    public int getCurrentPower(UUID playerId) {
        ensureProfile(playerId);
        return profiles.get(playerId).getCurrentPower();
    }

    @Override
    public int getMaxPower(UUID playerId) {
        ensureProfile(playerId);
        return profiles.get(playerId).getMaxPower();
    }

    @Override
    public int getFactionPower(Faction faction) {
        if (faction == null) {
            return 0;
        }

        int total = 0;
        for (UUID memberId : faction.getMembers()) {
            total += getCurrentPower(memberId);
        }
        int effective = (int) Math.floor(total * faction.getUpgrade().getPowerMultiplier());
        return Math.max(0, effective - faction.getOverclaimPenalty());
    }

    @Override
    public boolean isUnderPowered(Faction faction) {
        return faction != null && getFactionPower(faction) < faction.getLandCount();
    }

    @Override
    public boolean canClaimMore(Faction faction) {
        return faction != null && getFactionPower(faction) >= faction.getLandCount() + 1;
    }

    public void applyOverclaimPenalty(Faction faction) {
        if (faction != null) {
            faction.addOverclaimPenalty(OVERCLAIM_COST);
        }
    }

    public void handleDeath(Player player) {
        ensureProfile(player.getUniqueId());
        profiles.get(player.getUniqueId()).decrease(DEATH_LOSS);
        player.sendMessage("§cVous perdez " + DEATH_LOSS + " points de power. Power actuel : §e" + getCurrentPower(player.getUniqueId()));
    }

    public void rewardOnlinePlayers(Collection<? extends Player> players) {
        for (Player player : players) {
            ensureProfile(player.getUniqueId());
            PowerProfile profile = profiles.get(player.getUniqueId());
            int before = profile.getCurrentPower();
            profile.increase(PASSIVE_GAIN);

            if (profile.getCurrentPower() > before) {
                player.sendMessage("§aVous recuperez " + PASSIVE_GAIN + " point de power. §7(" + profile.getCurrentPower() + "/" + profile.getMaxPower() + ")");
            }
        }

        boolean changed = false;
        for (Faction faction : plugin.getFactionManager().getFactions()) {
            int previousPenalty = faction.getOverclaimPenalty();
            faction.reduceOverclaimPenalty(PASSIVE_GAIN);
            if (previousPenalty != faction.getOverclaimPenalty()) {
                changed = true;
            }
        }

        if (changed) {
            plugin.getFactionManager().save();
        }
    }

    @Override
    public void save() {
        jsonDatabase.write(FILE_NAME, profiles);
    }
}
