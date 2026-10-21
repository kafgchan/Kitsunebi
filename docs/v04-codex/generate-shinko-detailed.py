"""Reference-authored divine fox armor: separate geometry, not a palette swap.

Units are Minecraft model pixels. Static decorations inherit the existing armor
anchors, while nine sculpted tails keep the shared growth/animation contract.
"""
from pathlib import Path
import json, math, struct, zlib
ROOT=Path(__file__).resolve().parents[2]
OUT=Path(__file__).parent
A=ROOT/'common-1.21.1/src/main/resources/assets/kitsunebi'
bones=[];byname={}
def B(x,y,z,w,h,d,m):return {'from':[x,y,z],'size':[w,h,d],'material':m}
def bone(name,parent='root',pivot=(0,0,0),rotation=(0,0,0),boxes=()):
 b={'name':name,'parent':parent,'pivot':list(pivot),'rotation':[math.radians(v) for v in rotation],'boxes':list(boxes)}
 assert name not in byname,name
 bones.append(b);byname[name]=b;return b
def add(name,*boxes):byname[name]['boxes'].extend(boxes)
def bar(name,parent,a,b,width,depth,material):
 dx,dy=b[0]-a[0],b[1]-a[1];length=math.hypot(dx,dy)
 return bone(name,parent,((a[0]+b[0])/2,(a[1]+b[1])/2,a[2]),(0,0,math.degrees(math.atan2(dy,dx))),[B(-length/2,-width/2,-depth/2,length,width,depth,material)])
def jewel(name,parent,pivot,r=.85):
 bone(name,parent,pivot,boxes=[B(-r,-r,-.27,2*r,2*r,.55,14)])
 for i in range(8):
  angle=i*math.tau/8
  bone(name+'_rim_'+str(i),name,(math.cos(angle)*r,math.sin(angle)*r,-.35),(0,0,i*45+90),[B(-r*.45,-.18,-.18,r*.9,.36,.36,8 if i%2 else 4)])
 bone(name+'_stone',name,(0,0,-.59),(0,0,45),[B(-r*.50,-r*.50,-.17,r,r,.34,18),B(-r*.29,-r*.29,-.28,r*.58,r*.58,.12,16),B(-r*.13,-r*.30,-.34,r*.20,r*.20,.08,17)])
def tassel(name,parent,pivot,length=2.6,scale=1):
 bone(name,parent,pivot,boxes=[B(-.18*scale,0,-.18*scale,.36*scale,.6*scale,.36*scale,14),B(-.36*scale,.55*scale,-.3*scale,.72*scale,.48*scale,.6*scale,4)])
 for i in range(7):
  add(name,B((i-3)*.13*scale-.045,1.0*scale,-.2*scale,.10*scale,length+(i%2)*.12,.4*scale,8 if i%2 else 19))
def flame(name,parent,pivot,scale=1):
 bone(name,parent,pivot,boxes=[B(-.48*scale,-.8*scale,-.35*scale,.96*scale,1.15*scale,.7*scale,18),B(-.32*scale,-1.45*scale,-.28*scale,.64*scale,.7*scale,.56*scale,16),B(-.13*scale,-1.95*scale,-.16*scale,.26*scale,.6*scale,.32*scale,17),B(-.17*scale,-.8*scale,-.44*scale,.34*scale,.95*scale,.2*scale,17)])
 for sign in [-1,1]:
  bone(name+'_tongue_'+str(sign),name,(sign*.38*scale,-.35*scale,0),(0,0,sign*28),[B(-.12*scale,-.7*scale,-.2*scale,.24*scale,.9*scale,.4*scale,16)])
for name,pivot in [('head',(0,0,0)),('hat',(0,0,0)),('body',(0,0,0)),('right_arm',(-5,2,0)),('left_arm',(5,2,0)),('right_leg',(-1.9,12,0)),('left_leg',(1.9,12,0))]:bone(name,pivot=pivot)

# Head: gold nimbus, azure-tipped tapered ears, crown and porcelain temple mask.
bone('ears','head',boxes=[B(-4.2,-8.15,-3.4,8.4,.35,6.8,4),B(-4.22,-8.23,-3.45,8.44,.12,6.9,8)])
bone('divine_halo','ears',(0,-4.4,3.45))
for i in range(32):
 angle=i*math.tau/32
 bone('halo_arc_'+str(i),'divine_halo',(5.65*math.cos(angle),5.65*math.sin(angle),.035*(i%2)),(0,0,i*11.25+90),[B(-.59,-.25,-.3,1.18,.5,.6,4),B(-.49,-.08,-.37,.98,.16,.07,19)])
for i in range(8):
 angle=i*math.tau/8
 jewel('halo_star_'+str(i),'divine_halo',(5.7*math.cos(angle),5.7*math.sin(angle),-.12),.40)
bone('crown','ears',(0,-9.1,-.5),boxes=[B(-1.5,-.1,-.5,3,.42,1.0,4),B(-1.0,-.7,-.42,2,.6,.84,8),B(-.4,-1.45,-.35,.8,.75,.7,19)])
for side,sign in [('right',-1),('left',1)]:
 bone(side+'_ear','ears',(sign*2.8,-7.9,0),(0,0,sign*13))
 for tier,(w,y,h) in enumerate([(3.5,-2.4,2.5),(2.9,-3.5,1.1),(2.2,-4.4,.9),(1.45,-5.15,.75),(.7,-5.65,.5)]):
  add(side+'_ear',B(-w/2,y,-.88,w,h,1.76,18 if tier>2 else 2))
  if tier<4:add(side+'_ear',B(-w*.32,y+.12,-1.03,w*.64,max(.25,h-.15),.16,10 if tier<2 else 16))
 bone(side+'_ear_cuff',side+'_ear',(0,-.6,0),boxes=[B(-1.86,-.16,-1.08,3.72,.32,2.16,4),B(-1.75,-.02,-1.17,3.5,.1,.1,8)])
 jewel(side+'_ear_pin',side+'_ear',(sign*1.4,-1,-1.14),.38)
 tassel(side+'_head_tassel','ears',(sign*4.5,-5.8,1),2.1,.7)
bone('temple_mask','ears',(4.45,-4.5,-4.95),(0,-19,7),[B(-1.65,-2.2,-.42,3.3,3.5,.84,13),B(-1.2,1.3,-.5,2.4,.75,.8,13),B(-.75,2.05,-.38,1.5,.42,.65,2),B(-1.55,-3.35,-.25,.9,1.15,.7,18),B(.65,-3.35,-.25,.9,1.15,.7,18),B(-1.25,-2.98,-.34,.35,.65,.2,16),B(.9,-2.98,-.34,.35,.65,.2,16),B(-1.32,-.9,-.73,.8,.24,.25,11),B(.52,-.9,-.73,.8,.24,.25,11),B(-.27,.05,-.94,.54,.44,.4,18),B(-.46,.85,-.72,.92,.17,.2,18)])
for side,sign in [('right',-1),('left',1)]:
 bar('mask_brow_'+side,'temple_mask',(sign*.46,-1.5,-.69),(sign*1.38,-1.25,-.69),.23,.18,18)
 for i in range(3):
  bone('mask_cheek_'+side+str(i),'temple_mask',(sign*(.85+i*.19),.0+i*.32,-.73),(0,0,sign*-22),[B(-.26,-.08,-.12,.52,.16,.24,18)])
jewel('mask_bindi','temple_mask',(0,-1.88,-.57),.25)

# Body: layered kimono collars, cloud embroidery, separate shoulder jewelry.
bone('kimono','body',boxes=[B(-4.32,-.32,-2.42,8.64,12.64,4.84,0)])
bone('haori','body',boxes=[B(-4.48,.15,2.5,8.96,8.3,.4,0),B(-4.5,6.5,2.94,9,1.8,.34,18),B(-4.52,8.3,2.92,9.04,.25,.4,4)])
for sign in [-1,1]:
 add('haori',B(-4.4 if sign<0 else 3.12,2.5,-2.82,1.28,5.9,.30,5),B(-3.18 if sign<0 else 3.12,2.7,-2.94,.14,5.6,.12,4))
 bar('back_sash_'+str(sign),'haori',(sign*3.4,1.0,3.10),(sign*.25,5.8,3.10),.95,.24,5)
 bar('back_sash_gold_'+str(sign),'haori',(sign*3.4,1.0,3.32),(sign*.25,5.8,3.32),.14,.12,4)
 tassel('back_shoulder_tassel_'+str(sign),'haori',(sign*3.15,3.5,3.65),1.8,.65)
for name,x,angle,z,length in [('inner',-2.8,-29,-2.8,4.7),('outer',2.8,29,-3.18,8.1)]:
 bone('divine_collar_'+name,'kimono',(x,.1,z),(0,0,angle),[B(-.80,0,-.18,1.60,length,.36,5),B(-.74,0,-.31,.14,length,.12,4),B(.60,0,-.31,.14,length,.12,8),B(-.28,0,-.32,.56,length,.12,13)])
 for i in range(7):add('divine_collar_'+name,B(.41,.35+i*.95,-.39,.10,.35,.06,19))
for side,sign in [('right',-1),('left',1)]:
 jewel(side+'_chest_gem','haori',(sign*3.45,1.4,-3.65),.83)
 tassel(side+'_chest_tassel','haori',(sign*3.5,2.6,-3.58),2.3,.85)
 # Curved drape, stepped chain links and short hanging blue ribbon.
 for i in range(6):
  x=sign*(3.8+i*.34);y=1.6+.26*math.sin(i*.52)
  bone(side+'_drape_'+str(i),'haori',(x,y,-2.88),(0,0,-sign*(12+i*2)),[B(-.24,-.18,-.18,.48,.36,5.7,18),B(-.24,.18,-.25,.48,.12,5.84,4)])
 for i in range(5):
  x=sign*(2.8+i*.4);y=3.0+.35*math.sin(i*.65)
  bone(side+'_chain_'+str(i),'haori',(x,y,-3.0),(0,0,(i%2)*45),[B(-.21,-.13,-.16,.42,.26,.32,8)])
 # Embroidered cloud curls, each surface slightly ahead of its underlay.
 for motif in range(2):
  for j in range(5):
   angle=j*math.pi/4
   add('kimono',B(sign*(2.0+motif*.8)+math.cos(angle)*.28,5.8+motif*1.3+math.sin(angle)*.28,-2.60-motif*.02,.18,.18,.12,8))

# Arms: faceted gold shoulder armor and flared hollow, double-layer bell sleeves.
for side,sign in [('right',-1),('left',1)]:
 center=sign*1.0
 bone(side+'_sleeve',side+'_arm',boxes=[B(center-2.38,-2.32,-2.5,4.76,5.52,5,0)])
 bone(side+'_pauldron',side+'_sleeve',(center,-1.85,0),boxes=[B(-2.8,-.1,-3.0,5.6,.6,6,5),B(-2.5,-.85,-2.75,5, .75,5.5,4),B(-2.05,-1.4,-2.2,4.1,.55,4.4,8),B(-1.45,-1.7,-1.7,2.9,.3,3.4,19)])
 for i in range(7):
  add(side+'_pauldron',B(-2.35+i*.7,-.74,-2.86,.18,.42,.18,14),B(-2.35+i*.7,-.74,2.68,.18,.42,.18,14))
 jewel(side+'_shoulder_gem',side+'_pauldron',(sign*1.5,-.5,-3.14),.42)
 tassel(side+'_shoulder_tassel',side+'_pauldron',(sign*2.6,.4,-2.6),1.8,.65)
 for i in range(9):
  angle=math.pi*i/8
  bone(side+'_shoulder_rope_'+str(i),side+'_pauldron',(-2.2+i*.55,.45+math.sin(angle)*.8,-3.15-.04*(i%2)),(0,0,(-1 if i%2 else 1)*28),[B(-.27,-.16,-.18,.54,.32,.36,4 if i%2 else 8)])
 for tier,(y,h,w,d) in enumerate([(3.2,2.0,5.25,5.4),(5.2,2.1,6.1,6.15),(7.3,1.9,7.0,6.9),(9.2,.85,7.7,7.5)]):
  lo=center-w/2;z=d/2;mat=0 if tier<3 else 5
  for face in [B(lo,y,-z,w,h,.28,mat),B(lo,y,z-.28,w,h,.28,mat),B(lo,y,-z,.28,h,d,mat),B(lo+w-.28,y,-z,.28,h,d,mat)]:add(side+'_sleeve',face)
  # Gold seams on the outer corners, never coplanar with the white shell.
  add(side+'_sleeve',B(lo-.025,y,-z-.055,.57,h,.14,5),B(lo+w-.545,y,-z-.055,.57,h,.14,5),B(lo+.025,y,-z-.14,.14,h,.08,4),B(lo+w-.165,y,-z-.14,.14,h,.08,8))
 # A deeper trailing panel behind the hand creates the hanging sleeve silhouette.
 bone(side+'_sleeve_train',side+'_sleeve',(center,5.9,2.9),(sign*3,0,-sign*5),[B(-3.3,0,-.2,6.6,4.9,.4,0),B(-3.36,4.9,-.24,6.72,.45,.48,5),B(-3.4,5.35,-.28,6.8,.22,.56,4)])
 w=7.78;lo=center-w/2
 for y,h,m in [(9.15,.2,4),(10.05,.28,8),(10.33,.16,14)]:
  add(side+'_sleeve',B(lo,y,-3.8,w,h,.16,m),B(lo,y,3.64,w,h,.16,m),B(lo,y,-3.8,.16,h,7.6,m),B(lo+w-.16,y,-3.8,.16,h,7.6,m))
 for i in range(7):
  x=lo+.6+i*1.02;h=.65+(i%3)*.35
  add(side+'_sleeve',B(x,9.15-h,-3.56,.43,h,.22,18),B(x+.12,9.02-h,-3.66,.20,h*.6,.12,16))
  bone(side+'_cuff_flame_'+str(i),side+'_sleeve',(x+.2,9.15-h,-3.6),(0,0,45),[B(-.12,-.12,-.1,.24,.24,.2,17)])

# Waist: woven rope in two strands, central gem clasp and three-dimensional knots.
bone('obi','body',boxes=[B(-4.6,8.4,-3.03,9.2,2.7,6.06,5),B(-4.62,8.3,-3.09,9.24,.22,6.18,4),B(-4.62,11.1,-3.09,9.24,.22,6.18,4)])
for strand in range(2):
 for i in range(17):
  bone('rope_'+str(strand)+'_'+str(i),'obi',(-4.25+i*.53,9.1+strand*.80,-3.38-.065*((i+strand)%2)),(8 if i%2 else -8,12 if i%2 else -12,32 if (i+strand)%2 else -32),[B(-.27,-.39,-.32,.54,.78,.64,8 if (i+strand)%2 else 4)])
jewel('divine_waist_gem','obi',(0,9.9,-3.9),.85)
for sign in [-1,1]:
 for i in range(5):
  a=(sign*(.65+i*.30),10.65+i*.3,-3.75)
  bone('waist_knot_'+str(sign)+'_'+str(i),'obi',a,(0,0,sign*30),[B(-.28,-.23,-.25,.56,.46,.5,8 if i%2 else 4)])
 bar('waist_cord_'+str(sign),'obi',(sign*.75,10.8,-3.82),(sign*1.65,14.0,-3.82),.25,.32,4)
 tassel('waist_tassel_'+str(sign),'obi',(sign*1.65,13.8,-3.82),1.7,.78)
bone('back_knot','obi',(0,10.0,3.4),boxes=[B(-1.5,-.5,-.1,3,1,.75,4),B(-.6,-.8,.6,1.2,1.6,.7,8)])
for i in range(7):
 angle=math.pi*i/6
 bone('back_rope_'+str(i),'obi',(math.cos(angle)*2.6,10+math.sin(angle)*1.1,3.25),(0,0,i*25),[B(-.45,-.28,-.2,.9,.56,.4,4)])

# Long pleated hakama, split outer coat panels, paper streamers and raised geta.
for side,sign in [('right',-1),('left',1)]:
 bone(side+'_hakama',side+'_leg')
 for tier,(y,h,w,d) in enumerate([(-.3,3.2,4.7,5.6),(2.9,3.1,5.15,6.0),(6.0,2.8,5.7,6.5)]):
  z=-d/2-(.065 if sign>0 else 0)
  add(side+'_hakama',B(-w/2,y,z,w,h,d,5))
  for i in range(6):
   x=-w/2+.24+i*(w-.55)/5
   add(side+'_hakama',B(x,y,z-.17,.28,h,.18,12 if i%2 else 22))
  if tier==2:
   add(side+'_hakama',B(-w/2,y+h-.65,z-.22,w,.36,.16,4),B(-w/2,y+h-.29,z-.25,w,.16,.20,8))
 z0=-3.28-(.065 if sign>0 else 0)
 bone(side+'_coat',side+'_leg',boxes=[B(-2.3,0,z0,4.6,4.8,.32,5),B(-1.35,0,z0-.10,2.7,4.8,.12,0),B(-1.48,0,z0-.19,.14,4.8,.10,4),B(1.34,0,z0-.19,.14,4.8,.10,4)])
 for tier,(y,h,w) in enumerate([(4.8,1.8,4.85),(6.6,.8,5.15)]):
  z=-3.38-(.065 if sign>0 else 0)
  add(side+'_coat',B(-w/2,y,z,w,h,.35,5),B(-(w-1.8)/2,y,z-.12,w-1.8,h,.14,0),B(-(w-1.8)/2-.12,y,z-.22,.14,h,.10,4),B((w-1.8)/2-.02,y,z-.22,.14,h,.10,8))
 add(side+'_coat',B(-2.63,7.4,-3.56,5.26,.42,.42,5),B(-2.67,7.82,-3.6,5.34,.20,.48,4))
 for i in range(7):
  x=-2.3+i*.7
  bone(side+'_coat_seal_'+str(i),side+'_coat',(x,6.95,-3.66),(0,0,45),[B(-.15,-.15,-.1,.30,.30,.2,8)])
 for chain in range(2):
  for i in range(4):
   bone(side+'_paper_'+str(chain)+'_'+str(i),side+'_coat',(-1.95+chain*3.5+(.12 if i%2 else 0),.65+i*1.25,-3.7-chain*.035),(0,0,(1 if i%2 else -1)*27),[B(-.27,0,-.12,.54,1.35,.24,13),B(-.20,.12,-.19,.12,.75,.07,19)])
 bone(side+'_tabi',side+'_leg',boxes=[B(-2.31,8.0,-2.32,4.62,3.8,4.64,13),B(-2.48,11.0,-3.55,4.96,.45,5.95,5),B(-2.50,11.45,-3.57,5,.35,5.99,4),B(-2.48,11.8,-3.55,4.96,.4,5.95,9),B(-1.85,10.25,-3.50,3.7,.5,.4,18),B(-.32,10.15,-3.52,.64,.65,2.8,18)])
 for z in [-2.6,1.2]:add(side+'_tabi',B(-2.15,12.2,z,4.3,.30,.55,9))
 for sign2 in [-1,1]:
  add(side+'_tabi',B(sign2*1.65-.18,10.26,-3.64,.36,.4,.20,4))
 for i in range(5):add(side+'_tabi',B(-2.25+i*1.0,11.47,-3.63,.20,.19,.07,19))

# Nine sculpted tails: rounded voxel tufts, gold bindings and azure luminous tips.
for i in range(9):
 root='tail_'+str(i);bone(root,'body',(0,10.1,3.3))
 bone(root+'_base',root,boxes=[B(-.8,-.8,0,1.6,1.6,3.6,2)])
 bone(root+'_mid',root,(0,0,3.0),(2,0,0),[B(-1.35,-1.3,0,2.7,2.6,3.8,2)])
 bone(root+'_end',root+'_mid',(0,0,3.2),(5,0,0),[B(-1.95,-1.75,0,3.9,3.5,3.7,2)])
 bone(root+'_tip',root+'_end',(0,0,3.2),(7,0,0),[B(-1.75,-1.45,0,3.5,2.9,1.4,18),B(-1.3,-1.1,1.4,2.6,2.2,1.1,16),B(-.65,-.65,2.5,1.3,1.3,.8,17)])
 for segment,width,length in [('_mid',2.7,3.7),('_end',3.9,3.6)]:
  for tuft in range(3):
   z=.35+tuft*.95;w=width*(.76+.08*tuft)
   add(root+segment,B(-w/2,-width*.52-.13*tuft,z,w,.25,.85,13),B(-w/2,width*.48,z,w,.20,.85,6))
   for sign in [-1,1]:
    bone(root+segment+'_tuft_'+str(tuft)+'_'+str(sign),root+segment,(sign*(width/2-.08),0,z),(0,sign*-13,0),[B(-.18,-width*.33,0,.36,width*.66,1.2,2)])
 for sign in [-1,1]:
  bone(root+'_tip_tongue_'+str(sign),root+'_tip',(sign*1.4,0,.3),(0,sign*16,0),[B(-.35,-.8,0,.7,1.6,1.8,18),B(-.2,-.5,1.65,.4,1,.7,16)])
 add(root+'_end',B(-2.02,-1.85,.30,4.04,.22,.26,4),B(-2.02,1.63,.30,4.04,.22,.26,4))

# Decorative foxfire follows the armor; it never places blocks or changes combat.
for i,(x,y,z,s) in enumerate([(-10.4,2.5,1.7,.8),(10.4,3.3,1.7,.8),(-8.7,9.4,3.3,.65),(8.7,10.2,3.3,.65)]):
 flame('divine_flame_'+str(i),'haori',(x,y,z),s)
for i,sign in enumerate([-1,1]):flame('divine_head_flame_'+str(i),'ears',(sign*7.1,-3.0,2.5),.60)

data={'format':'kitsunebi.humanoid.v1','texture':'kitsunebi:textures/entity/shinko_shrine_palette.png','texture_size':[256,256],'tail_counts':[1,3,6,9],'skin_clearance_applied':True,'design':'Shinko: reference-specific halo, gems, gold pauldrons, layered flared sleeves, ropes, pleats, geta and sculpted luminous fox tails','bones':bones}
from fox_reference_shapes import refine
refine(data,divine=True)
for b in bones:
 for c in b['boxes']:
  w,h,d=c['size'];assert min(w,h,d)>0 and 1+2*(w+d)<32 and 1+h+d<32,(b['name'],c)
(OUT/'models/shinko_detailed.json').write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
colors=['f5f0e4','bc8c2c','fcfaff','2865ad','d8a83c','11213e','e5dfd4','6597d1','f3ca64','4b3422','b7d9f2','101a30','243a60','fff9e9','876022','182c50','67d9ff','efffff','286cc5','ffe19a','b9c9e1','7186aa','1b3158','d09c37','dcefff','4c9ae7','0f4b93','916928','c8dff2','fff2c9','38689e','88e6ff']
colors+=['f5f0e4']*(64-len(colors))
def chunk(k,d):return struct.pack('>I',len(d))+k+d+struct.pack('>I',zlib.crc32(k+d)&0xffffffff)
for glow in [False,True]:
 raw=bytearray()
 for y in range(256):
  raw.append(0)
  for x in range(256):
   material=(y//32)*8+x//32;rgb=bytes.fromhex(colors[material])
   shade=0 if glow else (-2 if (x//3+y//3)%4==0 else 0)
   raw.extend([max(0,v+shade) for v in rgb]);raw.append(255 if not glow or material in [16,17] else 0)
 name='shinko_shrine_palette'+('_glowmask' if glow else '')+'.png'
 (A/'textures/entity'/name).write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',256,256,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b''))
print(f'Shinko: {len(bones)} bones, {sum(len(b["boxes"]) for b in bones)} cuboids; 256px palette + emissive mask')
