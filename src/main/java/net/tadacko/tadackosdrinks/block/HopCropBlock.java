package net.tadacko.tadackosdrinks.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.tadacko.tadackosdrinks.item.ModItems;

public class HopCropBlock extends CropBlock {
    public static final int SPREAD_AGE = 2;
    public static final int MAX_AGE = 3;

    private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[] {
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D),
            Block.box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 13.0D),
            Block.box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 13.0D),
            Block.box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 13.0D)
    };

    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);

    public static final IntegerProperty UNSUPPORTED = IntegerProperty.create("unsupported", 0, 2);
    // how many scheduled ticks without support before breaking
    private static final int UNSUPPORTED_THRESHOLD = 2;
    private static final int CHECK_DELAY = 1; // check every tick while unsupported

    public HopCropBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(UNSUPPORTED, 0));
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE_BY_AGE[this.getAge(pState)];
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block changedBlock, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, changedBlock, fromPos, isMoving);
        if (level.isClientSide) return;

        if (!fromPos.equals(pos.above())) return;

        level.scheduleTick(pos, this, CHECK_DELAY);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pState.getValue(AGE) < getMaxAge()) return InteractionResult.PASS;

        if (!pLevel.isClientSide) {
            int j = 1 + pLevel.random.nextInt(2); // 50% split 1 or 2
            ItemStack drop = new ItemStack(ModItems.HOPS.get(), j);

            if (!pPlayer.addItem(drop)) {
                ItemEntity itemEntity = new ItemEntity(pLevel, pPos.getX() + 0.5, pPos.getY() + 0.5, pPos.getZ() + 0.5, drop);
                pLevel.addFreshEntity(itemEntity);
            }
            pLevel.setBlock(pPos, this.getStateForAge(getMaxAge() - 1), 2);
        }
        return InteractionResult.sidedSuccess(pLevel.isClientSide);
    }

    @Override
    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        if (!pLevel.isAreaLoaded(pPos, 1)) return;
        if (pLevel.getRawBrightness(pPos, 0) >= 9) {
            int currentAge = this.getAge(pState);
            float growthSpeed = getGrowthSpeed(this, pLevel, pPos);
            if (ForgeHooks.onCropsGrowPre(pLevel, pPos, pState, pRandom.nextInt((int) (120.0F / growthSpeed) + 1) == 0)) {
                if (currentAge >= SPREAD_AGE && pLevel.getBlockState(pPos.above()).is(ModBlocks.ROPE.get())) {
                    pLevel.setBlock(pPos.above(), this.getStateForAge(0), 2);
                } else if (currentAge < this.getMaxAge()) {
                    pLevel.setBlock(pPos, this.getStateForAge(currentAge + 1), 2);
                }
                ForgeHooks.onCropsGrowPost(pLevel, pPos, pState);
            }
        }
    }

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, IPlantable plantable) {
        return super.mayPlaceOn(state, world, pos);
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        return super.canSurvive(pState, pLevel, pPos) || (pLevel.getBlockState(pPos.below()).is(this) &&
                pLevel.getBlockState(pPos.below()).getValue(AGE) >= SPREAD_AGE);
    }

    @Override
    public void growCrops(Level pLevel, BlockPos pPos, BlockState pState) {
        int nextAge = this.getAge(pState) + this.getBonemealAgeIncrease(pLevel);
        int maxAge = this.getMaxAge();
        if (nextAge > maxAge) nextAge = maxAge;

        if (this.getAge(pState) >= SPREAD_AGE && pLevel.getBlockState(pPos.above()).is(ModBlocks.ROPE.get())) {
            pLevel.setBlock(pPos.above(), this.getStateForAge(0), 2);
        } else {
            pLevel.setBlock(pPos, this.getStateForAge(nextAge), 2);
        }
    }

    @Override
    protected int getBonemealAgeIncrease(Level pLevel) {
        return Mth.nextInt(pLevel.random, 1, 2);
    }

    @Override
    public int getMaxAge() {
        return MAX_AGE;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.HOP_SEEDS.get();
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader pLevel, BlockPos pPos, BlockState pState, boolean pIsClient) {
        return !(this.isMaxAge(pState) && (pLevel.getBlockState(pPos.above()).is(this) ||
                pLevel.getBlockState(pPos.above()).is(Blocks.AIR)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(AGE, UNSUPPORTED);
    }

    // onRemove + defer replacement to make it work in creative and drop stuff in survival
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.isAir() && level instanceof ServerLevel serverLevel) {
            serverLevel.getServer().execute(() -> {
                BlockState rope = ModBlocks.ROPE.get().defaultBlockState();
                level.setBlock(pos, rope, Block.UPDATE_ALL);
                level.scheduleTick(pos, ModBlocks.ROPE.get(), CHECK_DELAY);
            });
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        BlockState above = level.getBlockState(pos.above());
        boolean hasAboveSupport = above.is(this) || above.is(ModBlocks.ROPE.get()) || above.getBlock() instanceof TrellisBlock;

        if (hasAboveSupport) {
            if (state.getValue(UNSUPPORTED) != 0) {
                BlockState reset = state.setValue(UNSUPPORTED, 0);
                level.setBlock(pos, reset, Block.UPDATE_ALL);
            }
            return;
        }

        int unsupported = state.getValue(UNSUPPORTED);
        int next = Math.min(UNSUPPORTED_THRESHOLD, unsupported + CHECK_DELAY);
        BlockState updated = state.setValue(UNSUPPORTED, next);
        level.setBlock(pos, updated, Block.UPDATE_ALL);

        if (next == UNSUPPORTED_THRESHOLD) {
            level.destroyBlock(pos, true);
        } else {
            level.scheduleTick(pos, this, CHECK_DELAY);
        }
    }
}
