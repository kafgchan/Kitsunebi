package net.kafgchan.kitsunebi.client;

import java.util.ArrayList;
import java.util.List;
import net.kafgchan.kitsunebi.block.phantom.SlopeMesh;
import net.kafgchan.kitsunebi.block.phantom.StairSlopeSize;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;

/** Client-only renderer. Fabric embeds the compiled shared class in its client jar. */
public final class SmoothSlopeModel implements BakedModel {
    private final BakedModel original;
    private final List<BakedQuad> quads;

    public static BakedModel replace(ModelResourceLocation id, BakedModel original) {
        if (id == null || original == null || original instanceof SmoothSlopeModel
                || !id.id().getNamespace().equals("kitsunebi")
                || !id.id().getPath().startsWith("phantom_slope_")) return original;
        String sizeName=id.id().getPath().substring("phantom_slope_".length());
        StairSlopeSize size=switch(sizeName) {
            case "high" -> StairSlopeSize.HIGH; case "mid" -> StairSlopeSize.MID;
            case "low" -> StairSlopeSize.LOW; default -> null;
        };
        if(size==null) return original;
        Direction facing=Direction.NORTH; Half half=Half.BOTTOM; StairsShape shape=StairsShape.STRAIGHT;
        for(String property : id.getVariant().split(",")) {
            String[] pair=property.split("="); if(pair.length!=2) continue;
            switch(pair[0]) {
                case "facing" -> facing=Direction.byName(pair[1]);
                case "half" -> half=pair[1].equals("top")?Half.TOP:Half.BOTTOM;
                case "shape" -> shape=StairsShape.valueOf(pair[1].toUpperCase(java.util.Locale.ROOT));
            }
        }
        return new SmoothSlopeModel(original,SlopeMesh.create(size,facing,half,shape));
    }

    @SuppressWarnings("deprecation")
    private SmoothSlopeModel(BakedModel original,List<SlopeMesh.Face> faces) {
        this.original=original;
        TextureAtlasSprite sprite=original.getParticleIcon();
        List<BakedQuad> result=new ArrayList<>();
        for(var face:faces) {
            var a=face.points().get(0); var b=face.points().get(1); var c=face.points().get(2);
            double ux=b.x()-a.x(),uy=b.y()-a.y(),uz=b.z()-a.z();
            double vx=c.x()-a.x(),vy=c.y()-a.y(),vz=c.z()-a.z();
            double nx=uy*vz-uz*vy,ny=uz*vx-ux*vz,nz=ux*vy-uy*vx;
            double length=Math.sqrt(nx*nx+ny*ny+nz*nz); nx/=length; ny/=length; nz/=length;
            int normal=((int)(nx*127)&255)|(((int)(ny*127)&255)<<8)|(((int)(nz*127)&255)<<16);
            Direction direction=Direction.getNearest((float)nx,(float)ny,(float)nz);
            int[] data=new int[32];
            for(int i=0;i<4;i++) {
                var p=face.points().get(Math.min(i,2)); int offset=i*8;
                data[offset]=Float.floatToRawIntBits((float)p.x());
                data[offset+1]=Float.floatToRawIntBits((float)p.y());
                data[offset+2]=Float.floatToRawIntBits((float)p.z());
                data[offset+3]=-1;
                // Continuous planar mapping; no repeated texture strip per step.
                double u=Math.abs(ny)>0.1?p.x():(Math.abs(nx)>0.5?p.z():p.x());
                double v=Math.abs(ny)>0.1?p.z():1-p.y();
                data[offset+4]=Float.floatToRawIntBits(sprite.getU((float)u));
                data[offset+5]=Float.floatToRawIntBits(sprite.getV((float)v));
                data[offset+7]=normal;
            }
            result.add(new BakedQuad(data,-1,direction,sprite,true));
        }
        quads=List.copyOf(result);
    }

    @Override public List<BakedQuad> getQuads(BlockState state,Direction side,RandomSource random) {return side==null?quads:List.of();}
    @Override public boolean useAmbientOcclusion(){return false;}
    @Override public boolean isGui3d(){return true;}
    @Override public boolean usesBlockLight(){return true;}
    @Override public boolean isCustomRenderer(){return false;}
    @Override @SuppressWarnings("deprecation") public TextureAtlasSprite getParticleIcon(){return original.getParticleIcon();}
    @Override @SuppressWarnings("deprecation") public ItemTransforms getTransforms(){return original.getTransforms();}
    @Override public ItemOverrides getOverrides(){return original.getOverrides();}
}
