package net.tadacko.tadackosdrinks.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.tadacko.tadackosdrinks.block.ModBlocks;
import net.tadacko.tadackosdrinks.block.PlaceableDrinkwareBlock;
import net.tadacko.tadackosdrinks.block.entity.KegBlockEntity;
import net.tadacko.tadackosdrinks.block.entity.PlaceableDrinkwareBlockEntity;
import net.tadacko.tadackosdrinks.fluid.CauldronFluidRegistry;
import net.tadacko.tadackosdrinks.fluid.DrinkwareTransfer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class KegItem extends Item {

    public KegItem(Properties properties) {
        super(properties);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final LazyOptional<IFluidHandlerItem> handler =
                    LazyOptional.of(() -> new FluidHandlerItemStack(stack, KegBlockEntity.CAPACITY));

            @NotNull
            @Override
            public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(cap, handler.cast());
            }
        };
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null) return InteractionResult.PASS;

        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        ItemStack kegStack = context.getItemInHand();

        // crouch -> place the keg down as a block, carrying over its stored fluid
        if (player.isCrouching()) {
            BlockPos placePos = clickedState.canBeReplaced() ? clickedPos : clickedPos.relative(context.getClickedFace());
            if (level.getBlockState(placePos).canBeReplaced()) {
                return placeKeg(context, placePos);
            }
        }

        // clicked on a placed drinkware block -> try exchanging fluid with whatever's stored in it
        if (level.getBlockEntity(clickedPos) instanceof PlaceableDrinkwareBlockEntity jarBE) {
            Optional<IFluidHandlerItem> tank = kegStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve();
            if (tank.isPresent()) {
                ItemStack stored = jarBE.getStoredStack();

                Optional<Item> filled = DrinkwareTransfer.tryFill(tank.get(), stored);
                if (filled.isPresent()) {
                    updateDrinkwareBlock(level, clickedPos, clickedState, jarBE, filled.get());
                    level.playSound(null, clickedPos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }

                Optional<Item> emptied = DrinkwareTransfer.tryEmpty(tank.get(), stored);
                if (emptied.isPresent()) {
                    updateDrinkwareBlock(level, clickedPos, clickedState, jarBE, emptied.get());
                    level.playSound(null, clickedPos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }

        // Handle cauldron blocks — they expose no IFluidHandler capability so FluidUtil
        // won't find them. Every cauldron is treated as a single 1000 mB unit, like a bucket.
        Block clickedBlock = clickedState.getBlock();
        boolean isEmptyCauldron = clickedBlock == Blocks.CAULDRON;
        CauldronFluidRegistry.Entry cauldronEntry = CauldronFluidRegistry.getForBlock(clickedBlock);

        if (isEmptyCauldron || cauldronEntry != null) {
            if (!level.isClientSide) {
                Optional<IFluidHandlerItem> kegHandler = kegStack
                        .getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve();
                if (kegHandler.isPresent()) {
                    IFluidHandler.FluidAction kegAction = player.getAbilities().instabuild
                            ? IFluidHandler.FluidAction.SIMULATE
                            : IFluidHandler.FluidAction.EXECUTE;

                    if (cauldronEntry != null) {
                        // Filled cauldron -> drain its full amount into the keg and empty it
                        FluidStack toTransfer = new FluidStack(cauldronEntry.fluid(), cauldronEntry.amount());
                        if (kegHandler.get().fill(toTransfer, IFluidHandler.FluidAction.SIMULATE) >= cauldronEntry.amount()) {
                            kegHandler.get().fill(toTransfer, kegAction);
                            level.setBlock(clickedPos, Blocks.CAULDRON.defaultBlockState(), 3);
                            level.playSound(null, clickedPos, SoundEvents.BUCKET_FILL,
                                    SoundSource.BLOCKS, 1.0F, 1.0F);
                            return InteractionResult.sidedSuccess(false);
                        }
                    } else {
                        // Empty cauldron -> pour the keg's fluid in and fill the cauldron
                        FluidStack kegFluid = kegHandler.get()
                                .drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
                        if (!kegFluid.isEmpty()) {
                            Block targetCauldron = CauldronFluidRegistry.getCauldronForFluid(kegFluid.getFluid());
                            if (targetCauldron != null) {
                                CauldronFluidRegistry.Entry targetEntry =
                                        CauldronFluidRegistry.getForBlock(targetCauldron);
                                if (kegFluid.getAmount() >= targetEntry.amount()) {
                                    kegHandler.get().drain(
                                            new FluidStack(kegFluid.getFluid(), targetEntry.amount()),
                                            kegAction);
                                    BlockState newState = targetCauldron.defaultBlockState();
                                    // Water cauldron uses LEVEL; set to max so it appears full
                                    if (newState.hasProperty(LayeredCauldronBlock.LEVEL)) {
                                        int maxLevel = LayeredCauldronBlock.LEVEL.getPossibleValues()
                                                .stream().mapToInt(Integer::intValue).max().orElse(1);
                                        newState = newState.setValue(LayeredCauldronBlock.LEVEL, maxLevel);
                                    }
                                    level.setBlock(clickedPos, newState, 3);
                                    level.playSound(null, clickedPos, SoundEvents.BUCKET_EMPTY,
                                            SoundSource.BLOCKS, 1.0F, 1.0F);
                                    return InteractionResult.sidedSuccess(false);
                                }
                            }
                        }
                    }
                }
            } else {
                // Client-side: return success optimistically so the arm swings
                return InteractionResult.sidedSuccess(true);
            }
        }

        // otherwise, try to exchange fluid with whatever was clicked
        // (fermenting barrels, other placed kegs, anything exposing IFluidHandler)
        return FluidUtil.getFluidHandler(level, clickedPos, context.getClickedFace())
                .map(blockHandler -> FluidUtil.interactWithFluidHandler(player, context.getHand(), blockHandler)
                        ? InteractionResult.sidedSuccess(level.isClientSide)
                        : InteractionResult.PASS)
                .orElse(InteractionResult.PASS);
    }

    private InteractionResult placeKeg(UseOnContext context, BlockPos placePos) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        Direction facing = context.getHorizontalDirection().getOpposite();

        level.setBlock(placePos, ModBlocks.KEG_BLOCK.get().defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing), 3);
        level.playSound(null, placePos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);

        if (!level.isClientSide && level.getBlockEntity(placePos) instanceof KegBlockEntity keg) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains(FluidHandlerItemStack.FLUID_NBT_KEY)) {
                keg.loadFromItemTag(tag);
            }
        }

        if (player != null && !player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    // Updates the block entity's stored item AND the block's VARIANT property so the
    // rendered model (fluid level/colour) matches the new contents.
    private void updateDrinkwareBlock(Level level, BlockPos pos, BlockState state,
                                      PlaceableDrinkwareBlockEntity jarBE, Item resultItem) {
        jarBE.setStoredStack(new ItemStack(resultItem));

        if (resultItem instanceof PlaceableDrinkwareItem drinkwareItem) {
            level.setBlock(pos, state.setValue(PlaceableDrinkwareBlock.VARIANT, drinkwareItem.getVariant()), 3);
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getFluidAmount(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getFluidAmount(stack) / KegBlockEntity.CAPACITY);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        FluidStack fluid = getFluidStack(stack);
        if (fluid.isEmpty()) return 0xFFFFFF;
        return IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        FluidStack fluid = getFluidStack(stack);
        if (!fluid.isEmpty()) {
            tooltip.add(getFluidName(fluid).copy().withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(fluid.getAmount() + "/" + KegBlockEntity.CAPACITY + " mB").withStyle(ChatFormatting.GRAY));
        }
    }

    // helpers also used by the item color handler (see below)
    public static FluidStack getFluidStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(FluidHandlerItemStack.FLUID_NBT_KEY)) {
            return FluidStack.loadFluidStackFromNBT(tag.getCompound(FluidHandlerItemStack.FLUID_NBT_KEY));
        }
        return FluidStack.EMPTY;
    }

    private static int getFluidAmount(ItemStack stack) {
        return getFluidStack(stack).getAmount();
    }

    private static Component getFluidName(FluidStack fluidStack) {
        ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid());
        if (id == null) {
            return fluidStack.getDisplayName(); // fallback
        }
        return Component.translatable("fluid." + id.getNamespace() + "." + id.getPath());
    }
}