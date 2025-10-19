package com.arckenver.towny.cmdexecutor.towny;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.TownyPlugin;
import com.arckenver.towny.Utils;
import com.arckenver.towny.cmdelement.TownyNameElement;
import com.arckenver.towny.event.PlayerTeleportEvent;
import com.arckenver.towny.object.Towny;

public class TownyVisitExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.town.visit")
				.arguments(
						GenericArguments.optional(new TownyNameElement(Text.of("towny"))),
						GenericArguments.optional(GenericArguments.string(Text.of("name"))))
				.executor(new TownyVisitExecutor())
				.build(), "visit");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			Player player = (Player) src;
			if (!ctx.<String>getOne("towny").isPresent())
			{
				src.sendMessage(Text.of(LanguageHandler.colorYellow(), "/t visit <towny> [name]"));
				return CommandResult.success();
			}
			String townyName = ctx.<String>getOne("towny").get();
			Towny towny = DataHandler.getTowny(townyName);
			if (towny == null)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOTOWN));
				return CommandResult.success();
			}
			int clicker = Utils.CLICKER_NONE;
			
			Towny playerTowny = DataHandler.getTownyOfPlayer(player.getUniqueId());
			if (playerTowny != null && playerTowny.getUUID().equals(towny.getUUID())) {
				clicker = Utils.CLICKER_DEFAULT;
			}
			
			if (player.hasPermission("towny.admin.bypass.visit"))
			{
				clicker = Utils.CLICKER_ADMIN;
			}
			
			if (clicker == Utils.CLICKER_NONE && !towny.getFlag("public")) {
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_TOWNNOTPUBLIC));
				return CommandResult.success();
			}
			
			if (!ctx.<String>getOne("name").isPresent())
			{
				src.sendMessage(Text.builder()
						.append(Text.of(LanguageHandler.colorAqua(), LanguageHandler.INFO_TELEPORTLIST.split("\\{SPAWNLIST\\}")[0]))
						.append(Utils.formatTownySpawns(towny, LanguageHandler.colorYellow(), clicker))
						.append(Text.of(LanguageHandler.colorAqua(), LanguageHandler.INFO_TELEPORTLIST.split("\\{SPAWNLIST\\}")[1]))
						.append(Text.of(LanguageHandler.colorDarkGray(), " <- " + LanguageHandler.CLICK)).build());
				return CommandResult.success();
			}

			String spawnName = ctx.<String>getOne("name").get();
			Location<World> spawn = towny.getSpawn(spawnName);
			if (spawn == null)
			{
				src.sendMessage(Text.builder()
						.append(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_SPAWNNAME.split("\\{SPAWNLIST\\}")[0]))
						.append(Utils.formatTownySpawns(towny, LanguageHandler.colorYellow(), clicker))
						.append(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_SPAWNNAME.split("\\{SPAWNLIST\\}")[1]))
						.append(Text.of(LanguageHandler.colorDarkGray(), " <- " + LanguageHandler.CLICK)).build());
				return CommandResult.success();
			}
			
			if (player.hasPermission("towny.bypass.teleport.warmup")) {
				PlayerTeleportEvent event = new PlayerTeleportEvent(player, spawn, TownyPlugin.getCause());
				Sponge.getEventManager().post(event);
				if (!event.isCancelled())
				{
					player.setLocation(spawn);
					src.sendMessage(Text.of(LanguageHandler.colorAqua(), LanguageHandler.INFO_TELEPORTED));
				}
				return CommandResult.success();
			}
			
			src.sendMessage(Text.of(LanguageHandler.colorAqua(), LanguageHandler.INFO_TELEPORTCOOLDOWN));
			
			Scheduler scheduler = Sponge.getScheduler();
			Task.Builder taskBuilder = scheduler.createTaskBuilder();
			taskBuilder.execute(new Consumer<Task>() {
				
				@Override
				public void accept(Task t) {
					t.cancel();
					PlayerTeleportEvent event = new PlayerTeleportEvent(player, spawn, TownyPlugin.getCause());
					Sponge.getEventManager().post(event);
					if (!event.isCancelled())
					{
						player.setLocation(spawn);
						src.sendMessage(Text.of(LanguageHandler.colorAqua(), LanguageHandler.INFO_TELEPORTED));
					}
				}
			}).delay(10, TimeUnit.SECONDS).submit(TownyPlugin.getInstance());
		}
		else
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
