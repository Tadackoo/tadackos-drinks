package net.tadacko.tadackosdrinks.client;

import net.minecraft.core.BlockPos;

public class TrellisWireClientState {
    // Long.MIN_VALUE == no pos
    private static volatile long firstPosLong = Long.MIN_VALUE;

    public static void setFirstPos(long posLong) {
        firstPosLong = posLong;
    }

    public static void clearFirstPos() {
        firstPosLong = Long.MIN_VALUE;
    }

    public static boolean hasFirstPos() {
        return firstPosLong != Long.MIN_VALUE;
    }

    public static BlockPos getFirstPos() {
        if (firstPosLong == Long.MIN_VALUE) return null;
        return BlockPos.of(firstPosLong);
    }
}
