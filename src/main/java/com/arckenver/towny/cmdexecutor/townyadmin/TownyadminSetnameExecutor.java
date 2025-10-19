package com.arckenver.towny.cmdexecutor.townyadmin;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.cmdelement.TownyNameElement;
import com.arckenver.towny.object.Towny;

public class TownyadminSetnameExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.townyadmin.setname")
				.arguments(
						GenericArguments.optional(new TownyNameElement(Text.of("oldname"))),
						GenericArguments.optional(GenericArguments.string(Text.of("newname"))))
				.executor(new TownyadminSetnameExecutor())
				.build(), "setname", "rename");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (!ctx.<String> getOne("oldname").isPresent() || !ctx.<String> getOne("newname").isPresent())
		{
			src.sendMessage(Text.of(LanguageHandler.colorYellow(), "/ta setname <oldname> <newname>"));
			return CommandResult.success();
		}
		String oldName = ctx.<String> getOne("oldname").get();
		String newName = ctx.<String> getOne("newname").get();
		Towny towny = DataHandler.getTowny(oldName);
		if (towny == null)
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOTOWN));
			return CommandResult.success();
		}
		if (DataHandler.getTowny(newName) != null)
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NAMETAKEN));
			return CommandResult.success();
		}
		if (DataHandler.getTownyByTag(newName) != null)
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_TAGTAKEN));
			return CommandResult.success();
		}
		if (!newName.matches("[\\p{Alnum}\\p{IsIdeographic}\\p{IsLetter}\"_\"]*"))
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NAMEALPHA));
			return CommandResult.success();
		}
		if (newName.length() < ConfigHandler.getNode("others", "minTownyNameLength").getInt()
				|| newName.length() > ConfigHandler.getNode("others", "maxTownyNameLength").getInt())
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(),
					LanguageHandler.ERROR_NAMELENGTH
							.replaceAll("\\{MIN\\}",
									ConfigHandler.getNode("others", "minTownyNameLength").getString())
							.replaceAll("\\{MAX\\}",
									ConfigHandler.getNode("others", "maxTownyNameLength").getString())));
			return CommandResult.success();
		}
		towny.setName(newName);
		DataHandler.saveTowny(towny.getUUID());
		MessageChannel.TO_ALL.send(Text.of(LanguageHandler.colorRed(),
				LanguageHandler.INFO_RENAME.replaceAll("\\{OLDNAME\\}", oldName).replaceAll("\\{NEWNAME\\}", towny.getName())));
		return CommandResult.success();
	}
}

