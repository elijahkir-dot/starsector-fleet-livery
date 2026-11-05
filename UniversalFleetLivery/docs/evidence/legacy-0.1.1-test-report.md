# 0.1.1-alpha test report — 3 November 2026

**The reported script-security failure was reproduced in both original 0.1.0-alpha JARs. It is fixed in the tested 0.1.1-alpha image/texture paths. Live campaign acceptance remains pending.**

## Reproduction and regression

`security-tests/security/ScriptSecurityTest.java` instantiates the actual restricted loader `com.fs.starfarer.loading.scripts.new` from official RC8. A fresh URLClassLoader contains only the mod JAR; production classes are deliberately absent from the unrestricted application's classpath. Each production class's defining loader is checked. The harness asserts that File, ByteArrayOutputStream and reflect.Method remain blocked.

The fixture supplies the launcher's devMode=false setting, real filesystem resource roots and the actual RC8 SettingsAPI implementation. Only the empty creator-mask catalogue and optional-library presence are fixture values. All source decoding, recolouring, native registration, SpriteAPI draws and preview uploads execute from the restricted mod JAR. Test-bootstrap reflection is outside that loader; no production code disables or circumvents its blacklist.

| Check | Result |
|---|---|
| Original 0.1.0-alpha pipeline | REPRODUCED: File access and reflection are not allowed to scripts |
| Compile 14 implementation sources against official RC8 | PASS; Java17 / ECJ 3.38.0 |
| Image/model/policy tests | PASS; 55 checks |
| Lifecycle with public API doubles | PASS; 20 checks; not real campaign serialization |
| Resolve every 16 implementation class through restricted loader | PASS; fields, methods and constructors resolved |
| Four vanilla hulls through restricted loader | PASS; Wolf, Eagle, Onslaught, Paragon; 28 native sprite draws |
| Generated texture IDs and repeated-request cache | PASS; memory-only IDs; valid native textures; no GL errors |
| Original blacklist still active | PASS; all three deliberately forbidden classes rejected |
| Optional GraphicsLib adapter dispatch | PASS against a public signature fixture; real GraphicsLib is NOT tested |
| Static package validation | See `evidence/package-validation.txt` |
| Repeated JAR build | See `evidence/reproducibility.json` |

## Evidence

- `evidence/legacy-security-error.txt`: the original JAR reproducing the player's message.
- `evidence/script-security-tests.txt`: corrected JAR, actual loader, renderer and assertion results.
- `evidence/security-renderer.png`: fresh RC8 native framebuffer. Rows: Wolf, Eagle, Onslaught, Paragon. Columns: Original, Imperial, Naval, Industrial, Crimson, Arctic, custom purple. These are engine sprite draws, not a mock editor or external paint comparison.
- `evidence/automated-tests.txt`, `build.json`, `dependency-hashes.json`, `reproducibility.json`: compile, headless/lifecycle and build provenance.

Native rendering uses Mesa llvmpipe under Xvfb. It is a real RC8 renderer fixture, not a launcher or campaign. The signature-only `org.dark.shaders.util.ShaderLib` is test code in `security-tests/`; it is excluded from the production JAR and cannot replace a user's actual GraphicsLib.

## Pending acceptance and inherited evidence

The player's interactive campaign, ship picker/editor inputs, refit hook timing, live combat, actual saving/loading, sales/storage/recovery, long-session memory/performance and full third-party mod stack have not been tested here. No full-game success is claimed. Test using `IN_GAME_CHECKLIST.md`.

Historical 0.1.0 image/model evidence remains in this archive, explicitly superseded by this report. Earlier Diable Draft/Storm sprite renders used publicly licensed images only; the full Diable mod was not loaded. Those runs establish image-processing behaviour, not corrected-release mod-stack acceptance. No new third-party runtime compatibility claim is made.

Automatic region detection still groups colours rather than semantic armour parts. Monochrome hulls can have little independent Secondary/Accent coverage. This security patch changes resource transport and texture registration, not the mask/recolour algorithm or save-data encoding.
