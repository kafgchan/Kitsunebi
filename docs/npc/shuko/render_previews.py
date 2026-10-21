"""Software-only orthographic authoring checks, not Blockbench/game evidence."""
from pathlib import Path
import json,math
import numpy as np
from PIL import Image,ImageDraw
HERE=Path(__file__).resolve().parent
data=json.loads((HERE/'shuko.bbmodel').read_text(encoding='utf8'))
import base64,io
tex=Image.open(io.BytesIO(base64.b64decode(data['textures'][0]['source'].split(',')[1]))).convert('RGB')
texture=np.asarray(tex)
W,H,S=640,720,18
def matrix(r):
    x,y,z=np.radians(r);cx,cy,cz=np.cos([x,y,z]);sx,sy,sz=np.sin([x,y,z])
    return np.array([[cz,-sz,0],[sz,cz,0],[0,0,1]])@np.array([[cy,0,sy],[0,1,0],[-sy,0,cy]])@np.array([[1,0,0],[0,cx,-sx],[0,sx,cx]])
faces={'north':[0,1,2,3],'south':[5,4,7,6],'east':[1,5,6,2],'west':[4,0,3,7],'up':[4,5,1,0],'down':[3,2,6,7]}
def render(yaw):
    a=math.radians(yaw);M=np.array([[math.cos(a),0,math.sin(a)],[0,1,0],[-math.sin(a),0,math.cos(a)]])
    canvas=np.empty((H,W,3),dtype=np.uint8);canvas[:]=(48,42,55);zbuf=np.full((H,W),np.inf)
    def triangle(v,uv):
        x0=max(0,int(v[:,0].min()));x1=min(W-1,int(v[:,0].max())+1);y0=max(0,int(v[:,1].min()));y1=min(H-1,int(v[:,1].max())+1)
        if x0>x1 or y0>y1:return
        xx,yy=np.meshgrid(np.arange(x0,x1+1)+.5,np.arange(y0,y1+1)+.5)
        ax,ay=v[0,:2];bx,by=v[1,:2];cx,cy=v[2,:2];den=(by-cy)*(ax-cx)+(cx-bx)*(ay-cy)
        if abs(den)<.00001:return
        w0=((by-cy)*(xx-cx)+(cx-bx)*(yy-cy))/den;w1=((cy-ay)*(xx-cx)+(ax-cx)*(yy-cy))/den;w2=1-w0-w1
        zz=w0*v[0,2]+w1*v[1,2]+w2*v[2,2];sub=zbuf[y0:y1+1,x0:x1+1]
        ok=(w0>=0)&(w1>=0)&(w2>=0)&(zz<sub)
        if not ok.any():return
        us=np.clip((w0*uv[0,0]+w1*uv[1,0]+w2*uv[2,0]).astype(int),0,255);vs=np.clip((w0*uv[0,1]+w1*uv[1,1]+w2*uv[2,1]).astype(int),0,255)
        canvas[y0:y1+1,x0:x1+1][ok]=texture[vs[ok],us[ok]];sub[ok]=zz[ok]
    for e in data['elements']:
        x,y,z=e['from'];X,Y,Z=e['to']
        vertices=np.array([(x,Y,z),(X,Y,z),(X,y,z),(x,y,z),(x,Y,Z),(X,Y,Z),(X,y,Z),(x,y,Z)])
        pivot=np.array(e.get('origin',[0,0,0]));vertices=(vertices-pivot)@matrix(e.get('rotation',[0,0,0])).T+pivot
        vertices=vertices@M.T;vertices[:,0]=W/2+vertices[:,0]*S;vertices[:,1]=H-55-vertices[:,1]*S
        for f,indices in faces.items():
            q=vertices[indices];u,v,U,V=e['faces'][f]['uv'];uv=np.array([(u,v),(U-.001,v),(U-.001,V-.001),(u,V-.001)])
            for ids in ([0,1,2],[0,2,3]):triangle(q[ids],uv[ids])
    im=Image.fromarray(canvas);d=ImageDraw.Draw(im);d.text((15,12),'SOFTWARE AUTHORING PREVIEW / '+str(yaw)+' deg',fill='#eee0d3')
    return im
previews=HERE/'authoring-previews';previews.mkdir(exist_ok=True)
sheet=Image.new('RGB',(W*4,H))
for i,(name,angle) in enumerate((('front',0),('left',90),('back',180),('right',270))):
    im=render(angle);im.save(previews/(name+'.png'));sheet.paste(im,(W*i,0))
sheet.save(previews/'contact.png')
# Compare spatial sampling on the actual atlas at equal viewing size.
comp=Image.new('RGB',(768,280),'#302a37');d=ImageDraw.Draw(comp)
for i,size in enumerate((64,128,256)):
    t=tex.resize((size,size),Image.Resampling.BOX).resize((256,256),Image.Resampling.NEAREST)
    comp.paste(t,(i*256,24));d.text((i*256+8,5),f'{size} x {size}',fill='white')
comp.save(previews/'texture-resolution-comparison.png')
print(previews)
