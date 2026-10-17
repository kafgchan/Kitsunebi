package net.kafgchan.kitsunebi;

import net.kafgchan.kitsunebi.block.KitsunebiBlock;
import net.kafgchan.kitsunebi.block.phantom.*;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.CollisionContext;

/** Standalone regression harness for ParticleEngine.crack and block support. */
public final class ShapeRegression {
    public static void main(String[] args) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        // Standalone vanilla bootstrap freezes registries before mod registration.
        // Reopen only this test JVM, register fixtures, then freeze again.
        var registry = BuiltInRegistries.BLOCK;
        var frozen = net.minecraft.core.MappedRegistry.class.getDeclaredField("frozen");
        frozen.setAccessible(true);
        frozen.setBoolean(registry, false);
        var intrusive = net.minecraft.core.MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
        intrusive.setAccessible(true);
        intrusive.set(registry, new java.util.IdentityHashMap<>());
        Block[] blocks = {
            new KitsunebiBlock("white", BlockBehaviour.Properties.of()),
            new ArchitecturalKitsunebiBlock(BlockBehaviour.Properties.of()),
            new PhantomBlock(BlockBehaviour.Properties.of()),
            new PhantomSlabBlock(BlockBehaviour.Properties.of()),
            new PhantomVerticalSlabBlock(BlockBehaviour.Properties.of()),
            new PhantomQuarterSlabBlock(BlockBehaviour.Properties.of()),
            new PhantomVerticalQuarterBlock(BlockBehaviour.Properties.of()),
            new PhantomFenceBlock(BlockBehaviour.Properties.of()),
            new PhantomWallBlock(BlockBehaviour.Properties.of()),
            new PhantomDoorBlock(BlockBehaviour.Properties.of()),
            new PhantomButtonBlock(BlockBehaviour.Properties.of()),
            new PhantomPressurePlateBlock(BlockBehaviour.Properties.of()),
            new PhantomLeverBlock(BlockBehaviour.Properties.of()),
            new PhantomStairBlock(StairSlopeSize.HIGH, BlockBehaviour.Properties.of()),
            new PhantomStairBlock(StairSlopeSize.MID, BlockBehaviour.Properties.of()),
            new PhantomStairBlock(StairSlopeSize.LOW, BlockBehaviour.Properties.of()),
            new PhantomSlopeBlock(StairSlopeSize.HIGH, BlockBehaviour.Properties.of()),
            new PhantomSlopeBlock(StairSlopeSize.MID, BlockBehaviour.Properties.of()),
            new PhantomSlopeBlock(StairSlopeSize.LOW, BlockBehaviour.Properties.of())
        };
        for (int i = 0; i < blocks.length; i++) {
            Registry.register(registry, ResourceLocation.fromNamespaceAndPath("shape_test", "block_" + i), blocks[i]);
        }
        registry.freeze();
        registry.bindTags(java.util.Map.of(
                BlockTags.FENCES, java.util.List.of(blocks[7].builtInRegistryHolder()),
                BlockTags.WOODEN_FENCES, java.util.List.of(blocks[7].builtInRegistryHolder()),
                BlockTags.WALLS, java.util.List.of(blocks[8].builtInRegistryHolder())));
        var fence = (PhantomFenceBlock) blocks[7];
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (!fence.connectsTo(fence.defaultBlockState(), false, direction)) {
                throw new AssertionError("Fence does not connect: " + direction);
            }
        }
        int checked = 0;
        for (Block block : blocks) {
            for (var state : block.getStateDefinition().getPossibleStates()) {
                // A wall with no post and no arms is an unreachable, empty vanilla state.
                if (block instanceof PhantomWallBlock && !state.getValue(PhantomWallBlock.UP)
                        && state.getValue(PhantomWallBlock.NORTH_WALL).getSerializedName().equals("none")
                        && state.getValue(PhantomWallBlock.SOUTH_WALL).getSerializedName().equals("none")
                        && state.getValue(PhantomWallBlock.EAST_WALL).getSerializedName().equals("none")
                        && state.getValue(PhantomWallBlock.WEST_WALL).getSerializedName().equals("none")) continue;
                // Exact query and bounds operation from the reported crash.
                var shape = state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
                if (shape.isEmpty()) throw new AssertionError(block.getClass() + " " + state);
                shape.bounds();
                checked++;
            }
        }
        var cube = (ArchitecturalKitsunebiBlock) blocks[1];
        var state = cube.defaultBlockState();
        state.initCache();
        if (!state.isSolid()) throw new AssertionError("Item frames require solid support");
        for (Direction face : Direction.values()) {
            if (!state.isFaceSturdy(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, face)) {
                throw new AssertionError("Missing support face: " + face);
            }
        }
        if (!state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty()) {
            throw new AssertionError("Kitsunebi block must remain passable");
        }
        int meshes = 0;
        int surfaceSamples = 0;
        for (var size : StairSlopeSize.values()) {
            double h=size.getMaxHeight()/16.0,d=size.getMaxDepth()/16.0;
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                for (var shape : net.minecraft.world.level.block.state.properties.StairsShape.values()) {
                    for (int ix=1;ix<20;ix++) for(int iz=1;iz<20;iz++) {
                        double x=ix/20.0,z=iz/20.0,nx=x,nz=z;
                        int turns=switch(facing){case EAST->1;case SOUTH->2;case WEST->3;default->0;};
                        for(int t=0;t<turns;t++){double old=nx;nx=nz;nz=1-old;}
                        double expected=Double.NEGATIVE_INFINITY;
                        if(nz<=d || shape==net.minecraft.world.level.block.state.properties.StairsShape.INNER_LEFT
                                || shape==net.minecraft.world.level.block.state.properties.StairsShape.INNER_RIGHT) {
                            double primary=h*(1-nz/d);
                            boolean left=shape==net.minecraft.world.level.block.state.properties.StairsShape.INNER_LEFT
                                    ||shape==net.minecraft.world.level.block.state.properties.StairsShape.OUTER_LEFT;
                            double side=Math.max(0,h*(1-(left?nx:1-nx)/d));
                            expected=switch(shape){case STRAIGHT->primary;case INNER_LEFT,INNER_RIGHT->Math.max(primary,side);default->Math.min(primary,side);};
                        }
                        double actual=SlopeSurface.support(size,facing,shape,new net.minecraft.world.phys.AABB(x,0,z,x,1,z));
                        if(expected>1e-8 && (!Double.isFinite(actual)||Math.abs(actual-expected)>1e-8))
                            throw new AssertionError("Surface height mismatch: "+size+" "+facing+" "+shape+" "+x+","+z+" "+actual+" != "+expected);
                        surfaceSamples++;
                    }
                }
            }
            double previous=Double.NaN;
            for(int i=1;i<40;i++) {
                double z=d*(1-i/40.0);
                double y=SlopeSurface.support(size,Direction.NORTH,net.minecraft.world.level.block.state.properties.StairsShape.STRAIGHT,
                        new net.minecraft.world.phys.AABB(0.2,0,z,0.8,1,z));
                if(Double.isFinite(previous)&&Math.abs((y-previous)-h/40)>1e-8)
                    throw new AssertionError("Quantized ramp ascent");
                previous=y;
            }
        }
        System.out.println("PASS: "+surfaceSamples+" exact visual slope-height samples and continuous mesh surface.");
        for (var size : StairSlopeSize.values()) {
            double h = size.getMaxHeight()/16.0, d = size.getMaxDepth()/16.0;
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                for (var half : net.minecraft.world.level.block.state.properties.Half.values()) {
                    for (var shape : net.minecraft.world.level.block.state.properties.StairsShape.values()) {
                        var mesh = SlopeMesh.create(size, direction, half, shape);
                        double volume = 0;
                        for (var face : mesh) {
                            var a=face.points().get(0); var b=face.points().get(1); var c=face.points().get(2);
                            for (var p : face.points()) {
                                if (p.x() < -1e-9 || p.x() > 1+1e-9 || p.y() < -1e-9 || p.y() > 1+1e-9
                                        || p.z() < -1e-9 || p.z() > 1+1e-9) throw new AssertionError("Mesh outside block");
                            }
                            volume += (a.x()*(b.y()*c.z()-b.z()*c.y())
                                    +a.y()*(b.z()*c.x()-b.x()*c.z())
                                    +a.z()*(b.x()*c.y()-b.y()*c.x()))/6;
                        }
                        double expected = switch (shape) {
                            case STRAIGHT -> h*d/2;
                            case INNER_LEFT, INNER_RIGHT -> h*d-h*d*d/3;
                            case OUTER_LEFT, OUTER_RIGHT -> h*d*d/3;
                        };
                        if (Math.abs(volume-expected)>1e-8) throw new AssertionError("Wedge volume/winding: "
                                +size+" "+direction+" "+half+" "+shape+" "+volume+" != "+expected);
                        meshes++;
                    }
                }
            }
        }
        System.out.println("PASS: " + meshes + " continuous slope meshes: corner volumes, winding, bounds, rotations and inversion.");
        System.out.println("PASS: " + checked + " engine shape/bounds queries; six support faces; solid attachment support; no collision.");
    }
}
