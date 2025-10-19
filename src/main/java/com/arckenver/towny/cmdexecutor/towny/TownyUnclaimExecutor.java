package com.arckenver.towny.cmdexecutor.towny;

import java.math.BigDecimal;
import java.util.Optional;

import com.arckenver.towny.claim.ChunkClaimUtils;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.TownyPlugin;
import com.arckenver.towny.Utils;
import com.arckenver.towny.object.Towny;
import com.arckenver.towny.object.Point;
import com.arckenver.towny.object.Rect;
import com.arckenver.towny.object.Region;
import com.arckenver.towny.object.Plot;
import com.flowpowered.math.vector.Vector2i;

public class TownyUnclaimExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.town.unclaim")
				.arguments()
				.executor(new TownyUnclaimExecutor())
				.build(), "unclaim");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			Player player = (Player) src;

                        ChunkClaimUtils.selectCurrentChunk(player);

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
                        Point a = DataHandler.getFirstPoint(player.getUniqueId());
                        Point b = DataHandler.getSecondPoint(player.getUniqueId());
                        if (a == null || b == null)
                        {
                                src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NEEDCHUNKSELECT));
				return CommandResult.success();
			}
			if (!ConfigHandler.getNode("worlds").getNode(a.getWorld().getName()).getNode("enabled").getBoolean())
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PLUGINDISABLEDINWORLD));
				return CommandResult.success();
			}
			Rect rect = new Rect(a, b);
			if (!towny.getRegion().intersects(rect))
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NEEDINTERSECT));
				return CommandResult.success();
			}
			for (Location<World> spawn : towny.getSpawns().values())
			{
				if (rect.isInside(new Vector2i(spawn.getBlockX(), spawn.getBlockZ())))
				{
					src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_AREACONTAINSPAWN));
					return CommandResult.success();
				}
			}
			for (Plot plot : towny.getPlots().values())
			{
				if (plot.getRect().intersects(rect))
				{
					src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_SELECTIONCONTAINPLOT));
					return CommandResult.success();
				}
			}
			Region claimed = towny.getRegion().copy();
			claimed.removeRect(rect);
                        int blocksToUnclaim = towny.getRegion().size() - claimed.size();
                        int chunksToUnclaim = (blocksToUnclaim <= 0)
                                        ? 0
                                        : (int) Math.max(1, Math.round(blocksToUnclaim / (double) ChunkClaimUtils.CHUNK_AREA));
			
                        BigDecimal refund = BigDecimal.ZERO;
                        if (ConfigHandler.getNode("prices", "unclaimRefundPercentage").getInt() != 0)
                        {
                                if (TownyPlugin.getEcoService() == null)
				{
					src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOECO));
					return CommandResult.success();
				}
                            Optional<Account> optAccount = TownyPlugin.getOrCreateAccount("towny-" + towny.getUUID().toString());
				if (!optAccount.isPresent())
				{
					src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_ECONOTOWN));
					return CommandResult.success();
				}
                                double pricePerChunk = ConfigHandler.getNode("prices", "chunkClaimPrice").getDouble();
                                double refundPercent = ConfigHandler.getNode("prices", "unclaimRefundPercentage").getDouble() / 100D;
                                refund = BigDecimal.valueOf(chunksToUnclaim * pricePerChunk * refundPercent);
				TransactionResult result = optAccount.get().deposit(TownyPlugin.getEcoService().getDefaultCurrency(), refund, TownyPlugin.getCause());
				if (result.getResult() != ResultType.SUCCESS)
				{
					src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_ECOTRANSACTION));
					return CommandResult.success();
				}
			}
			
			
			towny.setRegion(claimed);
			DataHandler.addToWorldChunks(towny);
			DataHandler.saveTowny(towny.getUUID());
                        if (!refund.equals(BigDecimal.ZERO))
                        {
                                String str = LanguageHandler.INFO_UNCLAIMREFUND
                                                .replaceAll("\\{NUM\\}", Integer.toString(chunksToUnclaim))
                                                .replaceAll("\\{PERCENT\\}", ConfigHandler.getNode("prices", "unclaimRefundPercentage").getString());
				src.sendMessage(Text.builder()
						.append(Text.of(LanguageHandler.colorAqua(), str.split("\\{AMOUNT\\}")[0]))
						.append(Utils.formatPrice(LanguageHandler.colorAqua(), refund))
						.append(Text.of(LanguageHandler.colorAqua(), str.split("\\{AMOUNT\\}")[1])).build());
			}
			else
			{
				src.sendMessage(Text.of(LanguageHandler.colorAqua(), LanguageHandler.SUCCESS_UNCLAIM));
			}
		}
		else
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
