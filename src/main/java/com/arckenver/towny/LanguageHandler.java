package com.arckenver.towny;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

public class LanguageHandler
{
	public static String HELP_DESC_CMD_SETSPAWN = "set spawn with the given name";
	public static String HELP_DESC_CMD_DELSPAWN = "delete spawn with the given name";
	public static String HELP_DESC_CMD_SETNAME = "set town's name";
	public static String HELP_DESC_CMD_SETTAG = "set town's tag";

	public static String HELP_DESC_CMD_T_INFO = "get town details";
	public static String HELP_DESC_CMD_T_HERE = "get details of the town you're standing on";
	public static String HELP_DESC_CMD_T_SEE = "display particles in claimed areas";
	public static String HELP_DESC_CMD_T_LIST = "get the list of all towns";
	public static String HELP_DESC_CMD_T_CREATE = "create a new town";
	public static String HELP_DESC_CMD_T_DEPOSIT = "deposit money in your town bank";
	public static String HELP_DESC_CMD_T_WITHDRAW = "withdraw money from your town bank";
	public static String HELP_DESC_CMD_T_CLAIM = "claim the area you've selected";
	public static String HELP_DESC_CMD_T_UNCLAIM = "unclaim the area you've selected";
	public static String HELP_DESC_CMD_T_INVITE = "invite a player to your town";
	public static String HELP_DESC_CMD_T_JOIN = "ask town staff to let you in the town";
	public static String HELP_DESC_CMD_T_KICK = "kick a player out of your town";
	public static String HELP_DESC_CMD_T_LEAVE = "leave your town";
	public static String HELP_DESC_CMD_T_RESIGN = "resign as the town mayor";
	public static String HELP_DESC_CMD_T_MINISTER = "manage comayors";
	public static String HELP_DESC_CMD_T_PERM = "set town perm";
	public static String HELP_DESC_CMD_T_FLAG = "set town flag";
	public static String HELP_DESC_CMD_T_SPAWN = "teleport to spawn with the given name";
        public static String HELP_DESC_CMD_T_BUYEXTRA = "buy extra claimable chunks";
	public static String HELP_DESC_CMD_T_CITIZEN = "get player details";
	public static String HELP_DESC_CMD_T_TAXES = "set town taxes";
	public static String HELP_DESC_CMD_T_CHAT = "toggle town chat";
	public static String HELP_DESC_CMD_T_VISIT = "teleport to spawn of a public town";
	public static String HELP_DESC_CMD_T_COST = "display town prices";
        public static String HELP_DESC_CMD_T_HOME = "if you have a spawn named 'home', tp to it";

        public static String HELP_DESC_CMD_N_INFO = "get nation details";
        public static String HELP_DESC_CMD_N_LIST = "get the list of all nations";
        public static String HELP_DESC_CMD_N_CREATE = "create a new nation";
        public static String HELP_DESC_CMD_N_INVITE = "invite a town to your nation";
        public static String HELP_DESC_CMD_N_JOIN = "make your town join a nation";
        public static String HELP_DESC_CMD_N_LEAVE = "make your town leave its nation";
        public static String HELP_DESC_CMD_N_SETBOARD = "set the nation board";
        public static String HELP_DESC_CMD_N_SETTAG = "set the nation tag";
        public static String HELP_DESC_CMD_N_TOGGLEOPEN = "toggle whether the nation is open";

	public static String HELP_DESC_CMD_TA_RELOAD = "reloads config file";
	public static String HELP_DESC_CMD_TA_CREATE = "create admin town";
	public static String HELP_DESC_CMD_TA_CLAIM = "claims for admin town";
	public static String HELP_DESC_CMD_TA_DELETE = "delete given town";
	public static String HELP_DESC_CMD_TA_SETPRES = "set town's mayor";
	public static String HELP_DESC_CMD_TA_FORCEJOIN = "make player join town";
	public static String HELP_DESC_CMD_TA_FORCELEAVE = "make player leave town";
	public static String HELP_DESC_CMD_TA_ECO = "manage money";
	public static String HELP_DESC_CMD_TA_PERM = "set town perm";
	public static String HELP_DESC_CMD_TA_FLAG = "set town flag";
	public static String HELP_DESC_CMD_TA_SPY = "spy on towns' private channels";
	public static String HELP_DESC_CMD_TA_FORCEKEEPUP = "force town upkeep script to run";
        public static String HELP_DESC_CMD_TA_EXTRA = "manage extra chunks";
        public static String HELP_DESC_CMD_TA_EXTRAPLAYER = "manage extra chunks using player name";
	public static String HELP_DESC_CMD_TA_EXTRASPAWN = "manage extra spawns";
	public static String HELP_DESC_CMD_TA_EXTRASPAWNPLAYER = "manage extra spawns using player name";
	public static String HELP_DESC_CMD_TA_UNCLAIM = "unclaims for admin town";

	public static String HELP_DESC_CMD_P_INFO = "get plot details";
	public static String HELP_DESC_CMD_P_LIST = "get details of the plot you're standing on";
	public static String HELP_DESC_CMD_P_CREATE = "create a new plot";
	public static String HELP_DESC_CMD_P_COOWNER = "manage coowners";
	public static String HELP_DESC_CMD_P_SETOWNER = "set plot owner";
	public static String HELP_DESC_CMD_P_DELOWNER = "make this plot owner free";
	public static String HELP_DESC_CMD_P_PERM = "set plot perm";
	public static String HELP_DESC_CMD_P_FLAG = "set plot flag";
	public static String HELP_DESC_CMD_P_SELL = "put plot up for sale";
	public static String HELP_DESC_CMD_P_BUY = "buy the plot you're standing on";
	public static String HELP_DESC_CMD_P_DELETE = "delete specified plot (or standing on)";
	public static String HELP_DESC_CMD_P_RENAME = "rename plot";

	public static String HELP_DESC_CMD_TW_INFO = "get world details";
	public static String HELP_DESC_CMD_TW_LIST = "get the list of all worlds";
	public static String HELP_DESC_CMD_TW_ENABLE = "enable towns in specified world";
	public static String HELP_DESC_CMD_TW_DISABLE = "disable towns in specified world";
	public static String HELP_DESC_CMD_TW_PERM = "set world perm";
	public static String HELP_DESC_CMD_TW_FLAG = "set world flag";

	public static String ERROR_NOPLAYER = "You must be an in-game player to perform that command";

	public static String ERROR_BADTOWNNNAME = "Invalid town name";
	public static String ERROR_BADPLAYERNAME = "Invalid player name";
	public static String ERROR_BADWORLDNAME = "Invalid world name";
	public static String ERROR_BADPRESNAME = "Invalid successor name";
	public static String ERROR_BADPLOTNNAME = "Invalid plot name";

	public static String ERROR_NEEDTOWNNAME = "You must specify town name";
	public static String ERROR_NEEDPLAYERNAME = "You must specify player name";
	public static String ERROR_NEEDWORLDNAME = "You must specify world name";

        public static String ERROR_NOTOWN = "You must be in a town to perform that command, type /town ? for more help";
        public static String ERROR_NOTOWNYET = "There is no town created yet";
        public static String ERROR_NONATIONYET = "There is no nation created yet";
        public static String ERROR_NONATION = "You must be part of a nation to perform that command";
        public static String ERROR_NATION_NOT_FOUND = "Nation not found";
        public static String ERROR_TOWN_HAS_NATION = "Your town already belongs to a nation";
        public static String ERROR_TOWN_NO_NATION = "Your town does not belong to a nation";
        public static String ERROR_NATION_NAME_TAKEN = "That nation name is already in use";
        public static String ERROR_NATION_TAG_TAKEN = "That nation tag is already in use";
        public static String ERROR_NATION_NAME_LENGTH = "Nation name must be between {MIN} and {MAX} characters";
        public static String ERROR_NATION_TAG_LENGTH = "Nation tag must be between {MIN} and {MAX} characters";
        public static String ERROR_PERM_NATIONLEADER = "You must be the nation leader to perform that command";
        public static String ERROR_PERM_NATIONSTAFF = "You must be the nation leader or an assistant to perform that command";
        public static String ERROR_NATION_CLOSED = "This nation is not open";
        public static String ERROR_NATION_INVITE_REQUIRED = "Your town needs an invitation to join this nation";
        public static String ERROR_NEEDNATIONNAME = "You must specify nation name";
        public static String ERROR_NEEDTOWN = "You must specify a town";
        public static String ERROR_NATION_CAPITAL_REQUIRED = "A nation capital must remain in the nation";
        public static String ERROR_NATION_ALREADY_INVITED = "That town already has a pending nation invitation";
        public static String ERROR_NATION_SELF_RELATION = "You cannot perform that action on your own nation";
        public static String ERROR_NATION_ALREADY_ALLY = "That nation is already an ally";
        public static String ERROR_NATION_NOT_ALLY = "That nation is not an ally";
        public static String ERROR_NATION_ALREADY_ENEMY = "That nation is already an enemy";
        public static String ERROR_NATION_NOT_ENEMY = "That nation is not an enemy";
        public static String ERROR_NATION_NO_TARGET_TOWN = "That town is not part of your nation";
        public static String ERROR_NATION_NO_TARGET_PLAYER = "That player is not part of your nation";
        public static String ERROR_NATION_SPAWN_COST = "Nation spawn cost cannot exceed {MAX}";
        public static String ERROR_NATION_NO_SPAWN = "This nation does not have a spawn set";
        public static String ERROR_NATION_SPAWN_PRIVATE = "This nation spawn is not public";
        public static String ERROR_NATION_TAX_RANGE = "Nation taxes must be between 0 and {MAX}";
        public static String ERROR_NATION_BANK = "Could not access the nation's bank account";
        public static String ERROR_NATION_BANK_FUNDS = "The nation bank does not have enough funds";
        public static String ERROR_NATION_BANK_PLAYER_FUNDS = "You do not have enough funds";
        public static String ERROR_PLAYERNOTPARTOFTOWN = "That player is not part of the town";
	public static String ERROR_PLUGINDISABLEDINWORLD = "Town plugin is disabled for this world";
	public static String ERROR_PLUGINALREADYENABLED = "Town plugin is already enabled for this world";
	public static String ERROR_PLUGINALREADYDISABLE = "Town plugin is already disabled for this world";

	public static String ERROR_PERM_TOWNPRES = "You must be mayor of your town to perform that command";
	public static String ERROR_PERM_TOWNSTAFF = "You must be mayor or minister of your town to perform that command";
	public static String ERROR_PERM_LISTPLOTS = "You don't have permission to list all plots of that town";

	public static String ERROR_PLAYERALREADYPRES = "That player is already mayor";

	public static String ERROR_CONFIGFILE = "Could not load or create config file";
	public static String INFO_CONFIGRELOADED = "Config file has been reloaded";

	public static String INFO_UPKEEPANNOUNCE = "A new day is here! Towns now have to pay their upkeep.";
	public static String INFO_RENTTIME = "It's time to pay rent in your town, paying the landlord!";
        public static String INFO_TOWNFAILUPKEEP = "Town {TOWN} could not pay its upkeep and fell into ruins";
        public static String INFO_TOWNFALL = "Town {TOWN} fell into ruins!";
        public static String INFO_NATION_CREATED = "Nation {NATION} has been founded";
        public static String INFO_NATION_DISBANDED = "Nation {NATION} has disbanded";
        public static String INFO_NATION_INVITED = "{TOWN} has been invited to join the nation";
        public static String INFO_NATION_JOINED = "{TOWN} has joined the nation";
        public static String INFO_NATION_LEFT = "{TOWN} has left the nation";
        public static String INFO_NATION_BOARD = "Nation board updated";
        public static String INFO_NATION_TAG = "Nation tag updated";
        public static String INFO_NATION_OPEN = "Nation open status updated";
        public static String INFO_NATION_PUBLIC = "Nation public status updated";
        public static String INFO_NATION_NEUTRAL = "Nation neutrality updated";
        public static String INFO_NATION_CAPITAL = "Nation capital updated";
        public static String INFO_NATION_ASSISTANT_ADDED = "Assistant added to the nation";
        public static String INFO_NATION_ASSISTANT_REMOVED = "Assistant removed from the nation";
        public static String INFO_NATION_ALLY_ADDED = "Nation alliance created";
        public static String INFO_NATION_ALLY_REMOVED = "Nation alliance removed";
        public static String INFO_NATION_ENEMY_ADDED = "Nation enemy added";
        public static String INFO_NATION_ENEMY_REMOVED = "Nation enemy removed";
        public static String INFO_NATION_KING = "Nation leadership updated";
        public static String INFO_NATION_SPAWN_COST = "Nation spawn cost updated";
        public static String INFO_NATION_SPAWN_SET = "Nation spawn updated";
        public static String INFO_NATION_SPAWN_TRAVEL = "Teleporting to nation spawn";
        public static String INFO_NATION_TAXES = "Nation taxes updated";
        public static String INFO_NATION_TAXES_PERCENT = "Nation tax mode toggled";
        public static String INFO_NATION_GOVERNMENT = "Nation government updated to {GOVERNMENT}";
        public static String INFO_NATION_DEPOSIT = "Deposited {AMOUNT} into the nation bank";
        public static String INFO_NATION_WITHDRAW = "Withdrew {AMOUNT} from the nation bank";

	public static String ERROR_BADARG_GTS = "Invalid operation, use \"give\", \"take\" or \"set\"";
	public static String ERROR_BADARG_AR = "Invalid argument, you must use \"add\" or \"remove\"";
	public static String ERROR_BADARG_P = "Price must be a positive or null value";

	public static String ERROR_NO_PERMISSION = "You don't have permission to do that";

	public static String ERROR_NOECO = "There is no economy plugin on this server";
	public static String ERROR_CREATEECOTOWN = "Could not create town's account, please contact a server administrator";
	public static String ERROR_ECONOTOWN = "Could not get the town's account on the economy plugin of this server";
	public static String ERROR_ECOTRANSACTION = "An unexpected error has occurred while processing transaction";
	public static String ERROR_ECONOACCOUNT = "Could not get your account on the economy plugin of this server";
	public static String ERROR_ECONOOWNER = "Could not get plot owner's account on the economy plugin of this server";
	public static String ERROR_ECONOPLOT = "Could not get plot's account on the economy plugin of this server";
	public static String ERROR_NEEDMONEY = "You need {AMOUNT} to perform that transaction";
	public static String ERROR_NEEDMONEYTOWN = "Your town needs {AMOUNT} to perform that transaction";
	public static String ERROR_NOENOUGHMONEY = "You don't have that much money";
	public static String ERROR_NOENOUGHMONEYTOWN = "Your town doesn't have that much money";
        public static String ERROR_NOMORECHUNKS = "Your town can't buy more than {NUM} extra chunks";
	public static String ERROR_NEEDSTANDTOWN = "You must be standing in a town to perform that command";
	public static String ERROR_PERM_PLOTBUY = "You do not have permission to buy a plot in this town";
	public static String ERROR_PERM_PLOTRENT = "You do not have permission to rent a plot in this town";

        public static String SUCCESS_ADDCHUNKS = "Successfully bought {NUM} extra chunks for {AMOUNT}";
	public static String SUCCESS_UNCLAIM = "You successfully unclaimed this area";
	public static String SUCCESS_WITHDRAW = "You successfully took {AMOUNT} from your town that has now {BALANCE}";

        public static String INFO_UNCLAIMREFUND = "Your town was refunded {AMOUNT} for unclaiming these {NUM} chunk(s) ({PERCENT}% of the purchase price)";
	public static String INFO_PLOTFORSALE = "{PLAYER} put plot {PLOT} up for sale at {AMOUNT}";
	public static String INFO_PLOTFORRENT = "{PLOT} is now up for rent at {AMOUNT}";
	public static String INFO_PLOT_TAX_PAID = "Paid daily plot tax {AMOUNT} for plot {PLOT} to {TOWN}.";
	public static String INFO_PLOTTAX_FAIL = "You failed to pay plot tax for {PLOT}; plot is now unowned";
	public static String INFO_PLOT_TAX_TOWN_SUMMARY = "Collected {AMOUNT} in plot taxes from {COUNT} plots.";

	public static String INFO_PAYRENTPLOTBALANCE = "Paid rent for plot {PLOT} with value {VALUE} using plot's account.";
	public static String INFO_PAYRENTPLOTPLAYER = "Paid rent for plot {PLOT} with value {VALUE} using player's account.";
	public static String INFO_FAILEDRENT = "You failed your rent and are going to lose the plot {PLOT}";

	public static String ERROR_NOTRENTING = "You are not renting this plot at this moment!";

	public static String INFO_RENTINTERVAL = "You set your town rent interval to {NUMBER} hours.";

	public static String INFO_TOWNCHATON_ON = "You are now speaking in your town's private channel";
	public static String INFO_TOWNCHAT_OFF = "You are no longer speaking in your town's private channel";
	public static String INFO_TOWNSPY_ON = "You are now spying towns' private channels";
	public static String INFO_TOWNSPY_OFF = "You are no longer spying towns' private channels";

	public static String RES_FRIEND_ADDED = "Added friend.";
	public static String RES_FRIEND_REMOVED = "Removed friend.";
	public static String RES_FRIEND_EXISTS = "Already a friend.";
	public static String RES_FRIEND_ABSENT = "Not on your list.";
	public static String RES_TITLE_SET = "Title updated.";
	public static String RES_AUTOMAP_ON = "Auto-map enabled. Walk between plots to see the map.";
	public static String RES_AUTOMAP_OFF = "Auto-map disabled.";


        public static String ERROR_NEEDCHUNKSELECT = "You must select a chunk before using this command (stand in the target chunk and try again)";
	public static String ERROR_NEEDADJACENT = "Your selection must be adjacent to your region";
	public static String ERROR_NEEDINTERSECT = "Your selection must intersect your region";
	public static String ERROR_AREACONTAINSPAWN = "Your selection contains a spawn of your town";
        public static String ERROR_NOENOUGHCHUNKS = "Your town doesn't have enough chunks, you can buy extra ones with /town buyextra";
	public static String SUCCESS_CLAIM = "You successfully claimed this area";
	public static String ERROR_TOOCLOSE = "Too close to another town";
	public static String SUCCESS_OUTPOST = "You successfully created an outpost here";
	public static String ERROR_NEEDLEAVE = "You must leave your town to perform that command";
	public static String ERROR_NAMETAKEN = "That name is already taken";
	public static String ERROR_TAGTAKEN = "That tag is already taken";
	public static String ERROR_NAMEALPHA = "Town name must be alphanumeric";
	public static String ERROR_NAMELENGTH = "Town name must contain at least {MIN} and at most {MAX} characters";
	public static String ERROR_TAGALPHA = "Town tag must be alphanumeric";
	public static String ERROR_TAGLENGTH = "Town tag must contain at least {MIN} and at most {MAX} characters";
	public static String ERROR_DISPLAYLENGTH = "Town display name must contain at least {MIN} and at most {MAX} characters";
	public static String INFO_NEWTOWNANNOUNCE = "{PLAYER} has created a new town named {TOWN}";
	public static String INFO_NEWTOWN = "You successfully created town {TOWN}, don't forget to deposit money in the town's bank with /town deposit";
	public static String INFO_CLICK_DELSPAWN = "Click to delete spawn {SPAWNLIST} ";
	public static String ERROR_BADSPAWNNAME = "Your town doesn't have any spawn with that name";
	public static String SUCCESS_DELTOWN = "Successfully removed town spawn";
	public static String SUCCESS_DEPOSIT = "You've successfully given {AMOUNT} to your town that has now {BALANCE}";
	public static String SUCCESS_DEPOSIT_PLOT = "You've successfully given {AMOUNT} to your plot that has now {BALANCE}";
	public static String ERROR_HERE = "You are not standing on any town's region";
	public static String ERROR_ALREADYINTOWN = "That player is already in your town";
	public static String ERROR_ALREADYINVITED = "Your town already invited this citizen";
	public static String INFO_JOINTOWNANNOUNCE = "{PLAYER} joined the town";
	public static String INFO_JOINTOWN = "You joined town {TOWN}";
	public static String INFO_CLICK_TOWNINVITE = "You were invited to join town {TOWN}, {CLICKHERE} to accept invitation";
	public static String INFO_INVITSEND = "Request was sent to {RECEIVER}";
	public static String ERROR_ALREADYASKED = "You already asked that town";
	public static String ERROR_NOSTAFFONLINE = "There are no players in the town's staff connected yet";
	public static String INFO_CLICK_JOINREQUEST = "{PLAYER} wants to join your town, {CLICKHERE} to accept the request";
	public static String ERROR_NOTINTOWN = "That player is not in your town";
	public static String ERROR_NOKICKSELF = "You can't kick yourself out of your town, use /town leave to quit the town";
	public static String ERROR_KICKMAYOR = "You can't kick the mayor out of your town";
	public static String ERROR_KICKCOMAYOR = "You can't kick a fellow minister out of your town";
	public static String SUCCESS_KICK = "{PLAYER} was kicked out of your town";
	public static String ERROR_NEEDRESIGN = "You must first resign as mayor before you leave the town, use /town resign";
	public static String SUCCESS_LEAVETOWN = "You left your town";
	public static String INFO_LEAVETOWN = "{PLAYER} left the town";
	public static String ERROR_PERM_HANDLECOMAYOR = "You can't add/remove yourself from the comayors of your town";
	public static String ERROR_ALREADYCOMAYOR = "{PLAYER} is already minister of your town";
	public static String SUCCESS_ADDCOMAYOR = "{PLAYER} was successfully added to the comayors of your town";
	public static String INFO_ADDCOMAYOR = "{PLAYER} added you to the comayors of your town";
	public static String ERROR_NOCOMAYOR = "{PLAYER} is already not minister of your town";
	public static String SUCCESS_DELCOMAYOR = "{PLAYER} was successfully removed from the comayors of your town";
	public static String INFO_DELCOMAYOR = "{PLAYER} removed you from the comayors of your town";
	public static String INFO_SUCCESSOR = "{SUCCESSOR} now replaces {PLAYER} as town's mayor";
	public static String INFO_RENAME = "Town {OLDNAME} changed its name to {NEWNAME}";
	public static String INFO_TAG = "Town {NAME} changed its tag from {OLDTAG} to {NEWTAG}";
	public static String INFO_DISPLAY = "Town {NAME} changed its display name from {OLDNAME} to {NEWNAME}";
	public static String ERROR_BADSPAWNLOCATION = "Town spawn must be set inside your territory";
	public static String ERROR_ALPHASPAWN = "Spawn name must be alphanumeric and must contain between {MIN} and {MAX} characters";
	public static String SUCCESS_CHANGESPAWN = "Successfully changed the town spawn";
	public static String INFO_TELEPORTLIST = "You can teleport to {SPAWNLIST} ";
	public static String ERROR_SPAWNNAME = "Invalid spawn name, choose between {SPAWNLIST} ";
	public static String INFO_TELEPORTED = "Teleported you to the town spawn";
	public static String ERROR_NEEDSTANDPLOT = "You must be standing on a plot to perform that command";
	public static String ERROR_PLOTNFS = "This plot is not up for sale";
	public static String ERROR_PLOTNFR = "This plot is not up for rent";
	public static String INFO_PLOTBUY = "{PLAYER} bought your plot {PLOT} for {AMOUNT}";
	public static String INFO_PLOTRENT = "{PLAYER} started renting plot {PLOT}";
	public static String ERROR_NOSTANDPLOTTOWN = "You're not standing on any plot of your town";
	public static String ERROR_PERM_NOTOWNER = "You must be owner of that plot to perform that command";
	public static String ERROR_PERM_MANAGECOOWNER = "You can't add/remove yourself from the coowners of your plot";
	public static String ERROR_ALREADYCOOWNER = "{PLAYER} is already coowner of your plot";
	public static String SUCCESS_ADDCOOWNER = "{PLAYER} was successfully added to the coowners of your plot";
	public static String INFO_ADDCOOWNER = "{PLAYER} added you to the coowners of plot {PLOT}";
	public static String INFO_ALREADYNOCOOWNER = "{PLAYER} is already not coowner of your plot";
	public static String SUCCESS_DELCOOWNER = "{PLAYER} was successfully removed from the coowners of your plot";
	public static String INFO_DELCOOWNER = "{PLAYER} removed you from the coowners of plot {PLOT}";
	public static String ERROR_PLOTNAME = "There already is a plot with that name in your town";
	public static String ERROR_PLOTINTERSECT = "There is a plot that intersects with your selection";
	public static String SUCCESS_PLOTCREATE = "You have successfully created a plot named {PLOT}";
	public static String SUCCESS_SETOWNER = "You are now the owner of plot {PLOT} inside of your town";
	public static String ERROR_NOOWNER = "You must own this plot to perform that command";
	public static String ERROR_ISRENTING = "You are renting this plot, use /plot return instead";
	public static String ERROR_ISBOUGHT = "You have bought this plot, use /plot delowner instead";
	public static String INFO_NOOWNER = "Plot {PLOT} has now no owner";
	public static String INFO_RETURNRENT = "{PLAYER} returned plot {PLOT} and this plot now has no owner";
	public static String ERROR_NEEDSTANDPLOTSELF = "You must be standing on your plot to perform that command";
	public static String ERROR_NEEDPLOT = "You must specify plot name or stand on it";
	public static String HEADER_PLOTLIST = "{TOWN}'s plots are {PLOTLIST}";
	public static String ERROR_ALREADYOWNER = "Player is already owner of the plot";
	public static String ERROR_OWNERNEEDTOWN = "New owner must be part of your town";
	public static String SUCCESS_CHANGEOWNER = "{PLAYER} is now the new owner of plot {PLOT}";
	public static String INFO_CHANGEOWNER = "{PLAYER} set you as the owner of plot {PLOT}";
	public static String ERROR_SELECTIONCONTAINPLOT = "Your selection contains a plot of your town";
	public static String ERROR_PLOTNOTINTOWN = "Selected plot is not inside your town's region";
        public static String ERROR_PERM_BUILD = "You don't have permission to build here";
        public static String ERROR_PERM_INTERACT = "You don't have permission to interact here";
        public static String ERROR_PERM_DESTROY = "You don't have permission to destroy here";
        public static String ERROR_PERM_SWITCH = "You don't have permission to use switches here";
        public static String ERROR_PERM_ITEMUSE = "You don't have permission to use items here";
	public static String ERROR_PLAYERNOTINTOWN = "Player is not part of a town";
	public static String ERROR_PLAYERISPRES = "Player is mayor of their town, use /townyadmin setpres";
	public static String SUCCESS_GENERAL = "Success!";
	public static String SUCCESS_DELPLOT = "You've successfully deleted plot {PLOT} in your town";
	public static String ERROR_TAXEDIT = "Taxes editing is disabled";
	public static String ERROR_TAXMAX = "Taxes can't be higher than {AMOUNT}";
	public static String SUCCESS_CHANGETAX = "You successfully changed your town's taxes";
	public static String INFO_KICKUPKEEP = "You've been kicked out of your town because you didn't have enough money to pay for the taxes";
	public static String ERROR_MAXSPAWNREACH = "Your town can't have more than {MAX} spawns";
	public static String SUCCESS_PLOTRENAME = "You renamed the plot to {PLOT}";
	public static String ERROR_TOWNNOTPUBLIC = "This town is not public";
	public static String INFO_TELEPORTCOOLDOWN = "Teleport will start in 10 seconds";
	public static String ERROR_NOHOME = "No spawn named 'home' found. Make one with /town setspawn home";
	public static String DEFAULT_PLOTNAME = "Unnamed";
	public static String TOWN_ID = "Town ID";

	public static String TOAST_WILDNAME = "Wilderness";
	public static String TOAST_PVP = "PvP";
	public static String TOAST_NOPVP = "No PvP";
        public static String FORMAT_TOWN = "Town";
        public static String FORMAT_NATION = "Nation";
	public static String FORMAT_PLOT = "Plot";
	public static String FORMAT_PLOT_ID = "Plot ID";
	public static String FORMAT_SIZE = "Size";
	public static String FORMAT_MONEY = "Money";
	public static String FORMAT_PRICE = "Price";
	public static String FORMAT_RENT_PRICE = "Rent";
        public static String FORMAT_SPAWN = "Spawn";
        public static String FORMAT_SPAWN_COST = "Spawn Cost";
        public static String FORMAT_MAYOR = "Mayor";
        public static String FORMAT_COMAYORS = "CoMayors";
        public static String FORMAT_CITIZENS = "Citizens";
        public static String FORMAT_KING = "Leader";
        public static String FORMAT_ASSISTANTS = "Assistants";
        public static String FORMAT_ALLIES = "Allies";
        public static String FORMAT_ENEMIES = "Enemies";
        public static String FORMAT_GOVERNMENT = "Government";
        public static String FORMAT_OPEN = "Open";
        public static String FORMAT_PUBLIC = "Public";
        public static String FORMAT_NEUTRAL = "Neutral";
        public static String FORMAT_PERMISSIONS = "Permissions";
        public static String FORMAT_OUTSIDERS = "Outsiders";
        public static String FORMAT_RESIDENTS = "Residents";
        public static String FORMAT_FRIENDS = "Friends";
        public static String FORMAT_FLAGS = "Flags";
	public static String FORMAT_OWNER = "Owner";
	public static String FORMAT_COOWNER = "CoOwners";
	public static String FORMAT_NONE = "None";
	public static String FORMAT_UNKNOWN = "Unknown";
	public static String FORMAT_NFS = "Not for sale";
	public static String FORMAT_NFR = "Not renting";
	public static String FORMAT_CITIZEN = "Citizen";
	public static String FORMAT_COMAYOR = "CoMayor";
	public static String FORMAT_HERMIT = "Hermit";
	public static String FLAG_ENABLED = "ENABLED";
	public static String FLAG_DISABLED = "DISABLED";
	public static String FORMAT_TAXES = "Taxes";
	public static String FORMAT_UPKEEP = "Upkeep";
	public static String CLICK = "click";
	public static String FORMAT_ADMIN = "Admin";
	public static String FORMAT_PLOTS = "Plots";
	public static String FORMAT_RENT_INTERVAL = "Rent interval";
	public static String FORMAT_HOURS = "hours";
	public static String FORMAT_BALANCE = "Balance";

	public static String CLICKME = "click here";
        public static String HEADER_TOWNLIST = "Town List";
        public static String HEADER_NATIONLIST = "Nation List";
	public static String HEADER_WORLDLIST = "World List";
        public static String TYPE_BUILD = "BUILD";
        public static String TYPE_DESTROY = "DESTROY";
        public static String TYPE_SWITCH = "SWITCH";
        public static String TYPE_ITEMUSE = "ITEM USE";
        public static String TYPE_INTERACT = "INTERACT";
	public static String VALUE_TRUE = "true";

	public static String ERROR_NEGATIVEINTERVAL = "You can't input negative values for rent intervals!";

	public static String HEADER_TOWNCOST = "Town prices";
	public static String COST_MSG_TOWNCREATE = "Town creation";
	public static String COST_MSG_OUTPOSTCREATE = "Outpost creation";
	public static String COST_MSG_UPKEEP = "Upkeep per citizen";
        public static String COST_MSG_CLAIMPRICE = "Price per chunk claimed";
        public static String COST_MSG_EXTRAPRICE = "Price per extra chunk";
	public static String INFO_PLOTFS = "{PLAYER} made plot {PLOT} not for sale";

	private static File languageFile;
	private static ConfigurationLoader<CommentedConfigurationNode> languageManager;
	private static CommentedConfigurationNode language;

	public static void init(File rootDir)
	{
		languageFile = new File(rootDir, "TownsLanguage.conf");
		languageManager = HoconConfigurationLoader.builder().setPath(languageFile.toPath()).build();

		try
		{
			if (!languageFile.exists())
			{
				languageFile.getParentFile().mkdirs();
				languageFile.createNewFile();
				language = languageManager.load();
				languageManager.save(language);
			}
			language = languageManager.load();
		}
		catch (IOException e)
		{
			TownyPlugin.getLogger().error("Could not load or create language file !");
			e.printStackTrace();
		}

	}

	public static void load()
	{
		Field fields[] = LanguageHandler.class.getFields();
		for (int i = 0; i < fields.length; ++i) {
			if (fields[i].getType() != String.class)
				continue ;
			if (language.getNode(fields[i].getName()).getString() != null) {
				try {
					fields[i].set(String.class, language.getNode(fields[i].getName()).getString());
				} catch (IllegalArgumentException|IllegalAccessException e) {
					TownyPlugin.getLogger().error("Error whey loading language string " + fields[i].getName());
					e.printStackTrace();
				}
			} else {
				try {
					language.getNode(fields[i].getName()).setValue(fields[i].get(String.class));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					TownyPlugin.getLogger().error("Error whey saving language string " + fields[i].getName());
					e.printStackTrace();
				}
			}
		}

		save();
	}

	public static void save()
	{
		try
		{
			languageManager.save(language);
		}
		catch (IOException e)
		{
			TownyPlugin.getLogger().error("Could not save config file !");
		}
	}
}
