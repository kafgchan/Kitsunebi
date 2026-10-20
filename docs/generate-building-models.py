"""Generate resource models, without changing block behavior or visibility."""
import json
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'common-1.21.1/src/main/resources/assets/kitsunebi'
TEXTURE = 'kitsunebi:block/phantom_block'
FACES = ('down', 'up', 'north', 'south', 'west', 'east')


def write(relative, data):
    path = ASSETS / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + '\n', encoding='utf-8')


def element(box):
    x0, y0, z0, x1, y1, z1 = box
    uvs = {'up': [x0, z0, x1, z1], 'down': [x0, 16-z1, x1, 16-z0],
           'north': [16-x1, 16-y1, 16-x0, 16-y0],
           'south': [x0, 16-y1, x1, 16-y0],
           'west': [z0, 16-y1, z1, 16-y0],
           'east': [16-z1, 16-y1, 16-z0, 16-y0]}
    return {'from': [x0, y0, z0], 'to': [x1, y1, z1],
            'faces': {f: {'texture': '#texture', 'uv': uvs[f]} for f in FACES}}


def model(name, boxes, texture=TEXTURE):
    write(f'models/block/{name}.json', {
        'parent': 'minecraft:block/block',
        'textures': {'texture': texture, 'particle': texture},
        'elements': [element(b) for b in boxes]})


def item(name, parent):
    write(f'models/item/{name}.json', {'parent': f'kitsunebi:block/{parent}',
        'display': {'gui': {'rotation': [30, 45, 0], 'translation': [0, 0, 0], 'scale': [0.5, 0.5, 0.5]}}})


def rotate_box(box, turns):
    x0, y0, z0, x1, y1, z1 = box
    for _ in range(turns):
        x0, z0, x1, z1 = 16-z1, x0, 16-z0, x1
    return x0, y0, z0, x1, y1, z1


def clip(a, b):
    result = tuple(max(a[i], b[i]) for i in range(3)) + tuple(min(a[i], b[i]) for i in range(3, 6))
    return result if all(result[i] < result[i+3] for i in range(3)) else None


def merge_boxes(boxes):
    """Disjoint boxes remove overlaps and coplanar duplicate faces."""
    cells = set()
    for x0, y0, z0, x1, y1, z1 in boxes:
        cells.update((x, y, z) for x in range(round(x0*2), round(x1*2))
                     for y in range(round(y0*2), round(y1*2)) for z in range(round(z0*2), round(z1*2)))
    result = []
    while cells:
        x, y, z = min(cells)
        x1 = x+1
        while (x1, y, z) in cells:
            x1 += 1
        z1 = z+1
        while all((a, y, z1) in cells for a in range(x, x1)):
            z1 += 1
        y1 = y+1
        while all((a, y1, c) in cells for a in range(x, x1) for c in range(z, z1)):
            y1 += 1
        cells.difference_update((a, b, c) for a in range(x, x1) for b in range(y, y1) for c in range(z, z1))
        result.append(tuple(v/2 for v in (x, y, z, x1, y1, z1)))
    return result


def corner(primary, shape, low):
    if shape == 'straight':
        return [primary]
    side = rotate_box(primary, 3 if shape.endswith('left') else 1)
    if shape.startswith('inner'):
        return [primary] + ([side] if side else [])
    intersection = clip(primary, side) if side else None
    return [intersection] if intersection else []


for kind in ('stairs', 'slope'):
    for size in ('high', 'mid', 'low'):
        name = f'phantom_{kind}_{size}'
        height = 16 if size == 'high' else 8
        depth = 8 if size == 'low' else 16
        variants = {}
        for half in ('bottom', 'top'):
            for shape in ('straight', 'inner_left', 'inner_right', 'outer_left', 'outer_right'):
                boxes = []
                if kind == 'stairs':
                    boxes.extend(corner((0, 0, 0, 16, height//2, depth), shape, True) if size == "low" else [(0, 0, 0, 16, height//2, depth)])
                    boxes.extend(corner((0, height//2, 0, 16, height, depth//2), shape, size == 'low'))
                else:
                    # Fine visual slope; collision retains the existing four-step approximation.
                    for i in range(16):
                        boxes.extend(corner((0, i*height/16, 0, 16, (i+1)*height/16, depth-i*depth/16), shape, size == 'low'))
                if half == 'top':
                    boxes = [(a, 16-e, c, d, 16-b, f) for a, b, c, d, e, f in boxes]
                boxes = merge_boxes(boxes)
                for turns, facing in enumerate(('north', 'east', 'south', 'west')):
                    suffix = f'{name}_{facing}_{half}_{shape}'
                    model(suffix, [rotate_box(b, turns) for b in boxes])
                    variants[f'facing={facing},half={half},shape={shape}'] = {'model': f'kitsunebi:block/{suffix}'}
        write(f'blockstates/{name}.json', {'variants': variants})
        item(name, f'{name}_north_bottom_straight')

# Inventory representations for connected and interactive blocks.
inventory = {
    'phantom_fence': [(6, 0, 6, 10, 16, 10), (0, 6, 7, 16, 9, 9), (0, 12, 7, 16, 15, 9)],
    'phantom_wall': [(4, 0, 4, 12, 16, 12), (0, 0, 5, 16, 13, 11)],
    'phantom_door': [(0, 0, 6, 16, 16, 9)],
    'phantom_button': [(5, 0, 6, 11, 2, 10)],
    'phantom_pressure_plate': [(1, 0, 1, 15, 1, 15)],
    'phantom_lever': [(5, 0, 4, 11, 3, 12), (7, 3, 7, 9, 12, 9)],
}
for name, boxes in inventory.items():
    model(name + '_inventory', merge_boxes(boxes))
    item(name, name + '_inventory')

# Door inventory uses the tall silhouette of a door rather than a thin cube.
door = json.loads((ASSETS / 'models/block/phantom_door_inventory.json').read_text())
door['display'] = {'gui': {'rotation': [20, 30, 0], 'translation': [0, 0, 0], 'scale': [0.65, 1.25, 0.65]}}
write('models/block/phantom_door_inventory.json', door)

print('Generated 240 stair/slope state models, six shaped inventory models, and resource references.')

for name in ('phantom_slab', 'phantom_vertical_slab', 'phantom_quarter_slab', 'phantom_vertical_quarter'):
    path = ASSETS / f'models/item/{name}.json'
    data = json.loads(path.read_text())
    data['display'] = {'gui': {'rotation': [30, 45, 0], 'translation': [0, 0, 0], 'scale': [0.5, 0.5, 0.5]}}
    write(path.relative_to(ASSETS), data)

# Reuse the installed Minecraft version's connected/interactive state layouts.
# Wrappers replace vanilla materials while preserving shape and state transforms.
vanilla_jar = Path.home() / '.gradle/caches/fabric-loom/1.21.1/minecraft-client.jar'
with zipfile.ZipFile(vanilla_jar) as vanilla:
    for source, target in {'oak_fence': 'phantom_fence', 'cobblestone_wall': 'phantom_wall',
            'oak_door': 'phantom_door', 'oak_button': 'phantom_button',
            'oak_pressure_plate': 'phantom_pressure_plate', 'lever': 'phantom_lever'}.items():
        state = json.loads(vanilla.read(f'assets/minecraft/blockstates/{source}.json'))
        def rewrite(node):
            if isinstance(node, list):
                for child in node: rewrite(child)
            elif isinstance(node, dict):
                if 'model' in node:
                    original = node['model']
                    model_path = original.removeprefix('minecraft:')
                    name = target + '_' + model_path.split('/')[-1]
                    textures = set()
                    parent = model_path
                    while parent and not parent.startswith('builtin/'):
                        data = json.loads(vanilla.read(f'assets/minecraft/models/{parent}.json'))
                        textures.update(data.get('textures', {}))
                        parent = data.get('parent', '').removeprefix('minecraft:')
                    write(f'models/block/{name}.json', {'parent': original,
                        'textures': {key: TEXTURE for key in textures}})
                    node['model'] = f'kitsunebi:block/{name}'
                for child in node.values(): rewrite(child)
        rewrite(state)
        write(f'blockstates/{target}.json', state)

# Keep all icons inside the 16-pixel GUI drawing area; override inherited display.
for path in (ASSETS / 'models/item').glob('*.json'):
    data = json.loads(path.read_text())
    block = data.get('parent', '').startswith('kitsunebi:block/')
    scale = 0.625 if block else 1.0
    data.setdefault('display', {})['gui'] = {
        'rotation': [30, 45, 0] if block else [0, 0, 0],
        'translation': [0, 0, 0], 'scale': [scale, scale, scale]}
    write(path.relative_to(ASSETS), data)

# Final dedicated cosmetic geometry and per-item presentation overrides.
import runpy
runpy.run_path(str(ROOT / 'docs/generate-cosmetic-models.py'))
runpy.run_path(str(ROOT / 'docs/generate-altar-model.py'))

# Occupied quarter slices are rendered once each, preserving old layer states.
for name, vertical in [('phantom_quarter_slab', False), ('phantom_vertical_quarter', True)]:
    parts=[]
    for layer in range(4):
        for extra in range(16):
            for i in range(4):
                if not ((extra | (1 << layer)) & (1 << i)): continue
                for facing, rotation in ([('north',0),('east',90),('south',180),('west',270)] if vertical else [(None,0)]):
                    when={'layer':str(layer),'extra':str(extra)}
                    if facing: when['facing']=facing
                    apply={'model':f'kitsunebi:block/{name}_{i}'}
                    if rotation: apply['y']=rotation
                    parts.append({'when':when,'apply':apply})
    write(f'blockstates/{name}.json',{'multipart':parts})
variants={}
for facing, rotation in [('north',0),('east',90),('south',180),('west',270)]:
    for double in (False,True):
        apply={'model':'kitsunebi:block/phantom_block' if double else 'kitsunebi:block/phantom_vertical_slab'}
        if rotation and not double: apply['y']=rotation
        variants[f'facing={facing},double={str(double).lower()}']=apply
write('blockstates/phantom_vertical_slab.json',{'variants':variants})
