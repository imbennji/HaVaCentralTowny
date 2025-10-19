package com.arckenver.towny.object;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import com.arckenver.towny.LanguageHandler;

public enum PlotType {
        RESIDENTIAL("default", builder -> builder
                        .alias("residential")
                        .alias("home")),
        SHOP("shop", builder -> builder
                        .lockPerm(Towny.TYPE_RESIDENT, Towny.PERM_SWITCH, true)
                        .lockPerm(Towny.TYPE_RESIDENT, Towny.PERM_ITEM_USE, true)
                        .lockPerm(Towny.TYPE_OUTSIDER, Towny.PERM_SWITCH, true)
                        .lockPerm(Towny.TYPE_OUTSIDER, Towny.PERM_ITEM_USE, true)),
        EMBASSY("embassy", builder -> builder
                        .alias("emb")
                        .allowForeignOwnership()),
        FARM("farm", builder -> builder
                        .defaultPerm(Towny.TYPE_RESIDENT, Towny.PERM_DESTROY, true)
                        .defaultPerm(Towny.TYPE_OUTSIDER, Towny.PERM_DESTROY, true)
                        .lockFlag("mobs", true)),
        INN("inn", builder -> builder
                        .lockPerm(Towny.TYPE_OUTSIDER, Towny.PERM_SWITCH, true)
                        .lockPerm(Towny.TYPE_OUTSIDER, Towny.PERM_ITEM_USE, true)
                        .alias("hotel")),
        JAIL("jail", builder -> builder
                        .lockFlag("pvp", false)),
        ARENA("arena", builder -> builder
                        .lockFlag("pvp", true)),
        WILDS("wilds", builder -> builder
                        .defaultPerm(Towny.TYPE_RESIDENT, Towny.PERM_BUILD, true)
                        .defaultPerm(Towny.TYPE_RESIDENT, Towny.PERM_DESTROY, true)
                        .defaultPerm(Towny.TYPE_RESIDENT, Towny.PERM_SWITCH, true)
                        .defaultPerm(Towny.TYPE_RESIDENT, Towny.PERM_ITEM_USE, true)
                        .defaultPerm(Towny.TYPE_OUTSIDER, Towny.PERM_BUILD, true)
                        .defaultPerm(Towny.TYPE_OUTSIDER, Towny.PERM_DESTROY, true)
                        .defaultPerm(Towny.TYPE_OUTSIDER, Towny.PERM_SWITCH, true)
                        .defaultPerm(Towny.TYPE_OUTSIDER, Towny.PERM_ITEM_USE, true));

        private static final Map<String, PlotType> LOOKUP = new HashMap<>();

        static {
                for (PlotType type : values()) {
                        for (String alias : type.aliases) {
                                LOOKUP.put(alias, type);
                        }
                }
        }

        private final String id;
        private final Set<String> aliases;
        private final Map<String, Boolean> defaultFlags;
        private final Map<String, Boolean> forcedFlags;
        private final Map<PlotPermKey, Boolean> defaultPerms;
        private final Map<PlotPermKey, Boolean> forcedPerms;
        private final boolean foreignOwnership;

        PlotType(String id, Consumer<RuleBuilder> ruleConfigurer) {
                this.id = Objects.requireNonNull(id, "id");
                RuleBuilder builder = new RuleBuilder();
                builder.alias(id);
                builder.alias(name().toLowerCase(Locale.ENGLISH));
                if (ruleConfigurer != null) {
                        ruleConfigurer.accept(builder);
                }
                this.aliases = Collections.unmodifiableSet(new HashSet<>(builder.aliases));
                this.defaultFlags = Collections.unmodifiableMap(new HashMap<>(builder.defaultFlags));
                this.forcedFlags = Collections.unmodifiableMap(new HashMap<>(builder.forcedFlags));
                this.defaultPerms = Collections.unmodifiableMap(new HashMap<>(builder.defaultPerms));
                this.forcedPerms = Collections.unmodifiableMap(new HashMap<>(builder.forcedPerms));
                this.foreignOwnership = builder.foreignOwnership;
        }

        public String getId() {
                return id;
        }

        public boolean canChangeFlag(String flag) {
                return !forcedFlags.containsKey(normalize(flag));
        }

        public Boolean getForcedFlag(String flag) {
                return forcedFlags.get(normalize(flag));
        }

        public boolean canChangePerm(String type, String perm) {
                return !forcedPerms.containsKey(new PlotPermKey(type, perm));
        }

        public Boolean getForcedPerm(String type, String perm) {
                return forcedPerms.get(new PlotPermKey(type, perm));
        }

        public boolean allowsForeignOwnership() {
                return foreignOwnership;
        }

        public void applyDefaults(Plot plot) {
                for (Map.Entry<String, Boolean> entry : defaultFlags.entrySet()) {
                        plot.setFlagInternal(entry.getKey(), entry.getValue());
                }
                for (Map.Entry<String, Boolean> entry : forcedFlags.entrySet()) {
                        plot.setFlagInternal(entry.getKey(), entry.getValue());
                }
                for (Map.Entry<PlotPermKey, Boolean> entry : defaultPerms.entrySet()) {
                        plot.setPlotPermInternal(entry.getKey().type, entry.getKey().perm, entry.getValue());
                }
                for (Map.Entry<PlotPermKey, Boolean> entry : forcedPerms.entrySet()) {
                        plot.setPlotPermInternal(entry.getKey().type, entry.getKey().perm, entry.getValue());
                }
        }

        public void applyForced(Plot plot) {
                for (Map.Entry<String, Boolean> entry : forcedFlags.entrySet()) {
                        plot.setFlagInternal(entry.getKey(), entry.getValue());
                }
                for (Map.Entry<PlotPermKey, Boolean> entry : forcedPerms.entrySet()) {
                        plot.setPlotPermInternal(entry.getKey().type, entry.getKey().perm, entry.getValue());
                }
        }

        public String getDisplayName() {
                switch (this) {
                case SHOP:
                        return LanguageHandler.PLOT_TYPE_SHOP;
                case EMBASSY:
                        return LanguageHandler.PLOT_TYPE_EMBASSY;
                case FARM:
                        return LanguageHandler.PLOT_TYPE_FARM;
                case INN:
                        return LanguageHandler.PLOT_TYPE_INN;
                case JAIL:
                        return LanguageHandler.PLOT_TYPE_JAIL;
                case ARENA:
                        return LanguageHandler.PLOT_TYPE_ARENA;
                case WILDS:
                        return LanguageHandler.PLOT_TYPE_WILDS;
                case RESIDENTIAL:
                default:
                        return LanguageHandler.PLOT_TYPE_RESIDENTIAL;
                }
        }

        public static PlotType fromString(String raw) {
                if (raw == null) {
                                return RESIDENTIAL;
                }
                return Optional.ofNullable(LOOKUP.get(raw.toLowerCase(Locale.ENGLISH))).orElse(null);
        }

        public static Collection<String> getAliases() {
                return Collections.unmodifiableSet(new HashSet<>(LOOKUP.keySet()));
        }

        private static String normalize(String key) {
                return key == null ? "" : key.toLowerCase(Locale.ENGLISH);
        }

        private static final class PlotPermKey {
                private final String type;
                private final String perm;

                private PlotPermKey(String type, String perm) {
                        this.type = Towny.canonicalizePlotType(type);
                        this.perm = Towny.canonicalizePerm(perm);
                }

                @Override
                public boolean equals(Object obj) {
                        if (this == obj) {
                                return true;
                        }
                        if (!(obj instanceof PlotPermKey)) {
                                return false;
                        }
                        PlotPermKey other = (PlotPermKey) obj;
                        return Objects.equals(type, other.type) && Objects.equals(perm, other.perm);
                }

                @Override
                public int hashCode() {
                        return Objects.hash(type, perm);
                }
        }

        private static final class RuleBuilder {
                private final Set<String> aliases = new HashSet<>();
                private final Map<String, Boolean> defaultFlags = new HashMap<>();
                private final Map<String, Boolean> forcedFlags = new HashMap<>();
                private final Map<PlotPermKey, Boolean> defaultPerms = new HashMap<>();
                private final Map<PlotPermKey, Boolean> forcedPerms = new HashMap<>();
                private boolean foreignOwnership = false;

                RuleBuilder alias(String alias) {
                        if (alias != null && !alias.isEmpty()) {
                                aliases.add(alias.toLowerCase(Locale.ENGLISH));
                        }
                        return this;
                }

                RuleBuilder defaultFlag(String flag, boolean value) {
                        defaultFlags.put(normalize(flag), value);
                        return this;
                }

                RuleBuilder lockFlag(String flag, boolean value) {
                        String key = normalize(flag);
                        defaultFlags.put(key, value);
                        forcedFlags.put(key, value);
                        return this;
                }

                RuleBuilder defaultPerm(String type, String perm, boolean value) {
                        PlotPermKey key = new PlotPermKey(type, perm);
                        defaultPerms.put(key, value);
                        return this;
                }

                RuleBuilder lockPerm(String type, String perm, boolean value) {
                        PlotPermKey key = new PlotPermKey(type, perm);
                        defaultPerms.put(key, value);
                        forcedPerms.put(key, value);
                        return this;
                }

                RuleBuilder allowForeignOwnership() {
                        this.foreignOwnership = true;
                        return this;
                }
        }
}
