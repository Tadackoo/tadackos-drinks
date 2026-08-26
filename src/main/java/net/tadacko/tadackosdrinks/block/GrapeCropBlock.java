package net.tadacko.tadackosdrinks.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class GrapeCropBlock extends CropBlock {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 10);
    public static final BooleanProperty TRELLIS = BooleanProperty.create("trellis");
    public static final BooleanProperty WIRE_NORTH = BooleanProperty.create("wire_north");
    public static final BooleanProperty WIRE_SOUTH = BooleanProperty.create("wire_south");
    public static final BooleanProperty WIRE_EAST = BooleanProperty.create("wire_east");
    public static final BooleanProperty WIRE_WEST = BooleanProperty.create("wire_west");

    public static final int MAX_AGE = 3;

    // natural spread chance: 1 in SPREAD_TIME each eligible tick
    private static final int SPREAD_TIME = 5;

    private final Supplier<Item> seedItem;
    private final Supplier<Block> wireCropVariant;

    private static final VoxelShape[] SHAPES = new VoxelShape[] {
            Block.box(5.0D, 0.0D, 5.0D, 11.0D, 10.0D, 11.0D),
            Block.box(5.0D, 0.0D, 5.0D, 11.0D, 16.0D, 11.0D)
    };

    public GrapeCropBlock(Properties properties, Supplier<Item> seedItem, Supplier<Block> wireCropVariant) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AGE, 0)
                .setValue(VARIANT, 0)
                .setValue(TRELLIS, false)
                .setValue(WIRE_NORTH,  false)
                .setValue(WIRE_SOUTH,  false)
                .setValue(WIRE_EAST,  false)
                .setValue(WIRE_WEST,  false));
        this.seedItem = seedItem;
        this.wireCropVariant = wireCropVariant;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        boolean hasTrellis = pState.getValue(TRELLIS);
        return SHAPES[hasTrellis ? 1 : 0];
    }

    @Override
    protected int getBonemealAgeIncrease(Level pLevel) {
        return 1;
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return MAX_AGE;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return seedItem.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, VARIANT, TRELLIS, WIRE_NORTH, WIRE_SOUTH, WIRE_EAST, WIRE_WEST);
    }

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, IPlantable plantable) {
        return super.mayPlaceOn(state, world, pos);
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        return super.canSurvive(pState, pLevel, pPos) || pLevel.getBlockState(pPos.below()).is(this);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            boolean hasTrellisAbove = level.getBlockState(pos.above()).getBlock() instanceof TrellisBlock ||
                    level.getBlockState(pos.above()).getBlock() instanceof GrapeCropBlock;
            BlockState newState = state.setValue(TRELLIS, hasTrellisAbove);

            level.setBlock(pos, newState, 2);

            if (!this.canSurvive(newState, level, pos)) {
                level.destroyBlock(pos, true);
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.isClientSide) return;

        if (fromPos.equals(pos.above())) {
            boolean hasTrellis = level.getBlockState(pos.above()).getBlock() instanceof TrellisBlock ||
                    level.getBlockState(pos.above()).getBlock() instanceof GrapeCropBlock;
            int age = state.getValue(getAgeProperty());
            BlockState newState = state.setValue(TRELLIS, hasTrellis);

            if (!hasTrellis && age > 1) {
                newState = newState.setValue(getAgeProperty(), 1);
            }

            if (newState != state) {
                level.setBlock(pos, newState, 2);
            }

            if (!this.canSurvive(newState, level, pos)) {
                level.destroyBlock(pos, true);
            }
        } else {
            // If a horizontal neighbor changed, recompute wires
            if (fromPos.getY() == pos.getY()) {
                BlockState newState = updateWireConnections(state, level, pos);
                if (newState != state) level.setBlock(pos, newState, Block.UPDATE_ALL);
            }
        }
    }

    @Override
    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        if (!pLevel.isAreaLoaded(pPos, 1)) return;
        if (pLevel.getRawBrightness(pPos, 0) < 9) return;

        // keep wire flags up-to-date
        BlockState wireUpdated = updateWireConnections(pState, pLevel, pPos);
        if (wireUpdated != pState) pLevel.setBlock(pPos, wireUpdated, 2);

        int currentAge = this.getAge(pState);
        float growthSpeed = getGrowthSpeed(this, pLevel, pPos);

        boolean growthHappens = pRandom.nextInt((int) (120.0F / growthSpeed) + 1) == 0;
        if (!ForgeHooks.onCropsGrowPre(pLevel, pPos, pState, growthHappens)) return;
        if (!growthHappens) return;

        if (currentAge >= 1 && pRandom.nextInt(SPREAD_TIME) == 0) attemptSideSpread(pLevel, pPos);

        if (currentAge == MAX_AGE) {
            if (pLevel.getBlockState(pPos.above()).getBlock() instanceof TrellisBlock) {
                // Preserve VARIANT when spawning above
                BlockState newState = this.defaultBlockState()
                        .setValue(AGE, 0)
                        .setValue(VARIANT, pState.getValue(VARIANT));
                pLevel.setBlock(pPos.above(), newState, Block.UPDATE_ALL);
            }
        } else if (currentAge == 0 || pLevel.getBlockState(pPos.above()).getBlock() instanceof TrellisBlock) { // prevent going above age 1 if no trellis
            int nextAge = currentAge + 1;
            pLevel.setBlock(pPos, pState.setValue(AGE, nextAge), Block.UPDATE_ALL);
        }

        ForgeHooks.onCropsGrowPost(pLevel, pPos, pState);
    }


    @Override
    public boolean isValidBonemealTarget(LevelReader pLevel, BlockPos pPos, BlockState pState, boolean pIsClient) {
        int age = getAge(pState);

        if (age < MAX_AGE) return true;

        boolean canSpreadUp = pLevel.getBlockState(pPos.above()).getBlock() instanceof TrellisBlock;
        boolean canSpreadSideways = false;

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos wirePos = pPos.relative(dir);
            if (canGroundSpreadToWire((LevelAccessor) pLevel, wirePos, dir)) {
                canSpreadSideways = true;
                break;
            }
        }

        return canSpreadUp || canSpreadSideways;
    }

    @Override
    public void growCrops(Level pLevel, BlockPos pPos, BlockState pState) {
        int currentAge = this.getAge(pState);
        int nextAge = currentAge + this.getBonemealAgeIncrease(pLevel);
        if (nextAge > MAX_AGE) nextAge = MAX_AGE;

        if (currentAge >= 1) attemptSideSpread(pLevel, pPos);

        if (currentAge == MAX_AGE) {
            if (pLevel.getBlockState(pPos.above()).getBlock() instanceof TrellisBlock) {
                // Preserve VARIANT when spawning above
                BlockState newState = this.defaultBlockState()
                        .setValue(AGE, 0)
                        .setValue(VARIANT, pState.getValue(VARIANT));
                pLevel.setBlock(pPos.above(), newState, Block.UPDATE_ALL);
            }
        } else if (currentAge == 0 || pLevel.getBlockState(pPos.above()).getBlock() instanceof TrellisBlock) { // prevent going above age 1 if no trellis
            pLevel.setBlock(pPos, pState.setValue(AGE, nextAge), Block.UPDATE_ALL);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState state = this.defaultBlockState();
        boolean hasTrellisAbove = level.getBlockState(pos.above()).getBlock() instanceof TrellisBlock ||
                level.getBlockState(pos.above()).getBlock() instanceof GrapeCropBlock;
        state = state.setValue(getAgeProperty(), 0).setValue(TRELLIS, hasTrellisAbove);

        return updateWireConnections(state, level, pos);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        // Always tick, because grapes can spread even when mature
        return true;
    }

    // onRemove + defer replacement to make it work in creative and drop stuff in survival
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.isAir() && level instanceof ServerLevel serverLevel) {
            serverLevel.getServer().execute(() -> {
                BlockState trellis;
                switch (state.getValue(VARIANT)) {
                    case 1 -> trellis = ModBlocks.TRELLIS_SPRUCE.get().defaultBlockState();
                    case 2 -> trellis = ModBlocks.TRELLIS_BIRCH.get().defaultBlockState();
                    case 3 -> trellis = ModBlocks.TRELLIS_JUNGLE.get().defaultBlockState();
                    case 4 -> trellis = ModBlocks.TRELLIS_ACACIA.get().defaultBlockState();
                    case 5 -> trellis = ModBlocks.TRELLIS_DARK_OAK.get().defaultBlockState();
                    case 6 -> trellis = ModBlocks.TRELLIS_MANGROVE.get().defaultBlockState();
                    case 7 -> trellis = ModBlocks.TRELLIS_CHERRY.get().defaultBlockState();
                    case 8 -> trellis = ModBlocks.TRELLIS_BAMBOO.get().defaultBlockState();
                    case 9 -> trellis = ModBlocks.TRELLIS_CRIMSON.get().defaultBlockState();
                    case 10 -> trellis = ModBlocks.TRELLIS_WARPED.get().defaultBlockState();
                    default -> trellis = ModBlocks.TRELLIS_OAK.get().defaultBlockState();
                }
                level.setBlock(pos, trellis, Block.UPDATE_ALL);
                BlockState updated = trellis.updateShape(Direction.UP, level.getBlockState(pos.above()), level, pos, pos.above());
                level.setBlock(pos, updated, Block.UPDATE_ALL);
            });
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public static BlockState updateWireConnections(BlockState state, LevelAccessor level, BlockPos pos) {
        BlockState updated = state;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BooleanProperty wireProp = TrellisBlock.getWireProp(dir);
            BlockPos checkPos = pos.relative(dir);
            BlockState neighborState = level.getBlockState(checkPos);
            boolean hasWire = false;

            // Accept facing toward this block OR away from this block
            if (neighborState.getBlock() instanceof TrellisWireBlock) {
                Direction neighborFacing = neighborState.getValue(TrellisWireBlock.FACING);
                if (neighborFacing == dir.getOpposite() || neighborFacing == dir) {
                    hasWire = true;
                }
            } else if (neighborState.getBlock() instanceof GrapeWireCropBlock) {
                Direction neighborFacing = neighborState.getValue(GrapeWireCropBlock.FACING);
                if (neighborFacing == dir.getOpposite() || neighborFacing == dir) {
                    hasWire = true;
                }
            }

            updated = updated.setValue(wireProp, hasWire);
        }
        return updated;
    }

    // Helper that decides whether we can spread from ground crop at `pos` into a given wire at `wirePos`.
    // Uses same policy as GrapeWireCropBlock.canSpreadToWire but also ensures there's a TrellisWire block to replace.
    private boolean canGroundSpreadToWire(LevelAccessor level, BlockPos wirePos, Direction dir) {
        BlockState state = level.getBlockState(wirePos);

        if (state.getBlock() instanceof TrellisWireBlock) {
            Direction neighborFacing = state.getValue(TrellisWireBlock.FACING);
            return neighborFacing == dir || neighborFacing == dir.getOpposite();
        } else if (state.getBlock() instanceof GrapeWireCropBlock) {
            Direction neighborFacing = state.getValue(GrapeWireCropBlock.FACING);
            return neighborFacing == dir || neighborFacing == dir.getOpposite();
        }

        return false;
    }

    /**
     * Attempt to spread from the ground grape at 'pPos' into adjacent TrellisWire blocks.
     * Places the GRAPE_WIRE_CROP block at each valid wire position.
     */
    private void attemptSideSpread(LevelAccessor pLevel, BlockPos pPos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos wirePos = pPos.relative(dir);
            if (!canGroundSpreadToWire(pLevel, wirePos, dir)) continue;

            BlockState child = this.wireCropVariant.get().defaultBlockState()
            .setValue(GrapeWireCropBlock.AGE, 0).setValue(GrapeWireCropBlock.FACING, dir);

            pLevel.setBlock(wirePos, child, Block.UPDATE_ALL);
        }
    }
}