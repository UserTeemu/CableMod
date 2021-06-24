package dev.userconor.cablesandpipes.utils;

import net.minecraft.util.math.Direction;

import static net.minecraft.util.math.Direction.Axis.*;
import static net.minecraft.util.math.Direction.Axis.Y;

public class DirectionUtil {
    public static Direction[] getNeighborDirections(Direction direction) {
        Direction[] array = new Direction[4];
        switch (direction.getAxis()) {
            case X -> {
                array[0] = Direction.from(Y, Direction.AxisDirection.NEGATIVE);
                array[1] = Direction.from(Y, Direction.AxisDirection.POSITIVE);
                array[2] = Direction.from(Z, Direction.AxisDirection.NEGATIVE);
                array[3] = Direction.from(Z, Direction.AxisDirection.POSITIVE);
            }
            case Y -> {
                array[0] = Direction.from(X, Direction.AxisDirection.NEGATIVE);
                array[1] = Direction.from(X, Direction.AxisDirection.POSITIVE);
                array[2] = Direction.from(Z, Direction.AxisDirection.NEGATIVE);
                array[3] = Direction.from(Z, Direction.AxisDirection.POSITIVE);
            }
            case Z -> {
                array[0] = Direction.from(X, Direction.AxisDirection.NEGATIVE);
                array[1] = Direction.from(X, Direction.AxisDirection.POSITIVE);
                array[2] = Direction.from(Y, Direction.AxisDirection.NEGATIVE);
                array[3] = Direction.from(Y, Direction.AxisDirection.POSITIVE);
            }
        }
        return array;
    }
}
