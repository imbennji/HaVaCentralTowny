package com.arckenver.towny.cmdexecutor.townyadmin;

import com.arckenver.towny.cmdelement.TownyNameElement;
import com.arckenver.towny.cmdexecutor.plot.PlotListExecutor;
import com.arckenver.towny.task.RentCollectRunnable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public class TownyadminCollectRentExecutor implements CommandExecutor {

	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.townyadmin.collectrent")
				.arguments()
				.executor(new TownyadminCollectRentExecutor())
				.build(), "collectrent", "crent", "rentpay");
	}


	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		new RentCollectRunnable().run();
		return CommandResult.success();
	}


}
