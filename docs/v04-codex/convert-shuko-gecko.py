"""Convert the Shuko authoring model to GeckoLib; never overwrite shared assets."""
from pathlib import Path
import json, math, hashlib, argparse
ROOT=Path(__file__).resolve().parents[2]
parser=argparse.ArgumentParser();parser.add_argument('--family',choices=['shuko','shinko'],default='shuko');family=parser.parse_args().family
source=ROOT/'common-1.21.1/src/main/resources/assets/kitsunebi/codex/v04/armor_geometry.json'
detail=ROOT/f'docs/v04-codex/models/{family}_detailed.json'
if family=='shinko' and not detail.exists():raise FileNotFoundError('Run generate-shinko-detailed.py first')
if detail.exists():source=detail
data=json.loads(source.read_text(encoding='utf-8-sig'))
tex_w,tex_h=data.get('texture_size',[128,128]);columns=tex_w//32
rename={'head':'armorHead','hat':'hat','body':'armorBody','right_arm':'armorRightArm','left_arm':'armorLeftArm','right_leg':'armorRightLeg','left_leg':'armorLeftLeg'}
positions={'root':[0,0,0]}; bones=[]
for b in data['bones']:
    parent=b['parent']; p=[positions[parent][i]+b['pivot'][i] for i in range(3)]; positions[b['name']]=p
    out={'name':rename.get(b['name'],b['name']),'pivot':[p[0],24-p[1],p[2]],'rotation':[math.degrees(r) for r in b['rotation']]}
    if parent!='root':out['parent']=rename.get(parent,parent)
    if b['name'].endswith('_tabi'):
        out['parent']='armorRightBoot' if b['name'].startswith('right') else 'armorLeftBoot'
    cubes=[]
    for box_index,box in enumerate(b['boxes']):
        x,y,z=box['from'];w,h,d=box['size'];m=box['material']
        # Fit outside vanilla's 0.25px player skin overlay. Keep original assets untouched.
        # Only wrapping cloth shells get clearance; ornaments, UVs, tails and exposed skin stay intact.
        if box_index==0 and not data.get('skin_clearance_applied',False):
            if b['name']=='kimono': x,y,z,w,h,d=-4.3,-.3,-2.35,8.6,12.6,4.7
            elif b['name'] in ('right_sleeve','left_sleeve'):
                x=-3.4 if b['name'].startswith('right') else -1.35
                y,w,h=-2.3,4.75,8.4
            elif b['name'].endswith('_hakama'): x,y,w,h=-2.3,-.3,4.6,7.7
            elif b['name'].endswith('_tabi'): x,z,w,d=-2.3,-2.3,4.6,4.6
        if b['name'].endswith('_tabi') and box_index==1 and not data.get('skin_clearance_applied',False):
            # Cover the skin overlay's lower/heel faces too (vanilla pants extend to y=12.25).
            x,w,h,d=-2.35,4.7,1.0,5.75
        # GeckoLib floors box-UV dimensions: sub-pixel trim collapses onto the
        # neighboring palette tile's border. Give every face a nonzero inset UV.
        face_uv={face:{'uv':[(m%columns)*32+1,(m//columns)*32+1],'uv_size':[30,30]}
                 for face in ['north','south','east','west','up','down']}
        cubes.append({'origin':[p[0]+x,24-(p[1]+y+h),p[2]+z],'size':[w,h,d],'uv':face_uv})
    if cubes:out['cubes']=cubes
    bones.append(out)
for side in ['Right','Left']:
    p=positions[side.lower()+'_leg']
    bones.append({'name':'armor'+side+'Boot','pivot':[p[0],24-p[1],p[2]]})
# Resolve parent order for editors as well as GeckoLib.
pending=bones;ordered=[];known=set()
while pending:
    ready=[b for b in pending if 'parent' not in b or b['parent'] in known]
    if not ready:raise ValueError('Bone parent cycle')
    ordered+=ready;known.update(b['name'] for b in ready);pending=[b for b in pending if b not in ready]
output={'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':f'geometry.kitsunebi.{family}_armor','texture_width':tex_w,'texture_height':tex_h,'visible_bounds_width':4,'visible_bounds_height':4,'visible_bounds_offset':[0,1,0]},'bones':ordered}]}
assets=ROOT/'neoforge-1.21.1/src/main/resources/assets/kitsunebi'
(assets/f'geo/{family}_armor.geo.json').write_text(json.dumps(output,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
(assets/f'animations/{family}_armor.animation.json').write_text('{"format_version":"1.8.0","animations":{}}\n',encoding='utf-8')
print(f"Converted {len(data['bones'])} authoring bones + 2 empty foot anchors; {sum(len(b.get('cubes',[])) for b in bones)} cuboids with skin-overlay clearance")
print('Authoring geometry SHA256:',hashlib.sha256(source.read_bytes()).hexdigest())


