package com.arckenver.towny.cmdexecutor.townyadmin;

import com.arckenver.towny.TownyPlugin;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.LanguageHandler;

public class TownyadminReloadExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.townyadmin.reload")
				.arguments()
				.executor(new TownyadminReloadExecutor())
				.build(), "reload");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		LanguageHandler.init(TownyPlugin.getInstance().getDefaultConfigDir());
		LanguageHandler.load();
		ConfigHandler.load(src);
		return CommandResult.success();
	}
}
