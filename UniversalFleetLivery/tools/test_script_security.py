#!/usr/bin/env python3
"""Native RC8 regression test with the actual restricted script class loader.
Requires a desktop OpenGL display (or Xvfb), the user's RC8 game and a Java17 compiler.
The mod JAR is intentionally excluded from the app classpath.
"""
import argparse, os, pathlib, shutil, subprocess
ROOT = pathlib.Path(__file__).resolve().parents[1]
p = argparse.ArgumentParser(description=__doc__)
p.add_argument('--game', type=pathlib.Path, required=True)
p.add_argument('--ecj', type=pathlib.Path)
p.add_argument('--java', type=pathlib.Path, help='Runtime; use the bundled RC8 Java where required')
p.add_argument('--mod', type=pathlib.Path, default=ROOT, help='JAR under test; may be a previous release')
p.add_argument('--expect-legacy-error', action='store_true')
p.add_argument('--evidence', type=pathlib.Path, default=ROOT/'docs/evidence')
a = p.parse_args()
game = a.game.resolve()
if (game/'starsector-core').is_dir(): game /= 'starsector-core'
if not (game/'starfarer.api.jar').is_file(): p.error('Missing RC8 starfarer.api.jar')
compiler_java = shutil.which('java')
if a.ecj:
    compiler = [compiler_java, '-jar', str(a.ecj.resolve()), '-17', '-proc:none', '-encoding', 'UTF-8']
else:
    javac = shutil.which('javac')
    if not javac: p.error('JDK17 javac or --ecj is required')
    compiler = [javac, '--release', '17', '-proc:none', '-encoding', 'UTF-8']
classes = ROOT/'build/security-harness'
shutil.rmtree(classes, ignore_errors=True)
classes.mkdir(parents=True)
classpath = os.pathsep.join(str(x) for x in sorted(game.glob('*.jar')))
subprocess.run(compiler+['-classpath', classpath, '-d', str(classes)]+[str(x) for x in sorted((ROOT/'security-tests').rglob('*.java'))], check=True)
runtime = str(a.java.resolve()) if a.java else compiler_java
natives = game/'native'/('windows' if os.name == 'nt' else 'macosx' if os.sys.platform == 'darwin' else 'linux')
command = [runtime, '-noverify', '--add-opens=java.base/java.nio=ALL-UNNAMED', '--add-exports=java.base/jdk.internal.ref=ALL-UNNAMED', '-Djava.library.path='+str(natives), '-cp', str(classes)+os.pathsep+classpath, 'security.ScriptSecurityTest', str(game), str(a.mod.resolve()), str(a.evidence.resolve()), 'legacy' if a.expect_legacy_error else 'fixed']
raise SystemExit(subprocess.run(command).returncode)
