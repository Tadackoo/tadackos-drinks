package net.tadacko.tadackosdrinks.block.entity;

import com.google.common.collect.ImmutableSet;
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
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.tadacko.tadackosdrinks.block.CondenserPos;
import net.tadacko.tadackosdrinks.block.PotStillBlock;
import net.tadacko.tadackosdrinks.fluid.ModFluids;
import net.tadacko.tadackosdrinks.util.IFluidColorProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class PotStillBlockEntity extends BlockEntity implements IFluidColorProvider {
    public boolean isProcessing = false;
    private int progress = 0;
    private static final int MAX_PROGRESS = 2400 /*60*/; // 2 min
    private Direction condenserDir = null;
    private boolean cauldronPresent = false;

    private final FluidTank fluidTank = new FluidTank(3000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return VALID_FLUIDS.contains(stack.getFluid());
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            // Spirit fluids are only distilled in smaller batches — cap at 2000 mB
            int effectiveCapacity = SPIRIT_FLUIDS.contains(resource.getFluid()) ? 2000 : 3000;
            int space = effectiveCapacity - this.getFluidAmount();
            if (space <= 0) return 0;
            // Clamp the incoming amount to the effective remaining space, then delegate normally
            FluidStack limited = new FluidStack(resource, Math.min(resource.getAmount(), space));
            return super.fill(limited, action);
        }
    };

    private static final Set<Fluid> BASE_FLUIDS = Set.of(
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
    );

    private static final Set<Fluid> SPIRIT_FLUIDS = Set.of(
            ModFluids.SPIRIT_WHEAT_LOW.source().get(),
            ModFluids.SPIRIT_WHEAT_MID.source().get(),
            ModFluids.SPIRIT_BARLEY_LOW.source().get(),
            ModFluids.SPIRIT_BARLEY_MID.source().get(),

            ModFluids.SPIRIT_GRAPE_LOW.source().get(),
            ModFluids.SPIRIT_GRAPE_MID.source().get(),

            ModFluids.SPIRIT_APPLE_LOW.source().get(),
            ModFluids.SPIRIT_APPLE_MID.source().get(),

            ModFluids.SPIRIT_HONEY_LOW.source().get(),
            ModFluids.SPIRIT_HONEY_MID.source().get(),

            ModFluids.SPIRIT_SUGARCANE_JUICE_LOW.source().get(),
            ModFluids.SPIRIT_SUGARCANE_JUICE_MID.source().get(),
            ModFluids.SPIRIT_SUGARCANE_MOLASSES_LOW.source().get(),
            ModFluids.SPIRIT_SUGARCANE_MOLASSES_MID.source().get(),

            ModFluids.SPIRIT_POTATO_LOW.source().get(),
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

            ModFluids.SPIRIT_AGAVE_LOW.source().get(),
            ModFluids.CONCENTRATED_TEQUILA.source().get()
    );

    private static final Map<Fluid, BlockState> DISTILLATION_RESULTS = Map.ofEntries(
            Map.entry(ModFluids.WASH_WHEAT.source().get(), ModFluids.SPIRIT_WHEAT_LOW.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.BEER_WHEAT.source().get(), ModFluids.SPIRIT_WHEAT_LOW.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.BEER_WHEAT_HOPPED.source().get(), ModFluids.SPIRIT_WHEAT_LOW.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.SPIRIT_WHEAT_LOW.source().get(), ModFluids.SPIRIT_WHEAT_MID.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.SPIRIT_WHEAT_MID.source().get(), ModFluids.SPIRIT_WHEAT_HIGH.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.WASH_BARLEY.source().get(), ModFluids.SPIRIT_BARLEY_LOW.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.BEER_BARLEY.source().get(), ModFluids.SPIRIT_BARLEY_LOW.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.BEER_BARLEY_HOPPED.source().get(), ModFluids.SPIRIT_BARLEY_LOW.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.SPIRIT_BARLEY_LOW.source().get(), ModFluids.SPIRIT_BARLEY_MID.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.SPIRIT_BARLEY_MID.source().get(), ModFluids.SPIRIT_BARLEY_HIGH.cauldron().get().defaultBlockState()),

            Map.entry(ModFluids.MUST_RED_FERMENTED.source().get(), ModFluids.SPIRIT_GRAPE_LOW.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.WINE_RED.source().get(), ModFluids.SPIRIT_GRAPE_LOW.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.WINE_RED_AGED.source().get(), ModFluids.SPIRIT_GRAPE_LOW.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.WINE_ROSE.source().get(), ModFluids.SPIRIT_GRAPE_LOW.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.WINE_ROSE_AGED.source().get(), ModFluids.SPIRIT_GRAPE_LOW.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.MUST_WHITE_FERMENTED.source().get(), ModFluids.SPIRIT_GRAPE_LOW.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.WINE_ORANGE.source().get(), ModFluids.SPIRIT_GRAPE_LOW.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.WINE_ORANGE_AGED.source().get(), ModFluids.SPIRIT_GRAPE_LOW.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.WINE_WHITE.source().get(), ModFluids.SPIRIT_GRAPE_LOW.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.WINE_WHITE_AGED.source().get(), ModFluids.SPIRIT_GRAPE_LOW.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.SPIRIT_GRAPE_LOW.source().get(), ModFluids.SPIRIT_GRAPE_MID.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.SPIRIT_GRAPE_MID.source().get(), ModFluids.SPIRIT_GRAPE_HIGH.cauldron().get().defaultBlockState()),

            Map.entry(ModFluids.CIDER.source().get(), ModFluids.SPIRIT_APPLE_LOW.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.CIDER_AGED.source().get(), ModFluids.SPIRIT_APPLE_LOW.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.SPIRIT_APPLE_LOW.source().get(), ModFluids.SPIRIT_APPLE_MID.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.SPIRIT_APPLE_MID.source().get(), ModFluids.SPIRIT_APPLE_HIGH.cauldron().get().defaultBlockState()),

            Map.entry(ModFluids.MEAD.source().get(), ModFluids.SPIRIT_HONEY_LOW.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.MEAD_AGED.source().get(), ModFluids.SPIRIT_HONEY_LOW.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.SPIRIT_HONEY_LOW.source().get(), ModFluids.SPIRIT_HONEY_MID.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.SPIRIT_HONEY_MID.source().get(), ModFluids.SPIRIT_HONEY_HIGH.cauldron().get().defaultBlockState()),

            Map.entry(ModFluids.WASH_SUGARCANE_JUICE.source().get(), ModFluids.SPIRIT_SUGARCANE_JUICE_LOW.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.SPIRIT_SUGARCANE_JUICE_LOW.source().get(), ModFluids.SPIRIT_SUGARCANE_JUICE_MID.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.SPIRIT_SUGARCANE_JUICE_MID.source().get(), ModFluids.CONCENTRATED_RUM_JUICE.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.WASH_SUGARCANE_MOLASSES.source().get(), ModFluids.SPIRIT_SUGARCANE_MOLASSES_LOW.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.SPIRIT_SUGARCANE_MOLASSES_LOW.source().get(), ModFluids.SPIRIT_SUGARCANE_MOLASSES_MID.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.SPIRIT_SUGARCANE_MOLASSES_MID.source().get(), ModFluids.CONCENTRATED_RUM_MOLASSES.cauldron().get().defaultBlockState()),

            Map.entry(ModFluids.WASH_POTATO.source().get(), ModFluids.SPIRIT_POTATO_LOW.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.SPIRIT_POTATO_LOW.source().get(), ModFluids.SPIRIT_POTATO_MID.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.SPIRIT_POTATO_MID.source().get(), ModFluids.SPIRIT_POTATO_HIGH.cauldron().get().defaultBlockState()),

            Map.entry(ModFluids.SPIRIT_WHEAT_MID_SPICED.source().get(), ModFluids.CONCENTRATED_GIN_WHEAT.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.SPIRIT_BARLEY_MID_SPICED.source().get(), ModFluids.CONCENTRATED_GIN_BARLEY.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.SPIRIT_GRAPE_MID_SPICED.source().get(), ModFluids.CONCENTRATED_GIN_GRAPE.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.SPIRIT_APPLE_MID_SPICED.source().get(), ModFluids.CONCENTRATED_GIN_APPLE.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.SPIRIT_HONEY_MID_SPICED.source().get(), ModFluids.CONCENTRATED_GIN_HONEY.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.SPIRIT_SUGARCANE_JUICE_MID_SPICED.source().get(), ModFluids.CONCENTRATED_GIN_SUGARCANE_JUICE.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.SPIRIT_SUGARCANE_MOLASSES_MID_SPICED.source().get(), ModFluids.CONCENTRATED_GIN_SUGARCANE_MOLASSES.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.SPIRIT_POTATO_MID_SPICED.source().get(), ModFluids.CONCENTRATED_GIN_POTATO.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.SPIRIT_AGAVE_MID_SPICED.source().get(), ModFluids.CONCENTRATED_GIN_AGAVE.cauldron().get().defaultBlockState()),

            Map.entry(ModFluids.WASH_AGAVE.source().get(), ModFluids.SPIRIT_AGAVE_LOW.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.SPIRIT_AGAVE_LOW.source().get(), ModFluids.CONCENTRATED_TEQUILA.cauldron().get().defaultBlockState()),
            Map.entry(ModFluids.CONCENTRATED_TEQUILA.source().get(), ModFluids.SPIRIT_AGAVE_HIGH.cauldron().get().defaultBlockState())
    );

    private static final ImmutableSet<Fluid> VALID_FLUIDS = ImmutableSet.<Fluid>builder()
            .addAll(DISTILLATION_RESULTS.keySet())
            .build();

    public PotStillBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.POT_STILL.get(), pPos, pBlockState);
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
        nbt.putBoolean("clock", getBlockState().getValue(PotStillBlock.CLOCK));
        fluidTank.writeToNBT(nbt);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        isProcessing = nbt.getBoolean("isProcessing");
        progress = nbt.getInt("progress");
        // Restore the CLOCK state if level is available
        if (nbt.contains("clock") && level != null) {
            boolean clockState = nbt.getBoolean("clock");
            BlockState currentState = getBlockState();
            if (currentState.hasProperty(PotStillBlock.CLOCK) && currentState.getValue(PotStillBlock.CLOCK) != clockState) {
                level.setBlock(worldPosition, currentState.setValue(PotStillBlock.CLOCK, clockState), 3);
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

            // Force re-render on client
            if (level != null && level.isClientSide) {
                level.sendBlockUpdated(this.getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PotStillBlockEntity entity) {
        if (level.isClientSide) return;

        FluidStack fluidStack = entity.fluidTank.getFluid();
        Fluid currentFluid = fluidStack.getFluid();

        // Resolve condenser position from block state
        CondenserPos condenserEnum = state.getValue(PotStillBlock.CONDENSER);
        entity.condenserDir = condenserEnum.toDirection(); // null when NONE

        // The cauldron must be directly below the condenser block
        BlockPos condenserPos = entity.condenserDir != null ? pos.relative(entity.condenserDir) : null;
        BlockPos cauldronPos = condenserPos != null ? condenserPos.below() : null;
        entity.cauldronPresent = cauldronPos != null && level.getBlockState(cauldronPos).is(Blocks.CAULDRON); // empty cauldron only

        if (entity.isProcessing) {
            // Stop if the condenser or the cauldron below it disappeared
            if (entity.condenserDir == null || !entity.cauldronPresent || !isValidHeatSource(level, pos.below())) {
                entity.isProcessing = false;
                level.sendBlockUpdated(pos, state, state, 3);
                entity.setChanged();
                return;
            }

            entity.progress++;

            if (entity.progress % 20 == 0 && entity.getBlockState().getValue(PotStillBlock.CLOCK)) {
                entity.setChanged(); // mark dirty (avoid sending full block update every tick)
                // Force sync to client every second for clock hand rendering
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
            }

            if (entity.progress >= MAX_PROGRESS) {
                BlockState resultBlockState = DISTILLATION_RESULTS.get(currentFluid);

                if (resultBlockState != null) {
                    // Fill the cauldron below the condenser with the distilled spirit
                    level.setBlock(cauldronPos, resultBlockState, 3);
                    entity.fluidTank.drain(3000, IFluidHandler.FluidAction.EXECUTE);
                }

                entity.isProcessing  = false;
                entity.progress = 0;
                level.sendBlockUpdated(pos, state, state, 3);
                entity.setChanged();
            }
        } else if (entity.condenserDir != null && entity.cauldronPresent && isValidHeatSource(level, pos.below())) {
            if ((fluidStack.getAmount() >= 3000 && BASE_FLUIDS.contains(currentFluid)) ||
                    (fluidStack.getAmount() >= 2000 && SPIRIT_FLUIDS.contains(currentFluid))) {
                entity.isProcessing = true;
                level.sendBlockUpdated(pos, state, state, 3);
                entity.setChanged();
            }
        }
    }

    public boolean handleRightClick(Player player, InteractionHand hand) {
        if (level == null || level.isClientSide) return false;
        BlockPos pos = this.getBlockPos();
        BlockState state = this.getBlockState();

        // Generic fluid container (vanilla buckets, kegs, anything with IFluidHandlerItem)
        // -> bulk transfer with the barrel's tank.
        // Fluid validity for fills is handled internally by FluidTank.fill() via isFluidValid().
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

                if (!isValidHeatSource(level, pos.below()))
                    player.displayClientMessage(Component.translatable("message.tadackosdrinks.still_warning_missing_heat"), true);
                else if (condenserDir == null)
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

    public int getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return MAX_PROGRESS;
    }

    // returns a tag suitable for putting into an ItemStack under "BlockEntityTag"
    public CompoundTag saveToItemTag() {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag); // allowed here because this is the BE class
        return tag;
    }

    // Return true when the block entity has no meaningful data and can be represented by a plain item
    public boolean isDefaultState() {
        if (this.fluidTank != null && !this.fluidTank.isEmpty()) return false;
        if (this.getBlockState().getValue(PotStillBlock.CLOCK)) return false;
        if (this.progress != 0) return false;

        // If we got here, it's default/empty
        return true;
    }

    public static boolean isValidHeatSource(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (block == Blocks.MAGMA_BLOCK || block == Blocks.FIRE || block == Blocks.SOUL_FIRE) return true;

        if (block == Blocks.CAMPFIRE || block == Blocks.SOUL_CAMPFIRE) {
            return state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT);
        }

        if (state.getFluidState().is(net.minecraft.tags.FluidTags.LAVA)) return true;

        return false;
    }
}
