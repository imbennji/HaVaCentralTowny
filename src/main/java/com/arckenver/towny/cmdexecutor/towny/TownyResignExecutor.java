package com.arckenver.towny.cmdexecutor.towny;

import java.util.UUID;

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

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.cmdelement.CitizenNameElement;
import com.arckenver.towny.object.Towny;

public class TownyResignExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.town.resign")
				.arguments(GenericArguments.optional(new CitizenNameElement(Text.of("successor"))))
				.executor(new TownyResignExecutor())
				.build(), "resign");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			
			Player player = (Player) src;
			Towny towny = DataHandler.getTownyOfPlayer(player.getUniqueId());
			if (towny == null)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOTOWN));
				return CommandResult.success();
			}
			if (!towny.isPresident(player.getUniqueId()))
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PERM_TOWNPRES));
				return CommandResult.success();
			}
			if (!ctx.<String>getOne("successor").isPresent())
			{
				src.sendMessage(Text.of(LanguageHandler.colorYellow(), "/t resign <successor>"));
				return CommandResult.success();
			}
			String successorName = ctx.<String>getOne("successor").get();
			UUID successor = DataHandler.getPlayerUUID(successorName);
			if (successor == null)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_BADPRESNAME));
				return CommandResult.success();
			}
			if (!towny.isCitizen(successor))
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOTINTOWN));
				return CommandResult.success();
			}
			towny.removeMinister(successor);
			towny.setPresident(successor);
			DataHandler.saveTowny(towny.getUUID());
			for (UUID citizen : towny.getCitizens())
			{
				Sponge.getServer().getPlayer(citizen).ifPresent(
					p -> p.sendMessage(Text.of(LanguageHandler.colorAqua(), LanguageHandler.INFO_SUCCESSOR.replaceAll("\\{SUCCESSOR\\}", successorName).replaceAll("\\{PLAYER\\}", player.getName()))));
			}
		}
		else
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
