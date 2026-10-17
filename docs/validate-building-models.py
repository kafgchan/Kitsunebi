"""Resource integrity and key geometry invariants, run without a game client."""
import json
from pathlib import Path

ASSETS = Path(__file__).resolve().parents[1] / 'common/src/main/resources/assets/kitsunebi'
count = 0
for path in (ASSETS / 'models').rglob('*.json'):
    data = json.loads(path.read_text(encoding='utf-8'))
    parent = data.get('parent', '')
    if parent.startswith('kitsunebi:'):
        assert (ASSETS / 'models' / (parent.split(':')[1] + '.json')).is_file(), (path, parent)
    for texture in data.get('textures', {}).values():
        if texture.startswith('kitsunebi:'):
            assert (ASSETS / 'textures' / (texture.split(':')[1] + '.png')).is_file(), (path, texture)
    for element in data.get('elements', []):
        assert all(0 <= a < b <= 16 for a, b in zip(element['from'], element['to'])), path
    count += 1

for kind in ('stairs', 'slope'):
    for size in ('high', 'mid', 'low'):
        name = f'phantom_{kind}_{size}'
        variants = json.loads((ASSETS / f'blockstates/{name}.json').read_text())['variants']
        assert len(variants) == 40, name
        for value in variants.values():
            assert (ASSETS / 'models' / (value['model'].split(':')[1] + '.json')).is_file()
        for half in ('bottom', 'top'):
            path = ASSETS / f'models/block/{name}_north_{half}_straight.json'
            elements = json.loads(path.read_text())['elements']
            height = 16 if size == 'high' else 8
            assert min(e['from'][1] for e in elements) == (0 if half == 'bottom' else 16-height)
            assert max(e['to'][1] for e in elements) == (height if half == 'bottom' else 16)
            assert max(e['to'][2] for e in elements) == (8 if size == 'low' else 16)
            volume = sum((e['to'][0]-e['from'][0])*(e['to'][1]-e['from'][1])*(e['to'][2]-e['from'][2]) for e in elements)
            expected = {'stairs': {'high': 3072, 'mid': 1536, 'low': 768},
                        'slope': {'high': 2176, 'mid': 1088, 'low': 544}}[kind][size]
            assert volume == expected, (name, half, volume)
        item = json.loads((ASSETS / f'models/item/{name}.json').read_text())
        assert item['parent'].endswith('_north_bottom_straight')
        assert item['display']['gui']['rotation'] == [30, 45, 0]
for name in ('phantom_slab', 'phantom_vertical_slab', 'phantom_quarter_slab', 'phantom_vertical_quarter'):
    item = json.loads((ASSETS / f'models/item/{name}.json').read_text())
    assert item['display']['gui']['rotation'] == [30, 45, 0]
cube = json.loads((ASSETS / 'models/block/architectural_kitsunebi.json').read_text())
assert cube['parent'] == 'minecraft:block/cube_all'
ja = json.loads((ASSETS / 'lang/ja_jp.json').read_text(encoding='utf-8'))
assert ja['block.kitsunebi.architectural_kitsunebi'] == '狐火ブロック'
for path in (ASSETS / 'models/item').glob('*.json'):
    data = json.loads(path.read_text())
    gui = data['display']['gui']
    block = data.get('parent', '').startswith('kitsunebi:block/')
    cosmetic = path.stem.startswith('kitsunebi_') or path.stem == 'phantom_door'
    assert abs(gui['translation'][1]) <= 1 and gui['translation'][0] == gui['translation'][2] == 0, path
    assert max(gui['scale']) <= (1 if cosmetic else 0.625), path
    if block and not cosmetic:
        assert gui['scale'] == [0.625, 0.625, 0.625], path
mask = json.loads((ASSETS / 'models/item/kitsunebi_goggles.json').read_text())
assert ja['item.kitsunebi.kitsunebi_goggles'] == '狐火の仮面'
assert 'head' in mask['display'] and len(mask['elements']) > 20
assert mask['display']['gui']['rotation'] == [0,165,0]
for color in ('white','blue','cyan','green','purple','orange','red'):
    flame = json.loads((ASSETS / f'models/block/kitsunebi_{color}.json').read_text())
    assert flame['parent'] != 'minecraft:block/cross' and len(flame['elements']) > 50
    for element in flame['elements']:
        assert all(a < b for a,b in zip(element['from'],element['to']))
for tag, block in (('fences', 'phantom_fence'), ('wooden_fences', 'phantom_fence'), ('walls', 'phantom_wall')):
    path = ASSETS.parents[1] / f'data/minecraft/tags/block/{tag}.json'
    data = json.loads(path.read_text())
    assert data['replace'] is False and f'kitsunebi:{block}' in data['values'], path
for kind, volumes in [('stairs', (1216, 320)), ('slope', (901, 187))]:
    for shape, expected in [('inner_left', volumes[0]), ('inner_right', volumes[0]), ('outer_left', volumes[1]), ('outer_right', volumes[1])]:
        for facing in ('north', 'east', 'south', 'west'):
            for half in ('bottom', 'top'):
                path=ASSETS/f'models/block/phantom_{kind}_low_{facing}_{half}_{shape}.json'
                data=json.loads(path.read_text())
                volume=sum(__import__('math').prod(b-a for a,b in zip(e['from'],e['to'])) for e in data['elements'])
                assert volume==expected, (path, volume, expected)
print(f'PASS: {count} model JSONs, local references, 240 states and all low corner rotations/inversions.')
