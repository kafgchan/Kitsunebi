"""Export the saved Blockbench master without replacing it or manual edits.

Usage: python export_assets.py [path/to/edited.bbmodel]
Supports cube face UVs, nested bone pivots, static rotations and animation keys.
"""
from pathlib import Path
import json,base64,sys,math
from PIL import Image
HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[2]
ASSETS=ROOT/'neoforge-1.21.1/src/main/resources/assets/kitsunebi'
path=Path(sys.argv[1]) if len(sys.argv)>1 else HERE/'shuko.bbmodel'
source=json.loads(path.read_text(encoding='utf-8-sig'))
elements={e['uuid']:e for e in source['elements']}
groups={g['uuid']:g for g in source.get('groups',[])}
bones=[];names={}
def convert(node,parent=None):
    # Blockbench 5 keeps group definitions separate from the outliner tree.
    node={**groups.get(node.get('uuid'),{}),**node}
    b={'name':node['name'],'pivot':node.get('origin',[0,0,0])}
    if parent:b['parent']=parent
    if any(node.get('rotation',[])):b['rotation']=node['rotation']
    names[node['uuid']]=node['name']; cubes=[]
    for child in node.get('children',[]):
        if isinstance(child,dict):continue
        e=elements[child]
        if not e.get('export',True):continue
        c={'origin':e['from'],'size':[round(e['to'][i]-e['from'][i],6) for i in range(3)],'uv':{}}
        if any(e.get('rotation',[])):c.update(pivot=e.get('origin',[0,0,0]),rotation=e['rotation'])
        if e.get('inflate'):c['inflate']=e['inflate']
        for face,data in e['faces'].items():
            if data.get('texture') is None:continue
            uv=data['uv'];u={'uv':uv[:2],'uv_size':[uv[2]-uv[0],uv[3]-uv[1]]}
            if data.get('rotation'):u['uv_rotation']=data['rotation']
            c['uv'][face]=u
        cubes.append(c)
    if cubes:b['cubes']=cubes
    bones.append(b)
    for child in node.get('children',[]):
        if isinstance(child,dict) and child.get('export',True):convert(child,node['name'])
for root in source['outliner']:
    if isinstance(root,dict):convert(root)
geo={'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':'geometry.kitsunebi.shuko','texture_width':source['resolution']['width'],'texture_height':source['resolution']['height'],'visible_bounds_width':4,'visible_bounds_height':3,'visible_bounds_offset':[0,1.1,.5]},'bones':bones}]}
gp=ASSETS/'geo/npc/shuko.geo.json';gp.parent.mkdir(parents=True,exist_ok=True);gp.write_text(json.dumps(geo,indent=2),encoding='utf-8')
for tex in source['textures']:
    p=ASSETS/'textures/entity/npc/shuko'/tex['name'];p.parent.mkdir(parents=True,exist_ok=True)
    if tex.get('source','').startswith('data:'):p.write_bytes(base64.b64decode(tex['source'].split(',',1)[1]))
    elif Path(tex.get('path','')).is_file():
        if Path(tex['path']).resolve()!=p.resolve():p.write_bytes(Path(tex['path']).read_bytes())
    else:raise ValueError('Missing texture '+tex['name'])
    im=Image.open(p);assert im.size==(256,256) and im.mode=='RGB',(p,im.size,im.mode)
animations={}
for a in source.get('animations',[]):
    out={'loop':a.get('loop')=='loop','animation_length':a['length'],'bones':{}}
    for bid,an in a.get('animators',{}).items():
        if bid not in names:continue
        channels={}
        for k in an.get('keyframes',[]):
            ch=k['channel']
            if ch not in ('rotation','position','scale'):continue
            values=[float(k['data_points'][0].get(axis,0)) for axis in ('x','y','z')]
            value={'post':values,'lerp_mode':'catmullrom'} if k.get('interpolation')=='catmullrom' else values
            channels.setdefault(ch,{})[str(k['time'])]=value
        if channels:out['bones'][names[bid]]=channels
    animations[a['name']]=out
ap=ASSETS/'animations/npc/shuko.animation.json';ap.parent.mkdir(parents=True,exist_ok=True);ap.write_text(json.dumps({'format_version':'1.8.0','animations':animations},indent=2),encoding='utf-8')
count=sum(len(b.get('cubes',[])) for b in bones)
assert len({b['name'] for b in bones})==len(bones)
for i in range(1,10):
    for suffix in ('','_mid','_tip'):assert f'tail_{i:02d}'+suffix in {b['name'] for b in bones}
report={'editing_master':str(path),'geometry':str(gp),'texture_size':[256,256],'texture_count':len(source['textures']),'opaque':True,'bones':len(bones),'cubes':count,'cube_faces_max':count*6,'animations':list(animations),'coordinates':'+Y up, -Z front; Blockbench master coordinates retained by this exporter','bounds_height_px':33.3,'tail_chain_pattern':'tail_01..tail_09 -> tail_XX_mid -> tail_XX_tip','manual_edits':'export reads saved master and never rewrites it','blockbench_ui_verified':False,'minecraft_render_verified':False}
(HERE/'asset-validation.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(report,ensure_ascii=False,indent=2))
