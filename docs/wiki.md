# Towny Sponge Remake Wiki

> **Towny Sponge Remake** delivers the classic Towny settlement experience on Sponge API 7 servers. This wiki explains how to install, configure, administer, and enjoy every gameplay system without diving into the codebase.

---

## Contents

- [Overview](#overview)
- [Quick Start Checklist](#quick-start-checklist)
- [Gameplay Systems](#gameplay-systems)
  - [Residents](#residents)
  - [Towns](#towns)
  - [Plots](#plots)
  - [Nations (Planned)](#nations-planned)
- [Economy & Finance](#economy--finance)
  - [Claim Costs & Upkeep](#claim-costs--upkeep)
  - [Taxes](#taxes)
  - [Rentals & Leasing](#rentals--leasing)
  - [Bank Operations](#bank-operations)
- [Command Reference](#command-reference)
  - [Resident Commands](#resident-commands)
  - [Town Commands](#town-commands)
  - [Plot Commands](#plot-commands)
  - [Administrative Commands](#administrative-commands)
  - [World Commands](#world-commands)
  - [Map & Visualisation](#map--visualisation)
- [Permissions & Ranks](#permissions--ranks)
- [Communication Tools](#communication-tools)
- [Automation & Scheduling](#automation--scheduling)
- [Configuration Guide](#configuration-guide)
- [Data Management](#data-management)
- [Troubleshooting](#troubleshooting)
- [Frequently Asked Questions](#frequently-asked-questions)
- [Support Channels](#support-channels)
- [Roadmap & Release Notes](#roadmap--release-notes)
- [Glossary](#glossary)

---

## Overview

| Field | Details |
| --- | --- |
| **Platform** | Sponge API 7 (SpongeForge or SpongeVanilla) |
| **Game Version** | Minecraft 1.12.2 (matching Sponge API 7 baseline) |
| **Primary Focus** | Town creation, land protection, community economies |
| **Recommended Economy Provider** | EconomyLite, Total Economy, or any Sponge-compatible service |
| **Data Format** | JSON data files under `data/towny/` |
| **Configuration Location** | `config/towny/` |
| **Command Prefix** | `/towny` root with multiple subtrees |
| **Permission Manager Support** | Works with LuckPerms, PermissionsEx, and similar managers |

Towny Sponge Remake mirrors the beloved Towny gameplay loop: residents create towns, purchase plots, levy taxes, and cooperate in vibrant communities. It embraces Sponge conventions such as services, modular command structures, and highly configurable gameplay flags.

---

## Quick Start Checklist

1. **Install Dependencies**
   - Place the Towny Sponge Remake JAR in `mods/`.
   - Install a Sponge economy plugin so taxes, rent, and bank transfers function.
2. **Initial Launch**
   - Start the server once to generate configuration, language, and data folders.
   - Stop the server after generation to review the new files.
3. **Configure Essentials**
   - Adjust prices, taxes, and claim limits in `config/towny/TownsConfig.conf`.
   - Set preferred language packs inside `config/towny/lang/`.
   - Decide on default gameplay flags (PvP, explosions, fire spread) before reopening.
4. **Assign Permissions**
   - Use your permission manager to grant residents, assistants, and staff the appropriate nodes.
   - Confirm players inherit the ability to join towns and interact with claims.
5. **Educate Staff & Residents**
   - Share this wiki, especially the command reference and troubleshooting sections.
   - Encourage new mayors to rehearse claim workflows on a test world if available.
6. **Schedule Backups**
   - Automate copies of `data/towny/` and relevant configs before opening to the public.

---

## Gameplay Systems

### Residents

Residents are individual player records containing:

* Display preferences, including titles, surnames, and personal bio text.
* Town membership, rank history, allies, and jailed status.
* Financial state for rent, taxes, and bank contributions.
* Toggleable gameplay modes such as map overlays, spy participation, or admin bypass (if granted).

Residents can:

* Join or leave towns at will (subject to invitations and taxes).
* Purchase or rent plots for personal builds.
* Set personal spawns within town borders.
* Manage friend lists to share build permissions on private plots.

### Towns

Towns are resident-run municipalities featuring:

* **Governance**: A mayor-led structure with assistant ranks, custom roles, and optional town boards.
* **Land Management**: Claim town blocks, expand borders, designate outposts, and define home blocks for spawns.
* **Economy**: Maintain a shared bank account paying upkeep, taxes, and municipal expenses.
* **Security**: Set permissions and flags for residents, allies, and outsiders covering build/break, switches, explosions, fire, and mob spawning.
* **Communication**: Coordinate via private chat channels and announcements visible to members.

### Plots

Plots subdivide town land for fine-grained ownership:

* Owned or rented by residents, each with independent permissions.
* Supports co-owners, friend permissions, and per-plot flag overrides.
* Special designations such as shop, embassy, farm, or arena help organise municipal layout.
* Rent cycles can be hourly or daily, automatically renewing until cancelled.

### Nations (Planned)

Nation-level play, including alliances, inter-town taxation, and war mechanics, is planned for future releases. Follow release notes to adopt these systems as they become available.

---

## Economy & Finance

Towny Sponge Remake integrates tightly with the registered Sponge economy service.

### Claim Costs & Upkeep

* Base claim prices determine how much it costs to expand town borders.
* Upkeep charges scale with the number of claimed blocks, optional outpost multipliers, and special plot types.
* Town banks must retain sufficient funds before the daily upkeep cycle; lacking funds can freeze expansion or trigger downsizing.

### Taxes

* **Resident Taxes**: Daily charges per resident. Mayors can use flat or percentage-based models and exempt certain ranks.
* **Plot Taxes**: Optional daily fees on owned plots to encourage active development.
* **Town Tax Holidays**: Temporarily pause taxation by disabling the relevant toggle in the configuration when hosting events.

### Rentals & Leasing

* Towns can advertise plots for rent with prices and interval length.
* Tenants are warned before eviction if funds are insufficient.
* Optional deposits or upfront fees protect municipal investment in high-demand districts.

### Bank Operations

* Residents with permission can deposit personal currency into the town bank for public works.
* Withdrawals are limited to trusted ranks to prevent misuse.
* Bank statements appear in chat feedback, letting mayors audit income versus expenses.

---

## Command Reference

Towny mirrors the classic Towny command hierarchy. Use in-game help (`/towny ?`) for context; the tables below summarise the main entries.

### Resident Commands

| Command | Summary |
| --- | --- |
| `/resident` | View personal profile, including town membership, balance, and toggles. |
| `/resident friend <add/remove/list>` | Maintain friend lists that inherit plot permissions. |
| `/resident spawn` | Teleport to town or rented plot spawn if allowed. |
| `/resident mode <toggle>` | Enable map view, claim selection, spy, or similar modes. |
| `/resident set about <text>` | Update the biography message shown to others. |

### Town Commands

| Command | Summary |
| --- | --- |
| `/town new <name>` | Found a town at your current position when criteria are met. |
| `/town claim` | Claim the current chunk; modifiers support multiple claims or outposts. |
| `/town unclaim` | Release land to reduce upkeep or rearrange borders. |
| `/town deposit <amount>` | Move currency from personal balance into the town bank. |
| `/town withdraw <amount>` | Withdraw funds for mayor-approved projects. |
| `/town set <parameter>` | Configure board text, spawn, taxes, flags, or permission matrices. |
| `/town invite <player>` | Invite a resident to join your town. |
| `/town kick <player>` | Remove members who violate policies. |
| `/town rank <add/remove> <player> <rank>` | Promote or demote residents within the town. |

### Plot Commands

| Command | Summary |
| --- | --- |
| `/plot claim` | Purchase the plot you stand in, subject to town rules. |
| `/plot unclaim` | Return ownership to the town. |
| `/plot set <type>` | Label the plot (shop, embassy, arena, etc.). |
| `/plot perm <group> <toggle>` | Adjust build, destroy, switch, and item-use permissions for friends, allies, residents, or outsiders. |
| `/plot flag <flag> <on/off>` | Override PvP, mobs, explosions, and other behaviours locally. |
| `/plot rent <price> <interval>` | List plots for rent or cancel an existing lease. |

### Administrative Commands

| Command | Summary |
| --- | --- |
| `/townyadmin` | Access the administrative help tree. |
| `/townyadmin town <name> <action>` | Force-create, delete, or modify towns. |
| `/townyadmin resident <name> <action>` | Adjust resident data, including forced joins or resets. |
| `/townyadmin toggle <flag>` | Flip global flags instantly. |
| `/townyadmin economy <subcommand>` | Inject or remove currency from town and resident accounts. |
| `/townyadmin collectrent` | Run the rent scheduler manually to resolve timing concerns. |

### World Commands

| Command | Summary |
| --- | --- |
| `/townyworld` | Display world-level settings for PvP, explosions, wilderness permissions, and more. |
| `/townyworld toggle <flag>` | Switch fire spread, mob spawning, or similar global behaviours. |
| `/townyworld perm <group> <toggle>` | Control wilderness build/interact permissions. |

### Map & Visualisation

* `/towny map` renders an ASCII overview of surrounding claims and wilderness.
* `/towny here` summarises ownership, plot type, permissions, and taxation for the chunk you occupy.
* `/plot marker` toggles particle borders highlighting plot edges for easier surveying.

---

## Permissions & Ranks

Towny depends on your permission manager. Recommended practices:

* **Root Nodes**: Gate each command tree with nodes such as `towny.command.town`, `towny.command.plot`, and `towny.command.resident`.
* **Subcommand Nodes**: Grant precise control (e.g., `towny.command.town.claim`, `towny.command.resident.spawn`).
* **Administrative Nodes**: Restrict high-impact actions under `towny.command.townyadmin.*`.
* **Bypass & Spy Controls**: Provide separate nodes for plot protection bypass, admin spy chat, and debug readouts.
* **Rank Profiles**: Create ranks—Mayor, Assistant, Councillor, Citizen, Visitor—and map nodes accordingly.

Document rank expectations for your staff team so everyone understands the powers attached to each role.

---

## Communication Tools

* **Town Chat (`/tc`)**: Private channel for residents and assistants, ideal for planning builds or politics.
* **Nation Chat (future)**: Reserved for upcoming nation support; currently unused but planned.
* **Admin Spy**: Staff with spy privileges mirror town chat for moderation while respecting privacy policies.
* **Interactive Notifications**: Invitations, rent offers, and teleport requests use Sponge’s rich messages for quick acceptance or denial.

---

## Automation & Scheduling

* **Daily Upkeep Cycle**: Processes town upkeep, resident taxes, and plot taxes at a consistent server time. Towns lacking funds risk losing residents or unclaiming land.
* **Hourly Rent Cycle**: Handles rent invoices, warnings, and evictions for overdue tenants.
* **Auto-Save Tasks**: Persist resident, town, and plot data at regular intervals to guard against crashes.
* **Economy Syncing**: Monitors the registered economy provider and reconnects if the service reloads mid-session.

---

## Configuration Guide

* **General Settings (`TownyConfig.conf`)**: Toggle PvP defaults, explosion behaviour, wilderness permissions, and feature availability.
* **Towns Settings (`TownsConfig.conf`)**: Adjust claim prices, upkeep formulas, tax defaults, bonus block costs, and war preparations (if enabled).
* **Language Files (`lang/*.lang`)**: Translate or customise every message shown to players.
* **Data Limits**: Configure maximum residents per town, claim limits per player, and restrictions on outposts.
* **Notification Preferences**: Decide whether alerts appear in chat, action bar, or title overlays for events like entering towns or breaking protection rules.

After editing configuration files, restart the server or use the plugin’s reload command (if available) to apply changes.

---

## Data Management

* All persistent information lives in `data/towny/`.
* Town records detail borders, flags, residents, taxes, and bank balances.
* Resident data tracks personal settings, ranks, and financial history.
* Plot files document ownership, rental status, and local overrides.
* Implement an automated backup routine that includes world data alongside Towny’s directories to safeguard community progress.

---

## Troubleshooting

| Issue | Resolution |
| --- | --- |
| Players cannot build in wilderness | Confirm `/townyworld perm` grants outsiders build rights and verify your permission manager allows block interaction. |
| Taxes or rent are not charging | Ensure an economy plugin is active, verify account balances, and manually trigger `/townyadmin collectrent` if schedules stalled. |
| Towns lose residents unexpectedly | Review tax levels, offer grace periods, or subsidise members through the town bank. |
| Messages appear in the wrong language | Set the desired locale in configuration and reload the plugin after translating language files. |
| Migration from classic Towny | Back up both servers, convert data with the provided tools (when released), or recreate towns manually while noting feature differences. |

Document persistent issues in the project’s tracker with logs and configuration snippets for faster assistance.

---

## Frequently Asked Questions

**How large can a town grow?**  
Claim limits depend on configuration—adjust maximum town blocks and bonus purchases to fit your server’s economy.

**Can towns share land?**  
Towns cannot overlap claims, but allied towns may coordinate borders using plot designations and shared districts controlled via permissions.

**Does Towny support war events?**  
War mechanics are a roadmap feature. Until released, servers can simulate conflicts with manual rules or complementary combat plugins.

**How do I restore a deleted town?**  
Restore from your latest backup of `data/towny/`. Keeping daily snapshots prevents permanent loss.

**Can I disable rent entirely?**  
Yes. Turn off rent toggles in configuration or avoid listing plots for rent.

---

## Support Channels

* **Issue Tracker**: Report bugs and feature requests with reproduction steps, logs, and configuration details.
* **Pull Requests**: Contributions are welcome—follow repository guidelines and include testing notes where applicable.
* **Community Spaces**: Join the project’s Discord or forums (if advertised) to share best practices and showcase towns.

---

## Roadmap & Release Notes

Stay informed by reviewing release changelogs. Upcoming priorities include:

* Nation systems with alliances, capitals, and inter-town diplomacy.
* Enhanced map visualisation, including potential web-based claim viewers.
* Optional war events with configurable siege rules and victory conditions.
* Expanded API hooks for third-party plugins to track residents and towns.

---

## Glossary

| Term | Definition |
| --- | --- |
| **Resident** | A tracked player profile with personal settings and finances. |
| **Town** | Player-run municipality controlling claimed land, rules, and economy. |
| **Plot** | Subdivision of town land, either owned or rented by residents. |
| **Outpost** | Remote claim disconnected from a town’s main territory, often costing extra upkeep. |
| **Upkeep** | Recurring cost paid by towns to retain claims. |
| **Rent** | Periodic payment allowing residents to use a plot without ownership. |
| **Flag** | Toggle that enables or disables behaviours like PvP, explosions, or mob spawning. |
| **Permission Matrix** | Combined rules defining which groups may interact with blocks or entities. |
| **Economy Provider** | Sponge plugin delivering virtual currency transactions consumed by Towny. |
| **Spy Mode** | Administrator tool mirroring town chat for moderation. |

---

For the latest updates and best practices, monitor the repository’s announcements and share feedback with the community. Towny Sponge Remake thrives on collaborative storytelling—build thriving settlements and shape your server’s narrative together.
