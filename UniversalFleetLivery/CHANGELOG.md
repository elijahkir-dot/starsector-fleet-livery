# 0.1.1-alpha — 3 November 2026

Fix the shared “File access and reflection are not allowed to scripts” failure in 0.1.0-alpha.

- Read source resources only through SettingsAPI.openStream, assemble byte arrays without blocked ByteArrayOutputStream, and decode with an explicit in-memory ImageIO stream.
- Register generated images directly in RC8's native texture registry. No generated PNG files, disk caches, temporary directories, filesystem permissions or changes to script security.
- Keep the optional GraphicsLib adapter behind permitted public MethodHandles instead of blocked reflection. Its dispatch is tested against a signature fixture, not the real library.
- Add a regression fixture using Starsector's actual restricted script class loader. The original packages reproduce the reported error; corrected packages render successfully through the same loader.

Upgrade by closing the game and replacing the old mod directory completely. The mod ID, palette encoding, source hashes and save-data format are unchanged. Existing primitive palette/design records need no migration; full campaign upgrade acceptance remains pending. Old generated cache files are no longer used and can be deleted with the old directory.
