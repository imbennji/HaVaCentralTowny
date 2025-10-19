package com.arckenver.towny.cmdexecutor.towny;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.Utils;
import com.arckenver.towny.object.Towny;

public class TownyListExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.town.list")
				.arguments()
				.executor(new TownyListExecutor())
				.build(), "list", "l");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		List<Text> contents = new ArrayList<>();
		Iterator<Towny> iter = DataHandler.getTowny().values().iterator();
		if (!iter.hasNext())
		{
			contents.add(Text.of(LanguageHandler.colorYellow(), LanguageHandler.ERROR_NOTOWNYET));
		}
		else
		{
			while (iter.hasNext())
			{
				Towny towny = iter.next();
				if (!towny.isAdmin() || src.hasPermission("towny.admin.towny.listall"))
				{
					contents.add(Text.of(Utils.townyClickable(LanguageHandler.colorYellow(), towny.getRealName()), LanguageHandler.colorGold(), " [" + towny.getNumCitizens() + "]"));
				}
			}
		}
		PaginationList.builder()
		.title(Text.of(LanguageHandler.colorGold(), "{ ", LanguageHandler.colorYellow(), LanguageHandler.HEADER_TOWNLIST, LanguageHandler.colorGold(), " }"))
		.contents(contents)
		.padding(Text.of("-"))
		.sendTo(src);
		return CommandResult.success();
	}
}
