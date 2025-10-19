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
import com.arckenver.towny.cmdelement.TownyNameElement;
import com.arckenver.towny.cmdelement.PlayerNameElement;
import com.arckenver.towny.object.Towny;

public class TownyadminSetpresExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.townyadmin.setpres")
				.arguments(
						GenericArguments.optional(new TownyNameElement(Text.of("towny"))),
						GenericArguments.optional(new PlayerNameElement(Text.of("mayor"))))
				.executor(new TownyadminSetpresExecutor())
				.build(), "setpres", "setmayor");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (!ctx.<String>getOne("towny").isPresent() || !ctx.<String>getOne("mayor").isPresent())
		{
			src.sendMessage(Text.of(LanguageHandler.colorYellow(), "/ta setpres <towny> <mayor>"));
			return CommandResult.success();
		}
		String townyName = ctx.<String>getOne("towny").get();
		Towny towny = DataHandler.getTowny(townyName);
		if (towny == null)
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_BADTOWNNNAME));
			return CommandResult.success();
		}
		String mayorName = ctx.<String>getOne("mayor").get();
		UUID mayorUUID = DataHandler.getPlayerUUID(mayorName);
		if (mayorUUID == null)
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_BADPLAYERNAME));
			return CommandResult.success();
		}
		if (towny.isPresident(mayorUUID))
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PLAYERALREADYPRES));
			return CommandResult.success();
		}
		if (!towny.isCitizen(mayorUUID))
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PLAYERNOTPARTOFTOWN));
			return CommandResult.success();
		}
		UUID oldPresidentUUID = towny.getPresident();
		final String oldPresidentName = DataHandler.getPlayerName(oldPresidentUUID);
		towny.setPresident(mayorUUID);
		DataHandler.saveTowny(towny.getUUID());
		
		for (UUID citizen : towny.getCitizens())
		{
			Sponge.getServer().getPlayer(citizen).ifPresent(
					p -> p.sendMessage(Text.of(LanguageHandler.colorAqua(), LanguageHandler.INFO_SUCCESSOR
							.replaceAll("\\{SUCCESSOR\\}", mayorName)
							.replaceAll("\\{PLAYER\\}", (oldPresidentName == null) ? LanguageHandler.FORMAT_UNKNOWN : oldPresidentName))));
		}
		
		return CommandResult.success();
	}
}
