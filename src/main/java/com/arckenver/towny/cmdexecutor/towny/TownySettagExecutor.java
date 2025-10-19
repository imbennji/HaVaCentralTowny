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

public class TownySettagExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.town.settag")
				.arguments(GenericArguments.optional(GenericArguments.string(Text.of("tag"))))
				.executor(new TownySettagExecutor())
				.build(), "settag", "tag");
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
			if (!towny.isStaff(player.getUniqueId()))
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PERM_TOWNSTAFF));
				return CommandResult.success();
			}
			String newTag = null;
			if (ctx.<String>getOne("tag").isPresent())
				newTag = ctx.<String>getOne("tag").get();
			if (newTag != null && DataHandler.getTowny(newTag) != null)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NAMETAKEN));
				return CommandResult.success();
			}
			if (newTag != null && DataHandler.getTownyByTag(newTag) != null)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_TAGTAKEN));
				return CommandResult.success();
			}
			if (newTag != null && !newTag.matches("[\\p{Alnum}\\p{IsIdeographic}\\p{IsLetter}\"_\"]*"))
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_TAGALPHA));
				return CommandResult.success();
			}
			if (newTag != null && (newTag.length() < ConfigHandler.getNode("others", "minTownyTagLength").getInt() || newTag.length() > ConfigHandler.getNode("others", "maxTownyTagLength").getInt()))
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_TAGLENGTH
						.replaceAll("\\{MIN\\}", ConfigHandler.getNode("others", "minTownyTagLength").getString())
						.replaceAll("\\{MAX\\}", ConfigHandler.getNode("others", "maxTownyTagLength").getString())));
				return CommandResult.success();
			}
			String oldName = towny.getTag();
			towny.setTag(newTag);
			DataHandler.saveTowny(towny.getUUID());
			MessageChannel.TO_ALL.send(Text.of(LanguageHandler.colorAqua(), LanguageHandler.INFO_TAG
					.replaceAll("\\{NAME\\}", towny.getName())
					.replaceAll("\\{OLDTAG\\}", oldName)
					.replaceAll("\\{NEWTAG\\}", towny.getTag())));
		}
		else
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
