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
                                                                                .put("resident", Towny.TYPE_RESIDENT)
                                                                                .put("citizen", Towny.TYPE_RESIDENT)
                                                                                .put("ally", Towny.TYPE_ALLY)
                                                                                .put("nation", Towny.TYPE_NATION)
                                                                                .put("friend", Towny.TYPE_FRIEND)
                                                                                .put("coowner", Towny.TYPE_FRIEND)
                                                                                .put("outsider", Towny.TYPE_OUTSIDER)
                                                                                .build())),
                                                GenericArguments.optional(GenericArguments.choices(Text.of("perm"),
                                                                ImmutableMap.<String, String> builder()
                                                                                .put("build", Towny.PERM_BUILD)
                                                                                .put("destroy", Towny.PERM_DESTROY)
                                                                                .put("break", Towny.PERM_DESTROY)
                                                                                .put("switch", Towny.PERM_SWITCH)
                                                                                .put("itemuse", Towny.PERM_ITEM_USE)
                                                                                .put("item_use", Towny.PERM_ITEM_USE)
                                                                                .put("use", Towny.PERM_ITEM_USE)
                                                                                .put("interact", Towny.PERM_SWITCH)
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
