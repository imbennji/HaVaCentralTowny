package com.arckenver.towny.cmdexecutor.nation;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.cmdelement.TownyNameElement;
import com.arckenver.towny.object.Nation;
import com.arckenver.towny.object.NationRequest;
import com.arckenver.towny.object.Towny;
import org.spongepowered.api.Sponge;
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

import java.util.UUID;

public class NationInviteExecutor implements CommandExecutor {
    public static void create(CommandSpec.Builder cmd) {
        cmd.child(CommandSpec.builder()
                .description(Text.of(""))
                .permission("towny.command.nation.invite")
                .arguments(new TownyNameElement(Text.of("town")))
                .executor(new NationInviteExecutor())
                .build(), "invite", "add");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
        if (!(src instanceof Player)) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
            return CommandResult.success();
        }

        Player player = (Player) src;
        Towny town = DataHandler.getTownyOfPlayer(player.getUniqueId());
        if (town == null || !town.hasNation()) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NONATION));
            return CommandResult.success();
        }

        Nation nation = DataHandler.getNation(town.getNationUUID());
        if (nation == null) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NATION_NOT_FOUND));
            return CommandResult.success();
        }

        if (!nation.isCapital(town.getUUID()) || !town.isPresident(player.getUniqueId())) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_NATIONLEADER));
            return CommandResult.success();
        }

        String townName = ctx.<String>getOne("town").orElse("");
        Towny targetTown = DataHandler.getTowny(townName);
        if (targetTown == null) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADTOWNNNAME));
            return CommandResult.success();
        }

        if (targetTown.hasNation()) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_TOWN_HAS_NATION));
            return CommandResult.success();
        }

        if (DataHandler.getNationInviteRequest(nation.getUUID(), targetTown.getUUID()) != null) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NATION_ALREADY_INVITED));
            return CommandResult.success();
        }

        NationRequest request = new NationRequest(nation.getUUID(), targetTown.getUUID());
        DataHandler.addNationInviteRequest(request);

        UUID mayor = targetTown.getPresident();
        if (mayor != null) {
            Sponge.getServer().getPlayer(mayor).ifPresent(p ->
                    p.sendMessage(Text.of(TextColors.GOLD, LanguageHandler.INFO_NATION_INVITED.replace("{TOWN}", targetTown.getName()),
                            TextColors.YELLOW, " (/nation join " + nation.getRealName() + ")"))
            );
        }

        src.sendMessage(Text.of(TextColors.GREEN, LanguageHandler.INFO_NATION_INVITED.replace("{TOWN}", targetTown.getName())));
        return CommandResult.success();
    }
}
