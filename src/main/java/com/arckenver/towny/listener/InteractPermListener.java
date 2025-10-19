package com.arckenver.towny.listener;

import java.util.Optional;

import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.object.Towny;

public class InteractPermListener
{

	@Listener
	public void onCollideBlock(CollideBlockEvent event, @First Player player)
	{
		if (player.hasPermission("towny.admin.bypass.perm.build"))
		{
			return;
		}
		if (ConfigHandler.getNode("worlds").getNode(event.getTargetLocation().getExtent().getName()).getNode("enabled").getBoolean()
                                && !ConfigHandler.isWhitelisted("build", event.getTargetBlock().getType().getId())
                                && !DataHandler.getPerm(Towny.PERM_SWITCH, player.getUniqueId(), event.getTargetLocation()))
		{
			event.setCancelled(true);
		}
	}

	@Listener(order=Order.FIRST, beforeModifications = true)
	public void onInteract(InteractBlockEvent event, @First Player player)
	{
		if (!ConfigHandler.getNode("worlds").getNode(player.getWorld().getName()).getNode("enabled").getBoolean())
		{
			return;
		}
		if (player.hasPermission("towny.admin.bypass.perm.interact"))
		{
			return;
		}
                Optional<ItemStack> optItem = player.getItemInHand(HandTypes.MAIN_HAND);
                if (optItem.isPresent() && ConfigHandler.isWhitelisted("use", optItem.get().getType().getId()))
                        return;
		event.getTargetBlock().getLocation().ifPresent(loc -> {
                        if (!DataHandler.getPerm(Towny.PERM_SWITCH, player.getUniqueId(), loc))
                        {
                                event.setCancelled(true);
                                if (loc.getBlockType() != BlockTypes.STANDING_SIGN && loc.getBlockType() != BlockTypes.WALL_SIGN)
                                        player.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PERM_SWITCH));
                        }
                });
	}

	@Listener(order=Order.FIRST, beforeModifications = true)
	public void onInteract(InteractEntityEvent event, @First Player player)
	{
		if (!ConfigHandler.getNode("worlds").getNode(player.getWorld().getName()).getNode("enabled").getBoolean())
		{
			return;
		}
		if (player.hasPermission("towny.admin.bypass.perm.interact"))
		{
			return;
		}
		Entity target = event.getTargetEntity();
		if (target instanceof Player || target instanceof Monster)
		{
			return;
		}
		if (target instanceof ItemFrame || target instanceof ArmorStand)
		{
			if (player.hasPermission("towny.admin.bypass.perm.build"))
			{
				return;
			}
                        if (!DataHandler.getPerm(Towny.PERM_DESTROY, player.getUniqueId(), event.getTargetEntity().getLocation()))
                        {
                                event.setCancelled(true);
                                player.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PERM_DESTROY));
                        }
                        return;
                }
                if (!DataHandler.getPerm(Towny.PERM_ITEM_USE, player.getUniqueId(), event.getTargetEntity().getLocation()))
                {
                        event.setCancelled(true);
                        player.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PERM_ITEMUSE));
                }
        }
}
