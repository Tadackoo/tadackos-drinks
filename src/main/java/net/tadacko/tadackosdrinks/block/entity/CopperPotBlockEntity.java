package net.tadacko.tadackosdrinks.block.entity;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
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
import net.tadacko.tadackosdrinks.fluid.ModFluids;
import net.tadacko.tadackosdrinks.util.IFluidColorProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CopperPotBlockEntity extends BlockEntity implements IFluidColorProvider {
    private int progress = 0;
    private static final int MAX_PROGRESS = 2400 /*60*/; // 2 min

    private final FluidTank fluidTank = new FluidTank(1000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return VALID_FLUIDS.contains(stack.getFluid());
        }
    };

    private static final Map<Fluid, Fluid> BOILING_RESULTS = Map.ofEntries(
      Map.entry(ModFluids.WORT_WHEAT.source().get(), ModFluids.WORT_WHEAT_BOILED.source().get()),
      Map.entry(ModFluids.WORT_WHEAT_HOPPED.source().get(), ModFluids.WORT_WHEAT_BOILED_HOPPED.source().get()),
      Map.entry(ModFluids.WORT_BARLEY.source().get(), ModFluids.WORT_BARLEY_BOILED.source().get()),
      Map.entry(ModFluids.WORT_BARLEY_HOPPED.source().get(), ModFluids.WORT_BARLEY_BOILED_HOPPED.source().get()),
      Map.entry(ModFluids.JUICE_SUGARCANE.source().get(), ModFluids.SYRUP_SUGARCANE.source().get())
    );

    private static final ImmutableSet<Fluid> VALID_FLUIDS = ImmutableSet.<Fluid>builder()
            .addAll(BOILING_RESULTS.keySet())
            .addAll(BOILING_RESULTS.values())
            .build();

    public CopperPotBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COPPER_POT.get(), pos, state);
    }

    private LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return lazyFluidHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    // Called when the BE is attached to a level — apply pending data if necessary.
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
    protected void saveAdditional(CompoundTag nbt) {
        nbt.putInt("copper_pot.progress", this.progress);
        fluidTank.writeToNBT(nbt);

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        progress = nbt.getInt("copper_pot.progress");
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

    public static void tick(Level level, BlockPos pos, BlockState state, CopperPotBlockEntity entity) {
        if (level.isClientSide) {
            // Particle spawning
            FluidStack fluidStack = entity.fluidTank.getFluid();
            if (BOILING_RESULTS.containsValue(fluidStack.getFluid())) {
                if (level.getGameTime() % 10 == 0) {
                    level.addParticle(
                            ParticleTypes.POOF,
                            pos.getX() + 0.5,
                            pos.getY() + 1.3,
                            pos.getZ() + 0.5,
                            0, 0.02, 0 // Particle movement
                    );
                }
            }
            return;
        }

        // Check if there's wort in the tank to boil
        FluidStack fluidStack = entity.fluidTank.getFluid();
        if (PotStillBlockEntity.isValidHeatSource(level, pos.below()) && fluidStack.getAmount() > 0 && BOILING_RESULTS.containsKey(fluidStack.getFluid())) {
            entity.progress++;

            if (entity.progress % 20 == 0) {
                entity.setChanged(); // mark dirty (avoid sending full block update every tick)
                // Only send sendBlockUpdated when something visible changed
            }

            if (entity.progress >= MAX_PROGRESS) {
                Fluid resultFluid = BOILING_RESULTS.get(fluidStack.getFluid());

                if (resultFluid != null) {
                    FluidStack resultFluidStack = new FluidStack(resultFluid, fluidStack.getAmount());
                    entity.fluidTank.setFluid(resultFluidStack);
                    entity.progress = 0;
                    entity.setChanged();
                    // force client to request/apply BE NBT so client tick sees the new fluid and spawns particles
                    level.sendBlockUpdated(pos, state, state, 3);
                }
            }
        }
    }

    public boolean handleRightClick(Player player, InteractionHand hand) {
        if (level == null || level.isClientSide) return false;
        BlockPos pos = this.getBlockPos();
        BlockState state = this.getBlockState();

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
                player.displayClientMessage(Component.translatable("message.tadackosdrinks.copper_pot_warning_missing_heat"), true);

            return true;
        }

        return false;
    }

    @Override
    public FluidStack getFluid() {
        return this.fluidTank.getFluid();
    }

    // returns a tag suitable for putting into an ItemStack under "BlockEntityTag"
    public CompoundTag saveToItemTag() {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

    // Return true when the block entity has no meaningful data and can be represented by a plain item
    public boolean isDefaultState() {
        if (!this.fluidTank.isEmpty()) return false;
        if (this.progress != 0) return false;

        return true;
    }
}
