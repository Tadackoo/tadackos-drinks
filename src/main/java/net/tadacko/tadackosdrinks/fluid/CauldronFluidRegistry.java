package net.tadacko.tadackosdrinks.fluid;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class CauldronFluidRegistry {
    public record Entry(Fluid fluid, int amount) {}

    private static final Map<Block, Entry> BY_BLOCK = new HashMap<>();
    private static final Map<Fluid, Block> BY_FLUID = new HashMap<>();

    private CauldronFluidRegistry() {}

    public static void register(Block cauldronBlock, Fluid fluid, int amount) {
        BY_BLOCK.put(cauldronBlock, new Entry(fluid, amount));
        BY_FLUID.put(fluid, cauldronBlock);
    }

    @Nullable
    public static Entry getForBlock(Block block) { return BY_BLOCK.get(block); }

    @Nullable
    public static Block getCauldronForFluid(Fluid fluid) { return BY_FLUID.get(fluid); }
}