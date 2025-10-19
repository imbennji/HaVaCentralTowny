package com.arckenver.towny.cmdexecutor.resident;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.object.Resident;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.*;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.*;
import org.spongepowered.api.text.Text;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import com.arckenver.towny.LanguageHandler;

public class ResidentListExecutor implements CommandExecutor {
    public static void create(CommandSpec.Builder root) {
        root.child(spec(), "list");
    }
    public static CommandSpec spec() {
        return CommandSpec.builder()
                .permission("towny.command.resident.list")
                .executor(new ResidentListExecutor())
                .build();
    }

    @Override public CommandResult execute(CommandSource src, CommandContext args) {
        String online = Sponge.getServer().getOnlinePlayers().stream()
                .map(p -> p.getName())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.joining(", "));
        if (online.isEmpty()) online = "(none)";
        src.sendMessage(Text.of(LanguageHandler.colorGold(), "Online residents: ", LanguageHandler.colorYellow(), online));

        List<Resident> residents = DataHandler.getResidents().stream()
                .sorted(Comparator.comparingLong(Resident::getLastOnlineAt).reversed())
                .collect(Collectors.toList());

        String recent = residents.stream()
                .filter(r -> r.getLastOnlineAt() > 0)
                .limit(8)
                .map(ResidentListExecutor::formatResidentRecent)
                .collect(Collectors.joining(", "));
        if (recent.isEmpty()) {
            recent = "No recent activity";
        }
        src.sendMessage(Text.of(LanguageHandler.colorGold(), "Recently active: ", LanguageHandler.colorYellow(), recent));

        long inactiveThreshold = Duration.ofDays(7).toMillis();
        long now = System.currentTimeMillis();
        String inactive = residents.stream()
                .filter(r -> r.getLastOnlineAt() > 0 && (now - r.getLastOnlineAt()) >= inactiveThreshold)
                .sorted(Comparator.comparingLong(Resident::getLastOnlineAt))
                .limit(5)
                .map(ResidentListExecutor::formatResidentRecent)
                .collect(Collectors.joining(", "));
        if (!inactive.isEmpty()) {
            src.sendMessage(Text.of(LanguageHandler.colorGold(), "Inactive (7d+): ", LanguageHandler.colorYellow(), inactive));
        }
        return CommandResult.success();
    }

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private static String formatResidentRecent(Resident resident) {
        String name = resident.getLastKnownName();
        if (name == null || name.trim().isEmpty()) {
            name = resident.getId().toString();
        }
        long seen = resident.getLastOnlineAt();
        if (seen <= 0) {
            return name;
        }
        String when = DATE_TIME.format(Instant.ofEpochMilli(seen));
        String ago = formatAgo(seen);
        return name + " (" + when + ", " + ago + ")";
    }

    private static String formatAgo(long epochMs) {
        long diff = Math.max(0, System.currentTimeMillis() - epochMs);
        if (diff < 60_000L) {
            return (diff / 1000L) + "s ago";
        }
        long minutes = diff / 60_000L;
        if (minutes < 60) {
            return minutes + "m ago";
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + "h ago";
        }
        long days = hours / 24;
        return days + "d ago";
    }
}
