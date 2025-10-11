# Towny Sponge Remake Wiki
## Overview

---

## 1. Introduction

**What is Towny Sponge Remake?**

Towny Sponge Remake reimagines the beloved town management gameplay of the original Towny plugin for the Sponge ecosystem. It gives server communities a deep, economy-driven settlement simulator featuring resident-controlled towns, protected plots, taxes, rent, and a full complement of chat and administration tools. The project strives for parity with the legacy Spigot edition while embracing Sponge conventions such as services, configurability, and modular command trees.

**Key capabilities**

* Resident-created towns with rank hierarchies, boards, and optional tags.
* Land claims that protect builds, regulate permissions, and offer granular ownership through sub-plots.
* Robust economy hooks for upkeep, bank accounts, player taxation, and automated rent cycles.
* A comprehensive command suite covering residents, towns, plots, administrators, and world managers.
* Integrated chat channels for private town communication and administrative spy oversight.
* Highly configurable gameplay flags, price lists, and default behaviours so servers can tailor Towny to their community.
* Cross-town nations with leadership ranks, national banks, allied/enemy relations, configurable spawns, and taxation controls (war mechanics intentionally excluded).

---

## 2. Requirements & Installation

### 2.1 Prerequisites

* **Sponge API**: Designed for Sponge API 7.x. Running the latest SpongeForge/SpongeVanilla build for this API line is recommended.
* **Java Runtime**: Java 8 is the minimum supported runtime. Ensure both server and any accompanying tooling match this version.
* **Economy Provider (Optional but recommended)**: For taxes, upkeep, rent, and bank transfers, install a Sponge-compatible economy service (e.g., EconomyLite or Total Economy).

### 2.2 Installation Steps

1. Download the latest Towny Sponge Remake JAR from the project’s releases page.
2. Place the JAR in your server’s `mods/` directory.
3. Start the server once to allow Towny to generate its configuration, language files, and data folders.
4. Stop the server and review the generated files inside `config/towny/` to customise prices, flags, and language packs.
5. Restart the server to begin using Towny with your tailored settings.

### 2.3 Updating

1. Backup the following before upgrading: `config/towny/`, `data/towny/`, and the server world.
2. Replace the existing plugin JAR with the new release.
3. Start the server. Towny automatically updates configuration nodes, keeping custom values whenever possible.
4. Review the console for any migration notices. Revisit your configuration if new options were added.

---

## 3. Initial Setup Checklist

| Step | Description |
| --- | --- |
| 1. Economy Service | Register a Sponge economy plugin so Towny can transact taxes, rent, and bank deposits. Without one, monetary features gracefully disable. |
| 2. Configure Prices | Adjust claim costs, tax defaults, bonus chunk prices, and rent amounts in `config/towny/TownsConfig.conf`. |
| 3. Language Customisation | Edit `config/towny/lang/en_US.lang` (or your locale) to personalise in-game messages and help output. |
| 4. Permission Nodes | Assign town-related permissions through your permission manager (LuckPerms, etc.) to expose commands and features to specific ranks. |
| 5. World Flags | Use `/townyworld` commands to toggle wilderness build permissions, PvP, fire spread, and other world-level behaviours. |
| 6. Data Backup Strategy | Schedule regular copies of the `data/towny/` directory to preserve towns, residents, and plot states. |

---

## 4. Core Concepts

### 4.1 Residents

Residents represent individual players tracked by the plugin. Each resident profile records:

* Display preferences (titles, surnames, personal board text).
* Town membership, rank history, friends list, and jail status.
* Financial data such as bank balance contributions, tax arrears, and rent obligations.
* Toggleable gameplay modes (e.g., PvP, admin bypass, spy participation).

Residents can join towns, purchase plots, pay rent, set personal spawn points within their town, and manage friends with whom they share permissions.

### 4.2 Towns

Towns are the heart of the plugin. Founders select a name, board, and optional tag, then manage the following:

* **Government**: Mayor leadership with configurable assistant ranks (e.g., councillors, ministers).
* **Territory**: Claims expand using town chunks purchased with in-game currency. Outposts allow remote settlements, while home chunks govern spawn and daily upkeep.
* **Economy**: Town banks hold shared funds for taxes, upkeep, and public services. Mayors can deposit or withdraw with appropriate permissions.
* **Permissions & Flags**: Town flags control PvP, fire spread, mobs, explosions, and build/break permissions for residents, allies, or outsiders.
* **Communication**: Town chat channels keep conversations private, while optional spy monitoring ensures administrators can audit traffic when required.

### 4.3 Plots

Plots are subdivisions of town land:

* May be sold or rented to residents, granting localised control inside the town.
* Support co-owners, plot-level permissions, and bespoke flags overriding town defaults.
* Can be marked for specific uses (e.g., shop, farm, embassy) depending on town policy.
* Offer rent cycles ranging from hourly to daily, automatically charging residents and repossessing if payments lapse.

### 4.4 Nations

Nations unite multiple towns under shared leadership and economic policy:

* **Leadership Structure**: Each nation appoints a king drawn from the capital town’s residents. Kings can promote assistants to help manage invitations, finances, and diplomacy. Leadership automatically reconciles if towns leave or mayors change.
* **Membership Management**: Towns may receive invitations, request to join open nations, or be kicked. Capital towns can be reassigned, and nations disband automatically when membership drops below valid thresholds.
* **Economy**: Nations maintain their own bank balance for upkeep, spawn costs, and inter-town projects. Players with proper ranks can deposit to or withdraw from the nation bank, provided an economy service is installed.
* **Taxation**: Kings configure either flat-rate or percentage taxes collected from member towns. Limits mirror the Spigot edition so server owners can rely on familiar balances.
* **Spawns**: Nations may set a dedicated spawn point with optional public access and configurable teleport fees. Members can teleport using `/nation spawn`, paying the configured cost unless exempted.
* **Diplomacy**: Nation leaders manage lists of allies and enemies to coordinate protections and conflicts. War-specific mechanics remain out of scope for this remake.

---

## 5. Economy & Taxation

### 5.1 Claiming Costs

Towns spend currency to acquire new chunks. Configure base prices, additional outpost costs, and multipliers for expanding beyond default limits. Town banks must retain enough funds to cover both purchase costs and daily upkeep.

### 5.2 Upkeep

* **Town Upkeep**: Charged daily per claimed chunk plus optional extras for outposts or embassies. If a town cannot pay, residents may be evicted, and claims can regress.
* **Plot Upkeep**: Optional per-plot upkeep ensures private owners contribute to town expenses.

### 5.3 Taxes

* **Resident Taxes**: Daily charges applied to each resident. Mayors can exempt certain ranks or set percentage-based taxes.
* **Plot Taxes**: Additional daily fees on owned plots, encouraging active land use.

### 5.4 Rent System

* Towns can list plots for rent with configurable intervals (e.g., hourly, daily).
* Rent automatically charges the tenant’s balance; failures trigger warnings, then eviction.
* Optional deposit or upfront payment protects the town against early departures.

### 5.5 Bank Operations

Commands allow residents with permission to deposit personal funds into the town bank or withdraw for municipal projects. The plugin interacts with the registered economy service to ensure consistent balances.

---

## 6. Command Reference

Towny Sponge Remake mirrors the classic Towny command layout. Below is a high-level overview; consult in-game help (`/towny ?`) for context-sensitive details.

### 6.1 Resident Commands (`/resident`)

| Command | Purpose |
| --- | --- |
| `/resident` | Displays resident overview, including town membership, balance, and status toggles. |
| `/resident friend <add/remove/list>` | Manage personal friend lists, impacting cooperative plot permissions. |
| `/resident spawn` | Teleport to your town spawn or rented plot spawn if allowed. |
| `/resident mode <set/clear>` | Toggle modes such as map viewing, claim selection, or spy participation (where permitted). |
| `/resident set about <message>` | Update your resident biography shown to others. |

### 6.2 Town Commands (`/town`)

| Command | Purpose |
| --- | --- |
| `/town new <name>` | Create a new town at your current location if you meet the founding requirements. |
| `/town claim` | Claim the chunk you stand in; additional arguments handle multiple chunks, outposts, or selection claims. |
| `/town unclaim` | Release owned land to reduce upkeep or reallocate resources. |
| `/town deposit <amount>` / `/town withdraw <amount>` | Transfer funds between your personal account and the town bank. |
| `/town set <parameter>` | Configure town board, spawn, tax rates, flags, and permission matrices. |
| `/town invite <player>` / `/town kick <player>` | Manage resident membership. |
| `/town rank <add/remove> <player> <rank>` | Promote or demote residents to custom roles with associated permissions. |

### 6.3 Plot Commands (`/plot`)

| Command | Purpose |
| --- | --- |
| `/plot claim` / `/plot unclaim` | Purchase or release the plot you stand in, respecting town policies. |
| `/plot set <type>` | Define plot category (e.g., shop, embassy) for organisational purposes. |
| `/plot perm <group> <toggle>` | Adjust build, destroy, switch, or item-use permissions for friends, allies, residents, or outsiders. |
| `/plot flag <flag> <on/off>` | Override town-wide flags (PvP, mobs, explosions) on a per-plot basis. |
| `/plot rent <price> <interval>` | List or cancel rental offerings. |

### 6.4 Nation Commands (`/nation`)

| Command | Purpose |
| --- | --- |
| `/nation create <name>` | Form a new nation with your town as the capital, charging the configured creation price. |
| `/nation invite <town>` / `/nation kick <town>` | Manage town membership through invitations or removals. |
| `/nation join <nation>` / `/nation leave` | Accept invitations or voluntarily depart from a nation. |
| `/nation set capital <town>` | Reassign the capital town when leadership changes. |
| `/nation set king <player>` / `/nation assistant <add/remove> <player>` | Promote or demote national leadership roles. |
| `/nation deposit <amount>` / `/nation withdraw <amount>` | Transfer funds between your personal account and the nation bank. |
| `/nation taxes <flat|percent> <amount>` | Configure nation-wide taxes charged to member towns. |
| `/nation spawn` / `/nation set spawn` / `/nation set spawncost <amount>` | Use or adjust the nation spawn location and teleport fee. |
| `/nation toggle <public|open|neutral>` | Change nation accessibility and neutrality preferences (war combat toggles remain disabled). |
| `/nation ally <add|remove> <nation>` / `/nation enemy <add|remove> <nation>` | Manage allied and enemy relations with other nations. |

### 6.4 Administrative Commands (`/townyadmin`)

| Command | Purpose |
| --- | --- |
| `/townyadmin` | Opens the administrative help tree. |
| `/townyadmin town <name> <action>` | Force-create, delete, or modify towns directly. |
| `/townyadmin resident <name> <action>` | Adjust resident data, including forced joins, leaves, or toggle resets. |
| `/townyadmin toggle <flag>` | Override world-level or global flags instantly. |
| `/townyadmin economy <subcommand>` | Inject or remove currency from town and resident accounts. |
| `/townyadmin collectrent` | Manually trigger rent collection to resolve scheduling issues. |

### 6.5 World Commands (`/townyworld`)

| Command | Purpose |
| --- | --- |
| `/townyworld` | Display current world settings such as PvP, explosions, and wilderness permissions. |
| `/townyworld toggle <flag>` | Enable or disable world-level flags like fire spread or mob spawning. |
| `/townyworld perm <group> <toggle>` | Control who may build or interact in the wilderness. |

### 6.6 Map & Visualization

* `/towny map` displays an ASCII minimap showing nearby town claims and wilderness.
* `/towny here` summarises the current chunk: owner, plot type, permissions, and taxation.
* `/plot marker` toggles particle outlines marking plot boundaries for easier land management.

---

## 7. Permissions & Ranks

Towny integrates with Sponge permission managers. The plugin ships a granular node layout enabling precise control:

* **Global Nodes**: Gate access to each command root (`towny.command.town`, `towny.command.plot`, etc.).
* **Subcommand Nodes**: Provide fine-grained access, e.g., `towny.command.town.claim`, `towny.command.resident.spawn`.
* **Administrative Nodes**: Reserved for staff (`towny.command.townyadmin.*`) covering forced actions and global toggles.
* **Bypass & Spy**: Nodes controlling plot protection bypass, admin spy visibility, and debug outputs.

Use your permissions plugin to create ranks such as Mayor, Assistant, Citizen, or Visitor. Assign appropriate nodes so only trusted ranks can manage finances, change flags, or edit plots.

---

## 8. Communication Channels

* **Town Chat**: `/tc` or `/town chat` switches residents into a private channel. Messages broadcast only to residents and assistants.
* **Admin Spy**: Staff with spy privileges can mirror town chat to monitor for rule infractions. Spy status can be toggled per-resident for transparency.
* **Invite Notifications**: Residents receive clickable invitations for town joins, plot rentals, and teleports through Sponge’s interactive message system.

---

## 9. Scheduling & Automation

Towny Sponge Remake automates several recurring tasks:

* **Daily Upkeep Cycle**: Collects town upkeep, resident taxes, and plot taxes. Towns lacking funds risk losing residents or reverting claims.
* **Hourly Rent Cycle**: Processes plot rent payments, issuing reminders before evicting defaulting tenants.
* **Data Saves**: Regularly persists towns, residents, and plot data to JSON files under `data/towny/` to guard against crashes.
* **Economy Sync**: Watches for changes in the registered economy provider and re-establishes links automatically if the service reloads.

---

## 10. Configuration Highlights

* **General Settings**: Toggle features like friendly fire, wilderness build permissions, grief prevention defaults, or server-wide PvP rules.
* **Prices & Taxes**: Adjust base claim cost, additional chunk multipliers, rent amounts, daily taxes, and fees for extras like teleportation.
* **Ranks & Titles**: Define custom rank titles, prefixes, and the permissions they unlock.
* **Notifications**: Configure whether residents receive titles, action bar messages, or chat alerts for events such as entering towns or violating flags.
* **Integration Hooks**: Enable or disable compatibility tweaks for map plugins, scoreboard trackers, or external mods.

---

## 11. Data Storage & Backups

* All persistent data lives in `data/towny/` inside the server directory.
* Town files (`towns/<uuid>.json`) describe land, flags, residents, and financial state.
* Resident data (`residents.json`) stores player records, including ranks and balances.
* Regular backups are vital—schedule automated zips or off-site syncs to protect community progress.

---

## 12. Troubleshooting & FAQs

**Q: Why can’t players build in the wilderness?**
A: Check `/townyworld perm` to ensure the wilderness build flag is enabled. Additionally, confirm your permission plugin grants the necessary build rights.

**Q: Taxes or rent are not collecting. What should I do?**
A: Verify an economy provider is installed and functioning. Staff can run `/townyadmin collectrent` to force the scheduler, then inspect the console for warnings about insufficient funds or misconfigured prices.

**Q: Residents are being kicked for bankruptcy. How do we prevent this?**
A: Lower resident taxes, adjust rent intervals, or encourage towns to subsidise members via the town bank. Consider enabling grace periods for new residents by temporarily exempting them from taxes.

**Q: Can I change the language to something other than English?**
A: Yes. Duplicate the language file in `config/towny/lang/`, translate entries, and set the desired locale in the main configuration. Reload the plugin or restart the server for changes to apply.

**Q: How do I migrate from the Spigot version?**
A: Use the provided data conversion scripts (when available) or recreate towns manually. Back up both servers before attempting migration. Some features may differ due to Sponge platform behaviour.

---

## 13. Support & Contribution

* **Issue Tracking**: Report bugs and feature requests on the project’s issue tracker with reproduction steps, logs, and configuration snippets.
* **Pull Requests**: Contributions are welcome—follow the coding standards outlined in the repository README and submit PRs with comprehensive testing notes.
* **Community Channels**: Join the official Discord or forums (if provided) to discuss best practices, share town showcases, and coordinate with other server owners.

---

## 14. Release Notes & Roadmap

Stay informed by reviewing each release changelog. Upcoming milestones include:

* Nation quality-of-life improvements such as map overlays, scoreboards, and additional administrative tooling.
* Enhanced map visualisation with web-based claim viewers.
* Optional war events with configurable siege rules and victory conditions.
* API extensions exposing more resident and town data for third-party integrations.

---

## 15. Glossary

| Term | Definition |
| --- | --- |
| **Resident** | A tracked player profile with personal settings, town membership, and financial status. |
| **Town** | Player-governed settlement controlling claimed land, flags, and economy. |
| **Plot** | Subdivision of town land that can be owned, rented, or managed separately. |
| **Outpost** | Remote claim disconnected from the town’s main territory, often with increased upkeep. |
| **Upkeep** | Recurring cost paid by towns to retain claimed chunks and services. |
| **Rent** | Periodic payment allowing residents to occupy a plot without permanent ownership. |
| **Flag** | Toggle controlling behaviours like PvP, fire spread, or mob spawning. |
| **Permission Matrix** | Combined rules that determine whether residents, allies, or outsiders may interact with blocks or entities. |
| **Economy Service** | Sponge plugin providing virtual currency transactions utilised by Towny. |
| **Spy Mode** | Administrator tool mirroring town chat to ensure rule compliance. |

---

By following this guide, Sponge server owners can deploy the Towny Sponge Remake with confidence, fostering vibrant towns, thriving economies, and collaborative community storytelling. For the latest updates, always consult the project repository and release announcements.
