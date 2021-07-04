package dev.userteemu.cablemod.block.cable;

import net.minecraft.block.Block;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import static net.minecraft.util.math.Direction.*;

@SuppressWarnings("unused") // all enum constants have a purpose, even if they aren't ever directly referenced
public enum CableShape implements StringIdentifiable {
    // straight shapes
    NORTH_SOUTH(NORTH, SOUTH),
    EAST_WEST(EAST, WEST),
    UP_DOWN(UP, DOWN),

    // turns from down
    DOWN_NORTH(DOWN, NORTH),
    DOWN_EAST(DOWN, EAST),
    DOWN_SOUTH(DOWN, SOUTH),
    DOWN_WEST(DOWN, WEST),

    // turns from up
    UP_NORTH(UP, NORTH),
    UP_EAST(UP, EAST),
    UP_SOUTH(UP, SOUTH),
    UP_WEST(UP, WEST),

    // the rest
    NORTH_WEST(NORTH, WEST),
    NORTH_EAST(NORTH, EAST),
    SOUTH_WEST(SOUTH, WEST),
    SOUTH_EAST(SOUTH, EAST);

    public static final int cableThickness = 6;

    public final Direction from;
    public final Direction to;
    public VoxelShape cableShape;

    CableShape(Direction from, Direction to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public String asString() {
        return from.getName()+"_"+to.getName();
    }

    @Override
    public String toString() {
        return this.asString();
    }

    public boolean connectsTo(Direction direction) {
        return from == direction || to == direction;
    }

    public Direction getOtherDirection(Direction direction) {
        if (direction == from) return to;
        else if (direction == to) return from;
        else throw new IllegalArgumentException("Direction must be either \"from\" direction or \"to\" direction");
    }

    public boolean isStraight() {
        return from == to.getOpposite();
    }

    private VoxelShape createShape() {
        VoxelShape middle = Block.createCuboidShape(5D, 5D, 5D, 11D, 11D, 11D);

        return VoxelShapes.union(createCablePart(from, middle), middle, createCablePart(to, middle));
    }

    private static VoxelShape createCablePart(Direction direction, VoxelShape middle) {
        Vec3f vec = direction.getUnitVector();
        vec.multiplyComponentwise(5F / 16F, 5F / 16F, 5F / 16F);
        return middle.offset(vec.getX(), vec.getY(), vec.getZ());
    }

    public static void createShapes() {
        for (CableShape shape : values()) {
            shape.cableShape = shape.createShape();
        }
    }

    public static CableShape shapeOf(Direction a, Direction b) {
        if (a == b) throw new IllegalArgumentException("Directions cannot be the same");
        if (a == null || b == null) throw new IllegalArgumentException("Direction may not be null");
        for (CableShape shape : CableShape.values()) {
            if (shape.from == a) {
                if (shape.to == b) return shape;
            } else if (shape.from == b) {
                if (shape.to == a) return shape;
            }
        }
        throw new IllegalStateException("No all cable shape combinations exist! Please report this to the creators of Cables and Pipes");
    }
}
