# Architecture — 0.1.4

## Rendering

Game resource → in-memory ImageIO decode → automatic luminance/chroma masks → recoloured BufferedImage → native RC8 image registration → SettingsAPI.getSprite → ShipAPI.setSprite.

The renderer and native bridge are unchanged from 0.1.3; `evidence/unchanged-renderer.json` verifies the relevant class bytes. Each source/palette texture is reused, while individual ships receive their own SpriteAPI wrappers/transforms. Source alpha, dimensions, centre, angle and texture region are preserved. Hull bounds, armour, hardpoints, weapons and stats are untouched. Shared hull specifications and stock variants are never painted globally.

CPU baking was chosen because no verified public hook attaches a shader to all existing ship and thumbnail draws. It costs generation time once per source/palette, rather than a new custom draw pass for every ship. It preserves source detail but cannot infer semantic armour regions. Creator masks, decals and more channels remain extension possibilities, not implemented features.

Sources use SettingsAPI.openStream and explicit MemoryCacheImageInputStream, without disk caching or blocked file/reflection APIs. NativeTextureBridge binds one of the two verified public internal RC8 image-registration signatures through permitted MethodHandles. Actual restricted-loader native tests are included. The public hullmod/sprite pattern was researched using published mod examples; UFL source and assets are independent.

## Policy and migration

LiveryState persists only JDK maps, strings and booleans under the existing `ufl_settings_v1` sector key, with schema string `2`. It stores empire, active-fleet override/inheritance, per-NPC-fleet overrides keyed by fleet ID, per-ship overrides keyed by fleet-member ID, and legacy active-fleet hull/size maps.

Resolution: ship → active-fleet hull → active-fleet size → owning fleet → empire. Hull/size maps are ignored outside the active fleet. Missing fleet rules inherit. Explicit Original stops inheritance. Global disable resolves Original everywhere.

Factory rules use stable palette IDs. Custom assignments use `paint:` plus immutable Palette.encode data. The last-used Custom draft is separate. Migrating schema 1 snapshots each old shared-Custom assignment and preserves existing active-fleet policy. A newly created state inherits empire by default. No mod-defined object is stored in these preferences.

`standardizeFleet` clears only the supplied fleet's current ship overrides; for the active fleet it also clears legacy hull/size rules. Other fleets and the empire remain independent. The root restore action assigns Original to the active fleet rather than clearing the empire.

## Ownership and runtime lifecycle

LiveryService enumerates the active player fleet and inflated non-station fleets across SectorAPI.getAllLocations, filtered by exact player-faction ID. It checks isInflated before reading NPC FleetData, avoiding forced hidden-fleet inflation. Ships in station fleets, storage and production lists are excluded. Member-ID exceptions may return with a ship; new IDs resolve as new acquisitions.

LiveryScript syncs the active fleet every 350 ms and scans eligible empire fleets/cleans departed members every 2 seconds, including while paused. EmpireFleetListener uses the public FleetInflationListener callback and is registered transiently and idempotently. An early callback while still dormant is ignored; the regular scan catches it later. There is no off-thread OpenGL or mass NPC prewarming.

Variants are cloned before adding the zero-OP hidden runtime hullmod/tag, marked REFIT, and module variants recursively cloned/decorated (depth limit 12). Existing MagicLib paintjob tags cause UFL to yield. The hullmod swaps only the per-instance sprite. Before save, all tracked member attachments, plus active members, are stripped; the ability is removed. The transient script/listener introduce no campaign-owned mod class. After save or save failure the ability and runtime attachments are rebuilt. Unknown variant/ability copies inside another mod's private persistence remain outside this boundary.

## Fleet-screen drag regression

Inspection of actual RC8 fleet UI code found removeFleetMemberAtIndex during pickup, then setFleetMemberAtIndex/swapMembers during placement. Old cleanup interpreted temporary roster absence as a departure and stripped paint.

While the public campaign UI reports CoreUITabId.FLEET, members() retains previously tracked active-fleet objects absent from the roster whose FleetData is null or still points to the same fleet. It does not alter the real roster. This keeps their cosmetic variant alive during pickup and save callbacks. A confirmed transfer to another fleet is not retained; otherwise cleanup resumes on leaving the fleet screen. It can deliberately defer cleanup of a true removal until that screen closes.

The old JAR reproduces the failure in the regression fixture; the updated JAR preserves paint through pickup/drop and still strips it after a real departure. A live user gesture retest remains pending. No private UI renderer interception or shared-sprite override was added.

## UI and extension seams

Public InteractionDialogAPI provides root → scope → palette preview/apply. The native ship grid handoff is deferred one full frame after picker teardown. PaletteSwatches/preview drawing uses the existing tested callbacks. Utilities contains hull/size rules, diagnostics and the global switch. There is no refit-panel injection or new keyboard handler.

Palette, Recolor, LiveryState and LiveryService expose separable colour/policy/application functions. Another mod can use the plain rule maps and immutable snapshots for future manufacturing/NPC automation. Fleet IDs are engine-owned; a recreated fleet with a new ID inherits anew. Manual brush masks, named custom libraries, decals and patterns are not part of this release.

## Performance and failure handling

Cached native sprites render normally. Main-fleet prewarming handles one uncached source/palette per frame. First use in preview or an NPC combat spawn can still pause for generation. No real battle-FPS or large-empire scan benchmark is claimed.

Source-image LRU: 12 decoded images; encoded resource ceiling: 32 MiB. Default source-pixel ceiling: 4,194,304. Session generated-texture count and estimated GPU budget: 256 / 256 MiB, including padded dimensions/mipmaps. Live native textures are not evicted while engine sprites may refer to them. Budget exhaustion leaves new requests original; restart or adjust configuration.

Per-ship incompatibilities and texture errors are logged/deduplicated; retry is available in Utilities. Failure preserves the original sprite. No original files, generated PNGs or runtime caches are written.
