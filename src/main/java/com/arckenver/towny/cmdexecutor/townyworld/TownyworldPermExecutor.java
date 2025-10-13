package com.arckenver.towny.cmdexecutor.townyworld;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.Utils;
import com.arckenver.towny.object.Towny;
import com.google.common.collect.ImmutableMap;

public class TownyworldPermExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.townyworld.perm")
                                .arguments(
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
				.executor(new TownyworldPermExecutor())
				.build(), "perm");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		String worldName;
		if (ctx.<String>getOne("world").isPresent())
		{
			worldName = ctx.<String>getOne("world").get();
			if (!Sponge.getServer().getWorld(worldName).isPresent())
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADWORLDNAME));
				return CommandResult.success();
			}
		}
		else
		{
			if (src instanceof Player)
			{
				Player player = (Player) src;
				worldName = player.getWorld().getName();
			}
			else
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDWORLDNAME));
				return CommandResult.success();
			}
		}
                String perm = ctx.<String>getOne("perm").get();
                boolean currentVal = ConfigHandler.getNode("worlds").getNode(worldName).getNode("perms").getNode(perm).getBoolean();
                boolean bool = (ctx.<Boolean>getOne("bool").isPresent()) ? ctx.<Boolean>getOne("bool").get() : !currentVal;
                ConfigHandler.getNode("worlds").getNode(worldName).getNode("perms").getNode(perm).setValue(bool);
                if (Towny.PERM_SWITCH.equals(perm)) {
                        ConfigHandler.getNode("worlds").getNode(worldName).getNode("perms").getNode(Towny.PERM_INTERACT).setValue(bool);
                }
                ConfigHandler.save();
		src.sendMessage(Utils.formatWorldDescription(worldName));
		return CommandResult.success();
	}
}
