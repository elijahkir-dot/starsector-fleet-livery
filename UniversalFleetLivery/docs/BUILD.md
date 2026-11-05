# Reproducible build

The installed ZIP already includes `jars/UniversalFleetLivery.jar`. Building requires **JDK 17** and **Python 3.9+**, plus the jars from a locally installed Starsector. No game libraries are bundled or fetched by the build script.

## Primary, offline build

From the mod directory:

```sh
python3 tools/build.py --game /path/to/Starsector --test --package
```

On Windows:

```powershell
py tools/build.py --game "C:\Games\Starsector" --test --package
```

The script accepts either the game root or `starsector-core` directory. It compiles all `src/` files against local game jars, builds a sorted/timestamp-normalised JAR, runs the headless tests, writes build evidence and packages a source-inclusive ZIP into `dist/`.

A JRE alone does not supply javac. As an alternative, use a local **Eclipse ECJ 3.38.0** jar with Java 17:

```sh
python3 tools/build.py --game /path/to/Starsector --ecj /path/to/ecj-3.38.0.jar --test --package
```

Development used ECJ 3.38.0, obtained from https://repo.maven.apache.org/maven2/org/eclipse/jdt/ecj/3.38.0/ecj-3.38.0.jar . Exact dependency/compiler digests are recorded in `evidence/dependency-hashes.json`. Pin the same compiler and game jars for byte-identical results. javac and ECJ produce equivalent but not necessarily byte-identical class files. The deterministic archive build was repeated and its JAR hash compared.

## Optional Gradle configuration

A conventional `build.gradle` is included for an already installed Gradle 8.x/JDK17 environment:

```sh
gradle -PstarsectorDir=/path/to/Starsector clean jar coreChecks lifecycleChecks
```

Gradle was not installed/run in the development environment. The Python/ECJ route is the verified build. No Gradle wrapper or unrequested binary dependencies are included.

## Native sprite renderer fixture

`tests/ufl/NativeRenderTest.java` is a separate integration fixture, not a campaign or combat test. It needs the official RC8 jars, native LWJGL libraries and a working OpenGL/X display on Linux. The fixture bootstraps RC8's normal filesystem resource registry using isolated reflection; the mod itself does not use these internal bootstrap methods. SettingsAPI texture loading, relative source streams and SpriteAPI rendering use the real engine implementation. Only mod-directory metadata is substituted.

After a `--test` build, an example Linux invocation from the mod directory is:

```sh
java -noverify \
  --add-opens=java.base/java.nio=ALL-UNNAMED \
  --add-exports=java.base/jdk.internal.ref=ALL-UNNAMED \
  -Djava.library.path=/path/to/rc8/native/linux \
  -cp 'build/classes:build/test-classes:/path/to/rc8/*' \
  ufl.NativeRenderTest /path/to/rc8 /path/to/UniversalFleetLivery /path/to/evidence
```

Use RC8's bundled Java runtime and its native-library environment where required. `-noverify` matches RC8's launcher requirements for its obfuscated classes. It is for this offline renderer fixture, not an instruction to change a user's game settings. A real desktop or Xvfb/Mesa provides the GL context. The fixture does not launch Starsector, authenticate a license or simulate a battle. An optional fourth argument points to a local Diable Avionics resource root to run the two-image test; obtain those assets separately under their license. No source sprites are included in the ZIP.

## Source/resource validation

`tools/validate_package.py` checks manifest structure, CSV script references, compiled class version, mission variant existence, no bundled game jars, source/mission consistency and safe ZIP paths. With `--game`, it validates the referenced showcase variant against the installed game's data. This is separate from real launcher validation.

## Restricted script-loader regression (preferred native check)

Build the JAR first. With a working OpenGL display and RC8 native dependencies available:

```sh
python3 tools/test_script_security.py --game /path/to/rc8 --java /path/to/rc8/jre_linux/bin/java
```

Add `--ecj /path/to/ecj-3.38.0.jar` if javac is unavailable. The helper compiles only `security-tests/` into the unrestricted harness, excludes production classes from its application classpath, and loads the mod JAR through RC8's actual restricted loader. It verifies that forbidden classes remain forbidden. The helper requires the final completed-assertions marker as well as a zero exit code for the fixed suite. Capture stdout/stderr as `docs/evidence/script-security-tests.txt`. It also writes `security-renderer.png`. Linux/Xvfb/Mesa is the verified native environment; Windows/macOS execution of this helper has not been tested.

To reproduce the previous release, extract its original mod directory separately, then add `--mod /path/to/original/ModDirectory --expect-legacy-error`. This mode succeeds only when the old pipeline reproduces the security message. Original mod JARs and game libraries are not bundled in this release.

The harness alone uses reflection to set devMode=false, install resource roots and instantiate real RC8 settings, as the launcher normally would. This does not grant the production JAR additional privileges. Test-only ShaderLib implements a signature check and is excluded from the production JAR.

## Windows/Linux RC8 verification added in 0.1.2

The production JAR compiles byte-identically against both official distributions. The preferred security fixture detects RC8's Windows loader B/resource root C or Linux loader new/resource root, instead of assuming the Linux bootstrap.

A cross-distribution Java test on a Linux host can use Windows game JARs with Linux native libraries through `--native-dir /path/to/linux/native/linux`. Both distributions still require legitimate local game resources. It is not a Windows OS test. The original 0.1.1 failure can be reproduced with `--mod /path/to/old/ModDirectory --expect-registration-error` against Windows game JARs. Source/game binaries are not bundled or automatically fetched by these helpers.

Current native evidence is under `evidence/windows-fixed-renderer.txt` and `evidence/linux-fixed-renderer.txt`; earlier native logs are historical. The public symbol binding differs between the two official RC8 distributions despite identical version labels.

## 0.1.4 test suite

The verified Python --test route runs CoreTest, LifecycleTest, CatalogueTest, DialogTest, ScopeTest, DragRegressionTest and EmpireLifecycleTest. The last four use real API interfaces with test doubles; they do not simulate a licensed campaign. The native security fixture additionally runs the actual palette swatch and ship-preview callbacks for all 18 palettes through the restricted loader.

To reproduce the drag bug against an extracted original 0.1.3 JAR, compile current tests first, then place that old JAR ahead of production classes on the test runtime classpath and run `ufl.DragRegressionTest old`. This mode succeeds only when old cleanup strips a picked-up ship. Do not put the old JAR into the installed mod. The recorded reproduction is in evidence/old-drag-reproduction.txt.

Current reproducibility and byte-identical renderer comparisons are in evidence/reproducibility.json and evidence/unchanged-renderer.json. Historical cross-distribution logs remain included; the 0.1.4 compile/native run used Linux RC8. No Windows desktop or live campaign test is implied.
