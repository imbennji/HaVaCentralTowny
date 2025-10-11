# Towny Sponge Remake Wiki

## Overview
This repository is a Sponge API 7 port of the classic Towny plugin that recreates town, resident, and plot gameplay without nation mechanics. The plugin bootstraps through `TownyPlugin`, wiring configuration, language packs, persistence, command trees, listeners, scheduled upkeep tasks, and the public `TownyService` so that Sponge servers get feature parity with the Spigot experience covered so far.【F:src/main/java/com/arckenver/towny/TownyPlugin.java†L29-L132】【F:src/main/java/com/arckenver/towny/service/TownyService.java†L1-L39】

## Repository layout
| Path | Purpose |
| --- | --- |
| `src/main/java/com/arckenver/towny` | Core bootstrap classes, configuration, localization, persistence helpers, chat channels, utilities, and the public service API. |
| `src/main/java/com/arckenver/towny/object` | Domain models for towns, plots, residents, geometric helpers, and request tracking. |
| `src/main/java/com/arckenver/towny/channel` | Message channels for town chat and admin spy traffic. |
| `src/main/java/com/arckenver/towny/cmdexecutor` | Command registration and per-subcommand executors for `/town`, `/resident`, `/plot`, `/townyadmin`, and `/townyworld`. |
| `src/main/java/com/arckenver/towny/cmdelement` | Custom command argument parsers for Sponge’s command framework. |
| `src/main/java/com/arckenver/towny/event` | Custom events published by the plugin. |
| `src/main/java/com/arckenver/towny/listener` | Gameplay enforcement listeners (movement, permissions, combat, chat, etc.). |
| `src/main/java/com/arckenver/towny/serializer` | Gson serializers for persisting `Towny` regions and plots. |
| `src/main/java/com/arckenver/towny/task` | Scheduled tax and rent collection jobs. |
| `docs` | High-level documentation, including this wiki and a resident parity checklist. |

## Core runtime
### Plugin bootstrap and lifecycle
`TownyPlugin` initializes configuration, language, and data handlers during Sponge’s initialization phase; registers the `TownyService`; installs the command tree; hijacks legacy aliases; and registers listeners plus scheduled tasks when the server starts. Shutdown persists data back to disk. It also keeps the economy service reference current when providers change.【F:src/main/java/com/arckenver/towny/TownyPlugin.java†L29-L132】

`AliasHijacker` mirrors Towny’s short aliases (`/t`, `/ta`, `/tw`, `/p`, `/tc`) by intercepting command dispatch and tab completion, forwarding to whichever root command is available (e.g., `/t` → `/town`).【F:src/main/java/com/arckenver/towny/AliasHijacker.java†L1-L83】

### Configuration and localization
`ConfigHandler` loads and validates `TownsConfig.conf`, enforcing sane defaults for economy prices, gameplay toggles, rank titles, whitelist lists, and permission/flag matrices for towns, plots, and worlds. It exposes `getNode(...)` helpers so other systems can query settings at runtime.【F:src/main/java/com/arckenver/towny/ConfigHandler.java†L19-L138】【F:src/main/java/com/arckenver/towny/ConfigHandler.java†L139-L208】

`LanguageHandler` defines all player-facing strings (help lines, status messages, errors) and can reload overrides from disk, mirroring the original Towny language pack for parity.【F:src/main/java/com/arckenver/towny/LanguageHandler.java†L11-L200】

### Persistence and data access
`DataHandler` manages serialized town JSON files under `towns/`, the global `residents.json`, in-memory caches, spatial indexes, and helper lookups. It exposes convenience methods for flag/permission evaluation, claim validation, admin spy channels, invite queues, point selections for the golden axe tool, and resident/town lookups. Save operations write back to disk, and helper calls access Sponge services when needed (e.g., profile lookup, scheduler).【F:src/main/java/com/arckenver/towny/DataHandler.java†L37-L139】【F:src/main/java/com/arckenver/towny/DataHandler.java†L200-L312】

`TownySerializer` and `TownyDeserializer` plug into Gson so complex region/plot structures persist cleanly between restarts.【F:src/main/java/com/arckenver/towny/serializer/TownySerializer.java†L1-L158】【F:src/main/java/com/arckenver/towny/serializer/TownyDeserializer.java†L1-L173】

### Domain model snapshot
* `Towny` – Represents a town’s identity, board, tag, spawns, permissions, flags, plots, extras, tax settings, rent interval, and communication channel, with helpers for mayor/ministers, rank lookups, spawn validation, region math, bank capacity, and citizen collections.【F:src/main/java/com/arckenver/towny/object/Towny.java†L1-L116】【F:src/main/java/com/arckenver/towny/object/Towny.java†L117-L236】
* `Plot` – Tracks per-plot ownership, co-owners, permissions, flags, sale/rent metadata, balances, history, and teleport spawn, mirroring Towny’s plot feature set.【F:src/main/java/com/arckenver/towny/object/Plot.java†L1-L200】
* `Resident` – Sponge-specific resident profile containing identity metadata, rank history, economy ledger, jail/rent/spawn/mode toggles, admin bypass flags, invite tracking, and helper methods to normalize defaults, manage ranks, and update online timestamps.【F:src/main/java/com/arckenver/towny/object/Resident.java†L1-L120】【F:src/main/java/com/arckenver/towny/object/Resident.java†L121-L240】
* `Region`, `Rect`, `Point`, and `Towny` geometry helpers handle rectangular selections, adjacency checks, volume computations, and intersection tests for claim validation.【F:src/main/java/com/arckenver/towny/object/Region.java†L1-L200】【F:src/main/java/com/arckenver/towny/object/Rect.java†L1-L143】
* `Request` models invite/join requests with expiry, letting `DataHandler` manage outstanding prompts.【F:src/main/java/com/arckenver/towny/object/Request.java†L1-L86】

### Economy integration and scheduled jobs
`TownyService` is registered with Sponge’s `ServiceManager`, allowing other plugins to query town membership, towns at locations, and staff status (mayor/ministers).【F:src/main/java/com/arckenver/towny/service/TownyService.java†L1-L39】

`TaxesCollectRunnable` runs daily, collecting resident head taxes, per-plot rent, and town upkeep; it handles exemptions, removes citizens for bankruptcy, forfeits unpaid plots, and notifies staff, using Sponge’s economy API for transfers.【F:src/main/java/com/arckenver/towny/task/TaxesCollectRunnable.java†L30-L205】

`RentCollectRunnable` executes hourly, charging renters via plot or player accounts, evicting renters on failure, and broadcasting rent reminders when due.【F:src/main/java/com/arckenver/towny/task/RentCollectRunnable.java†L1-L173】

### Chat channels
`TownyMessageChannel` extends Sponge’s message channel so towns can broadcast private chat to citizens, and `AdminSpyMessageChannel` mirrors Towny’s spy channel so admins can listen in.【F:src/main/java/com/arckenver/towny/channel/TownyMessageChannel.java†L1-L120】【F:src/main/java/com/arckenver/towny/channel/AdminSpyMessageChannel.java†L1-L88】

## Command architecture
### Registration strategy
`TownyCmds.create` wires Sponge `CommandSpec` roots for `/town`, `/townyadmin`, `/plot`, `/townyworld`, and `/resident`, then reflectively loads every executor class inside each subpackage by invoking their static `create(CommandSpec.Builder)` helpers. This mirrors Towny’s modular command layout while keeping Sponge registration centralized.【F:src/main/java/com/arckenver/towny/cmdexecutor/TownyCmds.java†L20-L71】

Custom argument elements in `cmdelement` implement tab completion and validation for player names, citizens, towns, account owners, and worlds to keep executors concise.【F:src/main/java/com/arckenver/towny/cmdelement/PlayerNameElement.java†L1-L120】【F:src/main/java/com/arckenver/towny/cmdelement/TownyNameElement.java†L1-L120】

### `/resident` suite
The resident package supplies help plus targeted executors:
* `ResidentExecutor` renders the `/resident` help banner with Towny-specific descriptions.【F:src/main/java/com/arckenver/towny/cmdexecutor/resident/ResidentExecutor.java†L1-L53】
* `ResidentInfoExecutor`, `ResidentListExecutor`, `ResidentPlotListExecutor`, `ResidentFriendExecutor`, `ResidentModeExecutor`, `ResidentSetExecutor`, `ResidentSetAboutExecutor`, `ResidentTaxExecutor`, `ResidentSpawnExecutor`, and `ResidentToggleExecutor` implement information queries, friend management, mode toggles, profile updates, tax status, teleporting to own town spawns, and toggle flags, mirroring Spigot Towny commands.【F:src/main/java/com/arckenver/towny/cmdexecutor/resident/ResidentInfoExecutor.java†L1-L160】【F:src/main/java/com/arckenver/towny/cmdexecutor/resident/ResidentFriendExecutor.java†L1-L140】

### `/town` suite
The town command package mirrors Towny’s extensive feature set:
* Governance & metadata: `TownBoardExecutor`, `TownySetDisplayNameExecutor`, `TownySetnameExecutor`, `TownySettagExecutor`, `TownyMinisterExecutor`, `TownyCitizenExecutor` manage boards, display names, tags, staff appointments, and resident info.【F:src/main/java/com/arckenver/towny/cmdexecutor/towny/TownBoardExecutor.java†L20-L120】【F:src/main/java/com/arckenver/towny/cmdexecutor/towny/TownyMinisterExecutor.java†L1-L158】
* Territory: `TownyClaimExecutor`, `TownyClaimOutpostExecutor`, `TownyUnclaimExecutor`, `TownMapExecutor`, `TownyMarkExecutor`, `TownyHereExecutor`, `TownyHomeExecutor`, `TownyVisitExecutor` handle selection-based claiming, outposts, unclaim refunds, ASCII maps, particle markers, here/home info, and visiting public towns.【F:src/main/java/com/arckenver/towny/cmdexecutor/towny/TownyClaimExecutor.java†L20-L120】【F:src/main/java/com/arckenver/towny/cmdexecutor/towny/TownMapExecutor.java†L1-L180】
* Economy: `TownyDepositExecutor`, `TownyWithdrawExecutor`, `TownyTaxesExecutor`, `TownyBuyextraExecutor`, `TownyCostExecutor`, `TownySetRentIntervalExecutor` cover bank transfers, tax adjustments, extra block purchases, cost displays, and rent interval configuration.【F:src/main/java/com/arckenver/towny/cmdexecutor/towny/TownyDepositExecutor.java†L1-L160】【F:src/main/java/com/arckenver/towny/cmdexecutor/towny/TownySetRentIntervalExecutor.java†L1-L120】
* Membership: `TownyInviteExecutor`, `TownyJoinExecutor`, `TownyKickExecutor`, `TownyLeaveExecutor`, `TownyResignExecutor`, `TownyCreateExecutor` manage invitations, join requests, removals, voluntary leaves, mayor resignations, and town creation prompts.【F:src/main/java/com/arckenver/towny/cmdexecutor/towny/TownyInviteExecutor.java†L1-L200】【F:src/main/java/com/arckenver/towny/cmdexecutor/towny/TownyCreateExecutor.java†L1-L220】
* Flags & permissions: `TownyPermExecutor`, `TownyFlagExecutor`, `TownyChatExecutor`, `TownySpawnExecutor`, `TownyDelspawnExecutor`, `TownySetspawnExecutor` cover permission toggles, boolean flags, town chat channel, spawn teleportation, spawn removal, and spawn placement with validation.【F:src/main/java/com/arckenver/towny/cmdexecutor/towny/TownyPermExecutor.java†L1-L220】【F:src/main/java/com/arckenver/towny/cmdexecutor/towny/TownySetspawnExecutor.java†L1-L160】
* Information: `TownyInfoExecutor`, `TownyListExecutor`, `TownyHelpExecutor`, `TownyCostExecutor` display detailed summaries, lists, and help text.【F:src/main/java/com/arckenver/towny/cmdexecutor/towny/TownyInfoExecutor.java†L1-L220】【F:src/main/java/com/arckenver/towny/cmdexecutor/towny/TownyHelpExecutor.java†L1-L120】

### `/plot` suite
`PlotExecutor` renders help, while subcommands such as `PlotInfoExecutor`, `PlotListExecutor`, `PlotCreateExecutor`, `PlotDeleteExecutor`, `PlotRenameExecutor`, `PlotSetownerExecutor`, `PlotDelownerExecutor`, `PlotCoownerExecutor`, `PlotPermExecutor`, `PlotFlagExecutor`, `PlotSellExecutor`, `PlotUnsellExecutor`, `PlotBuyExecutor`, `PlotRentExecutor`, `PlotPutRentExecutor`, `PlotDepositExecutor`, `PlotWithdrawExecutor`, `PlotReturnExecutor`, and `PlotSetDisplayNameExecutor` implement per-plot management, sale/rent workflows, ownership transfer, and balance adjustments.【F:src/main/java/com/arckenver/towny/cmdexecutor/plot/PlotExecutor.java†L15-L45】【F:src/main/java/com/arckenver/towny/cmdexecutor/plot/PlotRentExecutor.java†L1-L200】

### `/townyadmin` suite
The admin package adds server-operator tools for forcing joins/leaves, creating or deleting towns, granting extra blocks/spawns, editing flags and perms, teleport spy, reloads, economy adjustments, rent collection, and upkeep forcing. Executors include `TownyadminExecutor` (help), `TownyadminReloadExecutor`, `TownyadminCreateExecutor`, `TownyadminClaimExecutor`, `TownyadminUnclaimExecutor`, `TownyadminDeleteExecutor`, `TownyadminSetpresExecutor`, `TownyadminForcejoinExecutor`, `TownyadminForceleaveExecutor`, `TownyadminEcoExecutor`, `TownyadminPermExecutor`, `TownyadminFlagExecutor`, `TownyadminSpyExecutor`, `TownyadminForceupkeepExecutor`, `TownyadminExtraExecutor`, `TownyadminExtraplayerExecutor`, `TownyadminExtraspawnExecutor`, `TownyadminExtraspawnplayerExecutor`, `TownyadminSetRentIntervalExecutor`, `TownyadminSetnameExecutor`, `TownyadminSetDisplayNameExecutor`, `TownyadminSetspawnExecutor`, `TownyadminDelspawnExecutor`, and `TownyadminCollectRentExecutor`.【F:src/main/java/com/arckenver/towny/cmdexecutor/townyadmin/TownyadminExecutor.java†L15-L63】【F:src/main/java/com/arckenver/towny/cmdexecutor/townyadmin/TownyadminForceupkeepExecutor.java†L1-L160】

### `/townyworld` suite
The world-level commands mirror Towny’s world toggles: `TownyworldExecutor` (help/info), `TownyworldInfoExecutor`, `TownyworldListExecutor`, `TownyworldEnableExecutor`, `TownyworldDisableExecutor`, `TownyworldPermExecutor`, and `TownyworldFlagExecutor` manage per-world enablement, permissions, and flags for the plugin.【F:src/main/java/com/arckenver/towny/cmdexecutor/townyworld/TownyworldExecutor.java†L15-L70】【F:src/main/java/com/arckenver/towny/cmdexecutor/townyworld/TownyworldFlagExecutor.java†L1-L132】

## Gameplay listeners and events
`PlayerConnectionListener`, `PlayerMoveListener`, `BuildPermListener`, `InteractPermListener`, `PvpListener`, `FireListener`, `ExplosionListener`, `MobSpawningListener`, `GoldenAxeListener`, and `ChatListener` enforce Towny’s rules: join/quit announcements, wilderness/town toast titles, build/break/place permission checks, interact throttling, PvP/fire/explosion/mob flag enforcement, golden axe selection for claims, and town chat or spy routing.【F:src/main/java/com/arckenver/towny/listener/PlayerConnectionListener.java†L1-L200】【F:src/main/java/com/arckenver/towny/listener/BuildPermListener.java†L1-L120】【F:src/main/java/com/arckenver/towny/listener/GoldenAxeListener.java†L1-L200】

`PlayerTeleportEvent` extends Sponge’s event API to fire custom teleport events for residents when spawn travel occurs.【F:src/main/java/com/arckenver/towny/event/PlayerTeleportEvent.java†L1-L83】

## Utilities and helpers
`Utils` centralizes formatting (e.g., price formatting, toast components), whitelist parsing, resident/town display templates, geometric math, fake player detection, and command cooldown helpers, reducing duplication across executors and listeners.【F:src/main/java/com/arckenver/towny/Utils.java†L1-L220】【F:src/main/java/com/arckenver/towny/Utils.java†L221-L440】

`cmdelement` classes (`PlayerNameElement`, `CitizenNameElement`, `TownyNameElement`, `WorldNameElement`, `AccountOwnerElement`) provide Sponge command argument parsing for online/offline players, residents, towns, worlds, and economy accounts, surfacing detailed error messages when lookup fails.【F:src/main/java/com/arckenver/towny/cmdelement/PlayerNameElement.java†L1-L120】【F:src/main/java/com/arckenver/towny/cmdelement/AccountOwnerElement.java†L1-L120】

## Data files and persistence layout
Towns persist under `towns/<uuid>.json` with Gson-serialized `Towny` objects, while residents share a single `residents.json` file for profiles. `DataHandler` loads these at startup, caches them, and writes back on save or key changes, keeping runtime state consistent across command, listener, and task invocations.【F:src/main/java/com/arckenver/towny/DataHandler.java†L37-L139】【F:src/main/java/com/arckenver/towny/DataHandler.java†L139-L200】

## Extensibility points
External Sponge mods can rely on the registered `TownyService` to resolve a player’s town, check mayor/minister status, or discover the town covering a location. Chat and spy channels expose Sponge `MessageChannel` implementations for integrations that want to broadcast or monitor Towny traffic.【F:src/main/java/com/arckenver/towny/service/TownyService.java†L1-L39】【F:src/main/java/com/arckenver/towny/channel/TownyMessageChannel.java†L1-L120】

