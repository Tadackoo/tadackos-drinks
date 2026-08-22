package net.tadacko.tadackosdrinks.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
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

public class ManualCrusherBlockEntity extends BlockEntity implements GeoBlockEntity {
    public boolean isProcessing = false;
    private int progress = 0;
    private static final int MAX_PROGRESS = 120; // 6 seconds

    private final ItemStackHandler inventory = new ItemStackHandler(1);

    private static final Map<Item, Item> CRUSHING_RESULTS = Map.ofEntries(
            Map.entry(ModItems.WHEAT_SEEDS_MALTED.get(), ModItems.WHEAT_SEEDS_CRUSHED.get()),
            Map.entry(ModItems.BARLEY_SEEDS_MALTED.get(), ModItems.BARLEY_SEEDS_CRUSHED.get()),
            Map.entry(Items.SUGAR_CANE, ModItems.SUGAR_CANE_CRUSHED.get()),
            Map.entry(Items.BAKED_POTATO, ModItems.POTATO_CRUSHED.get()),
            Map.entry(ModItems.AGAVE_PINA_BAKED.get(), ModItems.AGAVE_PINA_CRUSHED.get())
    );

    private static final Map<Item, Block> FLUID_RESULTS = Map.ofEntries(
            Map.entry(ModItems.GRAPES_RED.get(), ModFluids.MUST_RED.cauldron().get()),
            Map.entry(ModItems.GRAPES_WHITE.get(), ModFluids.MUST_WHITE.cauldron().get()),
            Map.entry(Items.APPLE, ModFluids.MUST_APPLE.cauldron().get())
    );

    public ManualCrusherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANUAL_CRUSHER.get(), pos, state);
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", inventory.serializeNBT());
        nbt.putBoolean("isProcessing", this.isProcessing);
        nbt.putInt("progress", this.progress);

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        inventory.deserializeNBT(nbt.getCompound("inventory"));
        this.isProcessing = nbt.getBoolean("isProcessing");
        this.progress = nbt.getInt("progress");
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

    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> tAnimationState) {
        if (this.isProcessing) {
            tAnimationState.getController().setAnimation(RawAnimation.begin().then("use", Animation.LoopType.PLAY_ONCE));
        } else {
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
        return RenderUtils.getCurrentTick();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ManualCrusherBlockEntity entity) {
        if (!entity.isProcessing) return;

        entity.progress++;
        if (entity.progress % 5 == 0 && entity.progress < MAX_PROGRESS - 20) {
            level.playSeededSound(null, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D,
                    SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.1F, 0.6F, 149684163);
            //level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.1F, 0.5F);
        }

        if (entity.progress >= MAX_PROGRESS) {
            Item originalInput = entity.inventory.getStackInSlot(0).getItem();
            // Completion: if originalInput is grapes or apples, try to put MUST_* into the cauldron below.
            if (FLUID_RESULTS.containsKey(originalInput)) {
                BlockPos below = pos.below();
                BlockState belowState = level.getBlockState(below);

                // require vanilla cauldron.
                if (belowState.getBlock() == Blocks.CAULDRON) {
                    BlockState newState = FLUID_RESULTS.get(originalInput).defaultBlockState();
                    level.setBlock(below, newState, 3);
                }

                // consumed into cauldron
                entity.finishProcessing(level, pos, state);
                return;
            }

            ItemStack inputStack = entity.inventory.getStackInSlot(0);

            Item crushedItem = CRUSHING_RESULTS.get(inputStack.getItem());
            ItemStack crushedStack;

            if (inputStack.getItem() == ModItems.AGAVE_PINA_BAKED.get()) {
                crushedStack = new ItemStack(crushedItem, 24 * inputStack.getCount());
            } else {
                crushedStack = new ItemStack(crushedItem, inputStack.getCount());
            }

            ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, crushedStack);
            level.addFreshEntity(itemEntity);

            // Reset the processing state
            entity.finishProcessing(level, pos, state);
        }
    }

    public boolean handleRightClick(Player player, InteractionHand hand) {
        if (level == null || level.isClientSide) return false;
        if (this.isProcessing) return false;

        BlockPos pos = this.getBlockPos();
        BlockState state = this.getBlockState();
        ItemStack heldItem = player.getItemInHand(hand);

        boolean isDryCrushable = CRUSHING_RESULTS.containsKey(heldItem.getItem());

        boolean isGrapes = heldItem.getItem() == ModItems.GRAPES_RED.get() ||
                heldItem.getItem() == ModItems.GRAPES_WHITE.get();

        boolean isApple = heldItem.getItem() == Items.APPLE;

        // If grapes, require a cauldron directly below to start processing
        if (isGrapes || isApple) {
            if (isGrapes && heldItem.getCount() < 12) {
                player.displayClientMessage(Component.translatable("message.tadackosdrinks.manual_crusher_fail_grape_count"), true);
                return false;
            } else if (isApple && heldItem.getCount() < 8) {
                player.displayClientMessage(Component.translatable("message.tadackosdrinks.manual_crusher_fail_apple_count"), true);
                return false;
            }
            BlockPos below = pos.below();
            BlockState belowState = level.getBlockState(below);

            if (belowState.getBlock() != Blocks.CAULDRON) {
                player.displayClientMessage(Component.translatable("message.tadackosdrinks.manual_crusher_fail_cauldron"), true);
                return false; // no cauldron below -> don't start
            }
        }

        if ((isDryCrushable || isGrapes || isApple) && this.inventory.getStackInSlot(0).isEmpty()) {
            // Save original input type so finalization knows what was processed
            int insertCount = heldItem.getCount();
            if (isGrapes) insertCount = 12;
            if (isApple) insertCount = 8;

            // Insert the item into the block's inventory (store exact count)
            this.inventory.setStackInSlot(0, new ItemStack(heldItem.getItem(), insertCount));

            // Remove the items from the player's hand
            if (!player.isCreative()) {
                heldItem.shrink(insertCount);
            }

            // Start the animation and processing
            this.progress = 0;
            this.isProcessing = true;

            if (level != null && !level.isClientSide) {
                // Notify the client
                level.sendBlockUpdated(pos, state, state, 3);
            }
            setChanged();
            return true;
        }

        return false;
    }

    // returns a tag suitable for putting into an ItemStack under "BlockEntityTag"
    public CompoundTag saveToItemTag() {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag); // allowed here because this is the BE class
        return tag;
    }

    // Return true when the block entity has no meaningful data and can be represented by a plain item
    public boolean isDefaultState() {
        if (this.progress != 0) return false;

        // If we got here, it's default/empty
        return true;
    }

    // for renderer
    public ItemStack getSeedStack() {
        return this.inventory.getStackInSlot(0);
    }

    private void finishProcessing(Level level, BlockPos pos, BlockState state) {
        this.isProcessing = false;
        this.progress = 0;
        this.inventory.setStackInSlot(0, ItemStack.EMPTY);

        if (!level.isClientSide) {
            level.sendBlockUpdated(pos, state, state, 3);
        }
        this.setChanged();
    }
}
