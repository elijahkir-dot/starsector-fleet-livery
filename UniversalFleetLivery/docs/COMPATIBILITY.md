# Compatibility report — 3 November 2026

0.1.1-alpha fixes the reported file/reflection restriction in both mods. Its in-memory texture registrar targets the exact official **0.98a-RC8** engine binary. Other RC builds have not been certified. No unlocker, GraphicsLib, MagicLib, LunaLib or Nexerelin dependency is added. The optional GraphicsLib adapter itself is not certified against the real library.

The corrected release has restricted-loader/native-renderer evidence for four vanilla hulls. Full interactive game and third-party stack acceptance remain pending. Old renderer evidence was collected under an ordinary loader and is retained only as historical image-processing evidence.


## Target identification

The user's actual PC installation, exact RC build and enabled mod versions were not accessible. Prior context indicates the 0.98a family; it is not an installation audit. Development acquired the official Linux **0.98a-RC8** distribution from Fractal Softworks' release host. Its API and bundled engine jars were used locally and are not distributed in this mod.

Reference source: https://f005.backblazeb2.com/file/fractalsoftworks/release/starsector_linux-0.98a-RC8.zip

The mod requires no additional libraries. Its Java class version is 61 (Java 17). Windows/macOS portability follows the normal Java/API path but has not been tested. Other RCs and later Starsector releases need verification against their own API and runtime; changing the manifest is not a compatibility test.

`LiveryModPlugin` logs the actual runtime version and enabled-mod IDs/versions to `starsector.log` at startup. To audit the user's installation, obtain `mods/enabled_mods.json`, the version/startup section of `starsector.log`, and any failing mod's `mod_info.json`. If its API differs from RC8, obtain that installation's `starsector-core/starfarer.api.jar` and `starfarer.api.zip` to rebuild/compare.

## Evidence matrix

| Surface / integration | Implemented behaviour | Evidence / remaining constraint |
|---|---|---|
| RC8 API compilation | All runtime classes and mission compiled | Verified against official RC8 jars |
| Native RC8 texture loader and SpriteAPI | Generated textures rendered with native engine wrappers | Verified in a small OpenGL fixture, not a launched campaign |
| Vanilla Wolf, Eagle, Onslaught, Paragon | Five fixed palettes plus a Custom example | 28 native draws, distinct texture IDs and no GL errors |
| Vanilla Lasher and Hammerhead | Automatic remapping and unchanged alpha | Included in 30 CPU image-bake cases; no live combat |
| Diable Avionics Draft and Storm images | Same generic pipeline; no hull-specific masks | 14 native draws from two source images; full faction pack was NOT loaded |
| Live combat / simulator | Hidden hullmod swaps each created ship's sprite | Compiled; real combat not run |
| Live refit ship | Same creation hook | Compiled; hook timing, caching and refresh unverified |
| Livery campaign UI | Native interaction dialog, ship picker, custom colour fields and two-image preview | Compiled; interactive layout/input not tested in a campaign |
| Native fleet-card thumbnails | Original hull appearance | Not implemented |
| Campaign fleet silhouettes / minimap | Original appearance | Not implemented; no use of FleetMember spriteOverride |
| Individual ship/hull/size/default policy | Inheritance, new acquisitions, exemptions | Verified with pure state tests and API lifecycle doubles |
| Real save/load, refit reset and uninstall | Primitive config, transient script, pre-save removal, restoration | Doubles pass; actual serialization and toolbar effects unverified |
| Stored/sold/transferred ships | Remove tracked runtime attachment; retain preference by ID | Doubles pass; third-party private variant copies untracked |
| Multi-module ships | Recursive variant decoration | Doubles verify isolation/inheritance; actual modules not flown |
| Nexerelin | No code dependency, player-fleet ownership only | Not loaded/tested; faction/NPC fleets are outside this alpha |
| Tahlan Shipworks / other packs | Ordinary image resources should use generic source lookup | Not loaded/tested; compatibility is not asserted |
| Existing MagicLib paintjobs | Detect `ML_paintjob-` and yield | Code guard; no full mod-stack test |
| GraphicsLib | Optional diffuse override to retain its lighting path | Method pattern verified in published MagicLib source; not loaded/tested |
| LunaLib | Not used | Native UI avoids the dependency |
| Missing image / session texture cap | Log once per request and retain original | Verified through real texture pipeline negative tests |
| Animated/custom-rendered hull parts | Only base hull image is remapped | A later sprite swap or custom shader can override UFL |
| Weapons, fighters, flames, shields, phase glow, damage overlays | Their existing rendering | Separate assets are not automatically recoloured |

## Important practical limits

A universal source-image transform is feasible for ordinary compatible hull images. A universally consistent, semantically correct repaint across every ship, layer, shader and native interface is not established and is not claimed.

The native preview shows structural detail retained, but artwork with extensive existing saturated paint can be dominated by accent colour. This is visible on some Diable/low-tech examples. Custom colours and strength help, but hand-authored region masks would improve specific hulls.

Use one paintjob system per ship. Recognised MagicLib paintjobs are respected; arbitrary other sprite-changing scripts are not reliably detectable. Run the included in-game checklist with the actual faction packs and GraphicsLib before treating this as suitable for a long campaign.
