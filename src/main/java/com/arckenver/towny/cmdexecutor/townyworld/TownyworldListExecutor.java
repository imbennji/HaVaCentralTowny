package com.arckenver.towny.cmdexecutor.townyworld;

import java.util.Iterator;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Builder;
import org.spongepowered.api.world.World;

import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.Utils;

public class TownyworldListExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.townyworld.list")
				.arguments()
				.executor(new TownyworldListExecutor())
				.build(), "list");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		Builder builder = Text.builder();
		Iterator<World> iter = Sponge.getServer().getWorlds().iterator();
		builder.append(Text.of(LanguageHandler.colorGold(), "--------{ ", LanguageHandler.colorYellow(), LanguageHandler.HEADER_WORLDLIST, LanguageHandler.colorGold(), " }--------\n"));
		while (iter.hasNext())
		{
			World world = iter.next();
			builder.append(Utils.worldClickable(LanguageHandler.colorYellow(), world.getName()));
			if (iter.hasNext())
			{
				builder.append(Text.of(LanguageHandler.colorYellow(), ", "));
			}
		}
		if (src.hasPermission("towny.command.townyworld.info"))
		{
			builder.append(Text.of(LanguageHandler.colorDarkGray(), " <- click"));
		}
		src.sendMessage(builder.build());
		return CommandResult.success();
	}
}
