"""Export the same authored geometry to the Fabric native Humanoid renderer."""
from pathlib import Path
import json, re
ROOT=Path(__file__).resolve().parents[2]
data=json.loads((Path(__file__).parent/'models/shuko_detailed.json').read_text(encoding='utf-8'))
target=ROOT/'common-1.21.1/src/main/java/net/kafgchan/kitsunebi/client/model/KitsunebiArmorVisualModel.java'
existing=target.read_text(encoding='utf-8')
start=existing.index('    public static LayerDefinition createLayer()')
end=existing.index('    public void configure(',start)
lines=[]
def number(n):return f'{n:.5f}F'
for b in data['bones']:
 cubes='CubeListBuilder.create()'
 for c in b['boxes']:
  m=c['material'];cubes+=f'.texOffs({(m%4)*32+1},{(m//4)*32+1}).addBox('+','.join(map(number,c['from']+c['size']))+')'
 pose=','.join(map(number,b['pivot']+b['rotation']))
 lines.append(f'        parts.put("{b["name"]}",parts.get("{b["parent"]}").addOrReplaceChild("{b["name"]}",{cubes},PartPose.offsetAndRotation({pose})));')
chunks=[lines[i:i+24] for i in range(0,len(lines),24)]
section='    public static LayerDefinition createLayer(){\n        MeshDefinition mesh=new MeshDefinition();\n        java.util.Map<String,PartDefinition> parts=new java.util.HashMap<>();parts.put("root",mesh.getRoot());\n'
section+=''.join(f'        build{i}(parts);\n' for i in range(len(chunks)))+'        return LayerDefinition.create(mesh,128,128);\n    }\n'
for i,chunk in enumerate(chunks):section+=f'    private static void build{i}(java.util.Map<String,PartDefinition> parts){{\n'+'\n'.join(chunk)+'\n    }\n'
target.write_text(existing[:start]+section+existing[end:],encoding='utf-8')
print('Exported reference humanoid geometry in bounded methods.')
