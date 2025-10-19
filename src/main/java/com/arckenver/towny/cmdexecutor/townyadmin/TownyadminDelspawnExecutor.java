package com.arckenver.towny.cmdexecutor.townyadmin;

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
import com.arckenver.towny.Utils;
import com.arckenver.towny.cmdelement.TownyNameElement;
import com.arckenver.towny.object.Towny;

public class TownyadminDelspawnExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.townyadmin.delspawn")
				.arguments(
						GenericArguments.optional(new TownyNameElement(Text.of("towny"))),
						GenericArguments.optional(GenericArguments.string(Text.of("name"))))
				.executor(new TownyadminDelspawnExecutor())
				.build(), "delspawn");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			if (!ctx.<String>getOne("towny").isPresent())
			{
				src.sendMessage(Text.of(LanguageHandler.colorYellow(), "/ta delspawn <towny> <name>"));
				return CommandResult.success();
			}
			Towny towny = DataHandler.getTowny(ctx.<String>getOne("towny").get());
			if (towny == null)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_BADTOWNNNAME));
				return CommandResult.success();
			}
			if (!ctx.<String>getOne("name").isPresent())
			{
				src.sendMessage(Text.builder()
						.append(Text.of(LanguageHandler.colorAqua(), LanguageHandler.INFO_CLICK_DELSPAWN.split("\\{SPAWNLIST\\}")[0]))
						.append(Utils.formatTownySpawns(towny, LanguageHandler.colorYellow(), "delhome"))
						.append(Text.of(LanguageHandler.colorAqua(), LanguageHandler.INFO_CLICK_DELSPAWN.split("\\{SPAWNLIST\\}")[1])).build());
				return CommandResult.success();
			}
			String spawnName = ctx.<String>getOne("name").get();
			if (towny.getSpawn(spawnName) == null)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_BADSPAWNNAME));
				return CommandResult.success();
			}
			towny.removeSpawn(spawnName);
			DataHandler.saveTowny(towny.getUUID());
			src.sendMessage(Text.of(LanguageHandler.colorAqua(), LanguageHandler.SUCCESS_DELTOWN));
		}
		else
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
