"""Software render the actual resource JSON/UVs; generated textures stay untouched."""
import json, math
from pathlib import Path
from PIL import Image, ImageDraw
ROOT=Path(__file__).resolve().parents[2]
A=ROOT/'common/src/main/resources/assets/kitsunebi'
names=['headdress','haori','hakama','tabi','sword','pickaxe','axe','shovel','hoe']
textures={'#fabric':Image.open(A/'textures/entity/kitsunebi_equipment.png').convert('RGB'),'#palette':Image.open(A/'textures/item/kitsunebi_equipment_palette.png').convert('RGB')}
sheet=Image.new('RGB',(1200,1200),(31,34,45))
draw=ImageDraw.Draw(sheet)

def rotate(p,rotation):
    if not rotation:return p
    axis=rotation['axis'];origin=rotation['origin'];a=math.radians(rotation['angle'])
    q=[p[i]-origin[i] for i in range(3)]
    i,j={'x':(1,2),'y':(2,0),'z':(0,1)}[axis]
    q[i],q[j]=q[i]*math.cos(a)-q[j]*math.sin(a),q[i]*math.sin(a)+q[j]*math.cos(a)
    return tuple(q[i]+origin[i] for i in range(3))

for view,name in enumerate(names):
    yaw=225
    model=json.loads((A/f'models/item/kitsunebi_{name}.json').read_text())
    angle=math.radians(yaw);pitch=math.radians(28)
    depth=[-math.inf]*(400*400)
    def project(p):
        x,y,z=p[0]-8,p[1]-8,p[2]-8
        side=x*math.cos(angle)-z*math.sin(angle)
        forward=x*math.sin(angle)+z*math.cos(angle)
        return (200+side*15,190-(y*math.cos(pitch)-forward*math.sin(pitch))*15,
                forward*math.cos(pitch)+y*math.sin(pitch))
    for e in model['elements']:
        x,y,z=e['from'];X,Y,Z=e['to']
        faces={'up':[(x,Y,z),(X,Y,z),(X,Y,Z),(x,Y,Z)],
               'down':[(x,y,Z),(X,y,Z),(X,y,z),(x,y,z)],
               'north':[(X,Y,z),(x,Y,z),(x,y,z),(X,y,z)],
               'south':[(x,Y,Z),(X,Y,Z),(X,y,Z),(x,y,Z)],
               'west':[(x,Y,z),(x,Y,Z),(x,y,Z),(x,y,z)],
               'east':[(X,Y,Z),(X,Y,z),(X,y,z),(X,y,Z)]}
        for face,corners in faces.items():
            texture=textures[e['faces'][face]['texture']]
            points=[project(rotate(p,e.get('rotation'))) for p in corners]
            u,v,U,V=e['faces'][face]['uv'];uvs=[(u,v),(U,v),(U,V),(u,V)]
            shade={'up':1,'down':.5,'north':.72,'south':.82,'west':.65,'east':.9}[face]
            for indices in ((0,1,2),(0,2,3)):
                a,b,c=[points[i] for i in indices]
                den=(b[1]-c[1])*(a[0]-c[0])+(c[0]-b[0])*(a[1]-c[1])
                if abs(den)<1e-9:continue
                for py in range(max(0,int(min(a[1],b[1],c[1]))),min(400,math.ceil(max(a[1],b[1],c[1])))):
                    for px in range(max(0,int(min(a[0],b[0],c[0]))),min(400,math.ceil(max(a[0],b[0],c[0])))):
                        wa=((b[1]-c[1])*(px+.5-c[0])+(c[0]-b[0])*(py+.5-c[1]))/den
                        wb=((c[1]-a[1])*(px+.5-c[0])+(a[0]-c[0])*(py+.5-c[1]))/den
                        wc=1-wa-wb
                        if min(wa,wb,wc)<-1e-8:continue
                        d=wa*a[2]+wb*b[2]+wc*c[2];index=py*400+px
                        if d<=depth[index]+1e-8:continue
                        depth[index]=d
                        coords=[uvs[i] for i in indices]
                        tu=sum(w*p[0] for w,p in zip((wa,wb,wc),coords))/16
                        tv=sum(w*p[1] for w,p in zip((wa,wb,wc),coords))/16
                        color=texture.getpixel((min(texture.width-1,int(tu*texture.width)),min(texture.height-1,int(tv*texture.height))))
                        sheet.putpixel(((view%3)*400+px,(view//3)*400+py),tuple(int(ch*shade) for ch in color))
    draw.text(((view%3)*400+125,(view//3)*400+365),name,fill=(225,222,235))
sheet.save(ROOT/'docs/v04-codex/equipment-preview.png')
print('Rendered nine actual item models: docs/v04-codex/equipment-preview.png')
