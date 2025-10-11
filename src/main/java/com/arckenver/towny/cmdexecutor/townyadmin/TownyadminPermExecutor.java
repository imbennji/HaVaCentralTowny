package com.arckenver.towny.cmdexecutor.townyadmin;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.Utils;
import com.arckenver.towny.cmdelement.TownyNameElement;
import com.arckenver.towny.object.Towny;
import com.google.common.collect.ImmutableMap;

public class TownyadminPermExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.townyadmin.perm")
				.arguments(
						GenericArguments.optional(new TownyNameElement(Text.of("towny"))),
						GenericArguments.optional(GenericArguments.choices(Text.of("type"),
								ImmutableMap.<String, String> builder()
										.put(Towny.TYPE_OUTSIDER, Towny.TYPE_OUTSIDER)
										.put(Towny.TYPE_CITIZEN, Towny.TYPE_CITIZEN)
										.put(Towny.TYPE_COOWNER, Towny.TYPE_COOWNER)
										.build())),
						GenericArguments.optional(GenericArguments.choices(Text.of("perm"),
								ImmutableMap.<String, String> builder()
										.put(Towny.PERM_BUILD, Towny.PERM_BUILD)
										.put(Towny.PERM_INTERACT, Towny.PERM_INTERACT)
										.build())),
						GenericArguments.optional(GenericArguments.bool(Text.of("bool"))))
				.executor(new TownyadminPermExecutor())
				.build(), "perm");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (!ctx.<String>getOne("towny").isPresent() || !ctx.<String>getOne("type").isPresent() || !ctx.<String>getOne("perm").isPresent())
		{
			src.sendMessage(Text.of(TextColors.YELLOW, "/ta perm <towny> <type> <perm> [true|false]"));
			return CommandResult.success();
		}
		String townyName = ctx.<String>getOne("towny").get();
		Towny towny = DataHandler.getTowny(townyName);
		if (towny == null)
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADTOWNNNAME));
			return CommandResult.success();
		}
		String type = ctx.<String>getOne("type").get();
		String perm = ctx.<String>getOne("perm").get();
		boolean bool = (ctx.<Boolean>getOne("bool").isPresent()) ? ctx.<Boolean>getOne("bool").get() : !towny.getPerm(type, perm);
		towny.setPerm(type, perm, bool);
		DataHandler.saveTowny(towny.getUUID());
		src.sendMessage(Utils.formatTownyDescription(towny, Utils.CLICKER_ADMIN));
		return CommandResult.success();
	}
}
