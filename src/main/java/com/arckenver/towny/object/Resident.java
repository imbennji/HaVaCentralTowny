package com.arckenver.towny.object;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

/**
 * Sponge representation of a Towny resident. This class mirrors the Spigot
 * plugin's Resident data model so feature parity (minus nations) can be reached.
 */
public class Resident {
    private UUID id;

    // Identity & profile metadata
    private String title = "";
    private String about = "";
    private String surname = "";
    private String chatPrefix = "";
    private String chatSuffix = "";
    private String locale = "";
    private String lastKnownName = "";
    private Queue<String> nameHistory = new ArrayDeque<>();

    // Friends
    private Set<UUID> friends = new LinkedHashSet<>();

    // Town membership metadata
    private UUID townId;
    private Set<String> townRanks = new LinkedHashSet<>();
    private List<TownRankHistoryEntry> townRankHistory = new ArrayList<>();

    // Lifecycle timestamps
    private long registeredAt = 0L;
    private long lastOnlineAt = 0L;
    private long lastLogoutAt = 0L;
    private long taxExemptUntil = 0L;

    // Economy integration
    private BigDecimal balance = BigDecimal.ZERO;
    private List<EconomyLedgerEntry> ledger = new ArrayList<>();
    private boolean bankrupt = false;
    private long bankruptcyDeclaredAt = 0L;
    private long lastTaxPaidAt = 0L;

    // Jail mechanics
    private boolean jailed = false;
    private UUID jailTownId;
    private UUID jailPlotId;
    private long jailReleaseAt = 0L;
    private int jailEscapes = 0;
    private List<JailRequest> jailRequests = new ArrayList<>();

    // Spawn/respawn management
    private String preferredSpawn = "";
    private UUID lastSpawnTownId;
    private long lastSpawnAt = 0L;
    private long spawnCooldownEndsAt = 0L;
    private long bedSpawnWarmupEndsAt = 0L;
    private boolean spawnAtHomeOnLogin = false;

    // Map toggle + throttle
    private boolean autoMap = false;
    private long lastAutoMapTs = 0L;

    // Modes (Spigot-like)
    private Set<String> modes = new LinkedHashSet<>();

    // Toggles (legacy compatibility)
    private boolean autoClaim = false;
    private boolean autoUnclaim = false;
    private boolean preferBedSpawn = false;

    private boolean plotBorder = false;
    private boolean constantPlotBorder = false;
    private boolean townBorder = false;
    private boolean borderTitles = true;

    private boolean pvp = false;
    private boolean fire = false;
    private boolean explosion = false;
    private boolean mobs = false;

    private boolean spy = false;
    private boolean ignorePlots = false;

    private boolean plotGroupMode = false;
    private boolean districtMode = false;

    private boolean adminBypass = false;
    private boolean infoTool = false;

    // Miscellaneous state
    private boolean ignoreTowny = false;
    private boolean debugMode = false;
    private String protectionStatus = "";
    private Set<String> pendingTownInvites = new LinkedHashSet<>();
    private Set<UUID> pendingPlotInvites = new LinkedHashSet<>();
    private Map<String, Integer> warStats = new LinkedHashMap<>();
    private Set<String> mutedNotifications = new LinkedHashSet<>();

    public Resident() {
        // gson
    }

    public Resident(UUID id) {
        this.id = id;
        normalizeAfterLoad(id);
    }

    /**
     * Ensures in-memory defaults after loading from persistence.
     */
    public void normalizeAfterLoad(UUID identifier) {
        this.id = identifier;
        if (friends == null) friends = new LinkedHashSet<>();
        if (townRanks == null) townRanks = new LinkedHashSet<>();
        if (townRankHistory == null) townRankHistory = new ArrayList<>();
        if (ledger == null) ledger = new ArrayList<>();
        if (jailRequests == null) jailRequests = new ArrayList<>();
        if (modes == null) modes = new LinkedHashSet<>();
        if (pendingTownInvites == null) pendingTownInvites = new LinkedHashSet<>();
        if (pendingPlotInvites == null) pendingPlotInvites = new LinkedHashSet<>();
        if (warStats == null) warStats = new LinkedHashMap<>();
        if (mutedNotifications == null) mutedNotifications = new LinkedHashSet<>();
        if (nameHistory == null) nameHistory = new ArrayDeque<>();
        if (balance == null) balance = BigDecimal.ZERO;
        if (registeredAt == 0L) registeredAt = System.currentTimeMillis();
        if (lastKnownName == null) lastKnownName = "";
        if (title == null) title = "";
        if (about == null) about = "";
        if (surname == null) surname = "";
        if (chatPrefix == null) chatPrefix = "";
        if (chatSuffix == null) chatSuffix = "";
        if (locale == null) locale = "";
        if (preferredSpawn == null) preferredSpawn = "";
        if (protectionStatus == null) protectionStatus = "";
    }

    public UUID getId() { return id; }

    // ---- Identity ----
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = (title == null ? "" : title); }

    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = (about == null ? "" : about); }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname == null ? "" : surname; }

    public String getChatPrefix() { return chatPrefix; }
    public void setChatPrefix(String chatPrefix) { this.chatPrefix = chatPrefix == null ? "" : chatPrefix; }

    public String getChatSuffix() { return chatSuffix; }
    public void setChatSuffix(String chatSuffix) { this.chatSuffix = chatSuffix == null ? "" : chatSuffix; }

    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale == null ? "" : locale; }

    public String getLastKnownName() { return lastKnownName; }
    public void setLastKnownName(String lastKnownName) {
        this.lastKnownName = lastKnownName == null ? "" : lastKnownName;
        if (!this.lastKnownName.isEmpty()) {
            if (!nameHistory.contains(this.lastKnownName)) {
                nameHistory.add(this.lastKnownName);
                while (nameHistory.size() > 10) {
                    nameHistory.poll();
                }
            }
        }
    }

    public Queue<String> getNameHistory() { return nameHistory; }

    // ---- Social ----
    public Set<UUID> getFriends() { return friends; }

    // ---- Town metadata ----
    public UUID getTownId() { return townId; }
    public void setTownId(UUID townId) { this.townId = townId; }

    public Set<String> getTownRanks() { return townRanks; }

    public boolean addTownRank(String rank, UUID actor) {
        if (rank == null || rank.trim().isEmpty()) return false;
        boolean added = townRanks.add(rank.toLowerCase());
        if (added) {
            townRankHistory.add(new TownRankHistoryEntry(System.currentTimeMillis(), rank.toLowerCase(), actor, TownRankHistoryEntry.ACTION_ADD));
        }
        return added;
    }

    public boolean removeTownRank(String rank, UUID actor) {
        if (rank == null || rank.trim().isEmpty()) return false;
        boolean removed = townRanks.remove(rank.toLowerCase());
        if (removed) {
            townRankHistory.add(new TownRankHistoryEntry(System.currentTimeMillis(), rank.toLowerCase(), actor, TownRankHistoryEntry.ACTION_REMOVE));
        }
        return removed;
    }

    public void clearTownRanks(UUID actor) {
        for (String rank : new ArrayList<>(townRanks)) {
            removeTownRank(rank, actor);
        }
    }

    public List<TownRankHistoryEntry> getTownRankHistory() { return townRankHistory; }

    public boolean hasTown() { return townId != null; }

    public boolean hasRank(String rank) {
        return rank != null && townRanks.contains(rank.toLowerCase());
    }

    public Optional<String> getPrimaryTownRank() {
        return townRanks.stream().findFirst();
    }

    public boolean isMayor() {
        return hasRank("mayor") || hasRank("king");
    }

    public boolean isAssistant() {
        return hasRank("assistant") || hasRank("comayor") || hasRank("assistant mayor");
    }

    // ---- Lifecycle ----
    public long getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(long registeredAt) { this.registeredAt = registeredAt; }

    public long getLastOnlineAt() { return lastOnlineAt; }
    public void setLastOnlineAt(long lastOnlineAt) { this.lastOnlineAt = lastOnlineAt; }

    public long getLastLogoutAt() { return lastLogoutAt; }
    public void setLastLogoutAt(long lastLogoutAt) { this.lastLogoutAt = lastLogoutAt; }

    public long getTaxExemptUntil() { return taxExemptUntil; }
    public void setTaxExemptUntil(long taxExemptUntil) { this.taxExemptUntil = taxExemptUntil; }

    // ---- Economy ----
    public BigDecimal getBalance() { return balance; }

    public void setBalance(BigDecimal balance) {
        this.balance = balance == null ? BigDecimal.ZERO : balance;
    }

    public boolean isBankrupt() { return bankrupt; }
    public void setBankrupt(boolean bankrupt) { this.bankrupt = bankrupt; }

    public long getBankruptcyDeclaredAt() { return bankruptcyDeclaredAt; }
    public void setBankruptcyDeclaredAt(long bankruptcyDeclaredAt) { this.bankruptcyDeclaredAt = bankruptcyDeclaredAt; }

    public long getLastTaxPaidAt() { return lastTaxPaidAt; }
    public void setLastTaxPaidAt(long lastTaxPaidAt) { this.lastTaxPaidAt = lastTaxPaidAt; }

    public List<EconomyLedgerEntry> getLedger() { return ledger; }

    public void recordTransaction(BigDecimal amount, String type, String cause) {
        if (amount == null) return;
        this.balance = this.balance.add(amount);
        ledger.add(new EconomyLedgerEntry(System.currentTimeMillis(), amount, type, cause, this.balance));
        while (ledger.size() > 50) {
            ledger.remove(0);
        }
    }

    // ---- Jail ----
    public boolean isJailed() { return jailed; }
    public void setJailed(boolean jailed) { this.jailed = jailed; }

    public UUID getJailTownId() { return jailTownId; }
    public void setJailTownId(UUID jailTownId) { this.jailTownId = jailTownId; }

    public UUID getJailPlotId() { return jailPlotId; }
    public void setJailPlotId(UUID jailPlotId) { this.jailPlotId = jailPlotId; }

    public long getJailReleaseAt() { return jailReleaseAt; }
    public void setJailReleaseAt(long jailReleaseAt) { this.jailReleaseAt = jailReleaseAt; }

    public int getJailEscapes() { return jailEscapes; }
    public void setJailEscapes(int jailEscapes) { this.jailEscapes = Math.max(0, jailEscapes); }

    public List<JailRequest> getJailRequests() { return jailRequests; }
    public void addJailRequest(JailRequest request) {
        if (request != null) {
            jailRequests.add(request);
        }
    }

    public void pruneExpiredJailRequests(long now) {
        jailRequests.removeIf(req -> req.getExpiresAt() > 0 && req.getExpiresAt() < now);
    }

    // ---- Spawn ----
    public String getPreferredSpawn() { return preferredSpawn; }
    public void setPreferredSpawn(String preferredSpawn) { this.preferredSpawn = preferredSpawn == null ? "" : preferredSpawn; }

    public UUID getLastSpawnTownId() { return lastSpawnTownId; }
    public void setLastSpawnTownId(UUID lastSpawnTownId) { this.lastSpawnTownId = lastSpawnTownId; }

    public long getLastSpawnAt() { return lastSpawnAt; }
    public void setLastSpawnAt(long lastSpawnAt) { this.lastSpawnAt = lastSpawnAt; }

    public long getSpawnCooldownEndsAt() { return spawnCooldownEndsAt; }
    public void setSpawnCooldownEndsAt(long spawnCooldownEndsAt) { this.spawnCooldownEndsAt = spawnCooldownEndsAt; }

    public long getBedSpawnWarmupEndsAt() { return bedSpawnWarmupEndsAt; }
    public void setBedSpawnWarmupEndsAt(long bedSpawnWarmupEndsAt) { this.bedSpawnWarmupEndsAt = bedSpawnWarmupEndsAt; }

    public boolean isSpawnAtHomeOnLogin() { return spawnAtHomeOnLogin; }
    public void setSpawnAtHomeOnLogin(boolean spawnAtHomeOnLogin) { this.spawnAtHomeOnLogin = spawnAtHomeOnLogin; }

    // ---- Map ----
    public boolean isAutoMap() { return autoMap; }
    public void setAutoMap(boolean autoMap) { this.autoMap = autoMap; }

    public long getLastAutoMapTs() { return lastAutoMapTs; }
    public void setLastAutoMapTs(long ts) { this.lastAutoMapTs = ts; }

    // ---- Modes ----
    public Set<String> getModes() { return modes; }

    public boolean toggleMode(String mode) {
        if (mode == null || mode.trim().isEmpty()) return false;
        String normalized = mode.toLowerCase();
        if (modes.contains(normalized)) {
            modes.remove(normalized);
            return false;
        }
        modes.add(normalized);
        return true;
    }

    public boolean setMode(String mode, boolean enabled) {
        if (mode == null || mode.trim().isEmpty()) return false;
        String normalized = mode.toLowerCase();
        if (enabled) {
            return modes.add(normalized);
        }
        return modes.remove(normalized);
    }

    // ---- Toggles (legacy) ----
    public boolean isAutoClaim() { return autoClaim; }
    public void setAutoClaim(boolean autoClaim) { this.autoClaim = autoClaim; }

    public boolean isAutoUnclaim() { return autoUnclaim; }
    public void setAutoUnclaim(boolean autoUnclaim) { this.autoUnclaim = autoUnclaim; }

    public boolean isPreferBedSpawn() { return preferBedSpawn; }
    public void setPreferBedSpawn(boolean preferBedSpawn) { this.preferBedSpawn = preferBedSpawn; }

    public boolean isPlotBorder() { return plotBorder; }
    public void setPlotBorder(boolean plotBorder) { this.plotBorder = plotBorder; }

    public boolean isConstantPlotBorder() { return constantPlotBorder; }
    public void setConstantPlotBorder(boolean constantPlotBorder) { this.constantPlotBorder = constantPlotBorder; }

    public boolean isTownBorder() { return townBorder; }
    public void setTownBorder(boolean townBorder) { this.townBorder = townBorder; }

    public boolean isBorderTitles() { return borderTitles; }
    public void setBorderTitles(boolean borderTitles) { this.borderTitles = borderTitles; }

    public boolean isPvp() { return pvp; }
    public void setPvp(boolean pvp) { this.pvp = pvp; }

    public boolean isFire() { return fire; }
    public void setFire(boolean fire) { this.fire = fire; }

    public boolean isExplosion() { return explosion; }
    public void setExplosion(boolean explosion) { this.explosion = explosion; }

    public boolean isMobs() { return mobs; }
    public void setMobs(boolean mobs) { this.mobs = mobs; }

    public boolean isSpy() { return spy; }
    public void setSpy(boolean spy) { this.spy = spy; }

    public boolean isIgnorePlots() { return ignorePlots; }
    public void setIgnorePlots(boolean ignorePlots) { this.ignorePlots = ignorePlots; }

    public boolean isPlotGroupMode() { return plotGroupMode; }
    public void setPlotGroupMode(boolean plotGroupMode) { this.plotGroupMode = plotGroupMode; }

    public boolean isDistrictMode() { return districtMode; }
    public void setDistrictMode(boolean districtMode) { this.districtMode = districtMode; }

    public boolean isAdminBypass() { return adminBypass; }
    public void setAdminBypass(boolean adminBypass) { this.adminBypass = adminBypass; }

    public boolean isInfoTool() { return infoTool; }
    public void setInfoTool(boolean infoTool) { this.infoTool = infoTool; }

    // ---- Misc ----
    public boolean isIgnoreTowny() { return ignoreTowny; }
    public void setIgnoreTowny(boolean ignoreTowny) { this.ignoreTowny = ignoreTowny; }

    public boolean isDebugMode() { return debugMode; }
    public void setDebugMode(boolean debugMode) { this.debugMode = debugMode; }

    public String getProtectionStatus() { return protectionStatus; }
    public void setProtectionStatus(String protectionStatus) { this.protectionStatus = protectionStatus == null ? "" : protectionStatus; }

    public Set<String> getPendingTownInvites() { return pendingTownInvites; }
    public Set<UUID> getPendingPlotInvites() { return pendingPlotInvites; }

    public void addTownInvite(String townName) {
        if (townName != null && !townName.trim().isEmpty()) {
            pendingTownInvites.add(townName);
        }
    }

    public void removeTownInvite(String townName) {
        if (townName != null) {
            pendingTownInvites.remove(townName);
        }
    }

    public void clearTownInvites() {
        pendingTownInvites.clear();
    }

    public void addPlotInvite(UUID plotId) {
        if (plotId != null) {
            pendingPlotInvites.add(plotId);
        }
    }

    public void removePlotInvite(UUID plotId) {
        if (plotId != null) {
            pendingPlotInvites.remove(plotId);
        }
    }

    public Map<String, Integer> getWarStats() { return warStats; }

    public void incrementWarStat(String key) {
        if (key == null) return;
        warStats.merge(key, 1, Integer::sum);
    }

    public Set<String> getMutedNotifications() { return mutedNotifications; }

    public void setNotificationMuted(String notification, boolean muted) {
        if (notification == null) return;
        String norm = notification.toLowerCase();
        if (muted) mutedNotifications.add(norm); else mutedNotifications.remove(norm);
    }

    public boolean isNotificationMuted(String notification) {
        return notification != null && mutedNotifications.contains(notification.toLowerCase());
    }

    // ------------------------------------------------------------
    // Nested value objects used for persistence
    // ------------------------------------------------------------
    public static final class TownRankHistoryEntry {
        public static final String ACTION_ADD = "add";
        public static final String ACTION_REMOVE = "remove";

        private long timestamp;
        private String rank;
        private UUID actor;
        private String action;

        public TownRankHistoryEntry() {}

        public TownRankHistoryEntry(long timestamp, String rank, UUID actor, String action) {
            this.timestamp = timestamp;
            this.rank = rank == null ? "" : rank;
            this.actor = actor;
            this.action = action == null ? ACTION_ADD : action;
        }

        public long getTimestamp() { return timestamp; }
        public String getRank() { return rank; }
        public UUID getActor() { return actor; }
        public String getAction() { return action; }
    }

    public static final class EconomyLedgerEntry {
        private long timestamp;
        private BigDecimal amount;
        private String type;
        private String cause;
        private BigDecimal resultingBalance;

        public EconomyLedgerEntry() {}

        public EconomyLedgerEntry(long timestamp, BigDecimal amount, String type, String cause, BigDecimal resultingBalance) {
            this.timestamp = timestamp;
            this.amount = amount;
            this.type = type == null ? "" : type;
            this.cause = cause == null ? "" : cause;
            this.resultingBalance = resultingBalance;
        }

        public long getTimestamp() { return timestamp; }
        public BigDecimal getAmount() { return amount; }
        public String getType() { return type; }
        public String getCause() { return cause; }
        public BigDecimal getResultingBalance() { return resultingBalance; }
    }

    public static final class JailRequest {
        private UUID townId;
        private long expiresAt;
        private String reason;

        public JailRequest() {}

        public JailRequest(UUID townId, long expiresAt, String reason) {
            this.townId = townId;
            this.expiresAt = expiresAt;
            this.reason = reason == null ? "" : reason;
        }

        public UUID getTownId() { return townId; }
        public long getExpiresAt() { return expiresAt; }
        public String getReason() { return reason; }
    }
}
