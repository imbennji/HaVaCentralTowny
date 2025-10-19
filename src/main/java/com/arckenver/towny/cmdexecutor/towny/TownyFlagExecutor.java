package com.arckenver.towny.cmdexecutor.towny;

import static org.spongepowered.api.util.SpongeApiTranslationHelper.t;

import java.util.stream.Collectors;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.Utils;
import com.arckenver.towny.object.Towny;

public class TownyFlagExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.town.flag")
				.arguments(
						GenericArguments.choices(Text.of("flag"), ConfigHandler.getNode("towny", "flags")
								.getChildrenMap()
								.keySet()
								.stream()
								.map(key -> key.toString())
								.collect(Collectors.toMap(flag -> flag, flag -> flag))),
						GenericArguments.optional(GenericArguments.bool(Text.of("bool"))))
                                .executor(new TownyFlagExecutor())
                                .build(), "flag", "toggle");
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
			String flag = ctx.<String>getOne("flag").get();
			if (!player.hasPermission("towny.command.towny.flag." + flag))
			{
				player.sendMessage(t("You do not have permission to use this command!"));
				return CommandResult.success();
			}
			boolean bool = (ctx.<Boolean>getOne("bool").isPresent()) ? ctx.<Boolean>getOne("bool").get() : !towny.getFlag(flag);
			towny.setFlag(flag, bool);
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
