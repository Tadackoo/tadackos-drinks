package net.tadacko.tadackosdrinks.block;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

public enum CondenserPos implements StringRepresentable {
    NONE("none", null, false),
    NORTH("north", Direction.NORTH, false),
    SOUTH("south", Direction.SOUTH, false),
    EAST("east", Direction.EAST, false),
    WEST("west", Direction.WEST, false),
    // "_BELOW" = this segment has no condenser of its own, but a condenser is
    // attached lower down in the same column, so a pipe passes through here.
    NORTH_BELOW("north_below", Direction.NORTH, true),
    SOUTH_BELOW("south_below", Direction.SOUTH, true),
    EAST_BELOW("east_below", Direction.EAST, true),
    WEST_BELOW("west_below", Direction.WEST, true);

    private final String serializedName;
    private final Direction direction;
    private final boolean belowVariant;

    CondenserPos(String serializedName, Direction direction, boolean belowVariant) {
        this.serializedName = serializedName;
        this.direction = direction;
        this.belowVariant = belowVariant;
    }

    /** Horizontal direction of the attachment/pipe. Null for NONE. */
    public Direction toDirection() {
        return direction;
    }

    /** True if this is a pass-through marker (condenser is attached at a lower segment). */
    public boolean isBelowVariant() {
        return belowVariant;
    }

    /** Converts a direct attachment into its pass-through counterpart (NORTH -> NORTH_BELOW, etc). */
    public CondenserPos asBelowVariant() {
        return switch (this) {
            case NORTH, NORTH_BELOW -> NORTH_BELOW;
            case SOUTH, SOUTH_BELOW -> SOUTH_BELOW;
            case EAST, EAST_BELOW -> EAST_BELOW;
            case WEST, WEST_BELOW -> WEST_BELOW;
            default -> NONE;
        };
    }

    public static CondenserPos fromDirection(Direction direction) {
        if (direction == null) return NONE;
        return switch (direction) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            default -> NONE;
        };
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}