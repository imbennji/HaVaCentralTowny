package com.arckenver.towny.cmdexecutor.townyadmin;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import com.arckenver.towny.task.TaxesCollectRunnable;

public class TownyadminForceupkeepExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.townyadmin.forceupkeep")
				.arguments()
				.executor(new TownyadminForceupkeepExecutor())
				.build(), "forceupkeep");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		new TaxesCollectRunnable().run();		
		return CommandResult.success();
	}
}
