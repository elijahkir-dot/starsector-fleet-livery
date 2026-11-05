# 0.1.3-alpha test report — 4 November 2026

The player confirms Fleet Livery 0.1.2 works and supplied fleet-screen screenshots. The separate Paint Workshop crashed when its editor was opened. This release updates Fleet Livery only. The new Fleet Livery menus have compiled/API-double and native-drawing evidence, not a live campaign layout test.

| Check | Outcome |
|---|---|
| Core image/policy tests | PASS; 55 checks, original fallback palettes and six-hull corpus |
| Lifecycle API doubles | PASS; 20 checks |
| Factory catalogue tests | PASS; 65 checks, all 18 entries, stable legacy colours/IDs, alpha preservation and atomic rejection |
| Dialog controller API doubles | PASS; 20 checks, all five palette pages, option counts, preview-only staging, editable draft and deferred picker handoff |
| Actual new swatch drawing callbacks | PASS; five pages / 500 frames, exact sampled swatch RGB, no swallowed render failures or GL errors |
| Actual existing ship-preview drawing callbacks | PASS; all 18 palettes on Wolf, Eagle and Ziggurat; 324 native sprite draws |
| Existing restricted-loader renderer tests | PASS; 28 additional native draws and blacklist checks |
| Core classes compared to installed 0.1.2 | PASS; byte-identical, see legacy-0.1.3-unchanged-core.json |
| Package validator | See package-validation.txt |
| Reproducible JAR build | See reproducibility.json |

The native fixture uses real RC8 SettingsAPI/native sprites and executes the mod's actual PaletteSwatches and LiveryDialog preview plugins through the restricted class loader, with Mesa/Xvfb on Linux. PositionAPI in the test supplies panel geometry; the controller test supplies public API doubles rather than a campaign. No complete new in-game window is claimed as tested.

Evidence: automated-tests.txt, palette-menu-native-tests.txt, build.json, legacy-0.1.3-unchanged-core.json, package-validation.txt, reproducibility.json. Controller logs deliberately reject texture loading because that test isolates staging; native rendering is tested separately. Expected negative cases are labelled.

Historical 0.1.2 Windows/Linux binding evidence remains included. The binary-identical native bridge and renderer remain those from that release. No new Windows desktop, actual combat/refit, save/reload or full mod-stack certification is claimed. The user's current screenshots establish visible hull recolouring in their fleet interface, not every lifecycle case.

Paint Workshop buffer investigation: actual 0.1.2 PreviewCanvas.render reproduced the four-element/16-element LWJGL failure. A separate source checkpoint corrects and contains that error, with real callback tests. It is not part of this playable Fleet Livery ZIP.
