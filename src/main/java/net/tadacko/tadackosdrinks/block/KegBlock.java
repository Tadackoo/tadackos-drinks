package net.tadacko.tadackosdrinks.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.tadacko.tadackosdrinks.block.entity.KegBlockEntity;
import net.tadacko.tadackosdrinks.fluid.DrinkwareTransfer;
import net.tadacko.tadackosdrinks.item.ModItems;

import java.util.Optional;

public class KegBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Block.box(
            3.0D, 0.0D, 3.0D,
            13.0D, 12.0D, 13.0D
    );

    public KegBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new KegBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.sidedSuccess(true);

        if (!(level.getBlockEntity(pos) instanceof KegBlockEntity keg)) return InteractionResult.PASS;

        ItemStack held = player.getItemInHand(hand);
        IFluidHandler tank = keg.getFluidHandler();

        // fill drinkware from keg
        Optional<Item> filled = DrinkwareTransfer.tryFill(tank, held);
        if (filled.isPresent()) {
            giveResult(player, hand, held, new ItemStack(filled.get()));
            level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.sidedSuccess(false);
        }

        // empty drinkware into keg
        Optional<Item> emptied = DrinkwareTransfer.tryEmpty(tank, held);
        if (emptied.isPresent()) {
            giveResult(player, hand, held, new ItemStack(emptied.get()));
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.sidedSuccess(false);
        }

        // generic fluid container transfer
        FluidStack before = keg.getFluid().copy();
        if (FluidUtil.interactWithFluidHandler(player, hand, tank)) {
            FluidStack after = keg.getFluid();
            SoundEvent sound = after.getAmount() > before.getAmount() ? SoundEvents.BUCKET_EMPTY : SoundEvents.BUCKET_FILL;
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.sidedSuccess(false);
        }
        return InteractionResult.PASS;
    }

    public static void pickUpKeg(Level level, BlockPos pos, KegBlockEntity keg, Player player) {
        ItemStack kegItem = buildKegItemStack(keg);

        if (!player.getInventory().add(kegItem)) {
            player.drop(kegItem, false);
        }

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        level.playSound(null, pos, SoundEvents.METAL_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private static ItemStack buildKegItemStack(KegBlockEntity keg) {
        ItemStack kegItem = new ItemStack(ModItems.KEG.get());

        CompoundTag fluidTag = keg.saveToItemTag();
        if (!fluidTag.isEmpty()) {
            kegItem.getOrCreateTag().merge(fluidTag);
        }

        return kegItem;
    }

    private void giveResult(Player player, InteractionHand hand, ItemStack held, ItemStack result) {
        held.shrink(1);
        if (held.isEmpty()) {
            player.setItemInHand(hand, result);
        } else if (!player.getInventory().add(result)) {
            player.drop(result, false);
        }
    }

    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && !player.getAbilities().instabuild
                && level.getBlockEntity(pos) instanceof KegBlockEntity keg) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), buildKegItemStack(keg));
        }
        super.playerWillDestroy(level, pos, state, player);
    }
}