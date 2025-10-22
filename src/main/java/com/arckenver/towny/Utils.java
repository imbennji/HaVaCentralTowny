package com.arckenver.towny;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.LiteralText.Builder;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.arckenver.towny.claim.ChunkClaimUtils;
import com.arckenver.towny.object.Nation;
import com.arckenver.towny.object.Towny;
import com.arckenver.towny.object.Plot;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import com.arckenver.towny.LanguageHandler;

public class Utils
{
        public static final int CLICKER_NONE = 0;
        public static final int CLICKER_DEFAULT = 1;
        public static final int CLICKER_ADMIN = 2;

        private static String quoteIfNeeded(String input) {
                if (input == null) {
                        return "";
                }
                return input.indexOf(' ') >= 0 ? '"' + input + '"' : input;
        }

        private static final DateTimeFormatter RESIDENT_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                        .withZone(ZoneId.systemDefault());

	// players

	private static final String[] FAKE_PLAYERS = {
			"00000000-0000-0000-0000-000000000000",
			"0d0c4ca0-4ff1-11e4-916c-0800200c9a66",
			"41c82c87-7afb-4024-ba57-13d2c99cae77"};

	public static boolean isFakePlayer(Player player) {
		String uuid = player.getUniqueId().toString();
		for (int i = 0; i < FAKE_PLAYERS.length; ++i) {
			if (uuid.equals(FAKE_PLAYERS[i]))
				return true;
		}
		return false;
	}

	public static boolean isFakePlayer(Event event) {
		return event.getContext().containsKey(EventContextKeys.FAKE_PLAYER);
	}

	public static User getUser(Event event) {
		final Cause cause = event.getCause();
        final EventContext context = event.getContext();
        User user = null;
        if (cause != null) {
            user = cause.first(User.class).orElse(null);
        }

        if (user == null) {
            user = context.get(EventContextKeys.NOTIFIER)
                    .orElse(context.get(EventContextKeys.OWNER)
                            .orElse(context.get(EventContextKeys.CREATOR)
                                    .orElse(null)));
        }

        if (user == null) {
            if (event instanceof ExplosionEvent) {
                // Check igniter
                final Living living = context.get(EventContextKeys.IGNITER).orElse(null);
                if (living != null && living instanceof User) {
                    user = (User) living;
                }
            }
        }

        return user;
	}



	// formatting





        public static Text formatTownyDescription(Towny towny, int clicker) {
                Builder builder = Text.builder("");
                builder.append(
                                Text.of(LanguageHandler.colorGold(), "----------{ "),
                                Text.of(LanguageHandler.colorYellow(),
						((ConfigHandler.getNode("others", "enableTownyRanks").getBoolean())
								? ConfigHandler.getTownyRank(towny.getNumCitizens()).getNode("townyTitle").getString()
								: LanguageHandler.FORMAT_TOWN)
								+ " - " + towny.getDisplayName()),
				Text.of(LanguageHandler.colorGold(), " }----------"));
		// --- show town board (supports & color codes) ---
		String board = towny.getBoard();
		if (board != null && !board.trim().isEmpty()) {
			builder.append(Text.of(LanguageHandler.colorGold(), "\nBoard: "));
			builder.append(TextSerializers.FORMATTING_CODE.deserialize(board));
		}
                builder.append(Text.of(LanguageHandler.colorGold(), "\n", LanguageHandler.TOWN_ID, ": ",
                                LanguageHandler.colorGreen(), towny.getRealName()));

                if (towny.hasNation()) {
                        Nation nation = DataHandler.getNation(towny.getNationUUID());
                        if (nation != null) {
                                builder.append(Text.of(LanguageHandler.colorGold(), "\n", LanguageHandler.FORMAT_NATION, ": ",
                                                LanguageHandler.colorAqua(), nation.getName()));
                        }
                }


		if (!towny.isAdmin()) {
			BigDecimal balance = null;
			if (TownyPlugin.getEcoService() != null) {
				Optional<Account> optAccount = TownyPlugin.getEcoService()
						.getOrCreateAccount("towny-" + towny.getUUID().toString());
				if (optAccount.isPresent()) {
					balance = optAccount.get().getBalance(TownyPlugin.getEcoService().getDefaultCurrency());
				}
			}

                        int claimedBlocks = towny.getRegion().size();
                        int claimedChunksIncremental = ChunkClaimUtils.toChunkCount(claimedBlocks);
                        int maxChunksAllowedIncremental = towny.maxChunkAllowance();

			builder.append(
					Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_SIZE + ": "),
					Text.of(LanguageHandler.colorYellow(), claimedChunksIncremental, "/", maxChunksAllowedIncremental + " chunk(s)"),
					Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_MONEY + ": "),
					(balance == null)
							? Text.of(LanguageHandler.colorGray(), LanguageHandler.FORMAT_UNKNOWN)
							: formatPrice(LanguageHandler.colorYellow(), balance),
					Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_TAXES + ": "),
					formatPrice(LanguageHandler.colorYellow(), BigDecimal.valueOf(towny.getTaxes())),
					Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_UPKEEP + ": "),
					formatPrice(LanguageHandler.colorYellow(), BigDecimal.valueOf(towny.getUpkeep())),
					Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_SPAWN + "(",
							LanguageHandler.colorYellow(), towny.getNumSpawns() + "/" + towny.getMaxSpawns(),
							LanguageHandler.colorGold(), "): ",
							LanguageHandler.colorYellow(), formatTownySpawns(towny, LanguageHandler.colorYellow(), clicker)),
					Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_MAYOR + ": "),
					citizenClickable(LanguageHandler.colorYellow(), DataHandler.getPlayerName(towny.getPresident())),
					Text.of(LanguageHandler.colorDarkGray(), " <- " + LanguageHandler.CLICK)
			);

			builder.append(Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_FLAGS + ":"));
                        for (Entry<String, Boolean> e : towny.getFlags().entrySet())
                        {
                                String flagName = e.getKey();
                                boolean enabled = e.getValue();
                                String flagCommandPrefix = (clicker == CLICKER_ADMIN)
                                                ? "/ta flag " + towny.getRealName() + " "
                                                : "/t flag ";

                                builder.append(Text.of(LanguageHandler.colorGold(),
                                                "\n    " + StringUtils.capitalize(flagName.toLowerCase()) + ": "));
                                builder.append(Text.builder(LanguageHandler.FLAG_ENABLED)
                                                .color(enabled ? LanguageHandler.colorYellow() : LanguageHandler.colorDarkGray())
                                                .onClick(TextActions.runCommand(flagCommandPrefix + flagName + " true"))
                                                .build());
                                builder.append(Text.of(LanguageHandler.colorGold(), "/"));
                                builder.append(Text.builder(LanguageHandler.FLAG_DISABLED)
                                                .color(enabled ? LanguageHandler.colorDarkGray() : LanguageHandler.colorYellow())
                                                .onClick(TextActions.runCommand(flagCommandPrefix + flagName + " false"))
                                                .build());
                                builder.append(Text.of(LanguageHandler.colorDarkGray(), " <- " + LanguageHandler.CLICK));
                        }
		}

		return builder.build();
	}


	public static Text formatCitizenDescription(String name)
	{
		UUID uuid = DataHandler.getPlayerUUID(name);
		if (uuid == null)
		{
			return Text.of(LanguageHandler.colorRed(), LanguageHandler.FORMAT_UNKNOWN);
		}

                Builder builder = Text.builder("");
                builder.append(
                                Text.of(LanguageHandler.colorGold(), "----------{ "),
                                Text.of(LanguageHandler.colorYellow(),
                                                DataHandler.getCitizenTitle(uuid) + " - " + name),
                                Text.of(LanguageHandler.colorGold(), " }----------")
                                );

                String surname = DataHandler.getResidentSurname(uuid);
                String prefix = DataHandler.getResidentChatPrefix(uuid);
                String suffix = DataHandler.getResidentChatSuffix(uuid);
                if ((surname != null && !surname.isEmpty()) || (prefix != null && !prefix.isEmpty()) || (suffix != null && !suffix.isEmpty())) {
                        builder.append(Text.of(LanguageHandler.colorGold(), "\nName style: "));
                        String style = "";
                        if (prefix != null && !prefix.isEmpty()) {
                                style += prefix + " ";
                        }
                        style += name;
                        if (surname != null && !surname.isEmpty()) {
                                style += " " + surname;
                        }
                        if (suffix != null && !suffix.isEmpty()) {
                                style += " " + suffix;
                        }
                        builder.append(Text.of(LanguageHandler.colorYellow(), style.trim()));
                }

                String locale = DataHandler.getResidentLocale(uuid);
                if (locale != null && !locale.isEmpty()) {
                        builder.append(Text.of(LanguageHandler.colorGold(), "\nLocale: ", LanguageHandler.colorYellow(), locale));
                }

                long registered = DataHandler.getResidentRegisteredAt(uuid);
                if (registered > 0) {
                        builder.append(Text.of(LanguageHandler.colorGold(), "\nRegistered: ", LanguageHandler.colorYellow(), RESIDENT_TIME.format(Instant.ofEpochMilli(registered))));
                }

                long lastOnline = DataHandler.getResidentLastOnline(uuid);
                if (lastOnline > 0) {
                        builder.append(Text.of(LanguageHandler.colorGold(), "\nLast online: ", LanguageHandler.colorYellow(), RESIDENT_TIME.format(Instant.ofEpochMilli(lastOnline))));
                }

                BigDecimal balance = null;
                EconomyService service = TownyPlugin.getEcoService();
                if (service != null)
                {
                        Optional<UniqueAccount> optAccount = TownyPlugin.getOrCreateUniqueAccount(uuid);
			if (optAccount.isPresent())
			{
				balance = optAccount.get().getBalance(TownyPlugin.getEcoService().getDefaultCurrency());
			}
		}
		builder.append(
				Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_MONEY + ": "),
				((balance == null) ? Text.of(LanguageHandler.colorGray(), LanguageHandler.FORMAT_UNKNOWN) : Text.builder()
						.append(Text.of(LanguageHandler.colorYellow(), TownyPlugin.getEcoService().getDefaultCurrency().format(balance)))
						.append(Text.of(LanguageHandler.colorYellow(), TownyPlugin.getEcoService().getDefaultCurrency().getSymbol()))
						.build())
				);

		builder.append(Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_TOWN + ": "));
		Towny towny = DataHandler.getTownyOfPlayer(uuid);
		if (towny != null)
		{
			builder.append(townyClickable(LanguageHandler.colorYellow(), towny.getRealName()));
			if (towny.isPresident(uuid))
			{
				builder.append(Text.of(LanguageHandler.colorYellow(), " (" + LanguageHandler.FORMAT_MAYOR + ")"));
			}
			else if (towny.isMinister(uuid))
			{
				builder.append(Text.of(LanguageHandler.colorYellow(), " (" + LanguageHandler.FORMAT_COMAYORS + ")"));
			}

			builder.append(Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_PLOTS + ": "));
			boolean ownNothing = true;
			for (Plot plot : towny.getPlots().values())
			{
				if (uuid.equals(plot.getOwner()) && plot.isNamed())
				{
					if (ownNothing)
					{
						ownNothing = false;
					}
					else
					{
						builder.append(Text.of(LanguageHandler.colorYellow(), ", "));
					}
					builder.append(plotClickable(LanguageHandler.colorYellow(), plot.getRealName()));
				}
			}
			if (ownNothing)
			{
				builder.append(Text.of(LanguageHandler.colorGray(), LanguageHandler.FORMAT_NONE));
			}

                        String title = DataHandler.getResidentTitle(uuid);
                        if (title != null && !title.trim().isEmpty()) {
                                builder.append(Text.of(LanguageHandler.colorGold(), "\nTitle: ", LanguageHandler.colorYellow(), title));
                        }

                        java.util.List<String> ranks = new java.util.ArrayList<>(DataHandler.getResidentTownRanks(uuid));
                        if (!ranks.isEmpty()) {
                                builder.append(Text.of(LanguageHandler.colorGold(), "\nRanks: ", LanguageHandler.colorYellow(), String.join(", ", ranks)));
                        }

                }
                else
                {
                        builder.append(Text.of(LanguageHandler.colorGray(), LanguageHandler.FORMAT_NONE));
                }

                if (DataHandler.isResidentJailed(uuid)) {
                        long release = DataHandler.getResidentJailRelease(uuid);
                        builder.append(Text.of(LanguageHandler.colorGold(), "\nStatus: ", LanguageHandler.colorRed(), "Jailed"));
                        if (release > 0) {
                                builder.append(Text.of(LanguageHandler.colorGold(), " (until ", LanguageHandler.colorYellow(), RESIDENT_TIME.format(Instant.ofEpochMilli(release)), LanguageHandler.colorGold(), ")"));
                        }
                } else if (DataHandler.isResidentBankrupt(uuid)) {
                        builder.append(Text.of(LanguageHandler.colorGold(), "\nStatus: ", LanguageHandler.colorRed(), "Bankrupt"));
                }

                long exempt = DataHandler.getResidentTaxExemptUntil(uuid);
                if (exempt > System.currentTimeMillis()) {
                        builder.append(Text.of(LanguageHandler.colorGold(), "\nTax exempt until: ", LanguageHandler.colorYellow(), RESIDENT_TIME.format(Instant.ofEpochMilli(exempt))));
                }

                Set<String> modes = DataHandler.getResidentModes(uuid);
                if (!modes.isEmpty()) {
                        builder.append(Text.of(LanguageHandler.colorGold(), "\nModes: ", LanguageHandler.colorYellow(), String.join(", ", modes)));
                }

                long spawnCooldown = DataHandler.getResidentSpawnCooldown(uuid);
                if (spawnCooldown > System.currentTimeMillis()) {
                        long remaining = spawnCooldown - System.currentTimeMillis();
                        builder.append(Text.of(LanguageHandler.colorGold(), "\nSpawn cooldown: ", LanguageHandler.colorYellow(), formatDuration(remaining)));
                }

                return builder.build();
        }

        public static Text formatNationDescription(Nation nation)
        {
                Builder builder = Text.builder("");
                builder.append(
                                Text.of(LanguageHandler.colorGold(), "----------{ "),
                                Text.of(LanguageHandler.colorYellow(), LanguageHandler.FORMAT_NATION + " - " + nation.getName()),
                                Text.of(LanguageHandler.colorGold(), " }----------"));

                if (nation.getBoard() != null && !nation.getBoard().trim().isEmpty()) {
                        builder.append(Text.of(LanguageHandler.colorGold(), "\nBoard: "));
                        builder.append(TextSerializers.FORMATTING_CODE.deserialize(nation.getBoard()));
                }

                builder.append(Text.of(LanguageHandler.colorGold(), "\nID: ", LanguageHandler.colorGreen(), nation.getRealName()));
                builder.append(Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_GOVERNMENT + ": ", LanguageHandler.colorYellow(),
                                nation.getGovernment().getDisplayName()));

                if (nation.getCapital() != null) {
                        Towny capital = DataHandler.getTowny(nation.getCapital());
                        if (capital != null) {
                                builder.append(Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_TOWN + " (" + LanguageHandler.FORMAT_MAYOR + "): "));
                                builder.append(Text.of(LanguageHandler.colorYellow(), capital.getName()));
                                UUID mayor = capital.getPresident();
                                if (mayor != null) {
                                        builder.append(Text.of(LanguageHandler.colorGold(), " - "));
                                        builder.append(Text.of(LanguageHandler.colorYellow(), DataHandler.getPlayerName(mayor)));
                                }
                        }
                }

                builder.append(Text.of(LanguageHandler.colorGold(), "\nTowns: "));
                if (nation.getTowns().isEmpty()) {
                        builder.append(Text.of(LanguageHandler.colorGray(), LanguageHandler.FORMAT_NONE));
                } else {
                        boolean first = true;
                        for (UUID townId : nation.getTowns()) {
                                Towny town = DataHandler.getTowny(townId);
                                if (town == null) {
                                        continue;
                                }
                                if (!first) {
                                        builder.append(Text.of(LanguageHandler.colorGold(), ", "));
                                }
                                builder.append(Text.of(LanguageHandler.colorYellow(), town.getName()));
                                first = false;
                        }
                        if (first) {
                                builder.append(Text.of(LanguageHandler.colorGray(), LanguageHandler.FORMAT_NONE));
                        }
                }

                builder.append(Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_OPEN + ": ", LanguageHandler.colorYellow(), nation.isOpen()));
                builder.append(Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_PUBLIC + ": ", LanguageHandler.colorYellow(), nation.isPublic()));
                builder.append(Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_NEUTRAL + ": ", LanguageHandler.colorYellow(), nation.isNeutral()));

                builder.append(Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_TAXES + ": "));
                if (nation.isTaxPercentage()) {
                        builder.append(Text.of(LanguageHandler.colorYellow(), nation.getTaxes() + "%"));
                } else {
                        builder.append(formatPrice(LanguageHandler.colorYellow(), BigDecimal.valueOf(nation.getTaxes())));
                }

                builder.append(Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_SPAWN_COST + ": "));
                builder.append(formatPrice(LanguageHandler.colorYellow(), BigDecimal.valueOf(nation.getSpawnCost())));

                builder.append(Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_ASSISTANTS + ": "));
                structureX(
                                nation.getAssistants().iterator(),
                                builder,
                                b -> b.append(Text.of(LanguageHandler.colorGray(), LanguageHandler.FORMAT_NONE)),
                                (b, assistant) -> b.append(citizenClickable(LanguageHandler.colorYellow(), DataHandler.getPlayerName(assistant))),
                                b -> b.append(Text.of(LanguageHandler.colorGold(), ", ")));

                builder.append(Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_ALLIES + ": "));
                builder.append(formatNationRelationList(nation.getAllies()));

                builder.append(Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_ENEMIES + ": "));
                builder.append(formatNationRelationList(nation.getEnemies()));

                return builder.build();
        }

        private static Text formatNationRelationList(Set<UUID> relationIds) {
                Text.Builder listBuilder = Text.builder();
                boolean first = true;
                for (UUID id : relationIds) {
                        Nation related = DataHandler.getNation(id);
                        if (related == null) {
                                continue;
                        }
                        if (!first) {
                                listBuilder.append(Text.of(LanguageHandler.colorGold(), ", "));
                        }
                        listBuilder.append(Text.builder(related.getName())
                                        .color(LanguageHandler.colorYellow())
                                        .onClick(TextActions.runCommand("/n info " + related.getRealName()))
                                        .build());
                        first = false;
                }
                if (first) {
                        listBuilder.append(Text.of(LanguageHandler.colorGray(), LanguageHandler.FORMAT_NONE));
                }
                return listBuilder.build();
        }

        private static String formatDuration(long millis) {
                if (millis <= 0) return "ready";
                long seconds = millis / 1000;
                long minutes = seconds / 60;
                long remaining = seconds % 60;
                if (minutes > 0) {
                        return minutes + "m " + remaining + "s";
                }
                return remaining + "s";
        }

	public static Text formatPlotDescription(Plot plot, Towny towny, int clicker)
	{
		Builder builder = Text.builder("");
		UUID owner = plot.getOwner();
		builder.append(
				Text.of(LanguageHandler.colorGold(), "----------{ "),
				Text.of(LanguageHandler.colorYellow(), "" + LanguageHandler.FORMAT_PLOT + " - " + plot.getDisplayName()),
				Text.of(LanguageHandler.colorGold(), " }----------"),
                                Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_TOWN + ": "),
                                Text.of(LanguageHandler.colorYellow(), towny.getDisplayName()),
                                Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_PLOT_ID + ": "),
                                Text.of(LanguageHandler.colorYellow(), plot.getName()),
                                Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_PLOT_TYPE + ": "),
                                Text.of(LanguageHandler.colorYellow(), plot.getType().getDisplayName()),
                                Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_OWNER + ": "),
                                (owner == null) ? Text.of(LanguageHandler.colorGray(), LanguageHandler.FORMAT_NONE) : citizenClickable(LanguageHandler.colorYellow(), DataHandler.getPlayerName(owner)),
                                                Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_COOWNER + ": ")
				);
		structureX(
				plot.getCoowners().iterator(),
				builder,
				(b) -> b.append(Text.of(LanguageHandler.colorGray(), LanguageHandler.FORMAT_NONE)),
				(b, uuid) -> b.append(citizenClickable(LanguageHandler.colorYellow(), DataHandler.getPlayerName(uuid))),
				(b) -> b.append(Text.of(LanguageHandler.colorYellow(), ", ")));

		builder.append(
				Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_PRICE + ": "),
				(plot.isForSale()) ? formatPrice(LanguageHandler.colorYellow(), plot.getPrice()) : Text.of(LanguageHandler.colorGray(), LanguageHandler.FORMAT_NFS)
		);
		builder.append(
				Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_RENT_PRICE + ": "),
				(plot.isForRent() && ((plot.isOwned() && clicker == CLICKER_DEFAULT) || !plot.isOwned()))
						? formatPrice(LanguageHandler.colorYellow(), plot.getRentalPrice()) : Text.of(LanguageHandler.colorGray(), LanguageHandler.FORMAT_NFR)
		);
		if(plot.isForRent() && clicker == CLICKER_DEFAULT) { //either staff, owner or coowner
			if(TownyPlugin.getEcoService() != null) {
                            Optional<Account> plotAccount = TownyPlugin.getOrCreateAccount("plot-" + plot.getUUID());
				plotAccount.ifPresent(account -> builder.append(
						Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_BALANCE + ": "),
						formatPrice(LanguageHandler.colorYellow(), account.getBalance(TownyPlugin.getEcoService().getDefaultCurrency()))
				));
			}
		}

                String[][] permGroups = new String[][] {
                                {LanguageHandler.FORMAT_OUTSIDERS, Towny.TYPE_OUTSIDER},
                                {LanguageHandler.FORMAT_ALLIES, Towny.TYPE_ALLY},
                                {LanguageHandler.FORMAT_RESIDENTS, Towny.TYPE_RESIDENT},
                                {LanguageHandler.FORMAT_FRIENDS, Towny.TYPE_FRIEND}
                };
                String[][] permOptions = new String[][] {
                                {LanguageHandler.TYPE_BUILD, Towny.PERM_BUILD},
                                {LanguageHandler.TYPE_DESTROY, Towny.PERM_DESTROY},
                                {LanguageHandler.TYPE_SWITCH, Towny.PERM_SWITCH},
                                {LanguageHandler.TYPE_ITEMUSE, Towny.PERM_ITEM_USE}
                };

                if (clicker == CLICKER_NONE)
                {
                        builder.append(Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_PERMISSIONS + ":"));
                        for (String[] group : permGroups) {
                                builder.append(Text.of(LanguageHandler.colorGold(), "\n    " + group[0] + ": "));
                                boolean first = true;
                                for (String[] option : permOptions) {
                                        if (!first) {
                                                builder.append(Text.of(LanguageHandler.colorGold(), "/"));
                                        }
                                        boolean allowed = plot.getPerm(group[1], option[1]);
                                        builder.append(Text.of(allowed ? LanguageHandler.colorGreen() : LanguageHandler.colorRed(), option[0]));
                                        first = false;
                                }
                        }

                        builder.append(Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_FLAGS + ":"));
                        for (Entry<String, Boolean> e : plot.getFlags().entrySet())
                        {
                                builder.append(Text.of(LanguageHandler.colorGold(), "\n    " + StringUtils.capitalize(e.getKey().toLowerCase()) + ": "));
				builder.append(Text.of((e.getValue()) ? LanguageHandler.colorYellow() : LanguageHandler.colorDarkGray(), LanguageHandler.FLAG_ENABLED));
				builder.append(Text.of(LanguageHandler.colorGold(), "/"));
				builder.append(Text.of((e.getValue()) ? LanguageHandler.colorDarkGray() : LanguageHandler.colorYellow(), LanguageHandler.FLAG_DISABLED));
			}
		}
                else if (clicker == CLICKER_DEFAULT)
                {
                        builder.append(Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_PERMISSIONS + ":"));
                        for (String[] group : permGroups) {
                                builder.append(Text.of(LanguageHandler.colorGold(), "\n    " + group[0] + ": "));
                                boolean first = true;
                                for (String[] option : permOptions) {
                                        if (!first) {
                                                builder.append(Text.of(LanguageHandler.colorGold(), "/"));
                                        }
                                        boolean allowed = plot.getPerm(group[1], option[1]);
                                        builder.append(Text.builder(option[0])
                                                        .color(allowed ? LanguageHandler.colorGreen() : LanguageHandler.colorRed())
                                                        .onClick(TextActions.runCommand("/z perm " + group[1] + " " + option[1])).build());
                                        first = false;
                                }
                                builder.append(Text.of(LanguageHandler.colorDarkGray(), " <- " + LanguageHandler.CLICK));
                        }

                        builder.append(Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_FLAGS + ":"));
			for (Entry<String, Boolean> e : plot.getFlags().entrySet())
			{
				builder.append(Text.of(LanguageHandler.colorGold(), "\n    " + StringUtils.capitalize(e.getKey().toLowerCase()) + ": "));
				builder.append(Text.builder(LanguageHandler.FLAG_ENABLED).color((e.getValue()) ? LanguageHandler.colorYellow() : LanguageHandler.colorDarkGray()).onClick(TextActions.runCommand("/z flag " + e.getKey() + " true")).build());
				builder.append(Text.of(LanguageHandler.colorGold(), "/"));
				builder.append(Text.builder(LanguageHandler.FLAG_DISABLED).color((e.getValue()) ? LanguageHandler.colorDarkGray() : LanguageHandler.colorYellow()).onClick(TextActions.runCommand("/z flag " + e.getKey() + " false")).build());
				builder.append(Text.of(LanguageHandler.colorDarkGray(), " <- " + LanguageHandler.CLICK));
			}
		}

		return builder.build();
	}

	public static Text formatWorldDescription(String name)
	{
		Builder builder = Text.builder("");
		builder.append(
				Text.of(LanguageHandler.colorGold(), "----------{ "),
				Text.of(LanguageHandler.colorYellow(), name),
				Text.of(LanguageHandler.colorGold(), " }----------")
				);

		boolean enabled = ConfigHandler.getNode("worlds").getNode(name).getNode("enabled").getBoolean();

		builder.append(Text.of(LanguageHandler.colorGold(), "\nEnabled: "));
		builder.append(Text.builder(LanguageHandler.FLAG_ENABLED)
				.color((enabled) ? LanguageHandler.colorYellow() : LanguageHandler.colorDarkGray())
				.onClick(TextActions.runCommand("/tw enable " + name)).build());
		builder.append(Text.of(LanguageHandler.colorGold(), "/"));
		builder.append(Text.builder(LanguageHandler.FLAG_DISABLED)
				.color((enabled) ? LanguageHandler.colorDarkGray() : LanguageHandler.colorYellow())
				.onClick(TextActions.runCommand("/tw disable " + name)).build());

		if (!enabled)
		{
			return builder.build();
		}

                builder.append(Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_PERMISSIONS + ": "));
                String[][] worldPermOptions = new String[][] {
                                {LanguageHandler.TYPE_BUILD, Towny.PERM_BUILD},
                                {LanguageHandler.TYPE_DESTROY, Towny.PERM_DESTROY},
                                {LanguageHandler.TYPE_SWITCH, Towny.PERM_SWITCH},
                                {LanguageHandler.TYPE_ITEMUSE, Towny.PERM_ITEM_USE}
                };
                boolean firstPerm = true;
                for (String[] option : worldPermOptions) {
                        if (!firstPerm) {
                                builder.append(Text.of(LanguageHandler.colorGold(), "/"));
                        }
                        boolean allowed = ConfigHandler.getNode("worlds").getNode(name).getNode("perms", option[1]).getBoolean();
                        builder.append(Text.builder(option[0])
                                        .color(allowed ? LanguageHandler.colorGreen() : LanguageHandler.colorRed())
                                        .onClick(TextActions.runCommand("/tw perm " + option[1]))
                                        .build());
                        firstPerm = false;
                }

                builder.append(Text.of(LanguageHandler.colorDarkGray(), " <- " + LanguageHandler.CLICK));

		builder.append(Text.of(LanguageHandler.colorGold(), "\n" + LanguageHandler.FORMAT_FLAGS + ":"));
		for (Entry<Object, ? extends CommentedConfigurationNode> e : ConfigHandler.getNode("worlds").getNode(name).getNode("flags").getChildrenMap().entrySet())
		{
			String flag = e.getKey().toString();
			boolean b = e.getValue().getBoolean();
			builder.append(Text.of(LanguageHandler.colorGold(), "\n    " + StringUtils.capitalize(flag.toLowerCase()) + ": "));
			builder.append(Text.builder(LanguageHandler.FLAG_ENABLED).color((b) ? LanguageHandler.colorYellow() : LanguageHandler.colorDarkGray()).onClick(TextActions.runCommand("/tw flag " + flag + " true")).build());
			builder.append(Text.of(LanguageHandler.colorGold(), "/"));
			builder.append(Text.builder(LanguageHandler.FLAG_DISABLED).color((b) ? LanguageHandler.colorDarkGray() : LanguageHandler.colorYellow()).onClick(TextActions.runCommand("/tw flag " + flag + " false")).build());
			builder.append(Text.of(LanguageHandler.colorDarkGray(), " <- " + LanguageHandler.CLICK));
		}

		return builder.build();
	}

	public static Text formatPrice(TextColor color, BigDecimal amount)
	{
		return Text.of(color, TownyPlugin.getEcoService().getDefaultCurrency().format(amount));
	}

	public static String formatPricePlain(BigDecimal amount)
	{
		return TextSerializers.FORMATTING_CODE.serialize(TownyPlugin.getEcoService().getDefaultCurrency().format(amount));
	}

	public static Text formatTownySpawns(Towny towny, TextColor color)
	{
		return formatTownySpawns(towny, color, CLICKER_DEFAULT);
	}

	public static Text formatTownySpawns(Towny towny, TextColor color, int clicker)
	{
		return formatTownySpawns(towny, color, "spawn", clicker);
	}

	public static Text formatTownySpawns(Towny towny, TextColor color, String cmd)
	{
		return formatTownySpawns(towny, color, cmd, CLICKER_DEFAULT);
	}

        public static Text formatTownySpawns(Towny towny, TextColor color, String cmd, int clicker)
        {
                if (clicker == CLICKER_DEFAULT)
                {
			return structureX(
					towny.getSpawns().keySet().iterator(),
					Text.builder(),
					(b) -> b.append(Text.of(LanguageHandler.colorGray(), LanguageHandler.FORMAT_NONE)),
					(b, spawnName) -> b.append(Text.builder(spawnName).color(color).onClick(TextActions.runCommand("/t " + cmd + " " + spawnName)).build()),
					(b) -> b.append(Text.of(color, ", "))).build();
		}
		if (clicker == CLICKER_ADMIN || towny.getFlag("public"))
		{
			return structureX(
					towny.getSpawns().keySet().iterator(),
					Text.builder(),
					(b) -> b.append(Text.of(LanguageHandler.colorGray(), LanguageHandler.FORMAT_NONE)),
					(b, spawnName) -> b.append(Text.builder(spawnName).color(color).onClick(TextActions.runCommand("/t visit " + towny.getRealName() + " " + spawnName)).build()),
					(b) -> b.append(Text.of(color, ", "))).build();
		}
		return structureX(
				towny.getSpawns().keySet().iterator(),
				Text.builder(),
				(b) -> b.append(Text.of(LanguageHandler.colorGray(), LanguageHandler.FORMAT_NONE)),
				(b, spawnName) -> b.append(Text.builder(spawnName).color(color).build()),
                                (b) -> b.append(Text.of(color, ", "))).build();

        }

        public static Text formatTownyOutpostSpawns(Towny towny, TextColor color) {
                List<Integer> indices = towny.getOutpostIndices();
                return structureX(
                                indices.iterator(),
                                Text.builder(),
                                (builder) -> builder.append(Text.of(LanguageHandler.colorGray(), LanguageHandler.FORMAT_NONE)),
                                (builder, index) -> builder.append(Text.builder(String.valueOf(index))
                                                .color(color)
                                                .onClick(TextActions.runCommand("/t outpost " + index))
                                                .build()),
                                (builder) -> builder.append(Text.of(color, ", "))).build();
        }

	// clickable

	public static Text townyClickable(TextColor color, String name)
	{
		if (name == null)
		{
			return Text.of(color, LanguageHandler.FORMAT_UNKNOWN);
		}
		return Text.builder(name.replace("_", " ")).color(color).onClick(TextActions.runCommand("/t info " + name)).build();
	}

	public static Text citizenClickable(TextColor color, String name)
	{
		if (name == null)
		{
			return Text.of(color, LanguageHandler.FORMAT_UNKNOWN);
		}
		return Text.builder(name).color(color).onClick(TextActions.runCommand("/t citizen " + name)).build();
	}

	public static Text plotClickable(TextColor color, String name)
	{
		if (name == null)
		{
			return Text.of(color, LanguageHandler.FORMAT_UNKNOWN);
		}
		return Text.builder(name.replace("_", " ")).color(color).onClick(TextActions.runCommand("/z info " + name)).build();
	}

	public static Text worldClickable(TextColor color, String name)
	{
		if (name == null)
		{
			return Text.of(color, LanguageHandler.FORMAT_UNKNOWN);
		}
		return Text.builder(name).color(color).onClick(TextActions.runCommand("/tw info " + name)).build();
	}

	// structure X

	public static <T, U> T structureX(Iterator<U> iter, T obj, Consumer<T> ifNot, BiConsumer<T, U> forEach, Consumer<T> separator)
	{
		if (!iter.hasNext())
		{
			ifNot.accept(obj);
		}
		else
		{
			while (iter.hasNext())
			{
				forEach.accept(obj, iter.next());
				if (iter.hasNext())
				{
					separator.accept(obj);
				}
			}
		}
		return obj;
	}
}
