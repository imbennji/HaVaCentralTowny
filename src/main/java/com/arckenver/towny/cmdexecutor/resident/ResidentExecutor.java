package com.arckenver.towny.cmdexecutor.resident;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.object.Towny;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * /resident [player]
 * Shows Towny-like resident info: Town, title, taxes, owned/rented plots.
 */
public final class ResidentExecutor implements CommandExecutor {

    public static void create(CommandSpec.Builder root) {
        root.child(spec(), "resident", "res");
    }

    public static CommandSpec spec() {
        return CommandSpec.builder()
                .description(Text.of("Show resident info"))
                .permission("towny.command.resident.execute")
                .arguments(GenericArguments.optional(GenericArguments.string(Text.of("player"))))
                .executor(new ResidentExecutor())
                .build();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        UUID targetId;
        String targetName;

        Optional<String> optName = args.getOne("player");

        if (!optName.isPresent()) {
            if (!(src instanceof Player)) {
                throw new CommandException(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
            }
            Player p = (Player) src;
            targetId = p.getUniqueId();
            targetName = p.getName();
        } else {
            String name = optName.get();
            UUID id = DataHandler.getPlayerUUID(name);
            if (id == null) {
                throw new CommandException(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_BADPLAYERNAME));
            }
            targetId = id;
            String n = DataHandler.getPlayerName(id);
            targetName = (n != null) ? n : name;
        }

        src.sendMessage(buildResidentInfo(targetId, targetName));
        return CommandResult.success();
    }

    private Text buildResidentInfo(UUID uuid, String name) {
        Towny town = DataHandler.getTownyOfPlayer(uuid);

        String title = DataHandler.getCitizenTitle(uuid); // Hermit/Citizen/CoMayor/MayorTitle
        int taxes = resolveTaxes(town);

        OwnedRented or = findOwnedAndRentedPlots(uuid, town);

        List<Text> lines = new ArrayList<>();
        lines.add(Text.of(LanguageHandler.colorGold(), "Resident: ",
                LanguageHandler.colorYellow(), name,
                LanguageHandler.colorDarkGray(), "  (", title, ")"));

        if (town != null) {
            Text.Builder townLine = Text.builder()
                    .append(Text.of(LanguageHandler.colorGray(), "Town: ", LanguageHandler.colorWhite(), town.getName()))
                    .onClick(TextActions.runCommand("/town info " + town.getName()))
                    .onHover(TextActions.showText(Text.of(LanguageHandler.colorYellow(), "Click for town info")));
            lines.add(townLine.build());
        } else {
            lines.add(Text.of(LanguageHandler.colorGray(), "Town: ", LanguageHandler.colorDarkGray(), "None"));
        }

        lines.add(Text.of(LanguageHandler.colorGray(), "Taxes Owed: ",
                LanguageHandler.colorWhite(), taxes, LanguageHandler.colorGray(), " / day"));

        BigDecimal balance = DataHandler.getResidentBalance(uuid);
        if (balance != null) {
            lines.add(Text.of(LanguageHandler.colorGray(), "Balance: ", LanguageHandler.colorWhite(), balance));
        }

        long registered = DataHandler.getResidentRegisteredAt(uuid);
        if (registered > 0) {
            lines.add(Text.of(LanguageHandler.colorGray(), "Registered: ", LanguageHandler.colorWhite(), formatInstant(registered)));
        }

        long lastOnline = DataHandler.getResidentLastOnline(uuid);
        if (lastOnline > 0) {
            lines.add(Text.of(LanguageHandler.colorGray(), "Last Online: ", LanguageHandler.colorWhite(), formatInstant(lastOnline)));
        }

        long exempt = DataHandler.getResidentTaxExemptUntil(uuid);
        if (exempt > System.currentTimeMillis()) {
            lines.add(Text.of(LanguageHandler.colorGray(), "Tax Exempt Until: ", LanguageHandler.colorWhite(), formatInstant(exempt)));
        }

        if (DataHandler.isResidentJailed(uuid)) {
            long release = DataHandler.getResidentJailRelease(uuid);
            String releaseText = (release > 0) ? formatInstant(release) : "Indefinite";
            lines.add(Text.of(LanguageHandler.colorRed(), "Jailed", LanguageHandler.colorGray(), " – release at ", LanguageHandler.colorWhite(), releaseText));
        }

        Set<String> modes = DataHandler.getResidentModes(uuid);
        if (!modes.isEmpty()) {
            lines.add(Text.of(LanguageHandler.colorGray(), "Modes: ", LanguageHandler.colorWhite(), String.join(", ", modes)));
        }

        lines.add(compactListLine(
                Text.of(LanguageHandler.colorGray(), "Owned Plots: "),
                or.ownedNames, or.ownedCount, LanguageHandler.colorYellow(), "/plot info"));

        if (or.rentedCount > 0) {
            lines.add(compactListLine(
                    Text.of(LanguageHandler.colorGray(), "Rented Plots: "),
                    or.rentedNames, or.rentedCount, LanguageHandler.colorAqua(), "/plot info"));
        } else {
            lines.add(Text.of(LanguageHandler.colorGray(), "Rented Plots: ", LanguageHandler.colorDarkGray(), "None"));
        }

        return Text.joinWith(Text.NEW_LINE, lines);
    }

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private static String formatInstant(long epochMs) {
        return DATE_TIME.format(Instant.ofEpochMilli(epochMs));
    }

    private int resolveTaxes(Towny town) {
        if (town == null) return ConfigHandler.getNode("towny", "defaultTaxes").getInt(0);
        try {
            Method m = town.getClass().getMethod("getTaxes");
            Object v = m.invoke(town);
            if (v instanceof Number) return ((Number) v).intValue();
        } catch (Throwable ignored) {}
        return ConfigHandler.getNode("towny", "defaultTaxes").getInt(0);
    }

    private static final class OwnedRented {
        int ownedCount = 0;
        int rentedCount = 0;
        List<String> ownedNames = Collections.emptyList();
        List<String> rentedNames = Collections.emptyList();
    }

    private OwnedRented findOwnedAndRentedPlots(UUID uuid, Towny town) {
        OwnedRented out = new OwnedRented();
        if (town == null) return out;

        Collection<?> plots = Collections.emptyList();
        try {
            Method getPlots = town.getClass().getMethod("getPlots");
            Object val = getPlots.invoke(town);
            if (val instanceof Collection) plots = (Collection<?>) val;
        } catch (Throwable ignored) {}

        List<String> owned = new ArrayList<>();
        List<String> rented = new ArrayList<>();

        for (Object p : plots) {
            boolean isOwner = invokeBool(p, "isOwner", uuid);
            boolean isRenter = invokeBool(p, "isRenter", uuid); // optional
            String pname = invokeString(p, "getName");
            if (pname == null || pname.trim().isEmpty()) pname = LanguageHandler.DEFAULT_PLOTNAME;

            if (isOwner) owned.add(pname);
            else if (isRenter) rented.add(pname);
        }

        out.ownedCount = owned.size();
        out.rentedCount = rented.size();
        out.ownedNames = compactNames(owned, 6);
        out.rentedNames = compactNames(rented, 6);
        return out;
    }

    private static boolean invokeBool(Object obj, String method, UUID uuid) {
        try {
            Method m = obj.getClass().getMethod(method, UUID.class);
            Object v = m.invoke(obj, uuid);
            return (v instanceof Boolean) && (Boolean) v;
        } catch (Throwable ignored) { return false; }
    }

    private static String invokeString(Object obj, String method) {
        try {
            Method m = obj.getClass().getMethod(method);
            Object v = m.invoke(obj);
            return (v != null) ? String.valueOf(v) : null;
        } catch (Throwable ignored) { return null; }
    }

    private static List<String> compactNames(List<String> names, int max) {
        if (names.size() <= max) return names;
        List<String> head = new ArrayList<>(names.subList(0, max));
        head.add("+" + (names.size() - max) + " more");
        return head;
    }

    // >>> FIXED: accept TextColor (interface), not TextColors (constants class)
    private Text compactListLine(Text label, List<String> names, int total, TextColor color, String clickCmdHint) {
        if (total == 0) {
            return Text.of(label, LanguageHandler.colorDarkGray(), "None");
        }

        Text.Builder b = Text.builder().append(label);

        String joined = names.stream().collect(Collectors.joining(", "));
        Text namesText = Text.builder(joined).color(color).build();

        b.append(
                namesText
                        .toBuilder()
                        .onHover(TextActions.showText(Text.of(LanguageHandler.colorYellow(), "Click to run ", clickCmdHint)))
                        .onClick(TextActions.runCommand(clickCmdHint))
                        .build()
        );

        b.append(Text.of(LanguageHandler.colorGray(), "  [total: ", total, "]"));
        return b.build();
    }
}
