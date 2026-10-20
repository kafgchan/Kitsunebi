"""Original v0.3 altar resource geometry. Does not modify crafting/game logic."""
import json
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'common-1.21.1/src/main/resources/assets/kitsunebi'
QUADS={'stone':(0,0),'cloth':(8,0),'crystal':(0,8),'paper':(8,8)}
elements=[]

def cube(bounds, material='stone', rotation=None):
    x,y,z,X,Y,Z=bounds
    u,v=QUADS[material]
    # Keep sampling strictly inside its quadrant, including mipmap edges.
    def uv(w,h): return [u+.04,v+.04,u+min(7.92,w/2),v+min(7.92,h/2)]
    faces={d:{'texture':'#atlas','uv':uv(w,h)} for d,w,h in [
        ('up',X-x,Z-z),('down',X-x,Z-z),('north',X-x,Y-y),('south',X-x,Y-y),
        ('west',Z-z,Y-y),('east',Z-z,Y-y)]}
    e={'from':[x,y,z],'to':[X,Y,Z],'faces':faces}
    if rotation: e['rotation']=rotation
    elements.append(e)

# Obsidian footing and substantial stone body, within the existing one-block bounds.
cube((0,0,0,16,2,16))
cube((1,2,1,15,11,15))
cube((0,11,0,16,13,16))
# Front/back hanging cloth, separated from stone faces to avoid z fighting.
cube((4,5,.75,12,11,1),'cloth')
cube((4,5,15,12,11,15.25),'cloth')
cube((3,13,2,13,13.25,14),'cloth')
# Four crystal settings make the altar readable from every direction.
for x in (1,13):
    for z in (1,13):
        cube((x,13,z,x+2,14,z+2))
        cube((x+.25,14,z+.25,x+1.75,15.25,z+1.75),'crystal')
        cube((x+.6,15.25,z+.6,x+1.4,16,z+1.4),'crystal')
# Open book with rigid cuboid pages, no billboard planes.
for left in (True,False):
    x0,x1=(4,8) if left else (8,12)
    rotation={'origin':[8,13.5,8],'axis':'z','angle':-22.5 if left else 22.5}
    cube((x0,13.5,4,x1,13.75,12),'cloth',rotation)
    cube((x0+.125,13.75,4.25,x1-.125,14.15,11.75),'paper',rotation)
cube((7.8,13.5,4,8.2,14.2,12),'cloth')
# Small fox emblem on both ceremonial cloth panels, with raised ears and eye marks.
for back in (False,True):
    z0,z1=(15.25,15.4) if back else (.6,.75)
    cube((6,7,z0,10,10,z1),'paper')
    cube((6,10,z0,7,11,z1),'paper')
    cube((9,10,z0,10,11,z1),'paper')
    a,b=(15.4,15.5) if back else (.5,.6)
    cube((6.5,8.5,a,7.5,9,b),'cloth')
    cube((8.5,8.5,a,9.5,9,b),'cloth')
    cube((7.5,7.5,a,8.5,8,b),'stone')

data={'parent':'minecraft:block/block','ambientocclusion':True,
      'textures':{'atlas':'kitsunebi:block/kitsunebi_altar_atlas','particle':'minecraft:block/obsidian'},
      'elements':elements}
(A/'models/block/kitsunebi_altar.json').write_text(json.dumps(data,indent=2)+'\n',encoding='utf-8')
item={'parent':'kitsunebi:block/kitsunebi_altar','display':{
    'gui':{'rotation':[30,225,0],'translation':[0,0,0],'scale':[.625,.625,.625]},
    'fixed':{'rotation':[0,180,0],'translation':[0,0,0],'scale':[.5,.5,.5]}}}
(A/'models/item/kitsunebi_altar.json').write_text(json.dumps(item,indent=2)+'\n',encoding='utf-8')
(A/'textures/block/kitsunebi_altar_atlas.png.mcmeta').write_text('{"texture":{"blur":false,"clamp":true}}\n',encoding='utf-8')
print(f'Generated original altar: {len(elements)} solid elements, four materials, open book and two fox emblems.')
