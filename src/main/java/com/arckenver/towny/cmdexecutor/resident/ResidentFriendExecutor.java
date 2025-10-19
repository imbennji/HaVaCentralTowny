package com.arckenver.towny.cmdexecutor.resident;

import com.arckenver.towny.DataHandler;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.command.*;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.*;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import com.arckenver.towny.LanguageHandler;

public class ResidentFriendExecutor implements CommandExecutor {

    // Map of user-typed tokens -> canonical action string
    private static final Map<String, String> ACTIONS = ImmutableMap.<String, String>builder()
            .put("add", "add")
            .put("remove", "remove")
            .put("list", "list")
            .build();

    public static void create(CommandSpec.Builder root) {
        root.child(CommandSpec.builder()
                .permission("towny.command.resident.friend")
                .arguments(
                        // choices needs a Map<String, ?> not a List
                        GenericArguments.optional(GenericArguments.choices(Text.of("action"), ACTIONS)),
                        GenericArguments.optional(GenericArguments.string(Text.of("player")))
                )
                .executor(new ResidentFriendExecutor())
                .build(), "friend", "friends");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
        if (!(src instanceof Player)) {
            throw new CommandException(Text.of(LanguageHandler.colorRed(), "Players only."));
        }
        Player p = (Player) src;

        // returns the mapped value ("add"/"remove"/"list") or null
        String action = ctx.<String>getOne("action").orElse("list");

        if (action.equalsIgnoreCase("list")) {
            Set<UUID> friends = DataHandler.getResidentFriends(p.getUniqueId());
            String names = friends.stream()
                    .map(DataHandler::getPlayerName)
                    .filter(n -> n != null)
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.joining(", "));
            if (names.isEmpty()) names = "(none)";
            p.sendMessage(Text.of(LanguageHandler.colorGold(), "Friends: ", LanguageHandler.colorYellow(), names));
            return CommandResult.success();
        }

        String targetName = ctx.<String>getOne("player").orElse(null);
        if (targetName == null) {
            throw new CommandException(Text.of(LanguageHandler.colorRed(), "Usage: /res friend " + action + " <player>"));
        }
        UUID target = DataHandler.getPlayerUUID(targetName);
        if (target == null) {
            throw new CommandException(Text.of(LanguageHandler.colorRed(), "Unknown player."));
        }

        if (action.equalsIgnoreCase("add")) {
            boolean ok = DataHandler.addResidentFriend(p.getUniqueId(), target);
            p.sendMessage(Text.of(ok ? LanguageHandler.colorGreen() : LanguageHandler.colorYellow(), ok ? "Added." : "Already a friend."));
            return CommandResult.success();
        }
        if (action.equalsIgnoreCase("remove")) {
            boolean ok = DataHandler.removeResidentFriend(p.getUniqueId(), target);
            p.sendMessage(Text.of(ok ? LanguageHandler.colorGreen() : LanguageHandler.colorYellow(), ok ? "Removed." : "Not in your list."));
            return CommandResult.success();
        }

        throw new CommandException(Text.of(LanguageHandler.colorRed(), "Unknown action."));
    }
}
