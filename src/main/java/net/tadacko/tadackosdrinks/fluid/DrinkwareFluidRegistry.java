package net.tadacko.tadackosdrinks.fluid;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

// Generic mapping between a fluid and the drinkware items used to serve it,
// so any block/item dealing with raw Fluids (kegs, taps, etc.) can fill/empty
// drinkware without needing to know about every individual drink type.
public final class DrinkwareFluidRegistry {

    public record Entry(Item emptyItem, Item fullItem, int servingMb) {}

    private static final Map<Fluid, Entry> BY_FLUID = new HashMap<>();
    private static final Map<Item, Fluid> FULL_TO_FLUID = new HashMap<>();

    private DrinkwareFluidRegistry() {}

    public static void register(Fluid fluid, Item emptyItem, Item fullItem, int servingMb) {
        BY_FLUID.put(fluid, new Entry(emptyItem, fullItem, servingMb));
        FULL_TO_FLUID.put(fullItem, fluid);
    }

    @Nullable
    public static Entry getByFluid(Fluid fluid) {
        return BY_FLUID.get(fluid);
    }

    @Nullable
    public static Fluid getFluidForFull(Item fullItem) {
        return FULL_TO_FLUID.get(fullItem);
    }
}