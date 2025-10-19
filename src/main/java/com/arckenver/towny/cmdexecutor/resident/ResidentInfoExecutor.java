package com.arckenver.towny.cmdexecutor.resident;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.Utils;
import org.spongepowered.api.command.*;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.*;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.stream.Collectors;

public class ResidentInfoExecutor implements CommandExecutor {
    public static void create(CommandSpec.Builder root) {
        root.child(spec(), "info", "");
    }
    public static CommandSpec spec() {
        return CommandSpec.builder()
                .permission("towny.command.resident.info")
                .arguments(GenericArguments.optional(GenericArguments.string(Text.of("player"))))
                .executor(new ResidentInfoExecutor())
                .build();
    }

    @Override public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String who = args.<String>getOne("player").orElse(null);
        if (who == null) {
            if (!(src instanceof Player)) throw new CommandException(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
            who = ((Player) src).getName();
        }
        UUID id = DataHandler.getPlayerUUID(who);
        if (id == null) throw new CommandException(Text.of(LanguageHandler.colorRed(), LanguageHandler.FORMAT_UNKNOWN));

        Text base = Utils.formatCitizenDescription(who);
        Text details = buildResidentDetails(id);
        String about = DataHandler.getResidentAbout(id);
        Text aboutLine = Text.EMPTY;
        if (about != null && !about.trim().isEmpty()) {
            aboutLine = Text.of(Text.NEW_LINE, LanguageHandler.colorGold(), "About: ", LanguageHandler.colorYellow(), about);
        }
        src.sendMessage(Text.of(base, details, aboutLine));
        return CommandResult.success();
    }

    private Text buildResidentDetails(UUID id) {
        long registered = DataHandler.getResidentRegisteredAt(id);
        long lastOnline = DataHandler.getResidentLastOnline(id);
        long lastLogout = DataHandler.getResidentLastLogout(id);

        List<Text> lines = new java.util.ArrayList<>();

        if (registered > 0) {
            lines.add(Text.of(Text.NEW_LINE, LanguageHandler.colorGray(), "Registered: ", LanguageHandler.colorYellow(), formatInstant(registered)));
        }

        if (lastOnline > 0) {
            String ago = formatAgo(lastOnline);
            lines.add(Text.of(Text.NEW_LINE, LanguageHandler.colorGray(), "Last Online: ", LanguageHandler.colorYellow(), formatInstant(lastOnline),
                    LanguageHandler.colorDarkGray(), " (", ago, ")"));
        }

        if (lastLogout > 0 && lastLogout != lastOnline) {
            lines.add(Text.of(Text.NEW_LINE, LanguageHandler.colorGray(), "Last Logout: ", LanguageHandler.colorYellow(), formatInstant(lastLogout)));
        }

        Queue<String> names = DataHandler.getResidentNameHistory(id);
        if (names != null && names.size() > 1) {
            String history = names.stream().collect(Collectors.joining(", "));
            lines.add(Text.of(Text.NEW_LINE, LanguageHandler.colorGray(), "Known As: ", LanguageHandler.colorYellow(), history));
        }

        BigDecimal balance = DataHandler.getResidentBalance(id);
        if (balance != null) {
            lines.add(Text.of(Text.NEW_LINE, LanguageHandler.colorGray(), "Account Balance: ", LanguageHandler.colorYellow(), balance));
        }

        long exemptUntil = DataHandler.getResidentTaxExemptUntil(id);
        if (exemptUntil > System.currentTimeMillis()) {
            lines.add(Text.of(Text.NEW_LINE, LanguageHandler.colorGray(), "Tax Exempt Until: ", LanguageHandler.colorYellow(), formatInstant(exemptUntil)));
        }

        if (DataHandler.isResidentBankrupt(id)) {
            lines.add(Text.of(Text.NEW_LINE, LanguageHandler.colorRed(), "Bankrupt since ", formatInstant(DataHandler.getResidentBankruptcyDeclaredAt(id))));
        }

        if (DataHandler.isResidentJailed(id)) {
            long release = DataHandler.getResidentJailRelease(id);
            Text releaseText = (release > 0)
                    ? Text.of(LanguageHandler.colorYellow(), formatInstant(release))
                    : Text.of(LanguageHandler.colorYellow(), "Indefinite");
            lines.add(Text.of(Text.NEW_LINE, LanguageHandler.colorRed(), "Jailed", LanguageHandler.colorGray(), " – Release: ", releaseText));
        }

        List<String> ranks = new java.util.ArrayList<>(DataHandler.getResidentTownRanks(id));
        if (!ranks.isEmpty()) {
            lines.add(Text.of(Text.NEW_LINE, LanguageHandler.colorGray(), "Town Ranks: ", LanguageHandler.colorYellow(), String.join(", ", ranks)));
        }

        java.util.Set<String> modes = DataHandler.getResidentModes(id);
        if (!modes.isEmpty()) {
            lines.add(Text.of(Text.NEW_LINE, LanguageHandler.colorGray(), "Modes: ", LanguageHandler.colorYellow(), String.join(", ", modes)));
        }

        long cooldown = DataHandler.getResidentSpawnCooldown(id);
        if (cooldown > System.currentTimeMillis()) {
            lines.add(Text.of(Text.NEW_LINE, LanguageHandler.colorGray(), "Spawn Cooldown: ", LanguageHandler.colorYellow(), formatDuration(cooldown - System.currentTimeMillis())));
        }

        if (lines.isEmpty()) {
            return Text.EMPTY;
        }
        Text result = Text.EMPTY;
        for (Text line : lines) {
            result = Text.of(result, line);
        }
        return result;
    }

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private static String formatInstant(long epochMs) {
        return DATE_TIME.format(Instant.ofEpochMilli(epochMs));
    }

    private static String formatAgo(long epochMs) {
        long diff = Math.max(0, System.currentTimeMillis() - epochMs);
        if (diff < 1000L) {
            return "moments ago";
        }
        Duration dur = Duration.ofMillis(diff);
        long days = dur.toDays();
        if (days > 0) {
            return days + "d ago";
        }
        long hours = dur.toHours();
        if (hours > 0) {
            return hours + "h ago";
        }
        long minutes = dur.toMinutes();
        if (minutes > 0) {
            return minutes + "m ago";
        }
        return (dur.getSeconds()) + "s ago";
    }

    private static String formatDuration(long millis) {
        if (millis <= 0) return "ready";
        Duration dur = Duration.ofMillis(millis);
        long minutes = dur.toMinutes();
        long seconds = dur.minusMinutes(minutes).getSeconds();
        return minutes + "m " + seconds + "s";
    }
}
