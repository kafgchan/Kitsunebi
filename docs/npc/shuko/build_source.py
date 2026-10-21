"""Create the independent Shuko NPC editing master once. Never edits equipment.

After artistic edits use export_assets.py, which reads the saved .bbmodel.
Rebuilding is deliberately opt-in (--rebuild) to protect manual Blockbench edits.
"""
from pathlib import Path
import json, math, uuid, base64, io, sys
from PIL import Image, ImageDraw

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
OUT = ROOT / 'neoforge-1.21.1/src/main/resources/assets/kitsunebi'
MASTER = HERE / 'shuko.bbmodel'
if MASTER.exists() and '--rebuild' not in sys.argv:
    raise SystemExit('Editing master exists. Use export_assets.py to preserve edits; --rebuild recreates it.')

COLORS = {
 'ivory':'#fff4ee','white':'#fff9f4','cloth':'#f9eee7','cloth_shadow':'#e6d1cc',
 'vermilion':'#c61f35','red':'#aa142d','red_light':'#e83b47','red_dark':'#761c31',
 'gold':'#e8b44d','gold_light':'#ffe3a1','gold_dark':'#9f622b',
 'hair':'#f7e5e5','hair_light':'#fff4ef','hair_shadow':'#dabac3','skin':'#ffe6df',
 'ear_inner':'#efb9c0','ear_tip':'#c8253d','sole':'#51303a','sock':'#fff4ef',
 'eye':'#b62145','mouth':'#a74e59','tail':'#fff2ed','tail_shadow':'#e9ced0',
 'tail_tip':'#c82b42','ribbon':'#b51934','cord':'#ecbc57','bell':'#f6c65c',
 'sleeve_pattern':'#fff6ee','hem_pattern':'#b82138','mask_pattern':'#fff4e9',
 'braid':'#e4ac43','obi_cloth':'#9e2335','panel_pattern':'#c7283c',
 'back_pattern':'#fff4ed','hair_strands':'#f7e8e8','tail_fur':'#fff4ed',
}
TILES = {name:(64+(i%6)*32, (i//6)*32,32,32) for i,name in enumerate(list(COLORS)[:36])}
# The upper-left 64-square is the expression region. Remaining tiles occupy x64..255.
def rgb(c): return tuple(bytes.fromhex(c.lstrip('#')))
def blend(c, delta): return tuple(max(0,min(255,v+delta)) for v in c)
def flower(d,x,y,r=5,color='#d92a41',center='#edc56d'):
    for a in range(0,360,72):
        px=x+math.cos(math.radians(a))*r*.65; py=y+math.sin(math.radians(a))*r*.65
        d.ellipse((px-r*.43,py-r*.43,px+r*.43,py+r*.43),fill=color)
    d.ellipse((x-1,y-1,x+1,y+1),fill=center)
def atlas(expression):
    im=Image.new('RGB',(256,256),'#fff4ee'); d=ImageDraw.Draw(im)
    for name,(x,y,w,h) in TILES.items():
        c=rgb(COLORS[name])
        for row in range(h):
            tone=int(6*math.cos(row/h*math.pi))-3
            d.line((x,y+row,x+w-1,y+row),fill=blend(c,tone))
        if name in ('hair','hair_light','hair_shadow','hair_strands','tail','tail_fur','tail_shadow'):
            for j in range(3,31,5):
                d.line((x+j,y+2,x+j-1,y+27), fill=blend(c,-12),width=1)
                d.line((x+j+1,y+3,x+j+1,y+24), fill=blend(c,5),width=1)
        if name in ('cloth','white','ivory','sock'):
            for j in range(4,30,7): d.line((x+j,y+2,x+j,y+30), fill=blend(c,-5))
        if name in ('vermilion','red','red_light','red_dark','ribbon','obi_cloth'):
            for j in (7,16,25): d.line((x+j,y+2,x+j,y+29),fill=blend(c,-8))
            d.line((x+1,y+1,x+30,y+1),fill=blend(c,12))
        if name in ('gold','gold_light','gold_dark','bell'):
            d.line((x+3,y+2,x+3,y+28),fill='#ffe6a8',width=3)
            d.line((x+28,y+1,x+28,y+30),fill='#a96d2c',width=2)
            if name=='bell':
                d.line((x+3,y+23,x+29,y+23),fill='#82502a',width=2)
                d.ellipse((x+14,y+20,x+18,y+24),fill='#69422a')
                d.line((x+16,y+22,x+16,y+30),fill='#74452b',width=2)
        if name in ('braid','cord'):
            for j in range(-30,40,7):
                d.line((x+max(0,j),y+max(0,-j),x+min(31,j+31),y+min(31,31-j)),fill='#9d632d',width=2)
                d.line((x+max(0,j+2),y+max(0,-j),x+min(31,j+33),y+min(31,31-j)),fill='#ffe5a1',width=2)
        if name in ('sleeve_pattern','back_pattern'):
            d.rectangle((x,y+25,x+31,y+31),fill='#b51c35')
            d.line((x,y+24,x+31,y+24),fill='#e8b655',width=2)
            for xx,yy,rr in ((7,20,5),(22,17,4),(15,9,3)): flower(d,x+xx,y+yy,rr)
            for xx in range(1,30,8):
                d.line((x+xx,y+31,x+xx+4,y+27,x+xx+8,y+31),fill='#fff0dc',width=1)
        if name in ('hem_pattern','panel_pattern'):
            d.line((x+2,y,x+2,y+31),fill='#edc369',width=2)
            d.line((x+29,y,x+29,y+31),fill='#edc369',width=2)
            for yy in (8,22): flower(d,x+16,y+yy,5,'#f8dfcb','#edb95c')
            d.line((x,y+30,x+31,y+30),fill='#fff0df',width=2)
        if name=='mask_pattern':
            d.polygon([(x+3,y+2),(x+10,y+4),(x+14,y+11),(x+9,y+13)],fill='#c22a3c')
            d.polygon([(x+28,y+2),(x+21,y+4),(x+17,y+11),(x+22,y+13)],fill='#c22a3c')
            d.line((x+4,y+17,x+10,y+14,x+13,y+16),fill='#ad1c33',width=2)
            d.line((x+19,y+16,x+22,y+14,x+28,y+17),fill='#ad1c33',width=2)
            for yy in (20,23):
                d.line((x+4,y+yy,x+10,y+yy+2),fill='#dc3343',width=2)
                d.line((x+22,y+yy+2,x+28,y+yy),fill='#dc3343',width=2)
            d.polygon([(x+13,y+23),(x+18,y+23),(x+16,y+26)],fill='#aa2637')
            d.line((x+16,y+26,x+16,y+28,x+13,y+29),fill='#a6283a')
            d.line((x+16,y+28,x+19,y+29),fill='#a6283a')
            d.polygon([(x+16,y+3),(x+13,y+9),(x+16,y+13),(x+19,y+9)],fill='#bd2337')
    # Opaque expression artwork, matching all six PNG layouts and UVs.
    d.rectangle((0,0,63,63),fill='#ffe9e2')
    for yy in range(64):
        d.line((0,yy,63,yy),fill=(255,234-int(yy*.08),227-int(yy*.1)))
    for x in (13,49):
        d.ellipse((x-6,43,x+6,48),fill='#f7b7bd')
        d.line((x-3,44,x-3,46),fill='#f1a6b1')
        d.line((x+1,44,x+1,46),fill='#f1a6b1')
    for cx in (18,45):
        if expression=='happy':
            d.line((cx-6,36,cx,32,cx+6,36),fill='#843246',width=2)
        else:
            top=29 if expression=='surprised' else 32
            bottom=43 if expression=='surprised' else 42
            d.rounded_rectangle((cx-6,top,cx+6,bottom),radius=3,fill='#fff7f2')
            d.ellipse((cx-4,top,cx+4,bottom+1),fill='#c2294c')
            d.ellipse((cx-3,top+4,cx+3,bottom),fill='#e95169')
            d.rectangle((cx-1,top+2,cx+1,bottom-2),fill='#7b1b3f')
            d.rectangle((cx-3,top+1,cx-1,top+3),fill='#fff7f0')
            d.point((cx+3,bottom-2),fill='#ffd9d6')
            if expression=='serious':
                off=2 if cx==18 else -2
                d.line((cx-7,top-1-off,cx+7,top-1+off),fill='#6b3544',width=2)
            else: d.line((cx-7,top,cx-2,top-2,cx+6,top),fill='#773442',width=2)
            brow=25 if expression=='worried' else 27
            slope=-3 if cx==18 else 3
            if expression=='worried': d.line((cx-5,brow-slope,cx+5,brow+slope),fill='#c4939c')
    if expression in ('smile','happy'):
        d.arc((27,45,36,52),0,180,fill='#a34456',width=1)
        if expression=='happy': d.ellipse((29,47,35,51),fill='#bf6070')
    elif expression=='surprised': d.ellipse((29,46,34,52),fill='#b95a6a')
    elif expression=='worried': d.arc((28,49,35,54),190,350,fill='#aa5667')
    else: d.line((29,49,34,49),fill='#b56573')
    return im

elements=[]; bones={}; order=[]
def uid(): return str(uuid.uuid4())
def bone(name,parent='body',pivot=(0,0,0),rotation=(0,0,0)):
    b={'name':name,'uuid':uid(),'origin':list(pivot),'rotation':list(rotation),'children':[],'export':True,'isOpen':True,'visibility':True,'autouv':0}
    bones[name]=b;order.append(name)
    if parent: bones[parent]['children'].append(b)
    return name
def cube(name,b,at,size,mat='white',rot=(0,0,0),pivot=None,front=None):
    end=[at[i]+size[i] for i in range(3)]
    e={'name':name,'uuid':uid(),'type':'cube','from':list(at),'to':end,'origin':list(pivot or [(at[i]+end[i])/2 for i in range(3)]),'rotation':list(rot),'rescale':False,'autouv':0,'color':0,'locked':False,'visibility':True,'export':True,'faces':{}}
    for f in ('north','south','east','west','up','down'):
        region=(0,0,64,64) if (f=='north' and front=='face') else TILES.get(front if f=='north' and front else mat,TILES['white'])
        x,y,w,h=region;e['faces'][f]={'uv':[x,y,x+w,y+h],'texture':0}
    elements.append(e);bones[b]['children'].append(e['uuid']);return e
def centered(name,b,c,s,mat,**kw): return cube(name,b,[c[i]-s[i]/2 for i in range(3)],s,mat,**kw)
def rod(name,b,a,z,w,mat):
    # A rectangular strand in the XY plane; all endpoints are authored explicitly.
    dx=z[0]-a[0];dy=z[1]-a[1];length=math.hypot(dx,dy)
    centered(name,b,[(a[i]+z[i])/2 for i in range(3)],(w,length,w),mat,rot=(0,0,-math.degrees(math.atan2(dx,dy))))

bone('root',None)
bone('body','root',(0,12,0))
bone('neck','body',(0,21,0))
bone('head','neck',(0,21.7,0))
bone('hair','head',(0,26,0))
for n,p in [('hair_fringe',(0,29, -3.1)),('hair_back',(0,26,2.6)),('hair_left',(4,26,0)),('hair_right',(-4,26,0))]:bone(n,'hair',p)
for s in (-1,1):
    side='left' if s==1 else 'right'
    bone('ear_'+side,'head',(s*3.2,29,0))
bone('mask','head',(4.6,27.2,-.9))
bone('mask_ribbon','mask',(5.5,27,0))
cube('face soft plane','head',(-4.4,21.5,-3.1),(8.8,8.0,6.2),'skin',front='face')
cube('chin','head',(-3.7,21.1,-2.85),(7.4,.65,5.7),'skin')
cube('neck','neck',(-1.15,20.4,-1.1),(2.3,1.3,2.2),'skin')
cube('crown','hair',(-4.8,28.2,-3.3),(9.6,1.9,6.8),'hair_light')
cube('crown rear volume','hair',(-4.35,29.7,-2.7),(8.7,.65,5.5),'hair')
for i in range(9):
    x=-4.5+i*1.02; end=25.8+(.6 if i in (0,8) else .3 if i in (3,4) else 0)
    cube('layered fringe %02d'%i,'hair_fringe',(x,end,-3.53),(1.12,29.2-end,.64),'hair_light' if i%3 else 'hair',rot=(0,0,(-5+i*1.3)))
    cube('fringe tapered tip %02d'%i,'hair_fringe',(x+.12,end-.38,-3.55),(.7,.6,.54),'hair',rot=(0,0,-8+i*2))
for s in (-1,1):
    side='left' if s==1 else 'right'
    for j in range(5):
        z=-2.85+j*1.3; bottom=21.5+(.35 if j%2 else 0)
        centered('short bob '+side+str(j),'hair_'+side,(s*4.6,(bottom+28.8)/2,z),(1.2,28.8-bottom,1.5),'hair' if j%2 else 'hair_light',rot=(0,0,s*(3+j)))
        centered('bob pointed lock '+side+str(j),'hair_'+side,(s*4.45,bottom-.2,z),(.78,.8,1.1),'hair_shadow',rot=(0,0,s*18))
    # Tall two-layer ears with a white body and vermilion extremity, as in the full view.
    b='ear_'+side
    centered('ear outer base '+side,b,(s*3.2,30.45,.25),(2.4,2.8,1.7),'white',rot=(0,0,-s*13))
    centered('ear outer taper '+side,b,(s*3.55,31.9,.25),(1.65,1.6,1.35),'ear_tip',rot=(0,0,-s*15))
    centered('ear point '+side,b,(s*3.75,32.8,.25),(.7,1.0,1.0),'ear_tip',rot=(0,0,-s*14))
    centered('ear inset '+side,b,(s*3.22,30.65,-.67),(1.35,2.4,.15),'ear_inner',rot=(0,0,-s*13))
    centered('ear inner white tuft '+side,b,(s*3.0,29.8,-.79),(.75,.65,.13),'white',rot=(0,0,s*18))
for i in range(9):
    x=-4.4+i*1.03; h=7.2+(.3 if i%2 else 0)
    cube('back bob strand %02d'%i,'hair_back',(x,21.8,2.6),(1.14,h,1.15),'hair' if i%2 else 'hair_light',rot=(0,0,(i-4)*1.4))
    cube('back bob tip %02d'%i,'hair_back',(x+.15,21.3,2.8),(.8,.9,.85),'hair_shadow',rot=(0,0,(i-4)*3))
# The character's left side is the screen-right side in the frontal reference.
centered('fox mask porcelain','mask',(4.95,27.1,-2.1),(2.8,4.4,.65),'white',rot=(0,-29,-14),front='mask_pattern')
for s in (-1,1):centered('fox mask pointed ear '+str(s),'mask',(4.95+s*.92,29.35,-2.1),(.75,1.15,.7),'ear_tip',rot=(0,-29,s*14-14))
centered('fox mask muzzle','mask',(4.95,25.55,-2.42),(1.4,.9,.5),'white',rot=(0,-29,-14),front='mask_pattern')
for s in (-1,1):
    centered('mask bow wing '+str(s),'mask_ribbon',(5.5+s*.55,27.3,.1),(1.05,1.2,.65),'ribbon',rot=(0,0,s*26))
cube('mask ribbon knot','mask_ribbon',(5.1,26.8,-.4),(.8,.8,.8),'gold')
for j in range(2):
    rod('mask ribbon hanging '+str(j),'mask_ribbon',(5.2+j*.45,26.6,.2),(5.1+j*.6,23.5,.2),.28,'ribbon')
    centered('mask ribbon gold end '+str(j),'mask_ribbon',(5.1+j*.6,23.35,.2),(.35,.48,.4),'gold')

# Torso and thick overlapping diagonal collar.
cube('upper kimono','body',(-2.75,13.7,-1.75),(5.5,7.3,3.7),'cloth')
cube('back shoulder yoke','body',(-3.3,18.7,.7),(6.6,2.2,1.5),'white')
bone('collar','body',(0,20,-1.9))
for s in (-1,1):
    centered('outer white crossed collar '+str(s),'collar',(s*.88,19.0,-2.03),(1.28,4.9,.42),'white',rot=(0,0,s*32))
    centered('red crossed collar '+str(s),'collar',(s*.67,19.12,-2.29),(.56,4.7,.26),'vermilion',rot=(0,0,s*32))
    centered('gold collar piping '+str(s),'collar',(s*.44,19.18,-2.44),(.13,4.5,.16),'gold_light',rot=(0,0,s*32))
cube('chest overlap','body',(-2.3,14.4,-2),(4.6,2,.35),'white')
for s in (-1,1):
    side='left' if s==1 else 'right'
    bone('sleeve_'+side,'body',(s*3,19.6,0))
    bone('hand_'+side,'sleeve_'+side,(s*7.05,16.2,-.3))
    bone('shoulder_bell_'+side,'body',(s*3.25,20.1,-.1))
    bone('shoulder_tassel_'+side,'shoulder_bell_'+side,(s*3.65,18.7,-1.3))
    b='sleeve_'+side
    centered('sleeve shoulder '+side,b,(s*3.9,18.8,.1),(2.6,3.3,3.9),'white',rot=(0,0,s*12))
    centered('wide furisode '+side,b,(s*5.65,15.75,.5),(4.3,5.9,4.3),'cloth',rot=(0,0,s*19))
    centered('sleeve front floral panel '+side,b,(s*5.65,14.5,-1.72),(4.25,3.9,.2),'sleeve_pattern',rot=(0,0,s*19))
    centered('sleeve back floral panel '+side,b,(s*5.65,14.5,2.72),(4.25,3.9,.2),'back_pattern',rot=(0,0,s*19))
    centered('sleeve lower broad red border '+side,b,(s*6.0,12.95,.5),(4.4,.7,4.65),'vermilion',rot=(0,0,s*19))
    centered('sleeve lower gold piping '+side,b,(s*6.1,12.55,.5),(4.45,.13,4.7),'gold',rot=(0,0,s*19))
    centered('red open cuff '+side,b,(s*7.1,16.5,-.05),(.6,2.8,3.9),'red_dark',rot=(0,0,s*19))
    centered('white cuff lip '+side,b,(s*7.35,16.7,-.05),(.25,2.45,3.75),'white',rot=(0,0,s*19))
    centered('cuff inner vermilion '+side,b,(s*7.5,16.6,-.05),(.15,1.9,2.8),'vermilion',rot=(0,0,s*19))
    centered('hand palm '+side,'hand_'+side,(s*7.82,16.3,-.45),(1.25,1.5,1.4),'skin',rot=(0,0,s*12))
    centered('thumb '+side,'hand_'+side,(s*7.47,15.8,-1.1),(.6,.75,.55),'skin',rot=(0,0,s*24))
    for j in range(3): centered('finger '+side+str(j),'hand_'+side,(s*(8.15+j*.08),15.75,-.85+j*.35),(.45,.6,.28),'skin')
    b='shoulder_bell_'+side
    centered('shoulder gold clasp '+side,b,(s*3.2,20.1,-1.1),(1.5,.65,2.4),'gold',rot=(0,0,s*14))
    rod('shoulder red suspension '+side,b,(s*3.25,20.1,-2.1),(s*4.15,18.4,-2.1),.25,'ribbon')
    centered('bell crown '+side,b,(s*4.1,18.5,-2.12),(.7,.4,.7),'gold_dark')
    centered('golden shoulder bell '+side,b,(s*4.1,18.05,-2.12),(1.15,.9,1.05),'bell')
    centered('bell lower rim '+side,b,(s*4.1,17.63,-2.12),(1.2,.15,1.1),'gold_light')
    for j in range(4): rod('shoulder tassel strand '+side+str(j),'shoulder_tassel_'+side,(s*(4.0+j*.1),17.5,-2.0),(s*(4.3+j*.14),15.8,-2.0),.14,'gold')
    rod('sleeve hanging red cord '+side,b,(s*6.5,12.65,-1.7),(s*7.25,10.8,-1.7),.2,'ribbon')
    centered('sleeve cord knot '+side,b,(s*7.25,10.8,-1.7),(.4,.5,.45),'gold')

bone('obi','body',(0,13.7,0))
bone('obi_knot','obi',(0,13.9,-2.6))
for s in (-1,1):bone('obi_tassel_'+('left' if s==1 else 'right'),'obi',(s*2.2,13,-2.2))
cube('red waist sash','obi',(-3.1,12.9,-2.3),(6.2,2.1,4.8),'obi_cloth')
cube('front heavy braided gold rope','obi',(-3.35,13.45,-2.9),(6.7,.88,.78),'braid')
cube('rear heavy braided gold rope','obi',(-3.35,13.45,2.28),(6.7,.88,.78),'braid')
for s in (-1,1):
    centered('side braided wrap '+str(s),'obi',(s*3.2,13.9,0),(.85,.88,5.2),'braid')
    for j in range(3):
        centered('knot interwoven lobe '+str(s)+str(j),'obi_knot',(s*(.5+j*.37),13.9+j*.15,-3.0),(.8,.65,.75),'braid',rot=(0,0,s*(20+j*15)))
    b='obi_tassel_'+('left' if s==1 else 'right')
    rod('gold rope hanging '+str(s),b,(s*.8,13.7,-3.05),(s*2.2,10.3,-2.7),.36,'braid')
    centered('tassel collar '+str(s),b,(s*2.2,10.25,-2.7),(.65,.55,.65),'gold_dark')
    for j in range(5): centered('gold waist tassel '+str(s)+str(j),b,(s*2.2+(j-2)*.14,9.35,-2.7),(.13,1.4,.32),'gold_light' if j%2 else 'gold',rot=(0,0,s*7))
cube('front red knot','obi_knot',(-.45,13.4,-3.5),(.9,1,.6),'ribbon')

bone('skirt','body',(0,12.7,0))
for n,p in [('skirt_front',(0,12.1,-1.7)),('skirt_back',(0,12.1,1.8)),('skirt_left',(2.2,12,0)),('skirt_right',(-2.2,12,0))]:bone(n,'skirt',p)
# Discrete flared, overlapping panels, with physical gold edging.
for i in range(9):
    a=math.radians(i*40);x=math.sin(a)*2.85;z=math.cos(a)*1.95
    b='skirt_back' if z>.7 else 'skirt_front' if z<-.7 else ('skirt_left' if x>0 else 'skirt_right')
    angle=i*40
    centered('under kimono pleat %02d'%i,b,(x,8.9,z),(2.35,7.1,1.1),'vermilion' if i%2==0 else 'red',rot=(0,angle,0))
    centered('flared under hem %02d'%i,b,(x*1.32,5.65,z*1.32),(2.7,1.2,1.2),'hem_pattern',rot=(0,angle,0))
for s in (-1,1):
    b='skirt_left' if s==1 else 'skirt_right'
    centered('white side panel '+str(s),b,(s*3.1,8.8,-.45),(2.0,7.8,3.5),'white',rot=(0,0,s*12))
    centered('white floral side hem '+str(s),b,(s*3.85,5.25,-.48),(2.25,1.8,3.7),'sleeve_pattern',rot=(0,0,s*12))
    centered('gold side panel border '+str(s),b,(s*4.0,8.8,-2.31),(.17,8.1,.18),'gold',rot=(0,0,s*12))
    centered('side red inner pleat '+str(s),b,(s*2.0,8.3,-2.0),(1.0,7.3,.6),'red_dark',rot=(0,0,s*5))
    centered('front gold panel edge '+str(s),'skirt_front',(s*1.22,8.55,-2.72),(.15,7.8,.15),'gold_light',rot=(0,0,s*3))
centered('central vermilion embroidered panel','skirt_front',(0,8.5,-2.7),(2.35,7.6,.48),'panel_pattern')
centered('central white hem trim','skirt_front',(0,4.72,-2.7),(2.5,.3,.65),'white')
centered('back white mantle panel','skirt_back',(0,8.5,2.65),(4.4,7.5,.7),'back_pattern')
# Red back bow visible above the fan-tail attachment.
bone('back_bow','obi',(0,13.8,2.8))
for s in (-1,1):
    centered('back bow loop '+str(s),'back_bow',(s*1.55,14.4,2.85),(2.5,2.0,1.1),'ribbon',rot=(0,0,s*22))
    centered('back bow tail '+str(s),'back_bow',(s*.9,11.85,3.1),(.9,3.4,.5),'vermilion',rot=(0,0,s*15))
    centered('back bow gold trim '+str(s),'back_bow',(s*1.31,10.25,3.1),(.95,.25,.6),'gold')
centered('back bow knot','back_bow',(0,14.2,3.0),(1.15,1.2,1.4),'gold')

for s in (-1,1):
    side='left' if s==1 else 'right';b='leg_'+side
    bone(b,'body',(s*1.45,5.2,0));bone('foot_'+side,b,(s*1.45,1.15,-.4))
    centered('short leg '+side,b,(s*1.45,3.25,0),(1.65,3.5,1.7),'sock')
    centered('tabi foot '+side,'foot_'+side,(s*1.45,1.5,-.55),(2.05,.95,3.25),'sock')
    centered('geta red lacquer '+side,'foot_'+side,(s*1.45,.88,-.55),(2.45,.45,3.7),'vermilion')
    centered('geta gold rim '+side,'foot_'+side,(s*1.45,.65,-.55),(2.5,.13,3.75),'gold')
    for zz in (-1.55,.5):centered('geta wooden tooth '+side+str(zz),'foot_'+side,(s*1.45,.3,zz),(2.05,.6,.55),'sole')
    for ss in (-1,1):centered('red hanao strap '+side+str(ss),'foot_'+side,(s*1.45+ss*.43,2.01,-.75),(.36,.24,1.65),'ribbon',rot=(0,ss*24,0))
    centered('tabi toe split '+side,'foot_'+side,(s*1.45,1.62,-2.19),(.08,.53,.025),'cloth_shadow')

bone('tail_root','body',(0,10.75,3.15))
centered('tail root exposed white tuft','tail_root',(0,10.7,3.8),(2.3,2.15,2),'tail')
# Nine separate 3-joint chains, laid out in a shallow 3D fan. Each tail is
# tapered on both ends, with individual fur locks rather than a flat plane.
tail_names=[]
for i,ang in enumerate((-79,-59,-40,-20,0,20,40,59,79),1):
    a=math.radians(ang);sx=math.sin(a);cy=math.cos(a)
    reach=12.4 if i%2 else 13.0
    zbase=4.15+(i%3)*.36
    points=[(sx*.55,10.8,zbase),(sx*4.5,10.8+cy*4.1,zbase+1.5),(sx*8.6,10.8+cy*8.4,zbase+2.15),(sx*reach,10.8+cy*reach,zbase+2.0)]
    names=[f'tail_{i:02d}',f'tail_{i:02d}_mid',f'tail_{i:02d}_tip'];tail_names.extend(names)
    for j,b in enumerate(names):bone(b,'tail_root' if j==0 else names[j-1],points[j])
    for j,b in enumerate(names):
        p=points[j];q=points[j+1];dx=q[0]-p[0];dy=q[1]-p[1];dz=q[2]-p[2]
        length=math.sqrt(dx*dx+dy*dy+dz*dz);rot=(-math.degrees(math.atan2(dz,math.hypot(dx,dy))),0,-math.degrees(math.atan2(dx,dy)))
        width=(1.6,2.45,2.2)[j];depth=(1.5,2.15,1.85)[j]
        mid=[p[k]+(q[k]-p[k])*(.27 if j==2 else .5) for k in range(3)]
        centered(f'white tail {i:02d} segment {j}',b,mid,(width,length*(.57 if j==2 else 1)+.3,depth),'tail' if j!=1 else 'tail_fur',rot=rot)
        # Long ridge locks define the faceted plush silhouette.
        for side in (-1,1):
            c=[mid[0]+side*cy*width*.42,mid[1]-side*sx*width*.42,mid[2]+.15]
            centered(f'tail {i:02d} fur lock {j} {side}',b,c,(width*.45,length*(.4 if j==2 else .72),depth*.83),'tail_shadow' if side<0 else 'tail',rot=(rot[0],0,rot[2]+side*8))
        if j==2:
            # Vermilion end with a stepped, not a flat-cut, transition.
            c=[p[k]+(q[k]-p[k])*.73 for k in range(3)]
            centered(f'tail {i:02d} vermilion end',b,c,(1.95,length*.61,2.02),'tail_tip',rot=rot)
            c=[p[k]+(q[k]-p[k])*.98 for k in range(3)]
            centered(f'tail {i:02d} narrow pointed end',b,c,(.85,1.05,1.1),'vermilion',rot=rot)
            for side in (-1,1):
                c=[p[k]+(q[k]-p[k])*.48 for k in range(3)];c[0]+=side*cy*.65;c[1]-=side*sx*.65
                centered(f'tail {i:02d} jagged white transition {side}',b,c,(.55,.8,2.08),'tail',rot=(rot[0],0,rot[2]+side*11))

def anim(name,length,loop=True):
    return {'uuid':uid(),'name':'animation.shuko.'+name,'loop':'loop' if loop else 'once','override':False,'length':length,'snapping':20,'animators':{}}
def keys(a,b,channel,values):
    animator=a['animators'].setdefault(bones[b]['uuid'],{'name':b,'type':'bone','keyframes':[]})
    for t,v in values:
        animator['keyframes'].append({'channel':channel,'data_points':[dict(zip(('x','y','z'),map(str,v)))],'uuid':uid(),'time':t,'color':-1,'interpolation':'catmullrom'})
idle=anim('idle',4)
keys(idle,'body','position',[(0,(0,0,0)),(2,(0,.18,0)),(4,(0,0,0))])
keys(idle,'head','rotation',[(0,(0,0,-1)),(2,(0,1,1)),(4,(0,0,-1))])
for i in range(1,10):
    phase=(i-5)*.2;amount=1.2+(i%3)*.35
    keys(idle,f'tail_{i:02d}','rotation',[(0,(0,phase,-amount)),(2,(.8,-phase,amount)),(4,(0,phase,-amount))])
    keys(idle,f'tail_{i:02d}_mid','rotation',[(0,(0,0,amount*.6)),(2,(0,0,-amount*.6)),(4,(0,0,amount*.6))])
for side,sign in (('left',1),('right',-1)):
    keys(idle,'sleeve_'+side,'rotation',[(0,(0,0,sign*.4)),(2,(0,0,-sign*.5)),(4,(0,0,sign*.4))])
    keys(idle,'shoulder_tassel_'+side,'rotation',[(0,(1,0,0)),(2,(-1,0,0)),(4,(1,0,0))])
walk=anim('walk',1.2)
keys(walk,'body','position',[(0,(0,0,0)),(.3,(0,.28,0)),(.6,(0,0,0)),(.9,(0,.28,0)),(1.2,(0,0,0))])
for side,sign in (('left',1),('right',-1)):
    keys(walk,'leg_'+side,'rotation',[(0,(sign*16,0,0)),(.6,(-sign*16,0,0)),(1.2,(sign*16,0,0))])
    keys(walk,'sleeve_'+side,'rotation',[(0,(-sign*5,0,sign)),(.6,(sign*5,0,-sign)),(1.2,(-sign*5,0,sign))])
    keys(walk,'skirt_'+side,'rotation',[(0,(sign*2,0,0)),(.6,(-sign*2,0,0)),(1.2,(sign*2,0,0))])
for i in range(1,10): keys(walk,f'tail_{i:02d}','rotation',[(0,(1,2,1)),(.6,(-1,-2,-1)),(1.2,(1,2,1))])
talk=anim('talk',2.4)
keys(talk,'head','rotation',[(0,(0,0,0)),(.6,(3,-3,-2)),(1.2,(-2,2,1)),(1.8,(2,0,-1)),(2.4,(0,0,0))])
keys(talk,'sleeve_left','rotation',[(0,(0,0,0)),(.6,(-6,0,-8)),(1.2,(-4,0,-5)),(1.8,(-6,0,-8)),(2.4,(0,0,0))])
keys(talk,'hand_left','rotation',[(0,(0,0,0)),(.6,(0,-8,-8)),(1.2,(0,8,-2)),(1.8,(0,-8,-8)),(2.4,(0,0,0))])
textures=[]
for expression in ('neutral','smile','happy','serious','surprised','worried'):
    im=atlas(expression); buf=io.BytesIO();im.save(buf,format='PNG')
    textures.append({'path':str(OUT / 'textures/entity/npc/shuko'/f'{expression}.png'),'name':expression+'.png','folder':'entity/npc/shuko','namespace':'kitsunebi','id':str(len(textures)),'uuid':uid(),'source':'data:image/png;base64,'+base64.b64encode(buf.getvalue()).decode(),'mode':'bitmap','saved':True,'visible':True,'internal':True,'particle':False,'render_mode':'default','render_sides':'auto','frame_time':1,'frame_order_type':'loop','frame_order':'','width':256,'height':256,'uv_width':256,'uv_height':256})
model={'meta':{'format_version':'4.10','model_format':'bedrock','box_uv':False},'name':'Shuko Independent NPC','model_identifier':'geometry.kitsunebi.shuko','visible_box':[4,3,1.2],'variable_placeholders':'','variable_placeholder_buttons':[],'resolution':{'width':256,'height':256},'elements':elements,'outliner':[bones['root']],'textures':textures,'animations':[idle,walk,talk],'animation_variable_placeholders':''}
HERE.mkdir(parents=True,exist_ok=True)
MASTER.write_text(json.dumps(model,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({'master':str(MASTER),'bones':len(bones),'cubes':len(elements),'height_px':33.3,'animations':[a['name'] for a in model['animations']],'tail_bones':tail_names},ensure_ascii=False,indent=2))
