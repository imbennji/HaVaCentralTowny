package com.arckenver.towny.cmdexecutor.towny;

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
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;
import java.util.UUID;

public final class TownBoardExecutor implements CommandExecutor {

    // Loaded by your reflection loader (TownyCmds.createCmds)
    public static void create(CommandSpec.Builder root) {
        // /town board
        root.child(
                CommandSpec.builder()
                        .description(Text.of("Show or set town board"))
                        .permission("towny.command.town.board")
                        .arguments(
                                GenericArguments.optional(GenericArguments.string(Text.of("action"))),           // set | clear | (absent)
                                GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("textOrTown"))) // text for set OR <town> when viewing
                        )
                        .executor(new TownBoardExecutor())
                        .build(),
                "board"
        );
    }

    private static final int MAX_BOARD_LEN = 160; // keep chat-friendly (and under MC 32k packet)

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        final Optional<String> actionOpt = args.getOne("action");
        final Optional<String> textOrTownOpt = args.getOne("textOrTown");

        if (!actionOpt.isPresent()) {
            // /town board  OR  /town board <town>
            if (textOrTownOpt.isPresent()) {
                // View other town’s board
                String townName = textOrTownOpt.get();
                Towny t = DataHandler.getTowny(townName);
                if (t == null) throw new CommandException(Text.of(TextColors.RED, LanguageHandler.ERROR_BADTOWNNNAME));
                showBoard(src, t, true);
            } else {
                // View own town’s board
                if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
                Player p = (Player) src;
                Towny t = DataHandler.getTownyOfPlayer(p.getUniqueId());
                if (t == null) throw new CommandException(Text.of(TextColors.RED, LanguageHandler.ERROR_NOTOWN));
                showBoard(src, t, false);
            }
            return CommandResult.success();
        }

        // Mutations: set / clear (player + staff perms)
        if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
        Player p = (Player) src;
        UUID uid = p.getUniqueId();
        Towny t = DataHandler.getTownyOfPlayer(uid);
        if (t == null) throw new CommandException(Text.of(TextColors.RED, LanguageHandler.ERROR_NOTOWN));
        if (!(t.isPresident(uid) || t.isMinister(uid))) {
            throw new CommandException(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_TOWNSTAFF));
        }

        String action = actionOpt.get().toLowerCase();
        switch (action) {
            case "set": {
                String text = textOrTownOpt.orElse("").trim();
                if (text.isEmpty()) {
                    throw new CommandException(Text.of(TextColors.RED, "Usage: /town board set <text>"));
                }
                if (text.length() > MAX_BOARD_LEN) {
                    throw new CommandException(Text.of(TextColors.RED, "Board too long (max " + MAX_BOARD_LEN + " chars)"));
                }
                t.setBoard(text);
                DataHandler.saveTowny(t.getUUID()); // persist
                src.sendMessage(Text.of(TextColors.GREEN, "Town board updated."));
                return CommandResult.success();
            }
            case "clear": {
                t.setBoard("");
                DataHandler.saveTowny(t.getUUID());
                src.sendMessage(Text.of(TextColors.GREEN, "Town board cleared."));
                return CommandResult.success();
            }
            default:
                throw new CommandException(Text.of(TextColors.RED,
                        "Unknown action. Try: /town board, /town board <town>, /town board set <text>, /town board clear"));
        }
    }

    private void showBoard(CommandSource to, Towny t, boolean includeTownName) {
        String board = t.getBoard();
        if (board.isEmpty()) {
            to.sendMessage(Text.of(TextColors.GRAY,
                    includeTownName ? ("Board [" + t.getName() + "]: ") : "Board: ",
                    TextColors.DARK_GRAY, "None"));
            return;
        }
        if (includeTownName) {
            to.sendMessage(Text.of(TextColors.GOLD, "Board [",
                    TextColors.YELLOW, t.getName(),
                    TextColors.GOLD, "]: ",
                    TextColors.WHITE, board));
        } else {
            to.sendMessage(Text.of(TextColors.GOLD, "Board: ",
                    TextColors.WHITE, board));
        }
    }
}
