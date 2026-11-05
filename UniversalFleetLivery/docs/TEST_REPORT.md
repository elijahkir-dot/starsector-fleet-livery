# Test report — 0.1.4-alpha — 4 November 2026

This is the Fleet Livery scope/menu/drag update. The player's earlier screenshots confirm fleet-screen paint in 0.1.2. No new live campaign, actual NPC patrol, Windows desktop, combat/refit or save/load session was run for 0.1.4. The separate Paint Workshop editor is not released here.

| Check | Result |
|---|---|
| Official Linux RC8 API compilation, Java 17 class target | PASS; 16 source files. Exact JAR hash in build.json. |
| Core image/policy tests | PASS; 55 checks. |
| Existing lifecycle API doubles | PASS; 20 checks. |
| Factory catalogue | PASS; 65 checks covering all 18 entries, legacy colours/IDs, alpha and invalid data. |
| Dialog controller API doubles | PASS; 24 checks: root/scopes/Utilities, catalogue pages, staged drafts, deferred picker handoff and saved Custom text-list selection. |
| Scope policy | PASS; 19 checks: inheritance, independence, custom snapshots, old-Custom migration, scoped reset and plain-map serialization. |
| Original 0.1.3 drag regression | REPRODUCED using the old released JAR: temporary roster removal strips paint. |
| Updated drag regression | PASS: pickup, held save callbacks, drop, and cleanup after actual departure. Uses public API doubles, not a live mouse gesture. |
| Empire lifecycle | PASS; 18 API-double checks: faction filtering, no forced inflation, station exclusion, transient/idempotent listener, independent rules, ownership changes, global disable and multi-fleet save cleanup. |
| Restricted script loader | PASS; 19 implementation classes resolve; prohibited classes remain prohibited. |
| Actual palette swatch callbacks | PASS; all 5 pages / 500 frames, exact RGB samples, no GL errors. |
| Actual ship-preview callbacks | PASS; all 18 presets on Wolf, Eagle and Ziggurat; 324 real native sprite draws. |
| Existing renderer regression | PASS; 28 further real RC8 native draws; complete native suite 127 assertions. |
| Renderer byte comparison with 0.1.3 | See unchanged-renderer.json; rendering pipeline classes unchanged. Service/state/menu do change. |
| Deterministic JAR rebuild | See reproducibility.json. |
| Source-inclusive installable ZIP validation | See package-validation.txt. |

The native fixture uses real RC8 SettingsAPI and SpriteAPI code through its actual restricted script loader, with Linux Xvfb/Mesa llvmpipe. Its panel positions are fixture-supplied. It executes real drawing callbacks, not a complete campaign window. The dialog/lifecycle fixtures use API doubles. Controller logs deliberately reject texture loading because native rendering has separate coverage; those expected warnings do not indicate a production test failure.

Engine-source inspection located the native pickup removal and placement operations, explaining the prior paint flicker. The revised fixture exercises the same roster transition. The live held thumbnail and real ownership/save interactions still need the acceptance checklist.

Evidence files: automated-tests.txt, build.json, palette-menu-native-tests.txt, old-drag-reproduction.txt, unchanged-renderer.json, reproducibility.json and package-validation.txt. Earlier Windows/Linux binding and Diable image evidence is retained with historical release reports; it is not fresh Windows/full-mod-stack certification.

One earlier native invocation exited with zero before its final assertions marker; that partial run is not counted as a pass (`evidence/native-incomplete-attempt.txt`). The helper now rejects such incomplete fixed-suite runs. A subsequent complete run passed, and the final guarded rerun also passed all 127 assertions.

No result here establishes save-corruption immunity, universal third-party support, actual large-empire performance, or the separate Workshop editor's stability.
