package com.arckenver.towny.object;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class Resident {
    private UUID id;

    // Profile
    private String title = "";
    private String about = "";

    // Friends
    private final Set<UUID> friends = new LinkedHashSet<>();

    // Map toggle + throttle
    private boolean autoMap = false;
    private long lastAutoMapTs = 0L;

    // Toggles (Spigot-like)
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

    public Resident() {} // gson
    public Resident(UUID id) { this.id = id; }

    public UUID getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = (title == null ? "" : title); }

    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = (about == null ? "" : about); }

    public Set<UUID> getFriends() { return friends; }

    public boolean isAutoMap() { return autoMap; }
    public void setAutoMap(boolean autoMap) { this.autoMap = autoMap; }

    public long getLastAutoMapTs() { return lastAutoMapTs; }
    public void setLastAutoMapTs(long ts) { this.lastAutoMapTs = ts; }

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
}
