package net.tadacko.tadackosdrinks.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.tadacko.tadackosdrinks.item.ModItems;

public class TrellisBlock extends Block implements SimpleWaterloggedBlock {
    public static final BooleanProperty ROPE    = BooleanProperty.create("rope");
    public static final BooleanProperty WIRE_NORTH = BooleanProperty.create("wire_north");
    public static final BooleanProperty WIRE_SOUTH = BooleanProperty.create("wire_south");
    public static final BooleanProperty WIRE_EAST = BooleanProperty.create("wire_east");
    public static final BooleanProperty WIRE_WEST = BooleanProperty.create("wire_west");
    public static final BooleanProperty UP    = BooleanProperty.create("up");
    public static final BooleanProperty DOWN  = BooleanProperty.create("down");
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty EAST  = BooleanProperty.create("east");
    public static final BooleanProperty WEST  = BooleanProperty.create("west");

    private static final VoxelShape CENTER = Block.box(6, 6, 6, 10, 10, 10);
    private static final VoxelShape ARM_UP    = Block.box(6, 10, 6, 10, 16, 10);
    private static final VoxelShape ARM_DOWN  = Block.box(6, 0, 6, 10, 6, 10);
    private static final VoxelShape ARM_NORTH = Block.box(6, 6, 0, 10, 10, 6);
    private static final VoxelShape ARM_SOUTH = Block.box(6, 6, 10,10, 10,16);
    private static final VoxelShape ARM_EAST  = Block.box(10,6, 6, 16,10,10);
    private static final VoxelShape ARM_WEST  = Block.box(0, 6, 6, 6, 10,10);

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public TrellisBlock(Properties props) {
        super(props);
        // default: no connections
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(ROPE,  false)
                        .setValue(WIRE_NORTH,  false)
                        .setValue(WIRE_SOUTH,  false)
                        .setValue(WIRE_EAST,  false)
                        .setValue(WIRE_WEST,  false)
                        .setValue(UP,    false)
                        .setValue(DOWN,  false)
                        .setValue(NORTH, false)
                        .setValue(SOUTH, false)
                        .setValue(EAST,  false)
                        .setValue(WEST,  false)
                        .setValue(WATERLOGGED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b) {
        b.add(ROPE, WIRE_NORTH, WIRE_SOUTH, WIRE_EAST, WIRE_WEST, UP, DOWN, NORTH, SOUTH, EAST, WEST, WATERLOGGED);
    }

    // compute all six flags at placement
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockState state = this.defaultBlockState();
        for (Direction dir : Direction.values()) {
            boolean conn = connectsTo(level, pos.relative(dir));
            state = state.setValue(getProp(dir), conn);
        }
        // initialize WIRE_* properties (horizontal only)
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BooleanProperty wireProp = getWireProp(dir);
            BlockPos checkPos = pos.relative(dir);
            BlockState neighborState = level.getBlockState(checkPos);
            boolean hasWire = false;

            // Count trellis wire or grape wire crop only when:
            // - the neighbor has no FACING property (treat as always connected), OR
            // - the neighbor's FACING points either toward OR away from this block
            if (neighborState.is(ModBlocks.TRELLIS_WIRE.get()) || neighborState.is(ModBlocks.GRAPE_WIRE_CROP_RED.get()) ||
                    neighborState.is(ModBlocks.GRAPE_WIRE_CROP_WHITE.get())) {
                if (!neighborState.hasProperty(GrapeWireCropBlock.FACING)) {
                    // neighbor has no facing property (e.g. trellis wire); count it
                    hasWire = true;
                } else {
                    Direction neighborFacing = neighborState.getValue(GrapeWireCropBlock.FACING);
                    // Accept facing toward this block OR away from this block
                    if (neighborFacing == dir.getOpposite() || neighborFacing == dir) {
                        hasWire = true;
                    }
                }
            }

            state = state.setValue(wireProp, hasWire);
        }
        boolean water = level.getFluidState(pos).getType() == Fluids.WATER;
        state = state.setValue(WATERLOGGED, water);

        return state;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.defaultFluidState() : super.getFluidState(state);
    }

    // update only the changed side, leave others alone
    @Override
    public BlockState updateShape(BlockState state, Direction side,
                                  BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        BlockState updated = state;

        // Recompute all six directional ARM properties (UP/DOWN/NORTH/...)
        for (Direction dir : Direction.values()) {
            BooleanProperty armProp = getProp(dir); // UP, DOWN, NORTH, SOUTH, EAST, WEST
            BlockPos checkPos = pos.relative(dir);
            boolean conn = connectsTo(level, checkPos);
            updated = updated.setValue(armProp, conn);
        }

        // Update horizontal WIRE_* properties based on presence of TrellisWire or GrapeWireCrop blocks
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BooleanProperty wireProp = getWireProp(dir);
            BlockPos checkPos = pos.relative(dir);
            BlockState neighborState = level.getBlockState(checkPos);
            boolean hasWire = false;

            // Count trellis wire or grape wire crop only when:
            // - the neighbor has no FACING property (treat as always connected), OR
            // - the neighbor's FACING points either toward OR away from this block
            if (neighborState.is(ModBlocks.TRELLIS_WIRE.get()) || neighborState.is(ModBlocks.GRAPE_WIRE_CROP_RED.get()) ||
                    neighborState.is(ModBlocks.GRAPE_WIRE_CROP_WHITE.get())) {
                if (!neighborState.hasProperty(GrapeWireCropBlock.FACING)) {
                    // neighbor has no facing property (e.g. trellis wire); count it
                    hasWire = true;
                } else {
                    Direction neighborFacing = neighborState.getValue(GrapeWireCropBlock.FACING);
                    // Accept facing toward this block OR away from this block
                    if (neighborFacing == dir.getOpposite() || neighborFacing == dir) {
                        hasWire = true;
                    }
                }
            }

            updated = updated.setValue(wireProp, hasWire);
        }

        // Preserve rope behaviour (unchanged)
        Block below = level.getBlockState(pos.below()).getBlock();
        boolean hasRope = below instanceof RopeBlock || below instanceof HopCropBlock;
        updated = updated.setValue(ROPE, hasRope);

        // schedule water tick if waterlogged so flowing works
        if (updated.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return updated;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        VoxelShape shape = CENTER;

        if (pState.getValue(DOWN))  shape = Shapes.or(shape, ARM_DOWN);
        if (pState.getValue(UP))    shape = Shapes.or(shape, ARM_UP);
        if (pState.getValue(NORTH)) shape = Shapes.or(shape, ARM_NORTH);
        if (pState.getValue(SOUTH)) shape = Shapes.or(shape, ARM_SOUTH);
        if (pState.getValue(WEST))  shape = Shapes.or(shape, ARM_WEST);
        if (pState.getValue(EAST))  shape = Shapes.or(shape, ARM_EAST);

        return shape;
    }

    @Override
    public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
        Item item = pUseContext.getItemInHand().getItem();
        return item == ModItems.GRAPE_SEEDS_RED.get() || item == ModItems.GRAPE_SEEDS_WHITE.get() ||
                item == ModBlocks.GRAPE_CROP_RED.get().asItem() || item == ModBlocks.GRAPE_CROP_WHITE.get().asItem();
    }

    private static BooleanProperty getProp(Direction dir) {
        return switch(dir) {
            case UP    -> UP;
            case DOWN  -> DOWN;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST  -> EAST;
            case WEST  -> WEST;
        };
    }

    // new helper: map horizontal directions to the WIRE_* boolean properties
    public static BooleanProperty getWireProp(Direction dir) {
        return switch(dir) {
            case NORTH -> WIRE_NORTH;
            case SOUTH -> WIRE_SOUTH;
            case EAST  -> WIRE_EAST;
            case WEST  -> WIRE_WEST;
            default -> throw new IllegalArgumentException("getWireProp only supports horizontal directions");
        };
    }

    private boolean connectsTo(BlockGetter world, BlockPos pos) {
        return world.getBlockState(pos).isSolid() || world.getBlockState(pos).getBlock() instanceof TrellisBlock ||
                world.getBlockState(pos).getBlock() instanceof GrapeCropBlock;
    }
}
