package me.changelin.factions.power;

public class PowerProfile {

    private int currentPower;
    private int maxPower;

    public PowerProfile() {
        this(10, 10);
    }

    public PowerProfile(int currentPower, int maxPower) {
        this.maxPower = Math.max(0, maxPower);
        this.currentPower = Math.max(0, Math.min(currentPower, this.maxPower));
    }

    public int getCurrentPower() {
        return currentPower;
    }

    public int getMaxPower() {
        return maxPower;
    }

    public void increaseMaxPower(int amount) {
        maxPower = Math.max(0, maxPower + Math.max(0, amount));
        currentPower = Math.min(currentPower, maxPower);
    }

    public void increase(int amount) {
        currentPower = Math.min(maxPower, currentPower + Math.max(0, amount));
    }

    public void decrease(int amount) {
        currentPower = Math.max(0, currentPower - Math.max(0, amount));
    }
}
