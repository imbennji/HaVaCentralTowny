package com.arckenver.towny.object;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * Lightweight representation of a Towny nation. The goal of this class is not
 * to perfectly mirror every single option provided by the Spigot plugin but to
 * expose the essential state needed by the Sponge remake: capital ownership,
 * town membership, public toggles and a nation spawn. Additional data can be
 * layered on later without breaking serialization.
 */
public class Nation {
    private UUID uuid;
    private String name;
    private String tag;
    private String board;
    private UUID capitalTown;
    private boolean open;
    private boolean neutral;
    private double taxes;
    private Location<World> spawn;

    private final Set<UUID> towns = new LinkedHashSet<>();
    private final Set<UUID> allies = new LinkedHashSet<>();
    private final Set<UUID> enemies = new LinkedHashSet<>();

    public Nation(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.board = "";
        this.tag = null;
        this.open = false;
        this.neutral = false;
        this.taxes = 0D;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public String getRealName() {
        return name;
    }

    public String getName() {
        return name.replace("_", " ");
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasTag() {
        return tag != null && !tag.trim().isEmpty();
    }

    public String getTag() {
        return hasTag() ? tag : getName();
    }

    public void setTag(String tag) {
        this.tag = (tag == null || tag.trim().isEmpty()) ? null : tag.trim();
    }

    public String getBoard() {
        return board == null ? "" : board;
    }

    public void setBoard(String board) {
        this.board = board == null ? "" : board.trim();
    }

    public UUID getCapital() {
        return capitalTown;
    }

    public void setCapital(UUID capitalTown) {
        this.capitalTown = capitalTown;
        if (capitalTown != null) {
            towns.add(capitalTown);
        }
    }

    public boolean isCapital(UUID townUUID) {
        return capitalTown != null && capitalTown.equals(townUUID);
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isNeutral() {
        return neutral;
    }

    public void setNeutral(boolean neutral) {
        this.neutral = neutral;
    }

    public double getTaxes() {
        return taxes;
    }

    public void setTaxes(double taxes) {
        this.taxes = taxes;
    }

    public Location<World> getSpawn() {
        return spawn;
    }

    public void setSpawn(Location<World> spawn) {
        this.spawn = spawn;
    }

    public Set<UUID> getTowns() {
        return Collections.unmodifiableSet(towns);
    }

    public boolean hasTown(UUID townUUID) {
        return towns.contains(townUUID);
    }

    public void addTown(UUID townUUID) {
        towns.add(townUUID);
    }

    public void removeTown(UUID townUUID) {
        towns.remove(townUUID);
        if (capitalTown != null && capitalTown.equals(townUUID)) {
            capitalTown = null;
        }
    }

    public Set<UUID> getAllies() {
        return Collections.unmodifiableSet(allies);
    }

    public void addAlly(UUID nationUUID) {
        allies.add(nationUUID);
    }

    public void removeAlly(UUID nationUUID) {
        allies.remove(nationUUID);
    }

    public Set<UUID> getEnemies() {
        return Collections.unmodifiableSet(enemies);
    }

    public void addEnemy(UUID nationUUID) {
        enemies.add(nationUUID);
    }

    public void removeEnemy(UUID nationUUID) {
        enemies.remove(nationUUID);
    }
}
