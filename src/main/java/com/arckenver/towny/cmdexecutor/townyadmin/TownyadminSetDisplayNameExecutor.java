package com.arckenver.towny.cmdexecutor.townyadmin;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.cmdelement.TownyNameElement;
import com.arckenver.towny.object.Towny;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TownyadminSetDisplayNameExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.townyadmin.setdisplayname")
				.arguments(
						new TownyNameElement(Text.of("towny")),
						GenericArguments.optional(GenericArguments.string(Text.of("displayName"))))
				.executor(new TownyadminSetDisplayNameExecutor())
				.build(), "setdisplayname", "setdname", "setdisplay");
	}

	@Nonnull
	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (!ctx.<String> getOne("towny").isPresent())
		{
			src.sendMessage(Text.of(LanguageHandler.colorYellow(), "/ta setdisplayname <towny> [tag]"));
			return CommandResult.success();
		}
		String newDisplayName = null;
		if (ctx.<String> getOne("displayName").isPresent())
			newDisplayName = ctx.<String> getOne("displayName").get();
		Towny towny = DataHandler.getTowny(ctx.<String> getOne("towny").get());
		if (towny == null)
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOTOWN));
			return CommandResult.success();
		}
		if (newDisplayName != null && (newDisplayName.length() < ConfigHandler.getNode("others", "minTownyDisplayLength").getInt()
				|| newDisplayName.length() > ConfigHandler.getNode("others", "maxTownyDisplayLength").getInt()))
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(),
					LanguageHandler.ERROR_DISPLAYLENGTH
							.replaceAll("\\{MIN\\}",
									ConfigHandler.getNode("others", "minTownyDisplayLength").getString())
							.replaceAll("\\{MAX\\}",
									ConfigHandler.getNode("others", "maxTownyDisplayLength").getString())));
			return CommandResult.success();
		}
		String oldName = towny.getDisplayName();
		towny.setDisplayName(newDisplayName);
		DataHandler.saveTowny(towny.getUUID());
		MessageChannel.TO_ALL.send(Text.of(LanguageHandler.colorAqua(),
				LanguageHandler.INFO_DISPLAY.replaceAll("\\{NAME\\}", towny.getName()).replaceAll("\\{OLDNAME\\}", oldName).replaceAll("\\{NEWNAME\\}", towny.getDisplayName())));
		return CommandResult.success();
	}
}

