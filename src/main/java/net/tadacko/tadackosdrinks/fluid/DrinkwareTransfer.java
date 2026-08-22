package net.tadacko.tadackosdrinks.fluid;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.Optional;

// Shared logic for exchanging fluid between an IFluidHandler (e.g. a keg's tank)
// and a "drinkware" ItemStack (either a held item or one stored in a block entity).
// Callers are responsible for actually swapping the ItemStack and playing sounds.
public final class DrinkwareTransfer {

    private DrinkwareTransfer() {}

    // If the tank holds a fluid this item is the "empty" drinkware for, and there's
    // enough of it, drains the serving amount from the tank and returns the matching "full" item.
    public static Optional<Item> tryFill(IFluidHandler tank, ItemStack drinkware) {
        if (drinkware.isEmpty()) return Optional.empty();

        FluidStack inTank = tank.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
        if (inTank.isEmpty()) return Optional.empty();

        DrinkwareFluidRegistry.Entry entry = DrinkwareFluidRegistry.getByFluid(inTank.getFluid());
        if (entry == null || drinkware.getItem() != entry.emptyItem() || inTank.getAmount() < entry.servingMb()) {
            return Optional.empty();
        }

        tank.drain(entry.servingMb(), IFluidHandler.FluidAction.EXECUTE);
        return Optional.of(entry.fullItem());
    }

    // If this item is a known "full" drinkware and the tank has room for its fluid
    // (and either is empty or already holds the same fluid), fills the tank and
    // returns the matching "empty" item.
    public static Optional<Item> tryEmpty(IFluidHandler tank, ItemStack drinkware) {
        if (drinkware.isEmpty()) return Optional.empty();

        var fluid = DrinkwareFluidRegistry.getFluidForFull(drinkware.getItem());
        if (fluid == null) return Optional.empty();

        DrinkwareFluidRegistry.Entry entry = DrinkwareFluidRegistry.getByFluid(fluid);
        if (entry == null) return Optional.empty();

        FluidStack toAdd = new FluidStack(fluid, entry.servingMb());
        FluidStack inTank = tank.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);

        if (!(inTank.isEmpty() || inTank.isFluidEqual(toAdd))) return Optional.empty();
        if (tank.fill(toAdd, IFluidHandler.FluidAction.SIMULATE) != toAdd.getAmount()) return Optional.empty();

        tank.fill(toAdd, IFluidHandler.FluidAction.EXECUTE);
        return Optional.of(entry.emptyItem());
    }
}