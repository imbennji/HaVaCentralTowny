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
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.cmdelement.TownyNameElement;
import com.arckenver.towny.object.Towny;

public class TownyadminSetspawnExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.townyadmin.setspawn")
				.arguments(
						GenericArguments.optional(new TownyNameElement(Text.of("towny"))),
						GenericArguments.optional(GenericArguments.string(Text.of("name"))))
				.executor(new TownyadminSetspawnExecutor())
				.build(), "setspawn");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			Player player = (Player) src;
			if (!ctx.<String>getOne("towny").isPresent() || !ctx.<String>getOne("name").isPresent())
			{
				src.sendMessage(Text.of(LanguageHandler.colorYellow(), "/ta setspawn <towny> <name>"));
				return CommandResult.success();
			}
			String townyName = ctx.<String>getOne("towny").get();
			Towny towny = DataHandler.getTowny(townyName);
			if (towny == null)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_BADTOWNNNAME));
				return CommandResult.success();
			}
			String spawnName = ctx.<String>getOne("name").get();
			
			Location<World> newSpawn = player.getLocation();
			if (!towny.getRegion().isInside(newSpawn))
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_BADSPAWNLOCATION));
				return CommandResult.success();
			}
			if (towny.getNumSpawns() + 1 > towny.getMaxSpawns() && !towny.getSpawns().containsKey(spawnName))
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_MAXSPAWNREACH
						.replaceAll("\\{MAX\\}", String.valueOf(towny.getMaxSpawns()))));
				return CommandResult.success();
			}
			if (!spawnName.matches("[\\p{Alnum}\\p{IsIdeographic}\\p{IsLetter}]{1,30}"))
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_ALPHASPAWN
						.replaceAll("\\{MIN\\}", ConfigHandler.getNode("others", "minPlotNameLength").getString())
						.replaceAll("\\{MAX\\}", ConfigHandler.getNode("others", "maxPlotNameLength").getString())));
				return CommandResult.success();
			}
			towny.addSpawn(spawnName, newSpawn);
			DataHandler.saveTowny(towny.getUUID());
			src.sendMessage(Text.of(LanguageHandler.colorAqua(), LanguageHandler.SUCCESS_CHANGESPAWN));
		}
		else
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
