"""Original equipment geometry only. Never edits registries, recipes, menus or shared locales."""
import json, math, struct, zlib, uuid
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
A=ROOT/'common/src/main/resources/assets/kitsunebi'
OUT=ROOT/'docs/v04-codex'
for p in [A/'textures/item',A/'models/item',A/'codex/v04',OUT/'integration/java/net/kafgchan/kitsunebi/client/model']:
    p.mkdir(parents=True,exist_ok=True)

def write(path,data):path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
# Code-native flat metal/lacquer swatches, matching the established cuboid material workflow.
palette=['f7f3e5','dfd6c3','c4b79b','d92f32','9f1828','dfbd73','edf3f1','9caeb5','27212b','61413b','fff0b5','c7e8ef','ffffff','8d6361','51404f','c9bfae']
def chunk(k,d):return struct.pack('>I',len(d))+k+d+struct.pack('>I',zlib.crc32(k+d)&0xffffffff)
pixels=[bytes.fromhex(palette[(y//4)*4+x//4])+b'\xff' for y in range(16) for x in range(16)]
raw=b''.join(b'\0'+b''.join(pixels[y*16:(y+1)*16]) for y in range(16))
(A/'textures/item/kitsunebi_equipment_palette.png').write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',16,16,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b''))
FACES=('north','south','west','east','up','down')
cloth={'ivory':(0,0),'red':(8,0),'fur':(0,8),'tip':(8,8)}
def box(bounds,material='ivory',rotation=None):
    if material in cloth:
        u,v=cloth[material];face={'texture':'#fabric','uv':[u+.12,v+.12,u+7.88,v+7.88]}
    else:
        u=(material%4)*4;v=(material//4)*4
        face={'texture':'#palette','uv':[u+.5,v+.5,u+3.5,v+3.5]}
    e={'from':list(bounds[:3]),'to':list(bounds[3:]),'faces':{d:dict(face) for d in FACES}}
    if rotation:e['rotation']=rotation
    return e
def model(name,elements,armor=False):
    data={'parent':'minecraft:block/block','textures':{'fabric':'kitsunebi:entity/kitsunebi_equipment','palette':'kitsunebi:item/kitsunebi_equipment_palette','particle':'kitsunebi:block/kitsunebi_white'},'elements':elements,
          'display':{'gui':{'rotation':[15,210,0] if armor else [20,225,-15],'translation':[0,0,0],'scale':[.9,.9,.9]},
                     'ground':{'rotation':[0,0,0],'translation':[0,3,0],'scale':[.5,.5,.5]},
                     'fixed':{'rotation':[0,180,0],'translation':[0,0,0],'scale':[.8,.8,.8]},
                     'firstperson_righthand':{'rotation':[0,90,-25],'translation':[1,1,0],'scale':[.9,.9,.9]},
                     'firstperson_lefthand':{'rotation':[0,-90,25],'translation':[1,1,0],'scale':[.9,.9,.9]},
                     'thirdperson_righthand':{'rotation':[0,90,0],'translation':[0,1,0],'scale':[.85,.85,.85]},
                     'thirdperson_lefthand':{'rotation':[0,-90,0],'translation':[0,1,0],'scale':[.85,.85,.85]}}}
    write(A/f'models/item/kitsunebi_{name}.json',data)

# Headband and tapering three-dimensional ears.
e=[box((3.5,6,5,12.5,7.25,11),'red')]
for x in (3.5,9):
    e += [box((x,7,6,x+3.5,11,9),'fur'),box((x+.5,11,6.25,x+3,14,8.75),'fur'),box((x+1,14,6.5,x+2.5,15.5,8.5),'fur'),box((x+1,8,5.8,x+2.5,12.5,6.1),'tip')]
e+=[box((7.25,6,4.5,8.75,7.6,5.2),5)]
model('headdress',e,True)

# White kimono under an open vermilion haori; modeled sleeves and sash.
e=[box((5,2,6,11,13,10),'ivory'),box((4,2,9.8,12,13.5,10.8),'red')]
for x in (3.8,10.3):e.append(box((x,2,5.7,x+1.9,13.5,6.4),'red'))
for x in (1,11):
    e += [box((x,7,5.5,x+4,13,10.5),'red'),box((x,7,5.3,x+4,8.2,10.7),'ivory')]
e += [box((5,11.5,5.5,8,13.5,5.9),'ivory'),box((8,10.5,5.4,10.6,12.5,5.8),'ivory'),box((4.8,4.5,5.3,11.2,5.5,6),5),box((7.3,3.8,4.8,8.7,5.8,5.5),'red')]
model('haori',e,True)

e=[box((3.8,11,5,12.2,14,11),'red'),box((4,1,5,7.85,11,11),'ivory'),box((8.15,1,5,12,11,11),'ivory')]
for x in (4,6,8.15,10.15):e.append(box((x,1,4.75,x+.35,10.8,5.1),1))
for x in (3.8,11.5):e.append(box((x,1,4.9,x+.7,11,11.1),'red'))
e += [box((3.8,1,4.8,12.2,2,11.2),'red'),box((7.2,11.7,4.2,8.8,13.3,5.2),5),box((6,11.7,4.4,7.4,13,5.2),'red'),box((8.6,11.7,4.4,10,13,5.2),'red')]
model('hakama',e,True)
e=[]
for x in (2.5,9):
    e += [box((x,1,4.5,x+4.5,2,12.5),9),box((x+.2,2,5,x+4.3,7.5,11.5),'ivory'),box((x+.2,2,4.4,x+2.1,3.8,5.4),'ivory'),box((x+2.4,2,4.4,x+4.3,3.8,5.4),'ivory'),box((x,3.8,6.5,x+4.5,4.5,7.5),'red'),box((x+1.8,2.2,4.3,x+2.7,4.5,7.5),'red')]
model('tabi',e,True)

def handle(top):
    e=[box((7.25,1,7.25,8.75,top,8.75),8),box((7,1,7,9,2,9),5)]
    for y in range(2,int(top)-1,2):e.append(box((7.1,y,7.1,8.9,y+.65,8.9),'red'))
    return e
e=handle(6)
e += [box((5.5,5.7,6.2,10.5,6.5,9.8),5),box((7.3,6.5,7.4,8.7,13.5,8.6),6),box((7.75,13.5,7.4,8.7,15,8.6),6),box((8.1,15,7.5,8.7,15.8,8.5),6),box((7.05,6.6,7.45,7.35,13.4,8.55),11)]
model('sword',e)
e=handle(11)+[box((3,10.5,7,13,12.5,9),6),box((2,9,7.25,4,11,8.75),6),box((12,9,7.25,14,11,8.75),6),box((6.5,10,6.75,9.5,13,9.25),5),box((7.2,11,6.5,8.8,12.4,6.8),'red')]
model('pickaxe',e)
e=handle(13)+[box((3,10,6.5,7.5,14.5,9.5),6),box((2,11,6.8,3,14,9.2),6),box((3,9.2,6.8,6,10,9.2),6),box((7,10,6.8,10,13.5,9.2),5),box((4,10.5,6.2,5.5,13.5,6.6),'red')]
model('axe',e)
e=handle(10)+[box((5.5,10,7,10.5,14,9),6),box((6.25,14,7.15,9.75,15,8.85),6),box((7,9,6.8,9,11,9.2),5),box((7.5,11,6.7,8.5,13,7.1),'red')]
model('shovel',e)
e=handle(13)+[box((4,12,7,12,14,9),6),box((3.5,9,7.1,5.5,12.5,8.9),6),box((7,11.5,6.75,9,14.5,9.25),5),box((7.3,12,6.5,8.7,13.5,6.8),'red')]
model('hoe',e)

# ModelPart bone hierarchy in vanilla humanoid coordinates (Y points down).
bones=[]
def bone(name,parent,pivot=(0,0,0),boxes=(),rotation=(0,0,0)):
    bones.append({'name':name,'parent':parent,'pivot':pivot,'rotation':rotation,'boxes':list(boxes)})
def cb(x,y,z,w,h,d,mat=0):return {'from':[x,y,z],'size':[w,h,d],'material':mat}
bone('head','root',boxes=[]);bone('hat','root')
bone('body','root');bone('right_arm','root',(-5,2,0));bone('left_arm','root',(5,2,0))
bone('right_leg','root',(-1.9,12,0));bone('left_leg','root',(1.9,12,0))
ears=[cb(-4.3,-8.1,-4.3,8.6,.9,8.6,1)]
for x in (-4.2,1):
    ears += [cb(x,-11.5,-1.5,3.2,4.2,2.5,2),cb(x+.4,-14,-1.2,2.4,2.5,2,2),cb(x+.9,-15,-.9,1.4,1,1.6,2),cb(x+1,-12.8,-1.7,1.2,4.3,.25,3)]
bone('ears','head',boxes=ears)
bone('kimono','body',boxes=[cb(-4.2,-.1,-2.2,8.4,12.3,4.4),cb(-2.5,.3,-2.55,2.5,2,.35,1),cb(0,2,-2.6,2.5,2,.35,1)])
bone('haori','body',boxes=[cb(-4.6,-.2,2.2,9.2,12.6,.55,1),cb(-4.6,-.2,-2.5,1.6,12.6,4.9,1),cb(3,-.2,-2.5,1.6,12.6,4.9,1),cb(-4.5,11.6,-2.5,9,1,5.1,1)])
for side in ('right','left'):
    x=-3.3 if side=='right' else -1.1
    bone(side+'_sleeve',side+'_arm',boxes=[cb(x,-2.2,-2.5,4.4,9.5,5,1),cb(x,6.7,-2.6,4.4,1.1,5.2)])
    bone(side+'_hakama',side+'_leg',boxes=[cb(-2.25,-.1,-2.5,4.5,10.8,5),cb(-2.3,9.7,-2.6,4.6,1.1,5.2,1),cb(-2.5,0,-2.55,.6,10,5.1,1)])
    bone(side+'_tabi',side+'_leg',boxes=[cb(-2.1,6,-2.1,4.2,5.7,4.2),cb(-2.2,10.5,-3.4,4.4,1.2,5.6,1),cb(-.5,9.9,-3.6,1,1,3.5,1)])
bone('obi','body',boxes=[cb(-4.4,9,-2.5,8.8,3.3,5,1),cb(-1,9,-3.1,2,2,1),cb(-2.6,9,-2.9,1.8,1.8,.7),cb(.8,9,-2.9,1.8,1.8,.7)])
for i in range(9):
    parent=f'tail_{i}'
    bone(parent,'body',(0,10,2.7))
    bone(parent+'_base',parent,boxes=[cb(-1.9,-1.9,0,3.8,3.8,4,2)])
    bone(parent+'_mid',parent,(0,-.8,3),[cb(-1.7,-1.7,0,3.4,3.4,4.5,2)],(-.12,0,0))
    bone(parent+'_end',parent+'_mid',(0,-1,3.5),[cb(-1.4,-1.4,0,2.8,2.8,4,2)],(-.18,0,0))
    bone(parent+'_tip',parent+'_end',(0,-.7,3),[cb(-1.25,-1.25,0,2.5,2.5,3,3),cb(-.8,-.8,3,1.6,1.6,1.2,3)],(-.2,0,0))
geometry={'format':'kitsunebi.humanoid.v1','texture':'kitsunebi:textures/entity/kitsunebi_equipment.png','texture_size':[64,64],'tail_counts':[1,3,6,9],'bones':bones}
write(A/'codex/v04/armor_geometry.json',geometry)

# A standalone integration class: compiled separately, never added to loader setup here.
lines=['package net.kafgchan.kitsunebi.client.model;','',
 'import net.minecraft.client.model.HumanoidModel;','import net.minecraft.client.model.geom.*;',
 'import net.minecraft.client.model.geom.builders.*;','import net.minecraft.world.entity.*;',
 'public final class KitsunebiArmorVisualModel<T extends LivingEntity> extends HumanoidModel<T> {',
 '    public KitsunebiArmorVisualModel(ModelPart root){super(root);}',
 '    public static LayerDefinition createLayer(){','        MeshDefinition mesh=new MeshDefinition();',
 '        PartDefinition root=mesh.getRoot();']
def f(n):return f'{float(n):.5f}F'
for b in bones:
    builder='CubeListBuilder.create()'
    for c in b['boxes']:
        mat=c['material'];u=(mat%2)*32;v=(mat//2)*32
        builder+=f'.texOffs({u},{v}).addBox('+','.join(f(n) for n in c['from']+c['size'])+')'
    pose='PartPose.offsetAndRotation('+','.join(f(n) for n in list(b['pivot'])+list(b['rotation']))+')'
    lines.append(f'        PartDefinition {b["name"]}={b["parent"]}.addOrReplaceChild("{b["name"]}",{builder},{pose});')
lines += ['        return LayerDefinition.create(mesh,64,64);','    }',
 '    public void configure(EquipmentSlot slot,int count,float age) {',
 '        setAllVisible(false);hat.visible=false;',
 '        head.visible=slot==EquipmentSlot.HEAD;',
 '        body.visible=slot==EquipmentSlot.CHEST||slot==EquipmentSlot.LEGS;',
 '        rightArm.visible=leftArm.visible=slot==EquipmentSlot.CHEST;',
 '        rightLeg.visible=leftLeg.visible=slot==EquipmentSlot.LEGS||slot==EquipmentSlot.FEET;',
 '        body.getChild("kimono").visible=body.getChild("haori").visible=slot==EquipmentSlot.CHEST;',
 '        body.getChild("obi").visible=slot==EquipmentSlot.LEGS;',
 '        for(ModelPart leg:new ModelPart[]{rightLeg,leftLeg}){',
 '            String side=leg==rightLeg?"right":"left";',
 '            leg.getChild(side+"_hakama").visible=slot==EquipmentSlot.LEGS;',
 '            leg.getChild(side+"_tabi").visible=slot==EquipmentSlot.FEET;',
 '        }',
 '        int tails=new int[]{1,3,6,9}[Math.max(1,Math.min(4,count))-1];',
 '        for(int i=0;i<9;i++){ModelPart tail=body.getChild("tail_"+i);',
 '            tail.visible=slot==EquipmentSlot.LEGS&&i<tails;',
 '            tail.yRot=(i-(tails-1)/2F)*0.26F+(float)Math.sin(age*.045F+i*.7F)*.07F;',
 '            tail.xRot=-.35F+(float)Math.sin(age*.04F+i*.9F)*.06F;',
 '        }','    }','}']
(OUT/'integration/java/net/kafgchan/kitsunebi/client/model/KitsunebiArmorVisualModel.java').write_text('\n'.join(lines)+'\n',encoding='utf-8')
print('Generated 9 item models, material palette, humanoid hierarchy and animated 1/3/6/9-tail integration model.')
