#!/usr/bin/env python3
"""Offline validation of JSON catalog, referenced assets and project structure."""
from pathlib import Path
import json, xml.etree.ElementTree as ET, sys
root=Path(__file__).resolve().parents[1]
src=root/'app/src/main'
required=[root/'settings.gradle.kts',root/'build.gradle.kts',root/'app/build.gradle.kts',
          src/'AndroidManifest.xml',root/'.github/workflows/build-apk.yml']
for p in required: assert p.is_file(), f'Missing {p}'
manifest=ET.parse(src/'AndroidManifest.xml')
android='{http://schemas.android.com/apk/res/android}'
assert any(c.attrib.get(android+'name')=='android.intent.category.LEANBACK_LAUNCHER'
           for c in manifest.iter('category')),'Missing TV launcher'
items=json.loads((src/'assets/catalog.json').read_text())['movies']
assert items and len(items)<=250
ids=set()
for movie in items:
    assert movie['id'] not in ids, f'Duplicate ID {movie["id"]}'
    ids.add(movie['id'])
    assert movie['type'] in ('Film','Serial'),movie['type']
    assert movie['videoUrl'].startswith(('http://','https://'))
    for art in ('poster','backdrop'):
        value=movie[art]
        if not value.startswith(('http://','https://')):
            assert any((src/'res/drawable-nodpi'/f'{value}.{ext}').is_file() for ext in ('jpg','png','webp')),value
for file in ('app_icon.png','tv_banner.png','poster_default.jpg','hero_cinema.jpg'):
    assert (src/'res/drawable-nodpi'/file).is_file()
print(f'OK: {len(items)} demo entries; Android TV manifest; drawable references; Gradle & CI project files.')
print('Warning: this is not an APK build and does not verify Java compilation or device playback.')
