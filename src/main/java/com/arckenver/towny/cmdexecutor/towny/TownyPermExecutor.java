package com.arckenver.towny.cmdexecutor.towny;

import static org.spongepowered.api.util.SpongeApiTranslationHelper.t;

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
import com.arckenver.towny.object.Towny;
import com.google.common.collect.ImmutableMap;

public class TownyPermExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.town.perm")
                                .arguments(
                                                GenericArguments.choices(Text.of("type"),
                                                                ImmutableMap.<String, String> builder()
                                                                                .put("resident", Towny.TYPE_RESIDENT)
                                                                                .put("citizen", Towny.TYPE_RESIDENT)
                                                                                .put("ally", Towny.TYPE_ALLY)
                                                                                .put("nation", Towny.TYPE_NATION)
                                                                                .put("outsider", Towny.TYPE_OUTSIDER)
                                                                                .build()),
                                                GenericArguments.choices(Text.of("perm"),
                                                                ImmutableMap.<String, String> builder()
                                                                                .put("build", Towny.PERM_BUILD)
                                                                                .put("destroy", Towny.PERM_DESTROY)
                                                                                .put("break", Towny.PERM_DESTROY)
                                                                                .put("switch", Towny.PERM_SWITCH)
                                                                                .put("itemuse", Towny.PERM_ITEM_USE)
                                                                                .put("item_use", Towny.PERM_ITEM_USE)
                                                                                .put("use", Towny.PERM_ITEM_USE)
                                                                                .put("interact", Towny.PERM_SWITCH)
                                                                                .build()),
						GenericArguments.optional(GenericArguments.bool(Text.of("bool"))))
				.executor(new TownyPermExecutor())
				.build(), "perm");
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
			String type = ctx.<String>getOne("type").get();
			String perm = ctx.<String>getOne("perm").get();
			if (!player.hasPermission("towny.command.towny.perm." + type + "." + perm))
			{
				player.sendMessage(t("You do not have permission to use this command!"));
				return CommandResult.success();
			}
			boolean bool = (ctx.<Boolean>getOne("bool").isPresent()) ? ctx.<Boolean>getOne("bool").get() : !towny.getPerm(type, perm);
			towny.setPerm(type, perm, bool);
			DataHandler.saveTowny(towny.getUUID());
                        int clicker = Utils.CLICKER_DEFAULT;
                        if (clicker != Utils.CLICKER_DEFAULT && src.hasPermission("towny.command.townyadmin"))
                        {
                                clicker = Utils.CLICKER_ADMIN;
                        }
			src.sendMessage(Utils.formatTownyDescription(towny, clicker));
		}
		else
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
