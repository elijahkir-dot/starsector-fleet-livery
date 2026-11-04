#!/usr/bin/env python3
"""Validate release structure without launching the game."""
import argparse, csv, json, pathlib, struct, zipfile
ROOT = pathlib.Path(__file__).resolve().parents[1]

def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('--game', type=pathlib.Path)
    p.add_argument('--zip', type=pathlib.Path)
    args = p.parse_args()
    manifest = json.loads((ROOT/'mod_info.json').read_text())
    assert manifest['id'] == 'universal_fleet_livery'
    assert manifest['gameVersion'] == '0.98a-RC8'
    assert not manifest.get('dependencies')
    jar_path = ROOT/manifest['jars'][0]
    with zipfile.ZipFile(jar_path) as jar:
        names = jar.namelist()
        for name in names:
            assert name.startswith(('ufl/', 'data/missions/ufl_showcase/')), name
            if name.endswith('.class'):
                magic, minor, major = struct.unpack('>IHH', jar.read(name)[:8])
                assert magic == 0xcafebabe and major == 61, (name, major)
        assert 'ufl/LiveryModPlugin.class' in names
        assert 'ufl/LiveryHullmod.class' in names
        assert 'data/missions/ufl_showcase/MissionDefinition.class' in names
        for csv_file, column in [('data/hullmods/hull_mods.csv','script'),('data/campaign/abilities.csv','plugin')]:
            with (ROOT/csv_file).open(newline='') as f:
                rows=list(csv.DictReader(f));assert len(rows)==1
                assert rows[0][column].replace('.','/')+'.class' in names
    source=ROOT/'src/data/missions/ufl_showcase/MissionDefinition.java'
    assert source.read_bytes()==(ROOT/'data/missions/ufl_showcase/MissionDefinition.java').read_bytes()
    descriptor=json.loads((ROOT/'data/missions/ufl_showcase/descriptor.json').read_text())
    assert (ROOT/'data/missions/ufl_showcase'/descriptor['icon']).is_file()
    if args.game:
        core=args.game.resolve()
        if (core/'starsector-core').is_dir():core/='starsector-core'
        variants=list((core/'data/variants').rglob('wolf_Assault.variant'))
        assert len(variants)==1,'Showcase Wolf variant must exist'
        assert 'wolf_Assault' in variants[0].read_text()
    if args.zip:
        with zipfile.ZipFile(args.zip) as z:
            for name in z.namelist():
                path=pathlib.PurePosixPath(name)
                assert path.parts[0]=='UniversalFleetLivery' and '..' not in path.parts
                assert not any(part in ('cache','build','research') for part in path.parts),name
                if name.endswith('.jar'):assert name=='UniversalFleetLivery/jars/UniversalFleetLivery.jar',name
            assert z.testzip() is None
            assert z.read('UniversalFleetLivery/jars/UniversalFleetLivery.jar')==jar_path.read_bytes()
    print('PASS package validation: manifest, scripts, class version, source/resources, safe ZIP paths and no bundled game dependencies')
if __name__=='__main__':main()
