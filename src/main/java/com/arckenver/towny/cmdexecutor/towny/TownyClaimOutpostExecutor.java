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

public class TownyClaimOutpostExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		// subcommand of TownyClaimOutpost
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			Player player = (Player) src;
                        // Automatically set the points based on the player's current chunk
                        ChunkClaimUtils.selectCurrentChunk(player);
			if (!ConfigHandler.getNode("worlds").getNode(player.getWorld().getName()).getNode("enabled").getBoolean())
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PLUGINDISABLEDINWORLD));
				return CommandResult.success();
			}
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
                        Location<World> loc = player.getLocation();
                        if (!DataHandler.canClaim(loc, false, towny.getUUID()))
                        {
                                src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_TOOCLOSE));
                                return CommandResult.success();
                        }

                        Point firstPoint = DataHandler.getFirstPoint(player.getUniqueId());
                        Point secondPoint = DataHandler.getSecondPoint(player.getUniqueId());
                        if (firstPoint == null || secondPoint == null)
                        {
                                src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NEEDCHUNKSELECT));
                                return CommandResult.success();
                        }
			
			if (TownyPlugin.getEcoService() == null)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOECO));
				return CommandResult.success();
			}
                    Optional<Account> optAccount = TownyPlugin.getOrCreateAccount("towny-" + towny.getUUID());
			if (!optAccount.isPresent())
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_ECONOTOWN));
				return CommandResult.success();
			}
			BigDecimal price = BigDecimal.valueOf(ConfigHandler.getNode("prices", "outpostCreationPrice").getDouble());
			TransactionResult result = optAccount.get().withdraw(TownyPlugin.getEcoService().getDefaultCurrency(), price, TownyPlugin.getCause());
			if (result.getResult() == ResultType.ACCOUNT_NO_FUNDS)
			{
				src.sendMessage(Text.builder()
						.append(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NEEDMONEYTOWN.split("\\{AMOUNT\\}")[0]))
						.append(Utils.formatPrice(LanguageHandler.colorRed(), price))
						.append(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NEEDMONEYTOWN.split("\\{AMOUNT\\}")[1])).build());
				return CommandResult.success();
			}
			else if (result.getResult() != ResultType.SUCCESS)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_ECOTRANSACTION));
				return CommandResult.success();
			}
			
                        towny.getRegion().addRect(new Rect(firstPoint, secondPoint));

                        int outpostIndex = towny.getNextOutpostIndex();
                        towny.addExtraSpawns(1);
                        towny.setOutpostSpawn(outpostIndex, player.getLocation());

                        DataHandler.addToWorldChunks(towny);
                        DataHandler.saveTowny(towny.getUUID());
                        src.sendMessage(Text.of(LanguageHandler.colorGreen(), LanguageHandler.SUCCESS_OUTPOST));
                        src.sendMessage(Text.of(LanguageHandler.colorAqua(),
                                        LanguageHandler.INFO_OUTPOST_SPAWN_SET.replace("{NUMBER}", String.valueOf(outpostIndex))));
                }
                else
                {
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
