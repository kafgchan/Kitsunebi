"""Reference-driven shrine outfit. Native cuboids and palette; no gameplay edits."""
from pathlib import Path
import json,math,struct,zlib,shutil
ROOT=Path(__file__).resolve().parents[2];A=ROOT/'common-1.21.1/src/main/resources/assets/kitsunebi';OUT=ROOT/'docs/v04-codex'
# Reuse only authoring utilities, without regenerating old production models.
src=(OUT/'generate-equipment.py').read_text(encoding='utf-8')
exec(src[:src.index('# Headband and tapering')])
colors=['f4f0e7','8e102e','faf6ed','a01533','d6a83d','29282c','d9d1c4','b82b41','e4be58','44302b','dfa6a3','201c20','474449','fff8e7','926527','6c1530']
def chunk2(k,d):return struct.pack('>I',len(d))+k+d+struct.pack('>I',zlib.crc32(k+d)&0xffffffff)
raw=b''
for y in range(128):
 row=b''
 for x in range(128):
  c=bytes.fromhex(colors[(y//32)*4+x//32]);shade=0 if (x//4+y//4)%3 else -3
  row+=bytes(max(0,v+shade) for v in c)+b'\xff'
 raw+=b'\0'+row
tex=A/'textures/entity/kitsunebi_shrine_palette.png'
tex.write_bytes(b'\x89PNG\r\n\x1a\n'+chunk2(b'IHDR',struct.pack('>IIBBBBB',128,128,8,6,0,0,0))+chunk2(b'IDAT',zlib.compress(raw))+chunk2(b'IEND',b''))
for n in [1,2]:shutil.copyfile(tex,A/f'textures/models/armor/kitsunebi_layer_{n}.png')
def cb(x,y,z,w,h,d,material=0):return {'from':[x,y,z],'size':[w,h,d],'material':material}
bones=[]
def bone(name,parent,pivot=(0,0,0),boxes=(),rotation=(0,0,0)):
 bones.append({'name':name,'parent':parent,'pivot':pivot,'rotation':rotation,'boxes':list(boxes)})
for name,pivot in [('head',(0,0,0)),('hat',(0,0,0)),('body',(0,0,0)),('right_arm',(-5,2,0)),('left_arm',(5,2,0)),('right_leg',(-1.9,12,0)),('left_leg',(1.9,12,0))]:bone(name,'root',pivot)
bone('ears','head',boxes=[cb(-4.1,-8.1,-3.7,8.2,.3,7.4,1)])
for side,sign in [('right',-1),('left',1)]:
 bone(side+'_ear','ears',(sign*2.6,-7.8,0),[cb(-1.65,-3.5,-1.1,3.3,3.8,2.2,11),cb(-1.4,-3.5,-1.3,2.8,3.6,2.3,2),cb(-1.15,-5,-.9,2.3,1.8,1.8,3),cb(-.7,-6,-.7,1.4,1.3,1.4,3),cb(-.9,-3.6,-1.4,1.8,2.8,.25,10)],(0,0,sign*.17))
# Small fox mask clipped at the temple, as in the side/front concept view.
mask=[cb(-1.7,-2.3,-.4,3.4,3.7,.8),cb(-1.2,1.3,-.55,2.4,.9,.8),cb(-1.6,-3.5,-.3,1,1.5,.7,3),cb(.6,-3.5,-.3,1,1.5,.7,3),cb(-1.4,-1.2,-.9,.9,.35,.4,11),cb(.5,-1.2,-.9,.9,.35,.4,11),cb(-.35,-.2,-1,.7,.6,.5,11),cb(-1.65,0,-.9,.85,.4,.3,3),cb(.8,0,-.9,.85,.4,.3,3),cb(-.25,-2,-.9,.5,.6,.25,3),cb(-.6,1,-.9,1.2,.3,.25,3)]
bone('temple_mask','ears',(4.25,-4.4,-2.8),mask,(0,-.5,.1))
bone('kimono','body',boxes=[cb(-4.15,0,-2.2,8.3,12.3,4.4),cb(-1.3,0,-2.5,2.6,1,.35,1)])
for i in range(6):
 # Stepped diagonal red collar crossing the ivory kimono.
 next(b for b in bones if b['name']=='kimono')['boxes'].append(cb(-2.7+i*.62,1.2+i*1.1,-2.65,.85,1.25,.35,1))
haori=[cb(-4.4,.1,2.2,8.8,8.8,.6,0),cb(-4.45,.2,-2.55,1.3,9.8,5,0),cb(3.15,.2,-2.55,1.3,9.8,5,0),cb(-4.5,1.2,-2.8,9,1.3,.55,1),cb(-4.6,.8,2.75,9.2,1.8,.55,1),cb(-4.5,2.4,2.8,9,1,.5,1)]
for x in [-3.4,3.4]:
 haori += [cb(x-.65,1,-3.3,1.3,1.3,.6,4),cb(x-.35,1.3,-3.65,.7,.7,.4,14),cb(x-.18,2.4,-3.1,.36,1.7,.35,4),cb(x-.5,3.8,-3.15,1,1.8,.6,8)]
# Two gold cords and dangling back tassels.
for x in [-3.3,0,3.3]:haori += [cb(x-.2,3.6,3.1,.4,2.1,.45,4),cb(x-.55,5.6,3.1,1.1,1.8,.7,8)]
bone('haori','body',boxes=haori)
for side in ['right','left']:
 x=-3.3 if side=='right' else -1.1
 bone(side+'_sleeve',side+'_arm',boxes=[cb(x,-2.1,-2.45,4.4,8.2,4.9),cb(x-.3,1.2,-2.8,5,1.2,5.6,1),cb(x-.45,5.8,-2.7,5.3,2.2,5.4,0),cb(x-.5,7.6,-2.8,5.4,3.4,5.6,1),cb(x-.45,10.5,-2.85,5.3,.8,5.7,4),cb(x+.3,8,-2.9,1.6,1.6,.3,0)])
 bone(side+'_hakama',side+'_leg',boxes=[cb(-2.25,0,-2.45,4.5,7.4,4.9,5),cb(-1.65,.3,-2.7,.65,6.9,.35,12),cb(.2,.3,-2.7,.65,6.9,.35,12),cb(-2.25,7,-2.5,4.5,.6,5,11)])
 sign=-1 if side=='right' else 1
 # Long split crimson haori panels follow the legs instead of clipping through them.
 bone(side+'_coat',side+'_leg',boxes=[cb(sign*.8-1.35,-1.3,-3,2.7,6.6,.65,1),cb(sign*.8-1.3,4.9,-3,2.6,1.3,.65,1),cb(sign*.8-1.3,6,-3,2.6,.7,.7,4),cb(sign*.8-.85,-.8,-3.4,1.2,3.4,.35,0),cb(sign*.8-.4,2.4,-3.4,.8,1.2,.35,0)])
 bone(side+'_tabi',side+'_leg',boxes=[cb(-2.1,7.2,-2.15,4.2,4.6,4.3),cb(-2.2,11.3,-3.4,4.4,.7,5.6,9),cb(-2.2,10.8,-3.4,4.4,.5,5.6,1),cb(-1.7,10,-3.4,3.4,.65,.6,1),cb(-.4,10,-3.4,.8,.7,3.2,1)])
obi=[cb(-4.35,8.4,-2.7,8.7,2.1,5.4,5),cb(-4.55,8.8,-3.1,9.1,.55,.6,4),cb(-4.55,9.7,-3.1,9.1,.55,.6,4),cb(-.8,9,-3.75,1.6,1.1,.9,8)]
for i in range(5):obi += [cb(-2.9+i*.6,10.3+i*.4,-3.4,.7,.7,.7,4)]
obi += [cb(-.4,12,-3.4,.6,2.1,.6,4),cb(-.7,13.8,-3.6,1.2,1.8,.8,8)]
bone('obi','body',boxes=obi)
for i in range(9):
 parent=f'tail_{i}';bone(parent,'body',(0,10,3.2))
 bone(parent+'_base',parent,boxes=[cb(-.8,-.8,0,1.6,1.6,4,2)])
 bone(parent+'_mid',parent,(0,0,3),[cb(-1.35,-1.35,0,2.7,2.7,4.5,2)],(.04,0,0))
 bone(parent+'_end',parent+'_mid',(0,0,3.7),[cb(-1.9,-1.8,0,3.8,3.6,4.2,2)],(.09,0,0))
 bone(parent+'_tip',parent+'_end',(0,0,3.5),[cb(-1.8,-1.6,0,3.6,3.2,2.6,3),cb(-1.1,-1.1,2.4,2.2,2.2,1.6,3)],(.12,0,0))
geometry={'format':'kitsunebi.humanoid.v1','texture':'kitsunebi:textures/entity/kitsunebi_shrine_palette.png','texture_size':[128,128],'tail_counts':[1,3,6,9],'bones':bones}
write(A/'codex/v04/armor_geometry.json',geometry)
# Emit the same geometry to the runtime HumanoidModel and the editable handoff source.
java=src[src.index('# A standalone integration class'):]
java=java.replace('u=(mat%2)*32;v=(mat//2)*32','u=(mat%4)*32;v=(mat//4)*32').replace('LayerDefinition.create(mesh,64,64)','LayerDefinition.create(mesh,128,128)')
java=java.replace('slot==EquipmentSlot.LEGS||slot==EquipmentSlot.FEET;', 'slot==EquipmentSlot.LEGS||slot==EquipmentSlot.FEET||slot==EquipmentSlot.CHEST;')
java=java.replace(" '            leg.getChild(side+\"_tabi\").visible=slot==EquipmentSlot.FEET;',", " '            leg.getChild(side+\"_tabi\").visible=slot==EquipmentSlot.FEET;',\n '            leg.getChild(side+\"_coat\").visible=slot==EquipmentSlot.CHEST;',")
java=java.replace('tail.x=(i-(tails-1)/2F)*.7F','tail.x=(i-(tails-1)/2F)*.45F').replace('tail.yRot=(i-(tails-1)/2F)*0.34F','tail.yRot=(i-(tails-1)/2F)*(tails==1?0:2.7F/(tails-1))').replace('tail.xRot=.68F','tail.xRot=-1.05F+(float)Math.sin(i*1.7F)*.025F')
exec(java)
shutil.copyfile(OUT/'integration/java/net/kafgchan/kitsunebi/client/model/KitsunebiArmorVisualModel.java',ROOT/'common-1.21.1/src/main/java/net/kafgchan/kitsunebi/client/model/KitsunebiArmorVisualModel.java')

def native_box(bounds,mat):
 u=(mat%4)*4;v=(mat//4)*4
 return {'from':list(bounds[:3]),'to':list(bounds[3:]),'faces':{f:{'texture':'#shrine','uv':[u+.1,v+.1,u+3.8,v+3.8]} for f in FACES}}
def save_item(name,e,armor=False):
 model(name,e,armor)
 p=A/f'models/item/kitsunebi_{name}.json';d=json.loads(p.read_text());d['textures']['shrine']='kitsunebi:entity/kitsunebi_shrine_palette';write(p,d)
# Icons derived from exactly the same body part shapes. Child rotations are baked to cuboid bounds.
lookup={b['name']:b for b in bones}
def transform(p,b):
 q=list(p)
 for angle,(i,j) in zip(b['rotation'],[(1,2),(2,0),(0,1)]):q[i],q[j]=q[i]*math.cos(angle)-q[j]*math.sin(angle),q[i]*math.sin(angle)+q[j]*math.cos(angle)
 q=[q[i]+b['pivot'][i] for i in range(3)]
 return transform(q,lookup[b['parent']]) if b['parent']!='root' else q
for name,parts in [('headdress',['ears']),('haori',['kimono','haori','right_sleeve','left_sleeve','right_coat','left_coat']),('hakama',['obi','right_hakama','left_hakama']),('tabi',['right_tabi','left_tabi'])]:
 shapes=[]
 for b in bones:
  cursor=b['name'];included=False
  while cursor!='root':
   if cursor in parts:included=True;break
   cursor=lookup[cursor]['parent']
  if not included:continue
  for c in b['boxes']:
   points=[transform([c['from'][k]+c['size'][k]*v[k] for k in range(3)],b) for v in [(x,y,z) for x in [0,1] for y in [0,1] for z in [0,1]]]
   points=[(p[0],-p[1],p[2]) for p in points]
   shapes.append(([min(p[k] for p in points) for k in range(3)],[max(p[k] for p in points) for k in range(3)],c['material']))
 lo=[min(s[0][k] for s in shapes) for k in range(3)];hi=[max(s[1][k] for s in shapes) for k in range(3)];scale=14/max(hi[k]-lo[k] for k in range(3));center=[(lo[k]+hi[k])/2 for k in range(3)]
 e=[native_box([(a[k]-center[k])*scale+8 for k in range(3)]+[(b[k]-center[k])*scale+8 for k in range(3)],m) for a,b,m in shapes]
 save_item(name,e,True)
# Standalone shrine fox mask keeps its existing equipped transform and internal save ID.
p=A/'models/item/kitsunebi_goggles.json';old=json.loads(p.read_text());e=[]
for c in mask:
 f=c['from'];sz=c['size'];e.append(native_box([8+f[0]*2,8-f[1]*2-sz[1]*2,8+f[2]*2,8+(f[0]+sz[0])*2,8-f[1]*2,8+(f[2]+sz[2])*2],c['material']))
save_item('goggles',e,True);d=json.loads(p.read_text());d['display']=old['display'];write(p,d)
# Ceremonial staff / gohei: dark lacquer shaft, gold ferrules, white zigzag paper.
e=[native_box((7.4,.5,7.4,8.6,11.7,8.6),9),native_box((7.15,.3,7.15,8.85,1.5,8.85),4),native_box((7.1,10.6,7.1,8.9,12,8.9),4),native_box((5,11.4,7,11,12.4,9),4),native_box((4.8,12,7,6,14.5,9),4),native_box((10,12,7,11.2,14.5,9),4),native_box((6.1,14,7.1,9.9,15,8.9),4),native_box((7.1,12,6.7,8.9,14.1,7.2),1)]
for side in [-1,1]:
 for i in range(4):
  x=8+side*(2+i*.7);y=13-i*1.2
  e.append(native_box((x-.8,y-.5,6.4,x+.8,y+.5,7.1),0))
  if i<3:e.append(native_box((x-.4,y-1.2,6.4,x+.5,y-.4,7.1),0))
save_item('gohei',e)
p=A/'models/item/kitsunebi_gohei.json';d=json.loads(p.read_text())
for hand in ['firstperson_righthand','firstperson_lefthand','thirdperson_righthand','thirdperson_lefthand']:d['display'][hand]['scale']=[1.35]*3
write(p,d)
p=ROOT/'common-1.21.1/src/main/resources/assets/minecraft/atlases/blocks.json';d=json.loads(p.read_text());entry={'type':'minecraft:single','resource':'kitsunebi:entity/kitsunebi_shrine_palette','sprite':'kitsunebi:entity/kitsunebi_shrine_palette'}
if entry not in d['sources']:d['sources'].append(entry)
write(p,d)
p=ROOT/'common-1.21.1/src/main/java/net/kafgchan/kitsunebi/client/model/KitsunebiArmorVisuals.java';s=p.read_text().replace('textures/entity/kitsunebi_equipment.png','textures/entity/kitsunebi_shrine_palette.png');p.write_text(s)
print('Reference outfit generated: ears + temple mask, ceremonial sleeves, gold cords, black hakama, geta and staff.')
