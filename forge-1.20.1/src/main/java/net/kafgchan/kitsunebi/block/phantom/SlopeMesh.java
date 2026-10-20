package net.kafgchan.kitsunebi.block.phantom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.ToDoubleFunction;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;

/** Closed, continuous wedge surfaces in block units. No visual stair slices. */
public final class SlopeMesh {
    public record Point(double x, double y, double z) {}
    private record Plan(double x, double z) {}
    public record Face(List<Point> points) {}

    public static List<Face> create(StairSlopeSize size, Direction facing, Half half, StairsShape shape) {
        double h = size.getMaxHeight()/16.0;
        double d = size.getMaxDepth()/16.0;
        List<Plan> footprint = List.of(new Plan(0,0), new Plan(0,d), new Plan(1,d), new Plan(1,0));
        ToDoubleFunction<Plan> primary = p -> h*(1-p.z/d);
        boolean left = shape == StairsShape.INNER_LEFT || shape == StairsShape.OUTER_LEFT;
        ToDoubleFunction<Plan> side = p -> h*(1-(left ? p.x : 1-p.x)/d);
        List<Face> faces = new ArrayList<>();
        if (shape == StairsShape.STRAIGHT) {
            extrude(footprint, primary, faces);
        } else {
            boolean inner = shape == StairsShape.INNER_LEFT || shape == StairsShape.INNER_RIGHT;
            List<Plan> domain = inner ? List.of(new Plan(0,0), new Plan(0,1), new Plan(1,1), new Plan(1,0)) : clip(footprint, side);
            ToDoubleFunction<Plan> difference = p -> primary.applyAsDouble(p)-side.applyAsDouble(p);
            extrude(clip(clip(domain, primary), p -> (inner ? 1 : -1)*difference.applyAsDouble(p)), primary, faces);
            extrude(clip(clip(domain, side), p -> (inner ? -1 : 1)*difference.applyAsDouble(p)), side, faces);
        }
        int turns = switch(facing) { case EAST -> 1; case SOUTH -> 2; case WEST -> 3; default -> 0; };
        List<Face> transformed = new ArrayList<>();
        for (Face face : faces) {
            List<Point> points = new ArrayList<>();
            for (Point p : face.points) {
                double x=p.x, z=p.z;
                for (int i=0;i<turns;i++) { double next=1-z; z=x; x=next; }
                points.add(new Point(x, half == Half.TOP ? 1-p.y : p.y, z));
            }
            if (half == Half.TOP) Collections.reverse(points);
            transformed.add(new Face(List.copyOf(points)));
        }
        return List.copyOf(transformed);
    }

    /** Clip a convex polygon to f(point) >= 0, keeping exact plane intersections. */
    private static List<Plan> clip(List<Plan> polygon, ToDoubleFunction<Plan> f) {
        List<Plan> result = new ArrayList<>();
        if (polygon.isEmpty()) return result;
        Plan a=polygon.get(polygon.size()-1); double fa=f.applyAsDouble(a);
        for (Plan b : polygon) {
            double fb=f.applyAsDouble(b);
            if ((fa>=0)!=(fb>=0)) {
                double t=fa/(fa-fb);
                result.add(new Plan(a.x+(b.x-a.x)*t,a.z+(b.z-a.z)*t));
            }
            if (fb>=0) result.add(b);
            a=b; fa=fb;
        }
        return result;
    }

    private static void extrude(List<Plan> polygon, ToDoubleFunction<Plan> height, List<Face> faces) {
        if (polygon.size()<3) return;
        List<Point> top=new ArrayList<>(), bottom=new ArrayList<>();
        for (Plan p : polygon) {
            top.add(new Point(p.x,Math.max(0,height.applyAsDouble(p)),p.z));
            bottom.add(new Point(p.x,0,p.z));
        }
        triangles(top,faces);
        Collections.reverse(bottom); triangles(bottom,faces);
        for (int i=0;i<top.size();i++) {
            Point a=top.get(i),b=top.get((i+1)%top.size());
            triangles(List.of(b,a,new Point(a.x,0,a.z),new Point(b.x,0,b.z)),faces);
        }
    }

    private static void triangles(List<Point> polygon,List<Face> faces) {
        for(int i=1;i<polygon.size()-1;i++) {
            Point a=polygon.get(0),b=polygon.get(i),c=polygon.get(i+1);
            double ux=b.x-a.x,uy=b.y-a.y,uz=b.z-a.z;
            double vx=c.x-a.x,vy=c.y-a.y,vz=c.z-a.z;
            double nx=uy*vz-uz*vy,ny=uz*vx-ux*vz,nz=ux*vy-uy*vx;
            if(nx*nx+ny*ny+nz*nz>1e-16) faces.add(new Face(List.of(a,b,c)));
        }
    }
}
