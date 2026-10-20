package net.kafgchan.kitsunebi.block.phantom;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToDoubleFunction;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.AABB;

/** Exact height of the visible surface under an entity's horizontal footprint. */
public final class SlopeSurface {
    private record Key(StairSlopeSize size, Direction facing, StairsShape shape) {}
    private static final ConcurrentHashMap<Key,List<SlopeMesh.Face>> CACHE = new ConcurrentHashMap<>();

    public static double support(StairSlopeSize size, Direction facing, StairsShape shape, AABB footprint) {
        var mesh=CACHE.computeIfAbsent(new Key(size,facing,shape),
                k -> SlopeMesh.create(k.size,k.facing,Half.BOTTOM,k.shape));
        double height=Double.NEGATIVE_INFINITY;
        for(var face:mesh) {
            var a=face.points().get(0);var b=face.points().get(1);var c=face.points().get(2);
            // Only the upward-facing sloped surfaces, not base or side walls.
            double normalY=(b.z()-a.z())*(c.x()-a.x())-(b.x()-a.x())*(c.z()-a.z());
            if(normalY<=1e-10) continue;
            List<SlopeMesh.Point> polygon=face.points();
            polygon=clip(polygon,p->p.x()-footprint.minX);
            polygon=clip(polygon,p->footprint.maxX-p.x());
            polygon=clip(polygon,p->p.z()-footprint.minZ);
            polygon=clip(polygon,p->footprint.maxZ-p.z());
            for(var p:polygon) height=Math.max(height,p.y());
        }
        return height;
    }

    private static List<SlopeMesh.Point> clip(List<SlopeMesh.Point> polygon,ToDoubleFunction<SlopeMesh.Point> f) {
        List<SlopeMesh.Point> out=new ArrayList<>();
        if(polygon.isEmpty()) return out;
        var a=polygon.getLast(); double fa=stable(f.applyAsDouble(a));
        for(var b:polygon) {
            double fb=stable(f.applyAsDouble(b));
            if((fa>=0)!=(fb>=0)) {
                double t=fa/(fa-fb);
                out.add(new SlopeMesh.Point(a.x()+(b.x()-a.x())*t,a.y()+(b.y()-a.y())*t,a.z()+(b.z()-a.z())*t));
            }
            if(fb>=0) out.add(b);
            a=b;fa=fb;
        }
        return out;
    }

    private static double stable(double distance) {
        return Math.abs(distance)<1e-10?0:distance;
    }
}
