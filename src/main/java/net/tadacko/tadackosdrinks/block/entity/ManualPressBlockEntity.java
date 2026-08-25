package net.tadacko.tadackosdrinks.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.tadacko.tadackosdrinks.fluid.ModFluids;
import net.tadacko.tadackosdrinks.item.ModItems;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.RenderUtils;

import java.util.Map;
import java.util.Set;

public class ManualPressBlockEntity extends BlockEntity implements GeoBlockEntity {
    public boolean isProcessing = false;
    private int progress = 0;
    private boolean isReturning = false;
    private static final int MAX_PROGRESS = 60; // 3 seconds
    private static final int TOTAL_ANIMATION_TIME = 120; // 6 seconds total

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

    private static final Map<Item, Fluid> ITEM_TO_FLUID = Map.ofEntries(
            Map.entry(ModItems.GRAPES_RED.get(), ModFluids.MUST_RED.source().get()),
            Map.entry(ModItems.GRAPES_WHITE.get(), ModFluids.MUST_WHITE.source().get()),
            Map.entry(ModItems.SUGAR_CANE_CRUSHED.get(), ModFluids.MUST_SUGARCANE.source().get()),
            Map.entry(ModItems.AGAVE_PINA_CRUSHED.get(), ModFluids.MUST_AGAVE.source().get())
    );

    private static final Map<Fluid, Block> PRESSING_RESULTS = Map.ofEntries(
            Map.entry(ModFluids.MUST_RED.source().get(), ModFluids.JUICE_GRAPE_ROSE.cauldron().get()),
            Map.entry(ModFluids.MUST_RED_FERMENTED.source().get(), ModFluids.WINE_RED.cauldron().get()),
            Map.entry(ModFluids.MUST_WHITE.source().get(), ModFluids.JUICE_GRAPE_WHITE.cauldron().get()),
            Map.entry(ModFluids.MUST_WHITE_FERMENTED.source().get(), ModFluids.WINE_ORANGE.cauldron().get()),
            Map.entry(ModFluids.MUST_APPLE.source().get(), ModFluids.JUICE_APPLE.cauldron().get()),
            Map.entry(ModFluids.MUST_SUGARCANE.source().get(), ModFluids.JUICE_SUGARCANE.cauldron().get()),
            Map.entry(ModFluids.MUST_AGAVE.source().get(), ModFluids.JUICE_AGAVE.cauldron().get())
    );

    private static final Set<Fluid> VALID_FLUIDS = PRESSING_RESULTS.keySet();

    @Override
    public void saveAdditional(CompoundTag nbt) {
        nbt.putBoolean("isProcessing", this.isProcessing);
        nbt.putBoolean("isReturning", this.isReturning);
        nbt.putInt("progress", this.progress);
        fluidTank.writeToNBT(nbt);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.isProcessing = nbt.getBoolean("isProcessing");
        this.isReturning = nbt.getBoolean("isReturning");
        this.progress = nbt.getInt("progress");
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
        this.load(pkt.getTag());
    }

    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    public ManualPressBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANUAL_PRESS.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> tAnimationState) {
        if (this.isProcessing || this.isReturning) {
            // Play the "use" animation when processing or returning
            tAnimationState.getController().setAnimation(RawAnimation.begin().then("use", Animation.LoopType.PLAY_ONCE));
        } else {
            // Play idle animation when idle
            tAnimationState.getController().setAnimation(RawAnimation.begin().then("idle", Animation.LoopType.LOOP));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object blockEntity) {
        // When processing or returning, control animation progress via progress
        if (this.isProcessing || this.isReturning) {
            if (this.progress <= MAX_PROGRESS) {
                // Forward animation: 0 to 60
                return this.progress;
            } else {
                // Reverse animation: 60 back down to 0
                // When progress is 61, return 59
                // When progress is 120, return 0
                return TOTAL_ANIMATION_TIME - this.progress;
            }
        }
        // When idle, use normal game ticks
        return RenderUtils.getCurrentTick();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ManualPressBlockEntity entity) {
        // ticker server side only, no guard needed

        FluidStack fluidStack = entity.fluidTank.getFluid();
        Fluid currentFluid = fluidStack.getFluid();

        if (entity.isProcessing) {
            if (level.getBlockState(pos.below()).getBlock() != Blocks.CAULDRON) {
                entity.isProcessing = false;
                level.sendBlockUpdated(pos, state, state, 3);
                entity.setChanged();
                return;
            }

            // don't question the donkey
            if (entity.progress % 20 == 0 || entity.progress == 0) {
                level.playSeededSound(null, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D,
                        SoundEvents.DONKEY_ANGRY, SoundSource.BLOCKS, 0.05F, 2.0F, 444964984);
            }
            entity.progress++;

            if (entity.progress >= MAX_PROGRESS) {
                BlockState resultBlockState = PRESSING_RESULTS.get(currentFluid).defaultBlockState();

                level.setBlock(pos.below(), resultBlockState, 3);
                entity.fluidTank.drain(1000, IFluidHandler.FluidAction.EXECUTE);

                // Switch to returning state instead of resetting
                entity.isProcessing = false;
                entity.isReturning = true;

                level.sendBlockUpdated(pos, state, state, 3);
                entity.setChanged();

                level.playSound(null, pos, SoundEvents.CROSSBOW_LOADING_END, SoundSource.BLOCKS, 1.0F, 1.0F);
            } else {
                // Send updates to sync progress to client for animation
                level.sendBlockUpdated(pos, state, state, 3);
                entity.setChanged();
            }
        } else if (entity.isReturning) {
            // Continue the return animation
            if (entity.progress % 20 == 0) {
                level.playSeededSound(null, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D,
                        SoundEvents.DONKEY_ANGRY, SoundSource.BLOCKS, 0.05F, 2.0F, 987898454);
            }
            entity.progress++;

            if (entity.progress >= TOTAL_ANIMATION_TIME) {
                // Animation complete, reset everything
                entity.isReturning = false;
                entity.progress = 0;
            }

            level.sendBlockUpdated(pos, state, state, 3);
            entity.setChanged();
        } else if (level.getBlockState(pos.below()).getBlock() == Blocks.CAULDRON && fluidStack.getAmount() > 0) {
            entity.isProcessing = true;
            level.sendBlockUpdated(pos, state, state, 3);
            entity.setChanged();
        }
    }

    public boolean handleRightClick(Player player, InteractionHand hand) {
        if (level == null || level.isClientSide) return false;
        if (this.isProcessing || this.isReturning) return false;

        ItemStack heldItem = player.getItemInHand(hand);
        BlockPos pos = this.getBlockPos();
        BlockState state = this.getBlockState();

        boolean isGrapes = heldItem.getItem() == ModItems.GRAPES_RED.get() ||
                heldItem.getItem() == ModItems.GRAPES_WHITE.get();

        boolean isSugarCane = heldItem.getItem() == ModItems.SUGAR_CANE_CRUSHED.get();

        boolean isAgave = heldItem.getItem() == ModItems.AGAVE_PINA_CRUSHED.get();

        if (isGrapes || isSugarCane || isAgave) {
            int insertCount = heldItem.getCount();
            if (isGrapes && insertCount < 12) {
                player.displayClientMessage(Component.translatable("message.tadackosdrinks.manual_crusher_fail_grape_count"), true);
                return false;
            } else if (isSugarCane && insertCount < 2) {
                player.displayClientMessage(Component.translatable("message.tadackosdrinks.manual_press_fail_sugar_cane_count"), true);
                return false;
            }

            if (isGrapes) insertCount = 12;
            if (isSugarCane) insertCount = 2;
            if (isAgave) insertCount = 1;

            if (level.getBlockState(pos.below()).getBlock() != Blocks.CAULDRON) {
                player.displayClientMessage(Component.translatable("message.tadackosdrinks.manual_press_fail_cauldron"), true);
                return false;
            }

            Fluid fluid = ITEM_TO_FLUID.get(heldItem.getItem());
            FluidStack fluidStack = new FluidStack(fluid, 1000);

            if (this.fluidTank.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE) == 1000) {
                this.fluidTank.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);

                if (!player.isCreative()) {
                    heldItem.shrink(insertCount);
                }

                level.sendBlockUpdated(pos, state, state, 3);
                setChanged(level, pos, state);

                return true;
            }
        }

        // generic fluid container transfer
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
            return true;
        }

        return false;
    }

    public FluidTank getFluidTank() {
        return fluidTank;
    }

    public int getProgress() {
        return progress;
    }

    public boolean isProcessing() {
        return isProcessing;
    }

    public boolean isReturning() {
        return isReturning;
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