"""Code-native voxel geometry and material palette for flames, masks and doors."""
import json
import math
import struct
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'common/src/main/resources/assets/kitsunebi'

def write(path, value):
    (A / path).write_text(json.dumps(value, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')

# Flat material swatches for native cuboid models, not painted image assets.
palette = ['f3efdf', 'd5d3cc', 'b2b5b5', 'bf202c', 'ed3944', '381f2b', 'e2b968', '714236']
colors = {'white': ['b8cbd3','e8f0ec','fffce4'], 'blue': ['2348c9','339ef4','b9f3ff'],
          'cyan': ['087f92','28d5d3','d2ffef'], 'green': ['158349','4fe390','e9ffd2'],
          'purple': ['7133b4','c77df6','f9dcff'], 'orange': ['cb4b16','ffae35','fff2b8'],
          'red': ['a91937','f44b4c','ffe0b0']}
indices = {}
for color, swatches in colors.items():
    indices[color] = list(range(len(palette),len(palette)+3))
    palette.extend(swatches)
palette += ['ffffff'] * (64-len(palette))
pixels = b''.join(bytes.fromhex(c) + b'\xff' for c in palette)
raw = b''.join(b'\x00'+pixels for _ in range(16))
def chunk(kind, data):
    return struct.pack('>I',len(data))+kind+data+struct.pack('>I',zlib.crc32(kind+data)&0xffffffff)
png = b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',64,16,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b'')
(A / 'textures/block/model_palette.png').write_bytes(png)

def face(index):
    return {'texture':'#palette','uv':[index/4+0.03,0,index/4+0.22,16]}

def box(bounds, material=0, shade=True):
    return {'from':list(bounds[:3]),'to':list(bounds[3:]),'shade':shade,
            'faces':{d:face(material) for d in ('north','south','east','west','up','down')}}

def native(elements):
    return {'parent':'minecraft:block/block','textures':{'palette':'kitsunebi:block/model_palette',
            'particle':'kitsunebi:block/phantom_block'},'elements':elements}

# Rounded droplet volume, curving tip, and detached wisps: no crossed sheets.
cells = set()
radii = [1.3,2.3,2.9,3.2,3.1,2.8,2.4,2,1.6,1.2,0.9,0.6]
for layer,radius in enumerate(radii):
    y = layer+2
    cx = 8 + max(0,layer-5)*0.23
    cz = 8 + math.sin(layer*0.5)*0.35
    for x in range(3,14):
        for z in range(3,14):
            if (x+0.5-cx)**2+(z+0.5-cz)**2 <= radius**2:
                cells.add((x,y,z))
cells.update({(10,14,8),(5,11,6),(4,12,6),(11,9,11)})
dirs = {'north':(0,0,-1),'south':(0,0,1),'east':(1,0,0),'west':(-1,0,0),'up':(0,1,0),'down':(0,-1,0)}
for color in colors:
    elements = []
    for x,y,z in sorted(cells):
        e = box((x,y,z,x+1,y+1,z+1),shade=False)
        e['faces'] = {}
        for d,(dx,dy,dz) in dirs.items():
            if (x+dx,y+dy,z+dz) in cells: continue
            distance = abs((x if d in ('north','south') else z)+0.5-8)
            brightness = 2 if distance < 1 and y < 8 else (1 if distance < 2 and y < 11 else 0)
            e['faces'][d] = face(indices[color][brightness])
        if e['faces']: elements.append(e)
    flame=native(elements)
    # Particle UVs sample the entire sprite, so never use the multicolored swatch sheet.
    flame['textures']['particle']=f'kitsunebi:block/kitsunebi_{color}'
    flame['render_type']='minecraft:translucent'
    write(f'models/block/kitsunebi_{color}.json',flame)
    write(f'models/item/kitsunebi_{color}.json',{'parent':f'kitsunebi:block/kitsunebi_{color}',
        'display':{'gui':{'rotation':[20,45,0],'translation':[0,0,0],'scale':[0.9,0.9,0.9]}}})

# White/red fox mask, side straps, raised muzzle and ears; legacy ID preserves saves.
mask = []
def part(b,m=0): mask.append(box(b,m))
part((4,5,2.7,12,11,3.7))
part((4.5,11,2.9,11.5,12,3.8))
part((5,4,2.5,11,5,3.6))
part((6,3.5,2.3,10,4,3.4))
# Tapered ears and inner red markings.
for left in (True,False):
    x=4 if left else 10
    part((x,11,3,x+2,14,4))
    part((x+0.5,14,3.1,x+1.5,15.5,4))
    part((x+0.6,11.8,2.85,x+1.4,14.5,3.05),3)
# Black eyes with red upper brows, red cheek accents.
for x in (4.8,9.1):
    part((x,8.1,2.4,x+2.1,8.7,2.72),5)
    part((x,8.9,2.35,x+2.1,9.5,2.72),3)
    part((x+0.2,6.5,2.35,x+1.4,7.1,2.72),4)
part((7.3,9.6,2.35,8.7,11.3,2.72),3)
part((6.3,5.1,1.5,9.7,7.3,2.75),0)
part((7,5.9,0.9,9,7,1.6),1)
part((7.4,6.2,0.55,8.6,6.85,0.95),5)
part((7,4.8,1.4,9,5.2,2),3)
part((3.65,8.2,3.6,4.05,9,12.35),3)
part((11.95,8.2,3.6,12.35,9,12.35),3)
part((3.65,8.2,12,12.35,9,12.4),3)
data = native(mask)
data['textures']['particle']='kitsunebi:block/kitsunebi_white'
data['display'] = {
    'gui':{'rotation':[0,165,0],'translation':[0,-1,0],'scale':[1,1,1]},
    'head':{'rotation':[0,0,0],'translation':[0,0,0],'scale':[1.6,1.6,1.6]},
    'firstperson_righthand':{'rotation':[0,160,0],'translation':[0,1,0],'scale':[0.7,0.7,0.7]},
    'thirdperson_righthand':{'rotation':[0,180,0],'translation':[0,1,0],'scale':[0.65,0.65,0.65]}}
write('models/item/kitsunebi_goggles.json',data)
names={'ja_jp':'狐火の仮面','en_us':'Kitsunebi Mask','ko_kr':'여우불 가면','zh_cn':'狐火面具','zh_tw':'狐火面具','zh_hk':'狐火面具'}
for locale,name in names.items():
    path=f'lang/{locale}.json'
    data=json.loads((A/path).read_text(encoding='utf-8'))
    data['item.kitsunebi.kitsunebi_goggles']=name
    write(path,data)

# A tall framed door icon, with open window, lower panel and gold handle.
door=[]
for b in [(4,0,7,5,16,9),(11,0,7,12,16,9),(5,15,7,11,16,9),
          (5,0,7,11,1,9),(5,7,7,11,8,9),(7.5,8,7,8.5,15,9),(5,1,7.5,11,7,8.5)]:
    e=box(b,1)
    e['faces']={d:{'texture':'#surface'} for d in dirs}
    door.append(e)
door.append(box((9.7,6.6,6.5,10.7,7.5,7),6))
data=native(door)
data['textures']['surface']='kitsunebi:block/phantom_block'
data['display']={'gui':{'rotation':[0,-12,0],'translation':[0,0,0],'scale':[1,1,1]}}
write('models/item/phantom_door.json',data)

# Add windows and a handle to placed door halves, keeping vanilla hinge transforms.
import zipfile
with zipfile.ZipFile(Path.home()/'.gradle/caches/fabric-loom/1.21.1/minecraft-client.jar') as vanilla:
    for path in (A/'models/block').glob('phantom_door_oak_door_*.json'):
        source='block/'+path.stem.removeprefix('phantom_door_')
        template=json.loads(vanilla.read('assets/minecraft/models/'+source+'.json'))
        template=json.loads(vanilla.read('assets/minecraft/models/'+template['parent'].removeprefix('minecraft:')+'.json'))
        a,b=template['elements'][0]['from'],template['elements'][0]['to']
        x0,x1=a[0],b[0]
        boxes=[(x0,0,0,x1,16,2),(x0,0,14,x1,16,16),(x0,0,2,x1,2,14),(x0,14,2,x1,16,14)]
        top='_top_' in path.stem
        boxes += [(x0,2,7,x1,14,9)] if top else [(x0+1,2,2,x1-1,14,14)]
        es=[]
        for bounds in boxes:
            e=box(bounds)
            e['faces']={d:{'texture':'#surface'} for d in dirs}
            es.append(e)
        if not top:
            z=3 if '_right' in path.stem else 12
            es.extend([box((max(0,x0-0.5),12,z,x0+0.1,13,z+1),6),box((x1-0.1,12,z,min(16,x1+0.5),13,z+1),6)])
        data=native(es);data['textures']['surface']='kitsunebi:block/phantom_block'
        write(path.relative_to(A),data)

print('Built seven volumetric flames, equipped fox mask, framed doors and vanilla-size GUI transforms.')
