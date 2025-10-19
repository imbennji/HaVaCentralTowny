package com.arckenver.towny.cmdexecutor.towny;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.object.Towny;

public class TownySetnameExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.town.setname")
				.arguments(GenericArguments.optional(GenericArguments.string(Text.of("name"))))
				.executor(new TownySetnameExecutor())
				.build(), "setname", "rename");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			if (!ctx.<String>getOne("name").isPresent())
			{
				src.sendMessage(Text.of(LanguageHandler.colorYellow(), "/t setname <name>"));
				return CommandResult.success();
			}
			Player player = (Player) src;
			Towny towny = DataHandler.getTownyOfPlayer(player.getUniqueId());
			if (towny == null)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOTOWN));
				return CommandResult.success();
			}
			if (!towny.isStaff(player.getUniqueId()))
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PERM_TOWNSTAFF));
				return CommandResult.success();
			}
			String newName = ctx.<String>getOne("name").get();
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
			if (newName.length() < ConfigHandler.getNode("others", "minTownyNameLength").getInt() || newName.length() > ConfigHandler.getNode("others", "maxTownyNameLength").getInt())
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NAMELENGTH
						.replaceAll("\\{MIN\\}", ConfigHandler.getNode("others", "minTownyNameLength").getString())
						.replaceAll("\\{MAX\\}", ConfigHandler.getNode("others", "maxTownyNameLength").getString())));
				return CommandResult.success();
			}
			String oldName = towny.getName();
			towny.setName(newName);
			DataHandler.saveTowny(towny.getUUID());
			MessageChannel.TO_ALL.send(Text.of(LanguageHandler.colorAqua(), LanguageHandler.INFO_RENAME
					.replaceAll("\\{OLDNAME\\}", oldName)
					.replaceAll("\\{NEWNAME\\}", towny.getName())));
		}
		else
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
