"""Wearable silhouette pass shared by the four reference outfits.

Keep the armor anchors and gameplay independent of the authored cloth. Narrow
cross-sections replace large rectangular sleeves, ears and flattened tail tiles.
"""
import math

def refine(data,divine=False):
    source=data['bones']
    standard_tails={f'tail_{i}{suffix}' for i in range(9) for suffix in ['', '_base','_mid','_end','_tip']}
    remove={b['name'] for b in source if
            (b['name'].startswith('tail_') and b['name'] not in standard_tails) or
            b['name'].startswith(('braid_','rope_')) or
            any(marker in b['name'] for marker in ['_cuff_flame_','_paper_','_shide_','_sleeve_train','_coat_seal_'])}
    while True:
        more={b['name'] for b in source if b['parent'] in remove}-remove
        if not more:break
        remove|=more
    source[:]=[b for b in source if b['name'] not in remove]
    lookup={b['name']:b for b in source}
    def B(x,y,z,w,h,d,m):return {'from':[x,y,z],'size':[w,h,d],'material':m}
    def node(name,parent,pivot=(0,0,0),rotation=(0,0,0),boxes=()):
        assert name not in lookup,name
        b={'name':name,'parent':parent,'pivot':list(pivot),'rotation':[math.radians(v) for v in rotation],'boxes':list(boxes)}
        source.append(b);lookup[name]=b;return b
    def add(name,*boxes):lookup[name]['boxes'].extend(boxes)
    def beam(name,parent,a,b,width=.24,material=4):
        dx,dy,dz=[b[i]-a[i] for i in range(3)];length=math.sqrt(dx*dx+dy*dy+dz*dz)
        rx=math.degrees(math.atan2(dz,math.hypot(dx,dy)));rz=math.degrees(math.atan2(-dx,dy))
        return node(name,parent,[(a[i]+b[i])/2 for i in range(3)],(rx,0,rz),[B(-width/2,-length/2,-width/2,width,length,width,material)])
    accent=18 if divine else 1
    if not divine:
        # Broad overlapping cloth under the colored collar, rather than painted V lines.
        node('reference_lapel_inner','kimono',(-2.8,.18,-2.64),(0,0,-29),[B(-1.05,0,-.12,2.1,4.6,.24,0),B(.94,0,-.20,.12,4.6,.08,6)])
        node('reference_lapel_outer','kimono',(2.8,.18,-2.98),(0,0,29),[B(-1.22,0,-.12,2.44,7.8,.24,0),B(-1.22,0,-.21,.12,7.8,.09,6)])

    # Broad triangular fox ears: fine tapered sections, not tall rectangular prongs.
    if not divine:lookup['ears']['boxes']=[]
    for side,sign in [('right',-1),('left',1)]:
        ear=lookup[side+'_ear'];ear['pivot']=[sign*2.75,-7.95,0];ear['rotation']=[0,0,math.radians(sign*12)]
        ear['boxes']=[]
        for i in range(20):
            y=-.24*(i+1);w=3.9*(1-i/20);depth=1.8*(1-i/30)
            add(side+'_ear',B(-w/2,y,-depth/2,w,.24,depth,accent if i>=15 else 2))
            if 1<=i<15:
                iw=w*.56;add(side+'_ear',B(-iw/2,y+.01,-depth/2-.14,iw,.225,.12,10))
        for edge in [-1,1]:
            node(side+'_reference_ear_tuft_'+str(edge),side+'_ear',(edge*1.5,-.3,0),(0,0,edge*25),[B(-.5,-.55,-.85,1,.7,1.7,2)])

    # A faceted porcelain fox mask: curved cheeks, tapered chin and painted strokes.
    if not divine:
        mask=lookup['temple_mask'];mask['boxes']=[]
        for i,(w,z) in enumerate([(2.7,-.35),(3.2,-.46),(3.4,-.51),(3.35,-.56),(3.1,-.6),(2.75,-.62),(2.3,-.59),(1.7,-.52)]):
            add('temple_mask',B(-w/2,-2.15+i*.53,z,w,.53,.85,13))
        for sign in [-1,1]:
            for i in range(4):
                w=.92*(1-i/5)
                add('temple_mask',B(sign*1.1-w/2,-3.30+i*.29,-.33,w,.3,.65,accent if i<2 else 13))
            node('reference_mask_brow_'+str(sign),'temple_mask',(sign*.9,-1.1,-.82),(0,0,-sign*12),[B(-.48,-.14,-.13,.96,.28,.26,accent),B(-.32,.14,-.16,.64,.14,.18,11)])
            for i in range(3):
                node('reference_mask_cheek_'+str(sign)+'_'+str(i),'temple_mask',(sign*(.78+i*.18),.05+i*.32,-.82),(0,0,sign*-25),[B(-.28,-.09,-.12,.56,.18,.24,accent)])
        add('temple_mask',B(-.34,-.04,-1.05,.68,.35,.38,11),B(-.21,.31,-1.01,.42,.2,.27,11),B(-.45,.94,-.86,.9,.16,.18,accent),B(-.15,-1.8,-.81,.3,.45,.2,accent))
        node('reference_head_knot','ears',(-4.4,-5.3,.6),boxes=[B(-.43,-.43,-.35,.86,.86,.7,4),B(-.55,-.22,-.28,1.1,.44,.56,8),B(-.2,.43,-.15,.4,2.2,.30,1)])
        for strand in range(2):
            for i in range(5):add('reference_head_knot',B(-.45+strand*.55+i*.075,1.6,-.20,.06,1.65,.28,8))

    # Continuously widening bell sleeves. Thin slices preserve the angled outline
    # while leaving a real opening and a separate, shaded inner lining.
    for side,sign in [('right',-1),('left',1)]:
        center=sign*1.0;name=side+'_sleeve'
        lookup[name]['boxes']=[B(center-2.38,-2.32,-2.5,4.76,4.72,5,0)]
        for i in range(24):
            y=2.4+i*.30;t=(i+.5)/24;w=4.85+2.75*t;d=5.05+2.45*t;x=center-w/2;z=-d/2
            mat=0 if i<19 else (5 if divine else 1)
            # Corner walls meet edge-to-edge. Shared exterior planes would fight
            # at oblique angles even when both faces sample the same palette.
            add(name,B(x,y,z,w,.30,.25,mat),B(x,y,-z-.25,w,.30,.25,mat),B(x,y,z+.25,.25,.30,d-.5,mat),B(x+w-.25,y,z+.25,.25,.30,d-.5,mat))
            add(name,B(x+.26,y,z+.28,w-.52,.30,.10,6),B(x+.26,y,-z-.38,w-.52,.30,.10,6))
            # Colored outer edges, rather than a plain rectangular white sleeve.
            add(name,B(x-.025,y,z-.10,.38,.30,.08,5 if divine else 1),B(x+w-.355,y,z-.10,.38,.30,.08,5 if divine else 1))
        x=center-3.83
        add(name,B(x,9.60,-3.79,7.66,.16,.15,4),B(x,9.60,3.64,7.66,.16,.15,4),B(x,9.60,-3.64,.15,.16,7.28,4),B(x+7.51,9.60,-3.64,.15,.16,7.28,4))
        # Pixel-stepped cuff pattern follows the tapered surface, so it never floats.
        for i in range(8):
            h=.42+(i%3)*.23;y=8.1-h;d=5.05+2.45*((y-2.4)/7.2)
            add(name,B(center-3.08+i*.83,y,-d/2-.12,.45,h+.2,.15,accent))
            if divine:add(name,B(center-2.98+i*.83,y+.12,-d/2-.21,.22,h*.6,.10,16))
        # The outer cloth hangs lower behind the wrist; fingers remain in the opening.
        node(side+'_reference_train',name,(center,4.5,3.38),(8,0,-sign*3),[B(-3.0,0,-.18,6,5.5,.36,0),B(-3.12,5.5,-.22,6.24,.75,.44,5 if divine else 1),B(-3.15,6.25,-.24,6.3,.13,.48,4)])
        # Fine raised folds fan out toward the cuff, independent of its trim.
        for fold in [-2,-1,1,2]:
            beam(side+'_reference_sleeve_fold_'+str(fold),name,
                 (center+fold*.71,2.72,-2.72),
                 (center+fold*1.10,7.35,-3.51),.075,6)

        # Ankle-length hakama with alternating faces forming actual pleat ridges.
        lookup[side+'_hakama']['boxes']=[]
        for i in range(19):
            w=4.7+i*.035;d=5.6+i*.035
            add(side+'_hakama',B(-w/2,-.2+i*.5,-d/2,w,.5,d,5))
        for face in [-1,1]:
            for i in range(7):
                x=-2.35+i*.78
                for edge in [-1,1]:
                    node(side+'_reference_pleat_'+str(face)+'_'+str(i)+'_'+str(edge),side+'_hakama',(x,.0,face*3.12),(0,edge*23,-(x/2.4)*2),[B(-.19,0,-.07,.38,9.15,.14,12 if edge>0 else 5)])

        # Long split coat: white inner panel, colored outer edge and hanging paper.
        coat=side+'_coat';lookup[coat]['boxes']=[]
        for i in range(18):
            y=i*.5;w=(2.65+i*.045) if divine else (2.0-i*.025);z=-3.38-i*.018
            center=-sign*.12 if divine else sign*.25
            add(coat,B(-w/2+center,y,z,w,.5,.28,0))
        node(side+'_reference_outer_coat',coat,(sign*1.70,0,-3.74),(0,0,-sign*3),[B(-.62,0,-.15,1.24,8.9,.3,5 if divine else 1),B(-.63,0,-.23,.12,8.9,.09,4),B(.51,0,-.23,.12,8.9,.09,4)])
        add(coat,B(-1.86,9.0,-3.79,3.72,.13,.44,4))
        for j in range(4):
            node(side+'_reference_paper_'+str(j),coat,(sign*1.5+(.15 if j%2 else 0),.7+j*1.17,-4.04),(0,0,(-1 if j%2 else 1)*25),[B(-.28,0,-.1,.56,1.27,.20,13)])
        node(side+'_reference_back_coat',coat,(0,.8,3.05),(6,0,-sign*3),[B(-1.6,0,-.15,3.2,7.4,.3,0),B(-1.7,7.4,-.17,3.4,.65,.34,5 if divine else 1),B(-1.73,8.05,-.2,3.46,.12,.4,4)])
        # Split-toe tabi, a layered wooden sole and two angled fabric straps.
        foot=side+'_tabi'
        lookup[foot]['boxes']=[B(-2.31,8.3,-2.32,4.62,3.35,4.64,13),B(-2.45,11.2,-3.48,4.9,1.1,5.9,9),B(-2.46,11.0,-3.49,4.92,.2,5.92,1 if not divine else 5),B(-2.44,11.22,-3.52,4.88,.14,.10,14),B(-2.44,11.91,-3.52,4.88,.14,.10,14),B(.12,10.1,-2.40,.08,1.05,.08,6),B(-1.6,10.65,-3.51,3.2,.36,.35,accent)]
        for edge in [-1,1]:beam(side+'_reference_geta_strap_'+str(edge),foot,(edge*1.65,10.72,-1.15),(0,10.3,-3.26),.43,accent)

    # True two-strand rope wrapping the waist in 3D, including its side/back faces.
    if not divine:
        lookup['obi']['boxes']=[B(-4.55,8.35,-3,9.1,2.3,6,5),B(-.68,9.0,-4.05,1.36,1.2,.65,8)]
    for strand in range(2):
        for i in range(64):
            def point(k):
                theta=k*math.tau/64;phase=theta*8+strand*math.pi;out=.16*math.cos(phase)
                def power(v):return math.copysign(abs(v)**.38,v)
                return ((4.86+out)*power(math.cos(theta)),9.48+.20*math.sin(phase),(3.43+out)*power(math.sin(theta)))
            beam('reference_waist_rope_'+str(strand)+'_'+str(i),'obi',point(i),point(i+1),.40,8 if strand else 4)
    for sign in [-1,1]:
        for i in range(14):
            a=i*math.tau/14;b=(i+1)*math.tau/14
            def loop(t):return (sign*(.55+1.05*(1+math.cos(t))/2),10.4+.48*math.sin(t),-4.0+.12*math.sin(t*2))
            beam('reference_waist_bow_'+str(sign)+'_'+str(i),'obi',loop(a),loop(b),.23,4)

    # Rounded golden bells instead of square plaques (the divine gems are retained).
    if not divine:
        for side in ['right','left']:
            # The golden binding is a separate outside layer, with no shared
            # side plane against the broader cloth underneath.
            lookup[side+'_shoulder_drape']['boxes'][1]['from'][0]= -1.03
            lookup[side+'_shoulder_drape']['boxes'][1]['size'][0]=2.06
            name=side+'_bell';lookup[name]['boxes']=[]
            for i in range(7):
                y=-.92+i*.27;radius=math.sqrt(max(.02,1-(y+.135)**2))*.98
                for x,w,h in [(-radius*.52,radius*1.04,radius*1.5),(-radius,radius*.48,radius), (radius*.52,radius*.48,radius)]:
                    add(name,B(x,y,-h/2,w,.275,h,4))
            add(name,B(-.10,-.15,-.86,.20,.65,.14,9),B(-.26,.95,-.23,.52,.38,.46,14),B(-.18,1.33,-.14,.36,1.0,.28,4),B(-.48,2.30,-.28,.96,.42,.56,8))
            for i in range(7):add(name,B(-.45+i*.13,2.72,-.23,.10,1.9+(i%2)*.15,.46,8))
    else:
        for b in source:
            if b['name'].startswith(('divine_flame_','divine_head_flame_')):
                for c in b['boxes']:
                    if c['material']==17:c['from'][1]+=.025

    # Rounded, longitudinally tapered tails. Three disjoint rectangles form each
    # octagonal cross-section: no overlapping front planes and no flat fur cards.
    for i in range(9):
        root='tail_'+str(i)
        for suffix,z,length,r0,r1 in [('_base',0,3.0,.72,1.05),('_mid',3.0,3.2,1.05,1.65),('_end',3.2,3.2,1.65,1.85),('_tip',3.2,3.2,1.85,.08)]:
            b=lookup[root+suffix];b['boxes']=[]
            if suffix!='_base':b['pivot']=[0,0,z]
            for section in range(8):
                t=(section+.5)/8;r=r0+(r1-r0)*t;h=r*.88;dz=length/8
                mat=(18 if divine else 3) if suffix=='_tip' and section<5 else (16 if divine else 3) if suffix=='_tip' else 2
                if divine and suffix=='_tip' and section>=7:mat=17
                for x,w,halfheight in [(-r*.50,r,h),(-r,r*.50,h*.71),(r*.5,r*.50,h*.71)]:
                    b['boxes'].append(B(x,-halfheight,section*dz,w,halfheight*2,dz,mat))
            if suffix in ['_mid','_end']:
                for sign in [-1,1]:
                    node(root+suffix+'_reference_tuft_'+str(sign),root+suffix,(sign*r1*.82,0,length*.62),(0,sign*18,0),[B(-.18,-r1*.45,0,.36,r1*.9,1.2,2)])
    data['reference_remodel']='Triangular ears; flared lined sleeves; long pleated hakama; full 3D waist braid; rounded sculpted tails'
    for b in source:
        for c in b['boxes']:
            w,h,d=c['size'];assert min(w,h,d)>0 and 1+2*(w+d)<32 and 1+h+d<32,(b['name'],c)
    return data
