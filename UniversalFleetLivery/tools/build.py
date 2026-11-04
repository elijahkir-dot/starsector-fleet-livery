#!/usr/bin/env python3
"""Offline, deterministic build using the user's game jars. Python 3.9+ and JDK 17.
No dependency downloads. An optional local Eclipse ECJ jar can substitute for javac.
"""
import argparse, hashlib, json, os, pathlib, shutil, subprocess, sys, zipfile
ROOT = pathlib.Path(__file__).resolve().parents[1]

def run(command):
    result = subprocess.run(command, cwd=ROOT, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    print(result.stdout, end='')
    if result.returncode:
        raise SystemExit(result.returncode)
    return result.stdout

def archive(paths, target, base):
    target.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(target, 'w', zipfile.ZIP_DEFLATED, compresslevel=9) as z:
        for path in sorted(paths):
            name = path.relative_to(base).as_posix()
            item = zipfile.ZipInfo(name, (2026, 1, 1, 0, 0, 0))
            item.compress_type = zipfile.ZIP_DEFLATED
            item.external_attr = 0o100644 << 16
            z.writestr(item, path.read_bytes())

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--game', type=pathlib.Path, required=True, help='Game root or starsector-core directory')
    parser.add_argument('--ecj', type=pathlib.Path, help='Optional local ECJ compiler jar (3.38.0 was used for this alpha)')
    parser.add_argument('--test', action='store_true', help='Run invariant and lifecycle-double tests')
    parser.add_argument('--package', action='store_true', help='Create installable source-inclusive ZIP')
    args = parser.parse_args()
    core = args.game.resolve()
    if (core / 'starsector-core').is_dir(): core /= 'starsector-core'
    if not (core / 'starfarer.api.jar').is_file(): parser.error('Missing starfarer.api.jar in game/core directory')
    jars = sorted(core.glob('*.jar'))
    classpath = os.pathsep.join(map(str, jars))
    java = shutil.which('java')
    if not java: parser.error('Java 17+ is required')
    if args.ecj:
        compiler = [java, '-jar', str(args.ecj.resolve()), '-17', '-proc:none', '-encoding', 'UTF-8']
    else:
        javac = shutil.which('javac')
        if not javac: parser.error('Use a JDK 17, or supply --ecj /path/to/ecj.jar')
        compiler = [javac, '--release', '17', '-proc:none', '-encoding', 'UTF-8', '-g:none']
    classes = ROOT / 'build/classes'
    shutil.rmtree(classes, ignore_errors=True)
    classes.mkdir(parents=True)
    sources = sorted((ROOT / 'src').rglob('*.java'))
    output = run(compiler + ['-classpath', classpath, '-d', str(classes)] + list(map(str, sources)))
    mission = ROOT / 'data/missions/ufl_showcase/MissionDefinition.java'
    shutil.copyfile(ROOT / 'src/data/missions/ufl_showcase/MissionDefinition.java', mission)
    jar = ROOT / 'jars/UniversalFleetLivery.jar'
    archive(classes.rglob('*.class'), jar, classes)
    report = {'java': subprocess.run([java, '-version'], capture_output=True, text=True).stderr.strip(),
              'compiler': 'ECJ '+args.ecj.name if args.ecj else 'javac --release 17',
              'api_sha256': hashlib.sha256((core/'starfarer.api.jar').read_bytes()).hexdigest(),
              'java_sources': len(sources), 'jar_sha256': hashlib.sha256(jar.read_bytes()).hexdigest(),
              'game_jars_bundled': False}
    if args.test:
        tests = ROOT / 'build/test-classes'
        shutil.rmtree(tests, ignore_errors=True);tests.mkdir(parents=True)
        run(compiler + ['-classpath', str(classes)+os.pathsep+classpath, '-d', str(tests)] + list(map(str, sorted((ROOT/'tests').rglob('*.java')))))
        testcp = os.pathsep.join([str(classes), str(tests), classpath])
        logs = [run([java, '-Djava.awt.headless=true', '-cp', testcp, 'ufl.CoreTest', str(core)]),
                run([java, '-Djava.awt.headless=true', '-cp', testcp, 'ufl.LifecycleTest'])]
        (ROOT/'docs/evidence').mkdir(parents=True, exist_ok=True)
        (ROOT/'docs/evidence/automated-tests.txt').write_text('\n'.join(logs), encoding='utf-8')
        report['tests'] = 'CoreTest and LifecycleTest passed; not live campaign tests'
    (ROOT/'docs/evidence').mkdir(parents=True, exist_ok=True)
    (ROOT/'docs/evidence/build.json').write_text(json.dumps(report, indent=2)+'\n', encoding='utf-8')
    if args.package:
        files = [p for p in ROOT.rglob('*') if p.is_file() and not any(x in p.relative_to(ROOT).parts for x in ('build','cache','dist','__pycache__','.git'))]
        out = ROOT / 'dist/UniversalFleetLivery-0.1.1-alpha.zip'
        archive(files, out, ROOT.parent)
        print('Packaged:', out)
    print('Built:', jar)
    print('SHA-256:', report['jar_sha256'])
if __name__ == '__main__': main()
