# Historical 0.1.0-alpha report — superseded

The tests below ran under an unrestricted application loader. They missed the script-security failure reported by the player. They document image algorithms and historical renderer experiments, not a successful in-game release. Use ../TEST_REPORT.md for the 0.1.1-alpha evidence.

# Test report — 3 November 2026

**Result: compiled installable alpha; source-image recolouring and native sprite rendering verified. Full gameplay acceptance is pending.** No licensed Starsector campaign/combat session or access to the user's PC/mod installation was available. No live-combat, live-refit, UI or save/load result is claimed.

## Verified results

| Test | Outcome |
|---|---|
| Compile all 13 Java implementation sources against official RC8 jars | PASS; ECJ 3.38.0 with Java 17; no compiler warnings |
| Core invariants and policy tests | PASS; 55 checks |
| Fleet lifecycle integration with API doubles | PASS; 20 checks |
| Native RC8 loader / sprite renderer, vanilla images | PASS; 28 draws across Wolf, Eagle, Onslaught and Paragon, with original + five fixed palettes + Custom |
| Native RC8 loader / sprite renderer, Diable images | PASS; 14 draws across Draft and Storm, with the same seven appearances |
| Native GL validation | PASS; valid texture IDs, no GL errors, nonempty framebuffer |
| Actual engine relative source lookup and absolute generated-PNG loading | PASS |
| Missing-source fallback | PASS; returns no replacement and logs the cause |
| Session texture-budget exhaustion | PASS; returns no replacement and logs the cap |
| Manifest, CSV class references, compiled class version and showcase resources | PASS via package validator |
| Deterministic rebuild | PASS; see `evidence/reproducibility.json` |

The 55 core checks include palette validation, deterministic output, exact alpha preservation, unchanged image dimensions, unchanged source pixels, strength-zero identity, distinct preset outputs, inheritance precedence, explicit Original exemption, disabling/re-enabling, whole-fleet standardisation, custom values and primitive-settings serialization. Thirty of the checks process five palettes across six real vanilla source images (Wolf, Eagle, Onslaught, Paragon, Lasher, Hammerhead).

The latest recorded vanilla corpus run processed **30 bakes / 1,482,840 pixels in about 326 ms**, including image reads and exact-alpha comparisons. This is a small CPU workload measurement from this environment, not a combat frame-rate benchmark or a guarantee for large modded sprites.

The 20 lifecycle checks use public API test doubles to exercise clone isolation, original stock/module variants left untouched, unrelated mods/tags retained, module inheritance, active MagicLib paintjob precedence, stable-policy idempotence, acquisitions, individual exemptions, sold/transferred cleanup, pre-save stripping/suspension, after-save restoration toggling, and isolation of a malformed third-party variant during sync/prewarming/save cleanup. They do not serialize a Starsector campaign and do not prove its callback timing or toolbar handling.

## Native renderer evidence

The native fixture uses the **actual RC8 SettingsAPI implementation, native SpriteAPI wrappers and game texture loader**, with LWJGL/OpenGL running on Mesa llvmpipe (LLVM 20.1.2, software renderer) under Xvfb. It bootstraps RC8's filesystem resource roots separately from the launcher and supplies mod-directory metadata. It does not instantiate a live fleet or combat engine. The UFL pixel/texture code runs directly in this fixture.

- `evidence/native-renderer.png`: rows are Wolf, Eagle, Onslaught, Paragon. Columns are Original, Imperial, Naval, Crimson, Industrial, Arctic, Custom. **28 draws**, **349,903 non-background framebuffer pixels**, no GL errors.
- `evidence/diable-native-renderer.png`: rows are Diable Avionics Draft and Storm. Same columns. **14 draws**, **91,459 non-background framebuffer pixels**, no GL errors.
- `evidence/native-renderer-tests.txt` and `evidence/diable-renderer-tests.txt`: stdout and expected negative-case warnings/stack traces. The deliberately missing image, forced texture cap and intentionally malformed variant in the lifecycle log are test inputs, not unexplained release failures.
- `evidence/automated-tests.txt`: core/lifecycle test output.
- `evidence/build.json`, `evidence/dependency-hashes.json`, `evidence/reproducibility.json`: compiler/API/JAR provenance and rebuild comparison.

Diable source images were acquired from public revision `b5bc16b305ffce738b1072c28d81c1a5cbcd9ea7` (manifest 2.9.5.3 / 0.98a-RC5). **The full Diable mod and its code were not loaded.** Credit Tartiflette and FlashFrozen, maintained by Caymon Joestar. Native image compatibility does not certify its animated parts, modules, hullmods or existing paintjob UI.

## Pending acceptance

- Actual launcher loading with UFL enabled.
- Showcase mission in live combat, including weapon/ship alignment, phasing, damage and enemy isolation.
- Campaign menu layout, text entry, input and ability assignment.
- Fleet refit hook timing and display refresh; refit persistence.
- Real saves, reloads, save failure recovery, toolbar persistence and clean mod removal.
- Actual sales, salvage, storage transfers and membership timing.
- Modular hulls and third-party custom rendering.
- GraphicsLib enabled/disabled and the user's complete mod stack, including Nexerelin and Tahlan.
- Battle frame time and memory behaviour over a long session.

Use `IN_GAME_CHECKLIST.md` to perform and record these checks. Milestone 1 is demonstrated at the native sprite-renderer level, but its live-combat acceptance gate remains open. Milestones 2–5 have compiled implementations and partial automated evidence; they are not represented as fully accepted gameplay milestones.
