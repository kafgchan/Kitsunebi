"""Export the same authored geometry to the Fabric native Humanoid renderer."""
from pathlib import Path
import json, re
ROOT=Path(__file__).resolve().parents[2]
data=json.loads((Path(__file__).parent/'models/shuko_detailed.json').read_text(encoding='utf-8'))
target=ROOT/'common-1.21.1/src/main/java/net/kafgchan/kitsunebi/client/model/KitsunebiArmorVisualModel.java'
existing=target.read_text(encoding='utf-8')
start=existing.index('        MeshDefinition mesh=')
end=existing.index('        return LayerDefinition.create(',start)
lines=['        MeshDefinition mesh=new MeshDefinition();','        PartDefinition root=mesh.getRoot();']
def number(n):return f'{n:.5f}F'
def ident(name):return re.sub(r'[^a-zA-Z0-9_]','_',name)
for b in data['bones']:
 cubes='CubeListBuilder.create()'
 for c in b['boxes']:
  m=c['material'];cubes+=f'.texOffs({(m%4)*32+1},{(m//4)*32+1}).addBox('+','.join(map(number,c['from']+c['size']))+')'
 pose=','.join(map(number,b['pivot']+b['rotation']))
 lines.append(f'        PartDefinition {ident(b["name"])}={ident(b["parent"])}.addOrReplaceChild("{b["name"]}",{cubes},PartPose.offsetAndRotation({pose}));')
target.write_text(existing[:start]+'\n'.join(lines)+'\n'+existing[end:],encoding='utf-8')
print('Exported shared detailed humanoid geometry.')
