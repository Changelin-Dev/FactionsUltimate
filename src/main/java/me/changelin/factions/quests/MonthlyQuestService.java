package me.changelin.factions.quests;

import com.google.gson.reflect.TypeToken;
import me.changelin.factions.FactionsPlugin;
import me.changelin.factions.api.QuestService;
import me.changelin.factions.database.JsonDatabase;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MonthlyQuestService implements QuestService {

    private static final String HISTORY_FILE_NAME = "quests-history.json";
    private static final String STATE_FILE_NAME = "monthly-quest.json";
    private static final Type STORE_TYPE = new TypeToken<List<QuestHistoryEntry>>() { }.getType();
    private static final Type STATE_TYPE = new TypeToken<MonthlyQuestState>() { }.getType();
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final String QUEST_ID = "community_diamond_drive";
    private static final String OBJECTIVE = "Donner 100 000 blocs de Diamant";
    private static final double TARGET_POINTS = 1_000_000.0;

    private final FactionsPlugin plugin;
    private final JsonDatabase jsonDatabase;
    private final List<QuestHistoryEntry> history;
    private MonthlyQuestState currentQuest;

    public MonthlyQuestService(FactionsPlugin plugin, JsonDatabase jsonDatabase) {
        this.plugin = plugin;
        this.jsonDatabase = jsonDatabase;
        this.history = new ArrayList<>(jsonDatabase.read(HISTORY_FILE_NAME, STORE_TYPE, ArrayList::new));
        this.currentQuest = jsonDatabase.read(STATE_FILE_NAME, STATE_TYPE, this::createDefaultQuest);
        ensureCurrentMonth();
    }

    @Override
    public List<QuestHistoryEntry> getHistory() {
        return List.copyOf(history);
    }

    @Override
    public void recordHistory(QuestHistoryEntry entry) {
        history.add(entry);
    }

    @Override
    public MonthlyQuestState getCurrentQuest() {
        ensureCurrentMonth();
        return currentQuest;
    }

    public void ensureCurrentMonth() {
        String activeMonth = currentMonthKey();
        if (!activeMonth.equals(currentQuest.getMonthKey())) {
            archiveCurrentQuest();
            currentQuest = createDefaultQuest();
            save();
        }
    }

    public QuestDepositResult deposit(Player player, ItemStack[] contents) {
        ensureCurrentMonth();
        double totalPoints = 0.0;
        int consumedStacks = 0;

        for (ItemStack item : contents) {
            if (item == null || item.getType().isAir()) {
                continue;
            }

            double value = getPointsFor(item.getType()) * item.getAmount();
            if (value <= 0.0) {
                continue;
            }

            totalPoints += value;
            consumedStacks++;
        }

        if (totalPoints <= 0.0) {
            return new QuestDepositResult(0.0, consumedStacks, currentQuest.getProgress());
        }

        currentQuest.addContribution(player.getUniqueId(), totalPoints);
        save();
        return new QuestDepositResult(totalPoints, consumedStacks, currentQuest.getProgress());
    }

    public double getPointsFor(Material material) {
        return switch (material) {
            case DIAMOND -> 10.0;
            case COBBLESTONE -> 0.1;
            default -> 0.0;
        };
    }

    public List<QuestRewardClaim> getAvailableRewards(UUID playerId) {
        ensureCurrentMonth();
        List<QuestRewardClaim> rewards = new ArrayList<>();
        QuestContribution contribution = currentQuest.getContribution(playerId);
        double progress = currentQuest.getProgress();

        if (progress >= 0.25 && !contribution.hasClaimedTier(1)) {
            rewards.add(new QuestRewardClaim(1, 1, 500.0, Material.IRON_INGOT, 8));
        }
        if (progress >= 0.60 && !contribution.hasClaimedTier(2)) {
            rewards.add(new QuestRewardClaim(2, 1, 1500.0, Material.GOLD_INGOT, 12));
        }
        if (progress >= 1.0 && !contribution.hasClaimedTier(3)) {
            rewards.add(new QuestRewardClaim(3, 2, 5000.0, Material.DIAMOND, 16));
        }

        return rewards;
    }

    public double getContributionPoints(UUID playerId) {
        ensureCurrentMonth();
        return currentQuest.getContribution(playerId).getTotalPoints();
    }

    public boolean claimReward(Player player, int tier) {
        ensureCurrentMonth();
        QuestContribution contribution = currentQuest.getContribution(player.getUniqueId());
        QuestRewardClaim reward = getAvailableRewards(player.getUniqueId()).stream()
                .filter(entry -> entry.tier() == tier)
                .findFirst()
                .orElse(null);

        if (reward == null) {
            return false;
        }

        contribution.setClaimedTier(tier);
        plugin.getPowerManager().increaseMaxPower(player.getUniqueId(), reward.maxPowerGain());

        if (plugin.getEconomyHook().isAvailable()) {
            plugin.getEconomyHook().getEconomy().depositPlayer(player, reward.moneyReward());
        }

        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(new ItemStack(reward.bonusMaterial(), reward.amount()));
        if (!leftovers.isEmpty()) {
            leftovers.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        }

        save();
        return true;
    }

    @Override
    public void save() {
        jsonDatabase.write(HISTORY_FILE_NAME, history);
        jsonDatabase.write(STATE_FILE_NAME, currentQuest);
    }

    private MonthlyQuestState createDefaultQuest() {
        return new MonthlyQuestState(currentMonthKey(), QUEST_ID, OBJECTIVE, TARGET_POINTS);
    }

    private void archiveCurrentQuest() {
        if (currentQuest == null) {
            return;
        }

        recordHistory(new QuestHistoryEntry("COMMUNAUTE", currentQuest.getQuestId(), currentQuest.getMonthKey(), LocalDate.now().toString()));
        for (UUID playerId : currentQuest.getContributions().keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.sendMessage("§6Les quetes mensuelles ont ete reinitialisees pour " + currentMonthKey() + ".");
            }
        }
    }

    private String currentMonthKey() {
        return YearMonth.now().format(MONTH_FORMAT);
    }

    public String formatProgress() {
        return String.format(Locale.US, "%.2f%%", currentQuest.getProgress() * 100.0);
    }

    public record QuestDepositResult(double points, int consumedStacks, double progress) {
    }

    public record QuestRewardClaim(int tier, int maxPowerGain, double moneyReward, Material bonusMaterial, int amount) {
    }
}
