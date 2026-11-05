# Compatibility — 0.1.4-alpha

Target: **Starsector 0.98a-RC8**, Java 17. No required mod dependencies. The exact user's mod stack has not been audited or run here. This is a compiled alpha; distinguish real renderer execution from live campaign acceptance.

| Area | Implemented behaviour / evidence |
|---|---|
| Current API/build | Compiles against official Linux RC8 game JARs; no fabricated API stubs. |
| Native texture engine | Byte-identical to 0.1.3; historical Windows and Linux RC8 restricted-loader tests retained. Current Linux/Mesa restricted-loader callbacks pass. |
| Factory previews | All 18 palettes on Wolf, Eagle and Ziggurat: 324 real native preview draws, plus 28 renderer regression draws. |
| Fleet cards | Player-confirmed recolouring in 0.1.2 screenshots. New drag regression passes with API doubles; live pickup/drop retest pending. |
| Ship / Fleet / Empire | Separate state, inheritance, migration and scope reset pass automated checks. New menu layout and real NPC fleet behaviour await campaign acceptance. |
| Empire eligibility | Player fleet plus activated non-station fleets with the sector player-faction ID. No forced inflation of dormant fleets. |
| Stations, stored ships, production-list entries | Not painted by empire automation. New arrivals inherit after joining an eligible fleet. |
| Foreign/allied/hired fleets | Excluded unless their actual faction ID is the player faction. Nexerelin ownership abstractions are not specially interpreted. |
| Combat/refit | Per-instance base-hull sprite swap implemented; live combat/refit and cache refresh still need acceptance. |
| Weapons / effects / decals | Separate graphics retain their original colours. |
| Modular/animated/custom-rendered hulls | Recursive variant rules implemented; independent graphics and scripted renderers are not universally supported. |
| Ownership, save/load and removal | API-double lifecycle coverage passes, including all touched fleets; real campaign serialization, transfer and uninstall remain pending. |
| GraphicsLib | Optional diffuse adapter unchanged. Real lighting combination not certified. |
| Nexerelin / Tahlan / full Diable pack | No dependency added; complete mod stack not loaded/tested together here. Earlier Diable image tests are historical only. |
| Actual Windows desktop / Mac / other RC builds | Current update not live-tested there. Historical Windows Java-engine tests ran on Linux; they are not Windows GUI tests. |

Automatic colour masks cannot identify semantic plating on arbitrary art. Neutral sources can have weak Secondary/Accent separation; saturated whole-hull paint can become mostly Accent. Missing/oversized images or texture-budget exhaustion preserve originals. Known MagicLib paintjobs take priority over UFL.

The native image-registration hook is internal RC8 code, isolated in NativeTextureBridge and checked for the two verified Windows/Linux signatures. Another engine build may reject it and report the underlying cause. Changing the manifest version string does not prove compatibility.

Upgrade migration preserves the old active-fleet standard and snapshots old Custom assignments. The sector key remains `ufl_settings_v1`; its data schema becomes 2. Downgrading to older releases after saving schema-2 Custom snapshots is not supported. Back up a save before testing the alpha upgrade.

No shared hull assets or third-party files are overwritten. Generated images stay in memory. Paint Workshop is not bundled or updated here; keep its unaccepted editor build disabled separately.
