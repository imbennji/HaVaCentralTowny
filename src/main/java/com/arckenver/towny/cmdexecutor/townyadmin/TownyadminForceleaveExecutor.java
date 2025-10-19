package com.arckenver.towny.cmdexecutor.townyadmin;

import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.cmdelement.PlayerNameElement;
import com.arckenver.towny.object.Towny;

public class TownyadminForceleaveExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.townyadmin.forceleave")
				.arguments(GenericArguments.optional(new PlayerNameElement(Text.of("player"))))
				.executor(new TownyadminForceleaveExecutor())
				.build(), "forceleave");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (!ctx.<String>getOne("player").isPresent())
		{
			src.sendMessage(Text.of(LanguageHandler.colorYellow(), "/ta forceleave <player>"));
			return CommandResult.success();
		}
		String playerName = ctx.<String>getOne("player").get();
		
		UUID uuid = DataHandler.getPlayerUUID(playerName);
		if (uuid == null)
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_BADPLAYERNAME));
			return CommandResult.success();
		}
		Towny towny = DataHandler.getTownyOfPlayer(uuid);
		if (towny == null)
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PLAYERNOTINTOWN));
			return CommandResult.success();
		}
		if (towny.isPresident(uuid))
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PLAYERISPRES));
			return CommandResult.success();
		}
		towny.removeCitizen(uuid);
		DataHandler.saveTowny(towny.getUUID());
		Sponge.getServer().getPlayer(uuid).ifPresent(p -> 
			p.sendMessage(Text.of(LanguageHandler.colorAqua(), LanguageHandler.SUCCESS_LEAVETOWN)));
		src.sendMessage(Text.of(LanguageHandler.colorGreen(), LanguageHandler.SUCCESS_GENERAL));
		return CommandResult.success();
	}
}
