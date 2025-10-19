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

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.Utils;
import com.arckenver.towny.cmdelement.WorldNameElement;
import com.arckenver.towny.object.Towny;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;

public class TownyworldEnableExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.townyworld.enable")
				.arguments(GenericArguments.optional(new WorldNameElement(Text.of("world"))))
				.executor(new TownyworldEnableExecutor())
				.build(), "enable");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		String worldName;
		if (ctx.<String>getOne("world").isPresent())
		{
			worldName = ctx.<String>getOne("world").get();
			if (!Sponge.getServer().getWorld(worldName).isPresent())
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_BADWORLDNAME));
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
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NEEDWORLDNAME));
				return CommandResult.success();
			}
		}
		CommentedConfigurationNode node = ConfigHandler.getNode("worlds").getNode(worldName);
		if (node.getNode("enabled").getBoolean())
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PLUGINALREADYENABLED));
			return CommandResult.success();
		}
		
		node.getNode("enabled").setValue(true);
		
                node.getNode("perms").getNode(Towny.PERM_BUILD).setValue(true);
                node.getNode("perms").getNode(Towny.PERM_DESTROY).setValue(true);
                node.getNode("perms").getNode(Towny.PERM_SWITCH).setValue(true);
                node.getNode("perms").getNode(Towny.PERM_ITEM_USE).setValue(true);
                node.getNode("perms").getNode(Towny.PERM_INTERACT).setValue(true);
		
		node.getNode("flags", "pvp").setValue(true);
		node.getNode("flags", "mobs").setValue(true);
		node.getNode("flags", "fire").setValue(true);
		node.getNode("flags", "explosions").setValue(true);
		
		ConfigHandler.save();
		src.sendMessage(Utils.formatWorldDescription(worldName));
		return CommandResult.success();
	}
}
