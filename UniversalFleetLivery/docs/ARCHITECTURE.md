# Architecture

## Chosen rendering path

Source resource → in-memory ImageIO decode → automatic continuous masks → recoloured BufferedImage → native RC8 image registration → `SettingsAPI.getSprite()` → `ShipAPI.setSprite(SpriteAPI)`.

Resource reading and per-ship sprite swapping use public APIs. The isolated NativeTextureBridge uses one verified internal RC8 image-registration method; see the version constraint below. The runtime does not intercept shaders or change shared hull assets. Each generated texture is shared by ships using the same source and palette, while each ship receives its own SpriteAPI wrapper and transform. Original size, centre, angle, alpha, tint, blend function and texture region are copied to preserve placement. Hull outlines, bounds, armor grids, hardpoints, weapons and stats are not changed.

Original sprites are read using relative resource names via `SettingsAPI.openStream()`; resource precedence remains the engine's responsibility. Byte arrays are assembled without blocked ByteArrayOutputStream; MemoryCacheImageInputStream prevents ImageIO disk caching. The actual restricted-loader fixture verifies decoding, generation and native rendering.

## Why procedural baking

| Candidate | Decision |
|---|---|
| Multiplicative sprite tint | Can darken pixels but cannot replace the original hue or produce convincing white paint; poor black/gold control. |
| Runtime shader | Possible through OpenGL, but no verified public hook attaches a shader to every existing ship draw and every native fleet/refit thumbnail. A combat overlay would also need to preserve occlusion, damage layers and shader state. Not selected. |
| Per-hull manual palettes/masks | Highest artistic control; contradicts the initial universal/no-manual-assets goal. A future optional mask provider can improve selected hulls. |
| CPU procedural image generation | Works with ordinary image resources, incurs no custom shader/draw pass per ship, and preserves the engine's own rendering path. Selected and exercised through RC8's actual renderer. |

The algorithm maps source luminance into paint shading, uses chroma to infer existing markings, and blends bright highlights. It preserves alpha byte-for-byte and never changes source dimensions. It does not discover meaningful primary/secondary regions from geometry. Saturated full-hull paint and baked-in engines can be recoloured as accent. This is an artistic limit rather than a claim of universal segmentation.

## Components

- `Palette`, `Palettes`, `Recolor`: immutable palettes and deterministic pixel processing, independent of Starsector.
- `LiveryState`: pure policy and primitive-only save data. Order: ship > base hull > size > default. A shared Custom template supports global edits.
- `LiveryService`: active-player ownership, clone-before-decoration, module recursion, membership changes, pre-save cleanup and one-source-per-frame prewarming. The original stock variant and shared hull specs are left alone.
- `LiveryTextures`: source decoding, limits, SHA-256 identity, in-memory native registration, session caching and failure deduplication. Original data and derived images are separate.
- `LiveryHullmod`: hidden zero-OP runtime attachment. Its ship-creation hook swaps the native SpriteAPI. It also requests the optional GraphicsLib diffuse-texture override when that mod is present.
- `LiveryDialog`, `LiveryAbility`: campaign menu and preview using supported public UI APIs. No private refit-panel injection.
- `LiveryModPlugin`, `LiveryScript`: application configuration, runtime environment log, transient campaign sync and save callbacks.
- `data.missions.ufl_showcase.MissionDefinition`: repeatable, real-game acceptance fixture using Wolves.

## Ownership and persistence

Only active player-fleet variants are decorated. Member IDs determine individual preferences; base hull IDs and size enums determine class rules. Active ship variants are cloned before changes; module variants are cloned recursively. Variants are marked REFIT so a modified clone is treated as owned refit data. The implementation never changes the shared `ShipHullSpecAPI`.

On removal from the active fleet, UFL removes its own hullmod and palette tag from its tracked member object. It retains primitive preference records, allowing the same member ID to regain its exception when returned. NPC fleets do not inherit the player default. A newly recovered ship with a different member ID uses the default/class rules.

Before save, runtime hullmods/tags and ability objects are stripped. The campaign script is transient. After save or a failed save, UFL reinstalls its runtime attachments. Real campaign serialization, refit cache invalidation and ability-bar behaviour remain acceptance requirements. Cleanup cannot see variants copied by unrelated mods into private storage.

## Performance and failure modes

Texture generation happens once per source/palette combination per session. Generated textures are not persisted on disk; saved palettes reconstruct them as needed. Main-thread work avoids calling OpenGL from worker threads. One uncached active-fleet source is warmed each frame; immediate preview or an unwarmed combat spawn can still cause a first-use pause. Cached draws use ordinary native sprite rendering. No battle-FPS claim has been measured.

The source-image LRU holds at most 12 decoded images. Source files are capped at 32 MiB and decoded dimensions are checked before full decoding. The default per-image ceiling is 4 million pixels. Generated GPU texture estimates include power-of-two dimensions and mipmaps. Session caps prevent uncontrolled texture growth. Loaded textures are not evicted while live engine sprites may reference them. Reaching a cap leaves new requests at original appearance and reports the reason; restart or increase the limit.

Errors are deduplicated per source/palette. The menu can retry skipped requests. Invalid Custom input stays in the editor. Missing/unsupported sprites, oversized images and texture-budget exhaustion preserve original appearances. No source asset is rewritten and no generated files are written.

## Integration boundaries and extension seams

MagicLib's published paintjob path uses a hidden hullmod and native sprite swap; its refit button injects panels using private/reflection helpers. UFL adopts the public hullmod/sprite pattern with independently implemented code and a separate public campaign UI. Active `ML_paintjob-` tags cause UFL to yield rather than compete. Other unrecognised sprite-swapping hullmods can still conflict.

GraphicsLib is optional: a small isolated reflective call to its verified `overrideShipTexture(ShipAPI, String)` method avoids a compile/runtime dependency. If unavailable, base rendering remains and lighting may use original colours. This integration has not been exercised with GraphicsLib loaded.

Future fleet providers can reuse `LiveryState.resolveId()` and `LiveryService.decorate()` for faction NPC fleets and colony defence fleets, with a separate ownership policy. Manufactured ships already inherit upon joining the active player fleet. Optional hull masks, additional templates and explicit glow/weapon integrations belong behind the recolour/texture interfaces. They do not require replacing the save schema.

## Script security and RC8 constraint

0.1.0-alpha used blocked java.io/file classes and an optional reflective adapter. Earlier ordinary-loader renderer tests missed that restriction. The 0.1.1 fixture reproduces the old failure and tests production classes behind the actual game blacklist.

NativeTextureBridge isolates `com.fs.graphics.oOoO.o00000(String, BufferedImage)`, verified against official 0.98a-RC8. This method registers an image in the same native registry used by normal sprites. It requires neither filesystem access nor reflection. It is an internal version-specific engine symbol, not a stable public API. Other RC builds must be retested; a missing/incompatible registrar is converted to a reported texture error so the original appearance remains. No support for arbitrary future builds is claimed.

The optional GraphicsLib call uses public MethodHandles lookup, which RC8's script loader allows. Only its public `overrideShipTexture(ShipAPI,String)` signature is invoked. No private-access lookup or security modification is used. A signature fixture verifies dispatch; real-library lighting acceptance is pending.
