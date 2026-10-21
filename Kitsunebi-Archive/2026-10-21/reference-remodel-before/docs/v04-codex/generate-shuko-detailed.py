"""NeoForge-only Shuko detail pass. Original shared geometry and palette stay intact."""
from pathlib import Path
import json, math
ROOT=Path(__file__).resolve().parents[2]
data=json.loads((ROOT/'common-1.21.1/src/main/resources/assets/kitsunebi/codex/v04/armor_geometry.json').read_text(encoding='utf-8-sig'))
bones={b['name']:b for b in data['bones']}
def box(x,y,z,w,h,d,m):
    return dict(from_=[x,y,z],size=[w,h,d],material=m)
def B(*args):
    b=box(*args);b['from']=b.pop('from_');return b
def child(name,parent,pivot,rotation,boxes):
    b=dict(name=name,parent=parent,pivot=pivot,rotation=[math.radians(a) for a in rotation],boxes=boxes)
    data['bones'].append(b);bones[name]=b
def setboxes(name,boxes):bones[name]['boxes']=boxes
# Leave a one-texel gutter around the largest native box UV net too.
setboxes('ears',[B(-4.1,-8.1,-3.5,8.2,.3,7,1)])
# Short, broad tapered fox ears; retain all head animation anchors.
for side,sign in [('right',-1),('left',1)]:
    ear=bones[side+'_ear'];ear['pivot']=[sign*2.75,-7.8,0];ear['rotation']=[0,0,sign*.15]
    ear['boxes']=[B(-1.75,-2.5,-.9,3.5,2.6,1.8,2),B(-1.35,-3.6,-.8,2.7,1.1,1.6,2),B(-1,-4.5,-.7,2,.9,1.4,1),B(-.55,-5.1,-.6,1.1,.6,1.2,1),B(-.85,-2.65,-1,1.7,2.4,.2,10)]
bones['temple_mask']['pivot']=[4.35,-4.4,-4.9]
bones['temple_mask']['rotation']=[0,-.32,.10]
for cube in bones['temple_mask']['boxes']:
    if cube['material']==0:cube['material']=13  # Masks stay porcelain white on Kokko too.
# Wrapping cloth sits outside both wide and slim skin overlays.
setboxes('kimono',[B(-4.3,-.3,-2.4,8.6,12.6,4.8,0)])
setboxes('haori',[B(-4.45,.1,2.42,8.9,8.8,.45,0),B(-4.5,1.1,2.92,9,1.2,.4,1)])
# Continuous tilted strips replace coplanar stair-step collar cubes.
for name,x,angle,z in [('inner',-2.8,-29,-2.75),('outer',2.8,29,-3.08)]:
    length=4.6 if name=='inner' else 7.8
    child('collar_'+name,'kimono',[x,.15,z],[0,0,angle],[B(-.65,0,-.15,1.3,length,.3,1),B(-.32,0,-.33,.64,length,.12,13)])
for side,sign in [('right',-1),('left',1)]:
    x=sign*3.4
    child(side+'_shoulder_drape','haori',[x,.8,-2.9],[0,0,-sign*18],[B(-1,0,-.1,2,1.15,5.9,1),B(-1,.95,-.2,2,.24,6.1,4)])
    # Faceted bell, recessed slit, ring and five-strand tassel.
    child(side+'_bell','haori',[x,1.35,-3.8],[0,0,0],[B(-.7,-.7,-.35,1.4,1.4,.8,4),B(-.95,-.4,-.3,1.9,.8,.7,4),B(-.4,-.95,-.3,.8,1.9,.7,4),B(-.12,-.2,-.48,.24,.65,.12,9),B(-.3,.9,-.15,.6,.5,.45,14),B(-.17,1.4,-.12,.34,1.1,.35,4),B(-.55,2.4,-.3,1.1,.5,.65,4)]+[B(-.52+i*.22,2.92,-.25,.18,1.7+(i%2)*.2,.5,8) for i in range(5)])
    # Hollow hanging double sleeves: four walls leave the hand visible.
    left=-3.4 if sign<0 else -1.35
    shell=[B(left,-2.3,-2.5,4.75,6.2,5,0)]
    lo=left-.65; width=6.05
    for y,h,mat,z,width_delta in [(3.9,4.7,0,3.05,0),(8.6,1.8,1,3.05,0),(10.4,.35,4,3.05,0)]:
        shell += [B(lo,y,-z,width,h,.36,mat),B(lo,y,z-.36,width,h,.36,mat),B(lo,y,-z,.36,h,2*z,mat),B(lo+width-.36,y,-z,.36,h,2*z,mat)]
    # Inner red lining visible around the opening, not across the fingers.
    shell += [B(lo+.38,9.9,-2.66,width-.76,.45,.18,3),B(lo+.38,9.9,2.48,width-.76,.45,.18,3)]
    setboxes(side+'_sleeve',shell)
    child(side+'_sleeve_drape',side+'_sleeve',[-.8 if sign<0 else .8,-.2,-2.85],[0,0,-sign*12],[B(-2.15,0,-.12,4.3,.85,.3,1),B(-2.15,.85,-.14,4.3,.18,.32,4)])
    setboxes(side+'_hakama',[B(-2.35,-.3,-2.7,4.7,8.1,5.4,5)]+[B(-2.2+i*1.05,0,-2.88,.44,7.7,.18,12) for i in range(5)])
    setboxes(side+'_coat',[B(-2.4,0,-3.1,4.8,6.5,.3,1),B(-2.4,6.5,-3.1,4.8,.35,.3,4),B(-2.4,0,-3.2,.22,6.5,.12,4),B(2.18,0,-3.2,.22,6.5,.12,4)])
    for i in range(3):
        child(side+'_shide_'+str(i),side+'_coat',[-1.1+i*.34,.7+i*1.35,-3.35],[0,0,(-1 if i%2 else 1)*23],[B(-.32,0,-.12,.64,1.65,.24,13)])
    setboxes(side+'_tabi',[B(-2.3,7.2,-2.3,4.6,4.6,4.6,13),B(-2.35,11.3,-3.4,4.7,1,5.75,9),B(-2.38,10.9,-3.43,4.76,.4,5.81,1),B(-1.7,10.2,-3.45,3.4,.55,.45,1),B(-.35,10.15,-3.5,.7,.65,2.6,1)])
setboxes('obi',[B(-4.55,8.4,-2.95,9.1,2.2,5.9,5),B(-.65,9,-4,1.3,1.2,.8,8),B(-1.3,9.25,-3.9,.65,.65,.65,4),B(.65,9.25,-3.9,.65,.65,.65,4)])
for i in range(12):
    child('braid_'+str(i),'obi',[-4.2+i*.73,9.3,-3.35-(.08 if i%2 else 0)],[0,0,32 if i%2 else -32],[B(-.32,-.5,-.2,.64,1,.4,4 if i%2 else 8)])
for sign in [-1,1]:
    child('hanging_cord_'+str(sign),'obi',[sign*.45,10.1,-3.7],[0,0,sign*-12],[B(-.16,0,-.12,.32,3.1,.3,4),B(-.48,3,-.25,.96,.55,.55,14)]+[B(-.45+i*.19,3.55,-.2,.15,1.65,.45,8) for i in range(5)])
data['skin_clearance_applied']=True
data['design']='Shuko detail pass: reference silhouette, existing palette and tail hierarchy'
output=Path(__file__).parent/'models/shuko_detailed.json';output.parent.mkdir(exist_ok=True)
output.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(f'Wrote {output}: {len(data["bones"])} bones')
