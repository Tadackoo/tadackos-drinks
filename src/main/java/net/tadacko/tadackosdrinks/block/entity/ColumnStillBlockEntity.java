package net.tadacko.tadackosdrinks.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.tadacko.tadackosdrinks.block.CondenserPos;
import net.tadacko.tadackosdrinks.block.ColumnStillBlock;
import net.tadacko.tadackosdrinks.fluid.ModFluids;
import net.tadacko.tadackosdrinks.util.IFluidColorProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class ColumnStillBlockEntity extends BlockEntity implements IFluidColorProvider {
    public boolean isProcessing = false;
    private int progress = 0;
    private ActiveCondenser activeCondenser = null;
    private boolean cauldronPresent = false;

    // Largest possible input requirement (height 8 tier) - the tank is always this big,
    // fill() below caps the *effective* capacity to whatever the current height's recipe needs.
    private static final int MAX_TANK_CAPACITY = 24000;

    private enum Family { WHEAT, BARLEY, GRAPE, APPLE, HONEY, SUGARCANE_JUICE, SUGARCANE_MOLASSES, POTATO, WHEAT_SPICED, BARLEY_SPICED,
        GRAPE_SPICED, APPLE_SPICED, HONEY_SPICED, SUGARCANE_JUICE_SPICED, SUGARCANE_MOLASSES_SPICED, POTATO_SPICED, AGAVE_SPICED, AGAVE }
    private enum Tier { LOW, MID, HIGH, MAX }

    private static final Map<Fluid, Family> FLUID_FAMILY = buildFamilyMap();
    // 0 = raw base fluid (wash/beer/wine/must/cider), 1/2/3 = already-distilled LOW/MID/HIGH spirit.
    // MAX is deliberately absent - it's the terminal output tier, never a valid input.
    private static final Map<Fluid, Integer> FLUID_INPUT_TIER = buildInputTierMap();
    private static final Map<Family, Map<Tier, BlockState>> FAMILY_TIER_RESULTS = buildTierResults();

    private static Map<Fluid, Family> buildFamilyMap() {
        Map<Fluid, Family> map = new HashMap<>();
        for (Fluid f : new Fluid[]{
                ModFluids.WASH_WHEAT.source().get(),
                ModFluids.BEER_WHEAT.source().get(),
                ModFluids.BEER_WHEAT_HOPPED.source().get(),
                ModFluids.SPIRIT_WHEAT_LOW.source().get(),
                ModFluids.SPIRIT_WHEAT_MID.source().get(),
                ModFluids.SPIRIT_WHEAT_HIGH.source().get()
        }) map.put(f, Family.WHEAT);

        for (Fluid f : new Fluid[]{
                ModFluids.WASH_BARLEY.source().get(),
                ModFluids.BEER_BARLEY.source().get(),
                ModFluids.BEER_BARLEY_HOPPED.source().get(),
                ModFluids.SPIRIT_BARLEY_LOW.source().get(),
                ModFluids.SPIRIT_BARLEY_MID.source().get(),
                ModFluids.SPIRIT_BARLEY_HIGH.source().get()
        }) map.put(f, Family.BARLEY);

        for (Fluid f : new Fluid[]{
                ModFluids.MUST_RED_FERMENTED.source().get(),
                ModFluids.WINE_RED.source().get(),
                ModFluids.WINE_RED_AGED.source().get(),
                ModFluids.WINE_ROSE.source().get(),
                ModFluids.WINE_ROSE_AGED.source().get(),
                ModFluids.MUST_WHITE_FERMENTED.source().get(),
                ModFluids.WINE_ORANGE.source().get(),
                ModFluids.WINE_ORANGE_AGED.source().get(),
                ModFluids.WINE_WHITE.source().get(),
                ModFluids.WINE_WHITE_AGED.source().get(),
                ModFluids.SPIRIT_GRAPE_LOW.source().get(),
                ModFluids.SPIRIT_GRAPE_MID.source().get(),
                ModFluids.SPIRIT_GRAPE_HIGH.source().get()
        }) map.put(f, Family.GRAPE);

        for (Fluid f : new Fluid[]{
                ModFluids.CIDER.source().get(),
                ModFluids.CIDER_AGED.source().get(),
                ModFluids.SPIRIT_APPLE_LOW.source().get(),
                ModFluids.SPIRIT_APPLE_MID.source().get(),
                ModFluids.SPIRIT_APPLE_HIGH.source().get()
        }) map.put(f, Family.APPLE);

        for (Fluid f : new Fluid[]{
                ModFluids.MEAD.source().get(),
                ModFluids.MEAD_AGED.source().get(),
                ModFluids.SPIRIT_HONEY_LOW.source().get(),
                ModFluids.SPIRIT_HONEY_MID.source().get(),
                ModFluids.SPIRIT_HONEY_HIGH.source().get()
        }) map.put(f, Family.HONEY);

        for (Fluid f : new Fluid[]{
                ModFluids.WASH_SUGARCANE_JUICE.source().get(),
                ModFluids.SPIRIT_SUGARCANE_JUICE_LOW.source().get(),
                ModFluids.SPIRIT_SUGARCANE_JUICE_MID.source().get(),
                ModFluids.CONCENTRATED_RUM_JUICE.source().get()
        }) map.put(f, Family.SUGARCANE_JUICE);

        for (Fluid f : new Fluid[]{
                ModFluids.WASH_SUGARCANE_MOLASSES.source().get(),
                ModFluids.SPIRIT_SUGARCANE_MOLASSES_LOW.source().get(),
                ModFluids.SPIRIT_SUGARCANE_MOLASSES_MID.source().get(),
                ModFluids.CONCENTRATED_RUM_MOLASSES.source().get()
        }) map.put(f, Family.SUGARCANE_MOLASSES);

        for (Fluid f : new Fluid[]{
                ModFluids.WASH_POTATO.source().get(),
                ModFluids.SPIRIT_POTATO_LOW.source().get(),
                ModFluids.SPIRIT_POTATO_MID.source().get(),
                ModFluids.SPIRIT_POTATO_HIGH.source().get()
        }) map.put(f, Family.POTATO);

        for (Fluid f : new Fluid[]{
                ModFluids.SPIRIT_WHEAT_MID_SPICED.source().get(),
                ModFluids.CONCENTRATED_GIN_WHEAT.source().get()
        }) map.put(f, Family.WHEAT_SPICED);

        for (Fluid f : new Fluid[]{
                ModFluids.SPIRIT_BARLEY_MID_SPICED.source().get(),
                ModFluids.CONCENTRATED_GIN_BARLEY.source().get()
        }) map.put(f, Family.BARLEY_SPICED);

        for (Fluid f : new Fluid[]{
                ModFluids.SPIRIT_GRAPE_MID_SPICED.source().get(),
                ModFluids.CONCENTRATED_GIN_GRAPE.source().get()
        }) map.put(f, Family.GRAPE_SPICED);

        for (Fluid f : new Fluid[]{
                ModFluids.SPIRIT_APPLE_MID_SPICED.source().get(),
                ModFluids.CONCENTRATED_GIN_APPLE.source().get()
        }) map.put(f, Family.APPLE_SPICED);

        for (Fluid f : new Fluid[]{
                ModFluids.SPIRIT_HONEY_MID_SPICED.source().get(),
                ModFluids.CONCENTRATED_GIN_HONEY.source().get()
        }) map.put(f, Family.HONEY_SPICED);

        for (Fluid f : new Fluid[]{
                ModFluids.SPIRIT_SUGARCANE_JUICE_MID_SPICED.source().get(),
                ModFluids.CONCENTRATED_GIN_SUGARCANE_JUICE.source().get()
        }) map.put(f, Family.SUGARCANE_JUICE_SPICED);

        for (Fluid f : new Fluid[]{
                ModFluids.SPIRIT_SUGARCANE_MOLASSES_MID_SPICED.source().get(),
                ModFluids.CONCENTRATED_GIN_SUGARCANE_MOLASSES.source().get()
        }) map.put(f, Family.SUGARCANE_MOLASSES_SPICED);

        for (Fluid f : new Fluid[]{
                ModFluids.SPIRIT_POTATO_MID_SPICED.source().get(),
                ModFluids.CONCENTRATED_GIN_POTATO.source().get()
        }) map.put(f, Family.POTATO_SPICED);

        for (Fluid f : new Fluid[]{
                ModFluids.SPIRIT_AGAVE_MID_SPICED.source().get(),
                ModFluids.CONCENTRATED_GIN_AGAVE.source().get()
        }) map.put(f, Family.AGAVE_SPICED);

        for (Fluid f : new Fluid[]{
                ModFluids.WASH_AGAVE.source().get(),
                ModFluids.SPIRIT_AGAVE_LOW.source().get(),
                ModFluids.CONCENTRATED_TEQUILA.source().get(),
                ModFluids.SPIRIT_AGAVE_HIGH.source().get()
        }) map.put(f, Family.AGAVE);

        return Map.copyOf(map);
    }

    private static Map<Fluid, Integer> buildInputTierMap() {
        Map<Fluid, Integer> map = new HashMap<>();
        for (Fluid f : new Fluid[]{
                ModFluids.WASH_WHEAT.source().get(),
                ModFluids.BEER_WHEAT.source().get(),
                ModFluids.BEER_WHEAT_HOPPED.source().get(),
                ModFluids.WASH_BARLEY.source().get(),
                ModFluids.BEER_BARLEY.source().get(),
                ModFluids.BEER_BARLEY_HOPPED.source().get(),
                ModFluids.MUST_RED_FERMENTED.source().get(),
                ModFluids.WINE_RED.source().get(),
                ModFluids.WINE_RED_AGED.source().get(),
                ModFluids.WINE_ROSE.source().get(),
                ModFluids.WINE_ROSE_AGED.source().get(),
                ModFluids.MUST_WHITE_FERMENTED.source().get(),
                ModFluids.WINE_ORANGE.source().get(),
                ModFluids.WINE_ORANGE_AGED.source().get(),
                ModFluids.WINE_WHITE.source().get(),
                ModFluids.WINE_WHITE_AGED.source().get(),
                ModFluids.CIDER.source().get(),
                ModFluids.CIDER_AGED.source().get(),
                ModFluids.MEAD.source().get(),
                ModFluids.MEAD_AGED.source().get(),
                ModFluids.WASH_SUGARCANE_JUICE.source().get(),
                ModFluids.WASH_SUGARCANE_MOLASSES.source().get(),
                ModFluids.WASH_POTATO.source().get(),
                ModFluids.WASH_AGAVE.source().get()
        }) map.put(f, 0);

        for (Fluid f : new Fluid[]{
                ModFluids.SPIRIT_WHEAT_LOW.source().get(),
                ModFluids.SPIRIT_BARLEY_LOW.source().get(),
                ModFluids.SPIRIT_GRAPE_LOW.source().get(),
                ModFluids.SPIRIT_APPLE_LOW.source().get(),
                ModFluids.SPIRIT_HONEY_LOW.source().get(),
                ModFluids.SPIRIT_SUGARCANE_JUICE_LOW.source().get(),
                ModFluids.SPIRIT_SUGARCANE_MOLASSES_LOW.source().get(),
                ModFluids.SPIRIT_POTATO_LOW.source().get(),
                ModFluids.SPIRIT_AGAVE_LOW.source().get()
        }) map.put(f, 1);

        for (Fluid f : new Fluid[]{
                ModFluids.SPIRIT_WHEAT_MID.source().get(),
                ModFluids.SPIRIT_BARLEY_MID.source().get(),
                ModFluids.SPIRIT_GRAPE_MID.source().get(),
                ModFluids.SPIRIT_APPLE_MID.source().get(),
                ModFluids.SPIRIT_HONEY_MID.source().get(),
                ModFluids.SPIRIT_SUGARCANE_JUICE_MID.source().get(),
                ModFluids.SPIRIT_SUGARCANE_MOLASSES_MID.source().get(),
                ModFluids.SPIRIT_POTATO_MID.source().get(),
                ModFluids.SPIRIT_WHEAT_MID_SPICED.source().get(),
                ModFluids.SPIRIT_BARLEY_MID_SPICED.source().get(),
                ModFluids.SPIRIT_GRAPE_MID_SPICED.source().get(),
                ModFluids.SPIRIT_APPLE_MID_SPICED.source().get(),
                ModFluids.SPIRIT_HONEY_MID_SPICED.source().get(),
                ModFluids.SPIRIT_SUGARCANE_JUICE_MID_SPICED.source().get(),
                ModFluids.SPIRIT_SUGARCANE_MOLASSES_MID_SPICED.source().get(),
                ModFluids.SPIRIT_POTATO_MID_SPICED.source().get(),
                ModFluids.SPIRIT_AGAVE_MID_SPICED.source().get(),
                ModFluids.CONCENTRATED_TEQUILA.source().get()
        }) map.put(f, 2);

        for (Fluid f : new Fluid[]{
                ModFluids.SPIRIT_WHEAT_HIGH.source().get(),
                ModFluids.SPIRIT_BARLEY_HIGH.source().get(),
                ModFluids.SPIRIT_GRAPE_HIGH.source().get(),
                ModFluids.SPIRIT_APPLE_HIGH.source().get(),
                ModFluids.SPIRIT_HONEY_HIGH.source().get(),
                ModFluids.CONCENTRATED_RUM_JUICE.source().get(),
                ModFluids.CONCENTRATED_RUM_MOLASSES.source().get(),
                ModFluids.SPIRIT_POTATO_HIGH.source().get(),
                ModFluids.CONCENTRATED_GIN_WHEAT.source().get(),
                ModFluids.CONCENTRATED_GIN_BARLEY.source().get(),
                ModFluids.CONCENTRATED_GIN_GRAPE.source().get(),
                ModFluids.CONCENTRATED_GIN_APPLE.source().get(),
                ModFluids.CONCENTRATED_GIN_HONEY.source().get(),
                ModFluids.CONCENTRATED_GIN_SUGARCANE_JUICE.source().get(),
                ModFluids.CONCENTRATED_GIN_SUGARCANE_MOLASSES.source().get(),
                ModFluids.CONCENTRATED_GIN_POTATO.source().get(),
                ModFluids.CONCENTRATED_GIN_AGAVE.source().get(),
                ModFluids.SPIRIT_AGAVE_HIGH.source().get()
        }) map.put(f, 3);

        return Map.copyOf(map);
    }

    private static Map<Family, Map<Tier, BlockState>> buildTierResults() {
        Map<Family, Map<Tier, BlockState>> map = new EnumMap<>(Family.class);

        map.put(Family.WHEAT, Map.of(
                Tier.LOW, ModFluids.SPIRIT_WHEAT_LOW.cauldron().get().defaultBlockState(),
                Tier.MID, ModFluids.SPIRIT_WHEAT_MID.cauldron().get().defaultBlockState(),
                Tier.HIGH, ModFluids.SPIRIT_WHEAT_HIGH.cauldron().get().defaultBlockState(),
                Tier.MAX, ModFluids.CONCENTRATED_VODKA_WHEAT.cauldron().get().defaultBlockState()
        ));
        map.put(Family.BARLEY, Map.of(
                Tier.LOW, ModFluids.SPIRIT_BARLEY_LOW.cauldron().get().defaultBlockState(),
                Tier.MID, ModFluids.SPIRIT_BARLEY_MID.cauldron().get().defaultBlockState(),
                Tier.HIGH, ModFluids.SPIRIT_BARLEY_HIGH.cauldron().get().defaultBlockState(),
                Tier.MAX, ModFluids.CONCENTRATED_VODKA_BARLEY.cauldron().get().defaultBlockState()
        ));
        map.put(Family.GRAPE, Map.of(
                Tier.LOW, ModFluids.SPIRIT_GRAPE_LOW.cauldron().get().defaultBlockState(),
                Tier.MID, ModFluids.SPIRIT_GRAPE_MID.cauldron().get().defaultBlockState(),
                Tier.HIGH, ModFluids.SPIRIT_GRAPE_HIGH.cauldron().get().defaultBlockState(),
                Tier.MAX, ModFluids.CONCENTRATED_VODKA_GRAPE.cauldron().get().defaultBlockState()
        ));
        map.put(Family.APPLE, Map.of(
                Tier.LOW, ModFluids.SPIRIT_APPLE_LOW.cauldron().get().defaultBlockState(),
                Tier.MID, ModFluids.SPIRIT_APPLE_MID.cauldron().get().defaultBlockState(),
                Tier.HIGH, ModFluids.SPIRIT_APPLE_HIGH.cauldron().get().defaultBlockState(),
                Tier.MAX, ModFluids.CONCENTRATED_VODKA_APPLE.cauldron().get().defaultBlockState()
        ));
        map.put(Family.HONEY, Map.of(
                Tier.LOW, ModFluids.SPIRIT_HONEY_LOW.cauldron().get().defaultBlockState(),
                Tier.MID, ModFluids.SPIRIT_HONEY_MID.cauldron().get().defaultBlockState(),
                Tier.HIGH, ModFluids.SPIRIT_HONEY_HIGH.cauldron().get().defaultBlockState(),
                Tier.MAX, ModFluids.CONCENTRATED_VODKA_HONEY.cauldron().get().defaultBlockState()
        ));
        map.put(Family.SUGARCANE_JUICE, Map.of(
                Tier.LOW, ModFluids.SPIRIT_SUGARCANE_JUICE_LOW.cauldron().get().defaultBlockState(),
                Tier.MID, ModFluids.SPIRIT_SUGARCANE_JUICE_MID.cauldron().get().defaultBlockState(),
                Tier.HIGH, ModFluids.CONCENTRATED_RUM_JUICE.cauldron().get().defaultBlockState(),
                Tier.MAX, ModFluids.CONCENTRATED_VODKA_SUGARCANE_JUICE.cauldron().get().defaultBlockState()
        ));
        map.put(Family.SUGARCANE_MOLASSES, Map.of(
                Tier.LOW, ModFluids.SPIRIT_SUGARCANE_MOLASSES_LOW.cauldron().get().defaultBlockState(),
                Tier.MID, ModFluids.SPIRIT_SUGARCANE_MOLASSES_MID.cauldron().get().defaultBlockState(),
                Tier.HIGH, ModFluids.CONCENTRATED_RUM_MOLASSES.cauldron().get().defaultBlockState(),
                Tier.MAX, ModFluids.CONCENTRATED_VODKA_SUGARCANE_MOLASSES.cauldron().get().defaultBlockState()
        ));
        map.put(Family.POTATO, Map.of(
                Tier.LOW, ModFluids.SPIRIT_POTATO_LOW.cauldron().get().defaultBlockState(),
                Tier.MID, ModFluids.SPIRIT_POTATO_MID.cauldron().get().defaultBlockState(),
                Tier.HIGH, ModFluids.SPIRIT_POTATO_HIGH.cauldron().get().defaultBlockState(),
                Tier.MAX, ModFluids.CONCENTRATED_VODKA_POTATO.cauldron().get().defaultBlockState()
        ));
        map.put(Family.WHEAT_SPICED, Map.of(
                Tier.MID, ModFluids.SPIRIT_WHEAT_MID_SPICED.cauldron().get().defaultBlockState(),
                Tier.HIGH, ModFluids.CONCENTRATED_GIN_WHEAT.cauldron().get().defaultBlockState(),
                Tier.MAX, ModFluids.CONCENTRATED_VODKA_WHEAT.cauldron().get().defaultBlockState()
        ));
        map.put(Family.BARLEY_SPICED, Map.of(
                Tier.MID, ModFluids.SPIRIT_BARLEY_MID_SPICED.cauldron().get().defaultBlockState(),
                Tier.HIGH, ModFluids.CONCENTRATED_GIN_BARLEY.cauldron().get().defaultBlockState(),
                Tier.MAX, ModFluids.CONCENTRATED_VODKA_BARLEY.cauldron().get().defaultBlockState()
        ));
        map.put(Family.GRAPE_SPICED, Map.of(
                Tier.MID, ModFluids.SPIRIT_GRAPE_MID_SPICED.cauldron().get().defaultBlockState(),
                Tier.HIGH, ModFluids.CONCENTRATED_GIN_GRAPE.cauldron().get().defaultBlockState(),
                Tier.MAX, ModFluids.CONCENTRATED_VODKA_GRAPE.cauldron().get().defaultBlockState()
        ));
        map.put(Family.APPLE_SPICED, Map.of(
                Tier.MID, ModFluids.SPIRIT_APPLE_MID_SPICED.cauldron().get().defaultBlockState(),
                Tier.HIGH, ModFluids.CONCENTRATED_GIN_APPLE.cauldron().get().defaultBlockState(),
                Tier.MAX, ModFluids.CONCENTRATED_VODKA_APPLE.cauldron().get().defaultBlockState()
        ));
        map.put(Family.HONEY_SPICED, Map.of(
                Tier.MID, ModFluids.SPIRIT_HONEY_MID_SPICED.cauldron().get().defaultBlockState(),
                Tier.HIGH, ModFluids.CONCENTRATED_GIN_HONEY.cauldron().get().defaultBlockState(),
                Tier.MAX, ModFluids.CONCENTRATED_VODKA_HONEY.cauldron().get().defaultBlockState()
        ));
        map.put(Family.SUGARCANE_JUICE_SPICED, Map.of(
                Tier.MID, ModFluids.SPIRIT_SUGARCANE_JUICE_MID_SPICED.cauldron().get().defaultBlockState(),
                Tier.HIGH, ModFluids.CONCENTRATED_GIN_SUGARCANE_JUICE.cauldron().get().defaultBlockState(),
                Tier.MAX, ModFluids.CONCENTRATED_VODKA_SUGARCANE_JUICE.cauldron().get().defaultBlockState()
        ));
        map.put(Family.SUGARCANE_MOLASSES_SPICED, Map.of(
                Tier.MID, ModFluids.SPIRIT_SUGARCANE_MOLASSES_MID_SPICED.cauldron().get().defaultBlockState(),
                Tier.HIGH, ModFluids.CONCENTRATED_GIN_SUGARCANE_MOLASSES.cauldron().get().defaultBlockState(),
                Tier.MAX, ModFluids.CONCENTRATED_VODKA_SUGARCANE_MOLASSES.cauldron().get().defaultBlockState()
        ));
        map.put(Family.POTATO_SPICED, Map.of(
                Tier.MID, ModFluids.SPIRIT_POTATO_MID_SPICED.cauldron().get().defaultBlockState(),
                Tier.HIGH, ModFluids.CONCENTRATED_GIN_POTATO.cauldron().get().defaultBlockState(),
                Tier.MAX, ModFluids.CONCENTRATED_VODKA_POTATO.cauldron().get().defaultBlockState()
        ));
        map.put(Family.AGAVE_SPICED, Map.of(
                Tier.MID, ModFluids.SPIRIT_AGAVE_MID_SPICED.cauldron().get().defaultBlockState(),
                Tier.HIGH, ModFluids.CONCENTRATED_GIN_AGAVE.cauldron().get().defaultBlockState(),
                Tier.MAX, ModFluids.CONCENTRATED_VODKA_AGAVE.cauldron().get().defaultBlockState()
        ));
        map.put(Family.AGAVE, Map.of(
                Tier.LOW, ModFluids.SPIRIT_AGAVE_LOW.cauldron().get().defaultBlockState(),
                Tier.MID, ModFluids.CONCENTRATED_TEQUILA.cauldron().get().defaultBlockState(),
                Tier.HIGH, ModFluids.SPIRIT_AGAVE_HIGH.cauldron().get().defaultBlockState(),
                Tier.MAX, ModFluids.CONCENTRATED_VODKA_AGAVE.cauldron().get().defaultBlockState()
        ));

        return Map.copyOf(map);
    }

    /**
     * mB of input fluid required per bucket of output, given what tier the input already is
     * (0 = raw base, 1/2/3 = already LOW/MID/HIGH) and the column's target tier for its height.
     * Each tier of head-start halves the input needed again, e.g. height 8 (target MAX, tier 4)
     * normally needs 24000 mB of base, but only 4000 mB (4 buckets) starting from MID (tier 2),
     * since MID already represents 2 tiers of prior distillation.
     * Returns 0 if there's no valid recipe (bad height, or input already at/above the target tier).
     */
    private static int requiredAmountFor(int inputTier, int height) {
        Tier target = tierFor(height);
        if (target == null) return 0;

        int targetTierNumber = target.ordinal() + 1; // LOW=1, MID=2, HIGH=3, MAX=4

        if (inputTier == 0) {
            return 1500 * (1 << targetTierNumber);
        }
        if (inputTier >= targetTierNumber) {
            return 0; // already at or past the target tier - nothing left to distill
        }
        return 1000 * (1 << (targetTierNumber - inputTier));
    }

    private static Tier tierFor(int height) {
        return switch (height) {
            case 2 -> Tier.LOW;
            case 4 -> Tier.MID;
            case 6 -> Tier.HIGH;
            case 8 -> Tier.MAX;
            default -> null;
        };
    }

    /** Processing time in ticks by height. */
    private static int maxProgressFor(int height) {
        return switch (height) {
            case 2 -> 1200 /*60*/;
            case 4 -> 1200 /*60*/;
            case 6 -> 1600 /*60*/;
            case 8 -> 2400 /*60*/; // 2 min
            default -> 1200;
        };
    }

    private static BlockState tierResult(Family family, Tier tier) {
        if (family == null || tier == null) return null;
        Map<Tier, BlockState> byTier = FAMILY_TIER_RESULTS.get(family);
        return byTier == null ? null : byTier.get(tier);
    }

    private final FluidTank fluidTank = new FluidTank(MAX_TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return FLUID_FAMILY.containsKey(stack.getFluid());
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            // Cap the effective capacity to whatever this specific fluid's tier actually needs to
            // reach the current height's target tier (see requiredAmountFor's doc comment).
            int height = getBlockState().hasProperty(ColumnStillBlock.HEIGHT)
                    ? getBlockState().getValue(ColumnStillBlock.HEIGHT)
                    : ColumnStillBlock.MAX_HEIGHT;

            Integer inputTier = FLUID_INPUT_TIER.get(resource.getFluid());
            int cap = inputTier == null ? MAX_TANK_CAPACITY : requiredAmountFor(inputTier, height);
            if (cap <= 0) cap = MAX_TANK_CAPACITY; // no recipe yet (bad height, or fluid already at/past target tier)

            int space = cap - this.getFluidAmount();
            if (space <= 0) return 0;
            FluidStack limited = new FluidStack(resource, Math.min(resource.getAmount(), space));
            return super.fill(limited, action);
        }
    };

    public ColumnStillBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.COLUMN_STILL.get(), pPos, pBlockState);
    }

    private LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return lazyFluidHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyFluidHandler = LazyOptional.of(() -> fluidTank);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyFluidHandler.invalidate();
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        nbt.putBoolean("isProcessing", isProcessing);
        nbt.putInt("progress", progress);
        nbt.putBoolean("clock", getBlockState().getValue(ColumnStillBlock.CLOCK));
        fluidTank.writeToNBT(nbt);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        isProcessing = nbt.getBoolean("isProcessing");
        progress = nbt.getInt("progress");
        if (nbt.contains("clock") && level != null) {
            boolean clockState = nbt.getBoolean("clock");
            BlockState currentState = getBlockState();
            if (currentState.hasProperty(ColumnStillBlock.CLOCK) && currentState.getValue(ColumnStillBlock.CLOCK) != clockState) {
                level.setBlock(worldPosition, currentState.setValue(ColumnStillBlock.CLOCK, clockState), 3);
            }
        }
        fluidTank.readFromNBT(nbt);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        this.saveAdditional(nbt);
        return nbt;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        this.load(tag);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            this.load(tag);
            if (level != null && level.isClientSide) {
                level.sendBlockUpdated(this.getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    /** Locates the topmost segment in the column with a *direct* condenser attachment (skips "_BELOW" pass-through markers). */
    private record ActiveCondenser(BlockPos condenserBlockPos, BlockPos cauldronPos) {}

    @Nullable
    private static ActiveCondenser findActiveCondenser(Level level, BlockPos bottomPos, int height) {
        for (int i = height - 1; i >= 0; i--) {
            BlockState segState = level.getBlockState(bottomPos.above(i));
            if (!segState.hasProperty(ColumnStillBlock.CONDENSER)) continue;

            CondenserPos cp = segState.getValue(ColumnStillBlock.CONDENSER);
            if (cp != CondenserPos.NONE && !cp.isBelowVariant()) {
                Direction dir = cp.toDirection();
                BlockPos condenserBlockPos = bottomPos.above(i).relative(dir);
                return new ActiveCondenser(condenserBlockPos, condenserBlockPos.below());
            }
        }
        return null;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ColumnStillBlockEntity entity) {
        if (level.isClientSide) return;
        if (state.getValue(ColumnStillBlock.SEGMENT) != 0) return; // safety: only the master processes

        int height = state.getValue(ColumnStillBlock.HEIGHT);
        Tier tier = tierFor(height);

        FluidStack fluidStack = entity.fluidTank.getFluid();
        Fluid currentFluid = fluidStack.getFluid();
        Family family = FLUID_FAMILY.get(currentFluid);
        Integer inputTier = FLUID_INPUT_TIER.get(currentFluid);
        int required = inputTier == null ? 0 : requiredAmountFor(inputTier, height);

        entity.activeCondenser = findActiveCondenser(level, pos, height);
        entity.cauldronPresent = entity.activeCondenser != null && level.getBlockState(entity.activeCondenser.cauldronPos()).is(Blocks.CAULDRON);
        boolean heated = PotStillBlockEntity.isValidHeatSource(level, pos.below());

        if (entity.isProcessing) {
            if (family == null || required <= 0 || tier == null || entity.activeCondenser == null || !entity.cauldronPresent || !heated) {
                entity.isProcessing = false;
                level.sendBlockUpdated(pos, state, state, 3);
                entity.setChanged();
                return;
            }

            entity.progress++;

            if (entity.progress % 20 == 0 && state.getValue(ColumnStillBlock.CLOCK)) {
                entity.setChanged();
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
            }

            int maxProgress = maxProgressFor(height);
            if (entity.progress >= maxProgress) {
                BlockState resultState = tierResult(family, tier);

                if (resultState != null) {
                    level.setBlock(entity.activeCondenser.cauldronPos(), resultState, 3);
                    entity.fluidTank.drain(required, IFluidHandler.FluidAction.EXECUTE);
                }

                entity.isProcessing = false;
                entity.progress = 0;
                level.sendBlockUpdated(pos, state, state, 3);
                entity.setChanged();
            }
        } else if (family != null && required > 0 && entity.activeCondenser != null && entity.cauldronPresent && heated
                && fluidStack.getAmount() >= required) {
            entity.isProcessing = true;
            level.sendBlockUpdated(pos, state, state, 3);
            entity.setChanged();
        }
    }

    public boolean handleRightClick(Player player, InteractionHand hand) {
        if (level == null || level.isClientSide) return false;
        BlockPos pos = this.getBlockPos();
        BlockState state = this.getBlockState();

        if (!this.isProcessing) {
            FluidStack before = this.fluidTank.getFluid().copy();
            if (FluidUtil.interactWithFluidHandler(player, hand, this.fluidTank)) {
                FluidStack after = this.fluidTank.getFluid();
                boolean wasDrained = after.getAmount() < before.getAmount();
                SoundEvent sound = wasDrained ? SoundEvents.BUCKET_FILL : SoundEvents.BUCKET_EMPTY;
                level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);

                if (wasDrained) {
                    this.progress = 0;
                }

                level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
                setChanged(level, pos, state);

                if (!PotStillBlockEntity.isValidHeatSource(level, pos.below()))
                    player.displayClientMessage(Component.translatable("message.tadackosdrinks.still_warning_missing_heat"), true);
                else if (activeCondenser == null)
                    player.displayClientMessage(Component.translatable("message.tadackosdrinks.still_warning_missing_condenser"), true);
                else if (!cauldronPresent)
                    player.displayClientMessage(Component.translatable("message.tadackosdrinks.still_warning_missing_cauldron"), true);

                return true;
            }
        }

        return false;
    }

    @Override
    public FluidStack getFluid() {
        return this.fluidTank.getFluid();
    }

    /** 0..1 fill ratio for rendering, relative to the current height's recipe capacity (or full tank if no recipe yet). */
    public float getFillPercent() {
        int height = getBlockState().hasProperty(ColumnStillBlock.HEIGHT)
                ? getBlockState().getValue(ColumnStillBlock.HEIGHT)
                : ColumnStillBlock.MAX_HEIGHT;
        int cap = requiredAmountFor(4, height);
        if (cap <= 0) cap = MAX_TANK_CAPACITY;
        return Math.min(1f, (float) fluidTank.getFluidAmount() / cap);
    }

    public int getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return maxProgressFor(getBlockState().getValue(ColumnStillBlock.HEIGHT));
    }

    public CompoundTag saveToItemTag() {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

    public boolean isDefaultState() {
        if (!this.fluidTank.isEmpty()) return false;
        if (this.getBlockState().getValue(ColumnStillBlock.CLOCK)) return false;
        if (this.progress != 0) return false;
        return true;
    }

    /**
     * The renderer draws geometry into blocks above this one (up to HEIGHT blocks tall). The default
     * 1x1x1 box would let the frustum cull the whole render() call whenever this block itself falls
     * out of view even though upper segments should still be visible - override to span the column.
     */
    @Override
    public AABB getRenderBoundingBox() {
        BlockState state = getBlockState();
        int height = state.hasProperty(ColumnStillBlock.HEIGHT) ? state.getValue(ColumnStillBlock.HEIGHT) : 1;
        BlockPos pos = getBlockPos();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + height, pos.getZ() + 1);
    }
}