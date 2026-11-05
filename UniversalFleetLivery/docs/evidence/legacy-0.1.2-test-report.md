# 0.1.2-alpha test report — 3 November 2026

**The player's 0.1.1 texture-registration failure is reproduced using official Windows RC8 JARs. Corrected texture paths render with both Windows and Linux engine code under their real restricted loaders. Full live campaign acceptance remains pending.**

## Concrete failures and corrections

1. Windows RC8 calls the public image registrar `super`, while Linux RC8 calls it `o00000`. The prior JAR linked directly to the Linux name. Its Windows run raises NoSuchMethodError and produces the exact generic message seen in the screenshot. Public MethodHandles now select/cache the independently verified platform binding.

## Results

| Test | Result |
|---|---|
| Compile against Windows RC8 API/engine JARs | PASS; Java17 / ECJ 3.38.0 |
| Compile against Linux RC8 API/engine JARs | PASS; byte-identical production JAR |
| Core/model/policy tests | PASS; 55 checks |
| Lifecycle tests | PASS; 20 checks with public API doubles |
| Resolve all production classes under actual restricted loaders | PASS; 16 classes including nested types |
| Prior Windows 0.1.1 texture failure | REPRODUCED; exact message plus NoSuchMethodError cause |
| Windows code, native sprites | PASS; 28 draws, including Ziggurat; binding is com.fs.graphics.oOoO.super |
| Linux code, native sprites | PASS; 28 draws, including Ziggurat; binding is com.fs.graphics.oOoO.o00000 |
| Forbidden File / ByteArrayOutputStream / reflect.Method | Still rejected by each real loader |
| Texture cache / GL errors | PASS; repeated requests reuse IDs, nonempty framebuffer, no GL errors |
| Optional GraphicsLib dispatch | PASS using signature fixture only; real library NOT tested |
| Static package validation / reproducible JAR | See package-validation.txt and reproducibility.json |

## Evidence and scope

- `evidence/windows-previous-renderer.txt`: old 0.1.1 JAR, actual Windows restricted loader, exact NoSuchMethodError and reported message.
- `evidence/windows-fixed-renderer.txt`, `evidence/linux-fixed-renderer.txt`: corrected binding, class-resolution, sprite and GL assertions.
- `evidence/windows/security-renderer.png`, `evidence/linux/security-renderer.png`: actual native framebuffers. Rows: Wolf, Eagle, Onslaught, Ziggurat. Columns: Original, Imperial, Naval, Industrial, Crimson, Arctic, custom purple. These are engine sprite draws, not mock interfaces.
- `evidence/automated-tests.txt`, `automated-tests-linux.txt`: model/lifecycle and entry logs as applicable. Deliberately malformed variants produce expected warnings.
- `evidence/build.json`, `build-linux.json`, dependency-hashes.json and reproducibility.json record compilation/provenance.

Both Java distributions run on the Linux host using Linux LWJGL/JRE native libraries, Xvfb and Mesa llvmpipe. This verifies actual Windows game Java code and its restricted loader, not a Windows desktop driver/session. Source resources come from the official game; Ziggurat's source hashes match across the two distributions.

The test harness alone uses unrestricted reflection to bootstrap resource roots/settings. Production code is loaded by a fresh child loader with the actual game restriction as its parent, and is excluded from the harness app classpath. Forbidden-class assertions remain active. No security setting is disabled. The ShaderLib signature fixture lives only in security-tests and is excluded from the mod JAR.

Actual campaign UI rendering/input, combat/refit integration, campaign serialization/reload/removal, the complete user's mod stack and long-session performance are still untested. The entry test models callback order verified in the official native picker/interaction implementation; it does not instantiate a live campaign window. Use IN_GAME_CHECKLIST.md for acceptance. Earlier reports/evidence are retained as historical, explicitly superseded.
