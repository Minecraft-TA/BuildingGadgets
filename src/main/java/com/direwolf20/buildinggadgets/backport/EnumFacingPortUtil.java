package com.direwolf20.buildinggadgets.backport;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class EnumFacingPortUtil {

    public static EnumFacing fromSideHit(int sideHit) {
        /**
         * Which side was hit. If its -1 then it went the full length of the ray trace. Bottom = 0, Top = 1, East = 2, West
         * = 3, North = 4, South = 5.
         */
        return switch (sideHit) {
            case 0 -> EnumFacing.DOWN;
            case 1 -> EnumFacing.UP;
            case 2 -> EnumFacing.EAST;
            case 3 -> EnumFacing.WEST;
            case 4 -> EnumFacing.NORTH;
            case 5 -> EnumFacing.SOUTH;
            default -> null; // TODO: null?
        };
    }

    public static EnumFacing getOpposite(EnumFacing facing) {
        return switch (facing) {
            case DOWN -> EnumFacing.UP;
            case UP -> EnumFacing.DOWN;
            case NORTH -> EnumFacing.SOUTH;
            case SOUTH -> EnumFacing.NORTH;
            case WEST -> EnumFacing.EAST;
            case EAST -> EnumFacing.WEST;
        };
    }

    public static EnumFacing getHorizontalFacing(Entity entity) {
        return EnumFacingPortUtil.byHorizontalIndex(MathHelper.floor_double((double) (entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3);
    }

    public static EnumFacing byHorizontalIndex(int index) {
        return switch (index) {
            case 0 -> EnumFacing.SOUTH;
            case 1 -> EnumFacing.WEST;
            case 2 -> EnumFacing.NORTH;
            case 3 -> EnumFacing.EAST;
            default -> throw new IllegalArgumentException("Invalid index! " + index);
        };
    }

    public static EnumFacing getFacingFromAxis(EnumFacingPortUtil.AxisDirection axisDirectionIn, EnumFacingPortUtil.Axis axisIn) {
        for (EnumFacing enumfacing : EnumFacing.values()) {
            if (getAxisDirection(enumfacing) == axisDirectionIn && getAxis(enumfacing) == axisIn) {
                return enumfacing;
            }
        }

        throw new IllegalArgumentException("No such direction: " + axisDirectionIn + " " + axisIn);
    }

    public static EnumFacing rotateY(EnumFacing facing)
    {
        return switch (facing) {
            case NORTH -> EnumFacing.EAST;
            case EAST -> EnumFacing.SOUTH;
            case SOUTH -> EnumFacing.WEST;
            case WEST -> EnumFacing.NORTH;
            default -> throw new IllegalStateException("Unable to get Y-rotated facing of " + facing);
        };
    }

    public static Axis getAxis(EnumFacing facing) {
        return switch (facing) {
            case DOWN, UP -> Axis.Y;
            case NORTH, SOUTH -> Axis.Z;
            case WEST, EAST -> Axis.X;
        };
    }

    public static AxisDirection getAxisDirection(EnumFacing facing) {
        return switch (facing) {
            case DOWN, NORTH, WEST -> AxisDirection.NEGATIVE;
            case UP, SOUTH, EAST -> AxisDirection.POSITIVE;
        };
    }

    public static enum Axis implements Predicate<EnumFacing> {
        X("x", Plane.HORIZONTAL),
        Y("y", Plane.VERTICAL),
        Z("z", Plane.HORIZONTAL);

        private static final Map<String, Axis> NAME_LOOKUP = Maps.<String, Axis>newHashMap();
        private final String name;
        private final Plane plane;

        private Axis(String name, Plane plane) {
            this.name = name;
            this.plane = plane;
        }

        @Nullable
        public static Axis byName(String name) {
            return name == null ? null : (Axis) NAME_LOOKUP.get(name.toLowerCase(Locale.ROOT));
        }

        public String getName2() {
            return this.name;
        }

        public boolean isVertical() {
            return this.plane == Plane.VERTICAL;
        }

        public boolean isHorizontal() {
            return this.plane == Plane.HORIZONTAL;
        }

        public String toString() {
            return this.name;
        }

        public boolean apply(@Nullable EnumFacing p_apply_1_) {
            return p_apply_1_ != null && getAxis(p_apply_1_) == this;
        }

        public Plane getPlane() {
            return this.plane;
        }

        public String getName() {
            return this.name;
        }

        static {
            for (Axis enumfacing$axis : values()) {
                NAME_LOOKUP.put(enumfacing$axis.getName2().toLowerCase(Locale.ROOT), enumfacing$axis);
            }
        }
    }

    public static enum AxisDirection {
        POSITIVE(1, "Towards positive"),
        NEGATIVE(-1, "Towards negative");

        private final int offset;
        private final String description;

        private AxisDirection(int offset, String description) {
            this.offset = offset;
            this.description = description;
        }

        public int getOffset() {
            return this.offset;
        }

        public String toString() {
            return this.description;
        }
    }

    public static enum Plane implements Predicate<EnumFacing>, Iterable<EnumFacing> {
        HORIZONTAL,
        VERTICAL;

        public EnumFacing[] facings() {
            switch (this) {
                case HORIZONTAL:
                    return new EnumFacing[]{EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST};
                case VERTICAL:
                    return new EnumFacing[]{EnumFacing.UP, EnumFacing.DOWN};
                default:
                    throw new Error("Someone's been tampering with the universe!");
            }
        }

        public EnumFacing random(Random rand) {
            EnumFacing[] aenumfacing = this.facings();
            return aenumfacing[rand.nextInt(aenumfacing.length)];
        }

        public boolean apply(@Nullable EnumFacing p_apply_1_) {
            return p_apply_1_ != null && getAxis(p_apply_1_).getPlane() == this;
        }

        public Iterator<EnumFacing> iterator() {
            return Iterators.<EnumFacing>forArray(this.facings());
        }
    }
}
