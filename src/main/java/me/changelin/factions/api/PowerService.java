package me.changelin.factions.api;

import me.changelin.factions.core.Faction;

import java.util.UUID;

public interface PowerService extends AutoSavable {
    int getCurrentPower(UUID playerId);

    int getMaxPower(UUID playerId);

    int getFactionPower(Faction faction);

    boolean isUnderPowered(Faction faction);

    boolean canClaimMore(Faction faction);
}
