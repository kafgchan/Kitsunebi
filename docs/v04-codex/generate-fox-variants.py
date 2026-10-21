"""Deterministic material palettes and native item geometry, matching the shared UV contract."""
from pathlib import Path
import json, math, struct, zlib, copy
ROOT=Path(__file__).resolve().parents[2]
A=ROOT/'common-1.21.1/src/main/resources/assets/kitsunebi'
data=json.loads((Path(__file__).parent/'models/shuko_detailed.json').read_text(encoding='utf-8'))
palettes={
 'shuko':['f4f0e7','8e102e','faf6ed','a01533','d6a83d','29282c','d9d1c4','b82b41','e4be58','44302b','dfa6a3','201c20','474449','fff8e7','926527','6c1530'],
 'byakko':['f0f5fc','2158a2','fafcff','3977c5','d6a83d','1b304d','d4e2f3','6698dd','e4be58','44302b','b5d5f3','182336','314e74','fffaf1','926527','193b73'],
 'kokko':['282332','7032a2','f5edf8','9c49c3','d6a83d','17141e','53445d','bd74d6','e4be58','44302b','d4addb','16111c','40364b','fff8ed','926527','492163']}
def chunk(kind,payload):return struct.pack('>I',len(payload))+kind+payload+struct.pack('>I',zlib.crc32(kind+payload)&0xffffffff)
for family,colors in palettes.items():
 raw=bytearray()
 for y in range(128):
  raw.append(0)
  for x in range(128):
   rgb=bytes.fromhex(colors[(y//32)*4+x//32]);shade=-3 if (x//4+y//4)%3==0 else 0
   raw.extend([max(0,c+shade) for c in rgb]);raw.append(255)
 png=b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',128,128,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b'')
 (A/f'textures/entity/{family}_shrine_palette.png').write_bytes(png)
# Native inventory items bake child rotations to cuboid bounds, preserving their material index.
lookup={b['name']:b for b in data['bones']}
def transform(point,b):
 q=list(point)
 for angle,(i,j) in zip(b['rotation'],[(1,2),(2,0),(0,1)]):q[i],q[j]=q[i]*math.cos(angle)-q[j]*math.sin(angle),q[i]*math.sin(angle)+q[j]*math.cos(angle)
 q=[q[i]+b['pivot'][i] for i in range(3)]
 return transform(q,lookup[b['parent']]) if b['parent']!='root' else q
for part,groups in [('headdress',['ears']),('haori',['kimono','haori','right_sleeve','left_sleeve','right_coat','left_coat']),('hakama',['obi','right_hakama','left_hakama']),('tabi',['right_tabi','left_tabi'])]:
 shapes=[]
 for b in data['bones']:
  cursor=b['name']
  while cursor!='root' and cursor not in groups:cursor=lookup[cursor]['parent']
  if cursor=='root':continue
  for cube in b['boxes']:
   points=[transform([cube['from'][k]+cube['size'][k]*v[k] for k in range(3)],b) for v in [(x,y,z) for x in [0,1] for y in [0,1] for z in [0,1]]]
   points=[(p[0],-p[1],p[2]) for p in points]
   shapes.append(([min(p[k] for p in points) for k in range(3)],[max(p[k] for p in points) for k in range(3)],cube['material']))
 lo=[min(s[0][k] for s in shapes) for k in range(3)];hi=[max(s[1][k] for s in shapes) for k in range(3)]
 scale=14/max(hi[k]-lo[k] for k in range(3));center=[(lo[k]+hi[k])/2 for k in range(3)]
 for family in palettes:
  model=copy.deepcopy(json.loads((A/f'models/item/kitsunebi_{part}.json').read_text()))
  model['textures']['shrine']=f'kitsunebi:entity/{family}_shrine_palette'
  model['textures']['particle']=model['textures']['shrine']
  model['elements']=[]
  for a,b,m in shapes:
   u=(m%4)*4;v=(m//4)*4
   model['elements'].append({'from':[(a[k]-center[k])*scale+8 for k in range(3)],'to':[(b[k]-center[k])*scale+8 for k in range(3)],'faces':{face:{'texture':'#shrine','uv':[u+.1,v+.1,u+3.8,v+3.8]} for face in ['up','down','north','south','east','west']}})
  name='kitsunebi' if family=='shuko' else family
  (A/f'models/item/{name}_{part}.json').write_text(json.dumps(model,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
atlas=A.parent/'minecraft/atlases/blocks.json';d=json.loads(atlas.read_text())
for family in palettes:
 resource=f'kitsunebi:entity/{family}_shrine_palette';entry={'type':'minecraft:single','resource':resource,'sprite':resource}
 if entry not in d['sources']:d['sources'].append(entry)
atlas.write_text(json.dumps(d,indent=2)+'\n',encoding='utf-8')
print('Generated Shuko / Byakko / Kokko palettes and 12 matching inventory models.')
