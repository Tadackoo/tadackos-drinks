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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.tadacko.tadackosdrinks.block.FermentingBarrelBlock;
import net.tadacko.tadackosdrinks.block.BarrelState;
import net.tadacko.tadackosdrinks.fluid.DrinkwareTransfer;
import net.tadacko.tadackosdrinks.fluid.ModFluids;
import net.tadacko.tadackosdrinks.item.ModItems;
import net.tadacko.tadackosdrinks.util.IFluidColorProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class FermentingBarrelBlockEntity extends BlockEntity implements IFluidColorProvider {
    // Constants for yeast types and maximum amount
    public static final int MAX_YEAST_AMOUNT = 2;
    public static final int MAX_GRAIN_AMOUNT = 4;
    public static final int MAX_SUGAR_AMOUNT = 4;

    // Add yeast tracking variables
    private int yeastAmount = 0;

    // ingredients for yeast
    private int grainAmount = 0;
    private int sugarAmount = 0;

    private int progress = 0;
    private static final int MAX_PROGRESS = 72000 /*60*/; // fermenting time 1h
    private static final int MAX_AGING_PROGRESS = 576000 /*60*/; // aging time 8h
    public boolean isProcessing = false;

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

    private static final Map<Fluid, Fluid> FERMENTING_RESULTS = Map.ofEntries(
            Map.entry(ModFluids.WORT_WHEAT.source().get(), ModFluids.WASH_WHEAT.source().get()),
            Map.entry(ModFluids.WORT_WHEAT_HOPPED.source().get(), ModFluids.WASH_WHEAT.source().get()),
            Map.entry(ModFluids.WORT_WHEAT_BOILED.source().get(), ModFluids.BEER_WHEAT.source().get()),
            Map.entry(ModFluids.WORT_WHEAT_BOILED_HOPPED.source().get(), ModFluids.BEER_WHEAT_HOPPED.source().get()),
            Map.entry(ModFluids.WORT_BARLEY.source().get(), ModFluids.WASH_BARLEY.source().get()),
            Map.entry(ModFluids.WORT_BARLEY_HOPPED.source().get(), ModFluids.WASH_BARLEY.source().get()),
            Map.entry(ModFluids.WORT_BARLEY_BOILED.source().get(), ModFluids.BEER_BARLEY.source().get()),
            Map.entry(ModFluids.WORT_BARLEY_BOILED_HOPPED.source().get(), ModFluids.BEER_BARLEY_HOPPED.source().get()),

            Map.entry(ModFluids.MUST_RED.source().get(), ModFluids.MUST_RED_FERMENTED.source().get()),
            Map.entry(ModFluids.MUST_WHITE.source().get(), ModFluids.MUST_WHITE_FERMENTED.source().get()),
            Map.entry(ModFluids.JUICE_GRAPE_ROSE.source().get(), ModFluids.WINE_ROSE.source().get()),
            Map.entry(ModFluids.JUICE_GRAPE_WHITE.source().get(), ModFluids.WINE_WHITE.source().get()),

            Map.entry(ModFluids.JUICE_APPLE.source().get(), ModFluids.CIDER.source().get()),

            Map.entry(ModFluids.DILUTED_HONEY.source().get(), ModFluids.MEAD.source().get()),

            Map.entry(ModFluids.JUICE_SUGARCANE.source().get(), ModFluids.WASH_SUGARCANE_JUICE.source().get()),
            Map.entry(ModFluids.DILUTED_MOLASSES_SUGARCANE.source().get(), ModFluids.WASH_SUGARCANE_MOLASSES.source().get()),

            Map.entry(ModFluids.MASH_POTATO.source().get(), ModFluids.WASH_POTATO.source().get()),

            Map.entry(ModFluids.JUICE_AGAVE.source().get(), ModFluids.WASH_AGAVE.source().get())
    );

    public static final Map<Fluid, Fluid> AGING_RESULTS = Map.ofEntries(
            Map.entry(ModFluids.WINE_RED.source().get(), ModFluids.WINE_RED_AGED.source().get()),
            Map.entry(ModFluids.WINE_ROSE.source().get(), ModFluids.WINE_ROSE_AGED.source().get()),
            Map.entry(ModFluids.WINE_ORANGE.source().get(), ModFluids.WINE_ORANGE_AGED.source().get()),
            Map.entry(ModFluids.WINE_WHITE.source().get(), ModFluids.WINE_WHITE_AGED.source().get()),

            Map.entry(ModFluids.CIDER.source().get(), ModFluids.CIDER_AGED.source().get()),

            Map.entry(ModFluids.MEAD.source().get(), ModFluids.MEAD_AGED.source().get()),

            Map.entry(ModFluids.SPIRIT_WHEAT_HIGH.source().get(), ModFluids.CONCENTRATED_WHISKY_WHEAT.source().get()),
            Map.entry(ModFluids.SPIRIT_BARLEY_HIGH.source().get(), ModFluids.CONCENTRATED_WHISKY_BARLEY.source().get()),

            Map.entry(ModFluids.SPIRIT_GRAPE_HIGH.source().get(), ModFluids.CONCENTRATED_BRANDY_GRAPE.source().get()),
            Map.entry(ModFluids.SPIRIT_APPLE_HIGH.source().get(), ModFluids.CONCENTRATED_BRANDY_APPLE.source().get()),

            Map.entry(ModFluids.CONCENTRATED_RUM_JUICE.source().get(), ModFluids.CONCENTRATED_RUM_JUICE_AGED.source().get()),
            Map.entry(ModFluids.RUM_JUICE.source().get(), ModFluids.RUM_JUICE_AGED.source().get()),
            Map.entry(ModFluids.CONCENTRATED_RUM_MOLASSES.source().get(), ModFluids.CONCENTRATED_RUM_MOLASSES_AGED.source().get()),
            Map.entry(ModFluids.RUM_MOLASSES.source().get(), ModFluids.RUM_MOLASSES_AGED.source().get()),

            Map.entry(ModFluids.CONCENTRATED_TEQUILA.source().get(), ModFluids.CONCENTRATED_TEQUILA_AGED.source().get()),
            Map.entry(ModFluids.TEQUILA.source().get(), ModFluids.TEQUILA_AGED.source().get())
    );

    private static final ImmutableSet<Fluid> VALID_FLUIDS = ImmutableSet.<Fluid>builder()
            .addAll(FERMENTING_RESULTS.keySet())
            .addAll(FERMENTING_RESULTS.values())
            .addAll(AGING_RESULTS.keySet())
            .addAll(AGING_RESULTS.values())
            .add(Fluids.WATER)
            .build();

    public FermentingBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FERMENTING_BARREL.get(), pos, state);
    }

    private LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();

    // all the lazy fluid handler stuff is apparently about exposing the fluid tank to other block entities (automation, mod compatibility)
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
    protected void saveAdditional(CompoundTag nbt) {
        nbt.putBoolean("isProcessing", isProcessing);
        nbt.putInt("progress", progress);
        nbt.putInt("yeastAmount", yeastAmount);
        nbt.putInt("grainAmount", grainAmount);
        nbt.putInt("sugarAmount", sugarAmount);
        nbt.putBoolean("clock", getBlockState().getValue(FermentingBarrelBlock.CLOCK));
        fluidTank.writeToNBT(nbt);

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        isProcessing = nbt.getBoolean("isProcessing");
        progress = nbt.getInt("progress");
        yeastAmount = nbt.getInt("yeastAmount");
        grainAmount = nbt.getInt("grainAmount");
        sugarAmount = nbt.getInt("sugarAmount");
        // Restore the CLOCK state if level is available
        if (nbt.contains("clock") && level != null) {
            boolean clockState = nbt.getBoolean("clock");
            BlockState currentState = getBlockState();
            if (currentState.hasProperty(FermentingBarrelBlock.CLOCK) && currentState.getValue(FermentingBarrelBlock.CLOCK) != clockState) {
                level.setBlock(worldPosition, currentState.setValue(FermentingBarrelBlock.CLOCK, clockState), 3);
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

    // Check if the fluid in the tank is valid for fermentation
    private boolean hasFluidForFermentation() {
        FluidStack fluidStack = fluidTank.getFluid();
        return fluidStack.getAmount() > 0 && FERMENTING_RESULTS.containsKey(fluidStack.getFluid());
    }

    private boolean hasFluidForAging() {
        FluidStack fluidStack = fluidTank.getFluid();
        return fluidStack.getAmount() > 0 && AGING_RESULTS.containsKey(fluidStack.getFluid());
    }

    // Check if the fluid in the tank is water
    private boolean hasWaterForCultivation() {
        FluidStack fluidStack = fluidTank.getFluid();
        return fluidStack.getAmount() > 0 && fluidStack.getFluid() == Fluids.WATER;
    }

    private boolean addIngredientCollision(int maxAmount, IntSupplier getter, IntConsumer setter) {
        if (level == null || level.isClientSide) return false;

        int currentAmount = getter.getAsInt();

        if (currentAmount >= maxAmount) return false;

        setter.accept(currentAmount + 1);

        setChanged(level, worldPosition, getBlockState());
        return true;
    }

    private boolean addIngredientClick(ItemStack stack, Player player, int maxAmount, IntSupplier getter, IntConsumer setter) {
        if (level == null || level.isClientSide) return false;

        int currentAmount = getter.getAsInt();
        int amountToAdd = Math.min(stack.getCount(), maxAmount - currentAmount);

        if (amountToAdd <= 0) return false;

        setter.accept(currentAmount + amountToAdd);

        if (!player.isCreative()) stack.shrink(amountToAdd);

        setChanged(level, worldPosition, getBlockState());
        return true;
    }

    public boolean addYeastCollision() {
        if (!hasFluidForFermentation()) return false;

        return addIngredientCollision(MAX_YEAST_AMOUNT, () -> yeastAmount, value -> yeastAmount = value);
    }

    public boolean addYeastClick(ItemStack yeastStack, Player player) {
        if (!hasFluidForFermentation()) return false;

        return addIngredientClick(yeastStack, player, MAX_YEAST_AMOUNT, () -> yeastAmount, value -> yeastAmount = value);
    }

    public boolean addGrainCollision() {
        if (!hasWaterForCultivation()) return false;

        return addIngredientCollision(MAX_GRAIN_AMOUNT, () -> grainAmount, value -> grainAmount = value);
    }

    public boolean addGrainClick(ItemStack grainStack, Player player) {
        if (!hasWaterForCultivation()) return false;

        return addIngredientClick(grainStack, player, MAX_GRAIN_AMOUNT, () -> grainAmount, value -> grainAmount = value);
    }

    public boolean addSugarCollision() {
        if (!hasWaterForCultivation()) return false;

        return addIngredientCollision(MAX_SUGAR_AMOUNT, () -> sugarAmount, value -> sugarAmount = value);
    }

    public boolean addSugarClick(ItemStack sugarStack, Player player) {
        if (!hasWaterForCultivation()) return false;

        return addIngredientClick(sugarStack, player, MAX_SUGAR_AMOUNT, () -> sugarAmount, value -> sugarAmount = value);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FermentingBarrelBlockEntity entity) {
        // only run on server
        if (level.isClientSide) return;

        if (!entity.isProcessing) return;

        // Check if there's wort in the tank
        FluidStack fluidStack = entity.fluidTank.getFluid();

        if (state.getValue(FermentingBarrelBlock.STATE) == BarrelState.CLOSED) {
            if (entity.hasFluidForFermentation() && entity.yeastAmount == MAX_YEAST_AMOUNT) {
                // Fermenting logic - only progress if we have yeast
                entity.progress++;

                syncClockIfNeeded(level, pos, state, entity);

                if (entity.progress >= MAX_PROGRESS) {
                    // Get the current fluid
                    Fluid currentFluid = fluidStack.getFluid();

                    // Convert to appropriate beer based on wort type
                    Fluid resultFluid = FERMENTING_RESULTS.get(currentFluid);

                    if (resultFluid != null) {
                        // Convert the wort to beer
                        FluidStack resultFluidStack = new FluidStack(resultFluid, fluidStack.getAmount());
                        entity.fluidTank.setFluid(resultFluidStack);

                        // Reset progress and yeast
                        entity.isProcessing = false;
                        entity.progress = 0;
                        entity.yeastAmount = 0;

                        entity.setChanged();
                    }
                }
            } else if (entity.hasFluidForAging()) {
                entity.progress++;

                syncClockIfNeeded(level, pos, state, entity);

                if (entity.progress >= MAX_AGING_PROGRESS) {

                    Fluid currentFluid = fluidStack.getFluid();

                    // Convert to appropriate fluid
                    Fluid resultFluid = AGING_RESULTS.get(currentFluid);

                    if (resultFluid != null) {
                        // Convert the wort to beer
                        FluidStack resultFluidStack = new FluidStack(resultFluid, fluidStack.getAmount());
                        entity.fluidTank.setFluid(resultFluidStack);

                        // Reset progress
                        entity.isProcessing = false;
                        entity.progress = 0;

                        entity.setChanged();
                    }
                }
            } else if (entity.hasWaterForCultivation() && entity.grainAmount == MAX_GRAIN_AMOUNT && entity.sugarAmount == MAX_SUGAR_AMOUNT) {
                entity.progress++;

                syncClockIfNeeded(level, pos, state, entity);

                if (entity.progress >= MAX_PROGRESS) {
                    level.setBlock(pos, state.setValue(FermentingBarrelBlock.STATE, BarrelState.YEAST), 3);

                    // Reset progress and ingredients except grain type, we need it in handleRightClick
                    entity.isProcessing = false;
                    entity.progress = 0;
                    entity.grainAmount = 0;
                    entity.sugarAmount = 0;

                    entity.setChanged();
                }
            }
        }
    }

    public boolean handleRightClick(Player player, InteractionHand hand) {
        if (level == null || level.isClientSide) return false; // client must not change world here
        ItemStack heldItem = player.getItemInHand(hand);

        BlockPos pos = this.getBlockPos();
        BlockState state = this.getBlockState();

        // Handle ingredient addition
        if (heldItem.getItem() == ModItems.YEAST.get()) {
            return addYeastClick(heldItem, player);
        } else if (heldItem.getItem() == Items.WHEAT || heldItem.getItem() == ModItems.BARLEY.get()) {
            return addGrainClick(heldItem, player);
        } else if (heldItem.getItem() == Items.SUGAR) {
            return addSugarClick(heldItem, player);
        }

        FluidStack fluidStack = this.fluidTank.getFluid();

        // Remove fluid if the player holds empty drinkware
        if (state.getValue(FermentingBarrelBlock.STATE) == BarrelState.OPEN) {
            Optional<Item> filledGlassItem = DrinkwareTransfer.tryFill(this.fluidTank, heldItem);
            if (filledGlassItem.isPresent()) {
                if (!player.isCreative()) {
                    ItemStack filledGlass = new ItemStack(filledGlassItem.get());
                    if (heldItem.getCount() == 1) {
                        player.setItemInHand(hand, filledGlass);
                    } else {
                        heldItem.shrink(1);

                        // Try to add the bucket to the player's inventory
                        boolean added = player.getInventory().add(filledGlass);

                        if (!added) {
                            // Drop the bucket if the inventory is full
                            player.drop(filledGlass, false);
                        }
                    }
                }

                level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);

                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);

                // Reset ingredients when emptying the barrel
                this.yeastAmount = 0;
                this.grainAmount = 0;
                this.sugarAmount = 0;
                this.progress = 0;

                setChanged(level, pos, state);
                return true;
            }
        }

        // Generic fluid container (vanilla buckets, kegs, anything with IFluidHandlerItem)
        // -> bulk transfer with the barrel's tank.
        // Fluid validity for fills is handled internally by FluidTank.fill() via isFluidValid().
        if (state.getValue(FermentingBarrelBlock.STATE) == BarrelState.OPEN) {
            FluidStack before = this.fluidTank.getFluid().copy();
            if (FluidUtil.interactWithFluidHandler(player, hand, this.fluidTank)) {
                FluidStack after = this.fluidTank.getFluid();
                boolean wasDrained = after.getAmount() < before.getAmount();
                SoundEvent sound = wasDrained ? SoundEvents.BUCKET_FILL : SoundEvents.BUCKET_EMPTY;
                level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);

                // Reset ingredients whenever fluid is removed, same as the old bucket-drain logic
                if (wasDrained) {
                    this.yeastAmount = 0;
                    this.grainAmount = 0;
                    this.sugarAmount = 0;
                    this.progress = 0;
                }

                level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
                setChanged(level, pos, state);
                return true;
            }
        }

        // Close the barrel
        if (heldItem.isEmpty() && state.getValue(FermentingBarrelBlock.STATE) == BarrelState.OPEN) {
            if ((hasFluidForFermentation() && this.yeastAmount == MAX_YEAST_AMOUNT) || hasFluidForAging() ||
                    (hasWaterForCultivation() && this.grainAmount == MAX_GRAIN_AMOUNT && this.sugarAmount == MAX_SUGAR_AMOUNT))
                this.isProcessing = true;
            level.setBlock(pos, state.setValue(FermentingBarrelBlock.STATE, BarrelState.CLOSED), 3);
            setChanged(level, pos, state);
            level.playSound(null, pos, SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        }

        // Open the barrel
        if (heldItem.isEmpty() && state.getValue(FermentingBarrelBlock.STATE) == BarrelState.CLOSED && !this.isProcessing) {
            level.setBlock(pos, state.setValue(FermentingBarrelBlock.STATE, BarrelState.OPEN), 3);
            setChanged(level, pos, state);
            level.playSound(null, pos, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        }

        // Open a barrel with yeast
        if (heldItem.isEmpty() && state.getValue(FermentingBarrelBlock.STATE) == BarrelState.YEAST && fluidStack.getAmount() != 0 &&
                fluidStack.getFluid() == Fluids.WATER) {
            this.fluidTank.drain(1000, IFluidHandler.FluidAction.EXECUTE);

            ItemStack itemStack = new ItemStack(ModItems.YEAST.get(), 8);

            ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, itemStack);
            level.addFreshEntity(itemEntity);

            level.setBlock(pos, state.setValue(FermentingBarrelBlock.STATE, BarrelState.OPEN), 3);
            setChanged(level, pos, state);
            level.playSound(null, pos, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        }

        // Can't open the barrel
        if (heldItem.isEmpty() && state.getValue(FermentingBarrelBlock.STATE) == BarrelState.CLOSED && this.progress != 0) {
            player.displayClientMessage(Component.translatable("message.tadackosdrinks.fermenting_barrel_open_fail_progress"), true);
        }

        return false;
    }

    // Method to handle item entity collisions (called from Block class)
    public boolean handleItemEntityCollision(ItemStack stack) {
        if (level == null || level.isClientSide) return false;
        Item item = stack.getItem();

        // Check if the item is a yeast type
        if (item == ModItems.YEAST.get()) {
            if (yeastAmount >= MAX_YEAST_AMOUNT) return false;
            return addYeastCollision();
        } else if (item == Items.WHEAT || item == ModItems.BARLEY.get()) {
            if (grainAmount >= MAX_GRAIN_AMOUNT) return false;
            return addGrainCollision();
        } else if (item == Items.SUGAR) {
            if (sugarAmount >= MAX_SUGAR_AMOUNT) return false;
            return addSugarCollision();
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

    public int getMaxAgingProgress() {
        return MAX_AGING_PROGRESS;
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
        if (this.getBlockState().getValue(FermentingBarrelBlock.CLOCK)) return false;
        if (this.yeastAmount != 0) return false;
        if (this.grainAmount != 0) return false;
        if (this.sugarAmount != 0) return false;
        if (this.progress != 0) return false;

        // If we got here, it's default/empty
        return true;
    }

    private static void syncClockIfNeeded(Level level, BlockPos pos, BlockState state, FermentingBarrelBlockEntity entity) {
        if (entity.progress % 20 == 0 && entity.getBlockState().getValue(FermentingBarrelBlock.CLOCK)) {
            entity.setChanged(); // mark dirty (avoid sending full block update every tick)
            // Force sync to client every second for clock hand rendering
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }
}