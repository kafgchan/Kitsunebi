"""Software render the actual resource JSON/UVs; generated textures stay untouched."""
import json, math
from pathlib import Path
from PIL import Image, ImageDraw
ROOT=Path(__file__).resolve().parents[2]
A=ROOT/'common-1.21.1/src/main/resources/assets/kitsunebi'
names=['1 tail / 315 deg','3 tails / 0 deg','6 tails / 0 deg','9 tails / 0 deg']
geometry=json.loads((A/'codex/v04/armor_geometry.json').read_text())
textures={'#fabric':Image.open(A/'textures/entity/kitsunebi_equipment.png').convert('RGB'),'#palette':Image.open(A/'textures/item/kitsunebi_equipment_palette.png').convert('RGB')}
sheet=Image.new('RGB',(1600,400),(31,34,45))
draw=ImageDraw.Draw(sheet)

def transform(point,bone,lookup,tails):
    r=list(bone['rotation']);name=bone['name']
    if name.startswith('tail_') and name.count('_')==1:
        i=int(name.split('_')[1]);r=[-.35,(i-(tails-1)/2)*.26,0]
    q=list(point)
    for angle,(i,j) in zip(r,[(1,2),(2,0),(0,1)]):
        q[i],q[j]=q[i]*math.cos(angle)-q[j]*math.sin(angle),q[i]*math.sin(angle)+q[j]*math.cos(angle)
    q=[q[i]+bone['pivot'][i] for i in range(3)]
    if bone['parent']!='root':q=transform(q,lookup[bone['parent']],lookup,tails)
    return q

def rotate(point,rotation):
    q=transform(point,rotation['bone'],rotation['lookup'],rotation['tails'])
    return (q[0]+8,16-q[1],q[2]+8)

for view,name in enumerate(names):
    yaw=315 if view==0 else 0
    tails=[1,3,6,9][view]
    lookup={b['name']:b for b in geometry['bones']}
    elements=[]
    for bone in geometry['bones']:
        if bone['name'].startswith('tail_') and int(bone['name'].split('_')[1])>=tails:continue
        for box in bone['boxes']:
            xyz=box['from'];end=[xyz[i]+box['size'][i] for i in range(3)]
            material=box['material'];u=(material%2)*8;v=(material//2)*8
            elements.append({'from':xyz,'to':end,'rotation':{'bone':bone,'lookup':lookup,'tails':tails},'faces':{f:{'texture':'#fabric','uv':[u,v,u+7.8,v+7.8]} for f in ['up','down','north','south','west','east']}})
    # Neutral mannequin head makes the head-mounted ears visible in context.
    elements.append({'from':[-4,-8,-4],'to':[4,0,4],'rotation':{'bone':lookup['head'],'lookup':lookup,'tails':tails},'faces':{f:{'texture':'#palette','uv':[8.5,12.5,11,15]} for f in ['up','down','north','south','west','east']}})
    model={'elements':elements}
    angle=math.radians(yaw);pitch=math.radians(28)
    depth=[-math.inf]*(400*400)
    def project(p):
        x,y,z=p[0]-8,p[1]-8,p[2]-8
        side=x*math.cos(angle)-z*math.sin(angle)
        forward=x*math.sin(angle)+z*math.cos(angle)
        return (200+side*8,190-(y*math.cos(pitch)-forward*math.sin(pitch))*8,
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
                        sheet.putpixel((view*400+px,py),tuple(int(ch*shade) for ch in color))
    draw.text((view*400+125,365),name,fill=(225,222,235))
sheet.save(ROOT/'docs/v04-codex/armor-preview.png')
print('Rendered articulated armor geometry (software preview): docs/v04-codex/armor-preview.png')
