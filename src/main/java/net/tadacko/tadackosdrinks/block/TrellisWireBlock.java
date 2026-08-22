package net.tadacko.tadackosdrinks.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.tadacko.tadackosdrinks.item.ModItems;

public class TrellisWireBlock extends Block {
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
    public static final IntegerProperty UNSUPPORTED = IntegerProperty.create("unsupported", 0, 2);
    // how many scheduled ticks without support before breaking
    private static final int UNSUPPORTED_THRESHOLD = 2;
    // schedule delay between checks (in ticks)
    private static final int CHECK_DELAY = 1; // check every tick while unsupported

    public TrellisWireBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(UNSUPPORTED, 0));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // Check East & West (east must face west, west must face east)
        boolean east = isConnectorFacing(level, pos.east(), Direction.WEST);
        boolean west = isConnectorFacing(level, pos.west(), Direction.EAST);

        // Check North & South (north must face south, south must face north)
        boolean north = isConnectorFacing(level, pos.north(), Direction.SOUTH);
        boolean south = isConnectorFacing(level, pos.south(), Direction.NORTH);

        // survive if connected on both sides along X (east+west) OR along Z (north+south)
        return (east && west) || (north && south);
    }

    // Destroy when supports removed
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block changedBlock, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, changedBlock, fromPos, isMoving);
        if (level.isClientSide) return;

        // schedule a survival check shortly after neighbor changed
        level.scheduleTick(pos, this, CHECK_DELAY);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        switch(state.getValue(FACING)) {
            case NORTH:
            case SOUTH:
                return Block.box(6.0D, 6.0D, 0.0D, 10.0D, 10.0D, 16.0D);
            case EAST:
            case WEST:
                return Block.box(0.0D, 6.0D, 6.0D, 16.0D, 10.0D, 10.0D);
            default:
                return Block.box(6.0D, 6.0D, 6.0D, 10.0D, 10.0D, 10.0D);
        }
    }

    /*@Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        // Only care about horizontal neighbor changes (wire connectivity is horizontal)
        if (direction.getAxis().isHorizontal()) {
            if (!canSurvive(state, level, pos)) {
                return Blocks.AIR.defaultBlockState(); // break if sides invalid
            }
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }*/

    @Override
    public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
        Item item = pUseContext.getItemInHand().getItem();
        return item == ModItems.HOP_SEEDS.get() || item == ModBlocks.HOP_CROP.get().asItem();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, UNSUPPORTED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        LevelReader level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        // Only allow horizontal directions
        if (clickedFace.getAxis().isHorizontal()) {
            return this.defaultBlockState().setValue(FACING, clickedFace.getOpposite());
        }

        // If vertical face clicked, try horizontal neighbors
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (neighborState.getBlock() instanceof TrellisBlock || neighborState.is(ModBlocks.GRAPE_CROP_RED.get()) ||
                    neighborState.is(ModBlocks.GRAPE_CROP_WHITE.get()) || neighborState.is(ModBlocks.TRELLIS_WIRE.get()) ||
                    neighborState.is(ModBlocks.GRAPE_WIRE_CROP_RED.get()) || neighborState.is(ModBlocks.GRAPE_WIRE_CROP_WHITE.get())) {
                return this.defaultBlockState().setValue(FACING, dir.getOpposite());
            }
        }

        return this.defaultBlockState(); // fallback
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        // check survival (use your existing canSurvive logic)
        boolean survives = this.canSurvive(state, level, pos);

        int unsupported = state.getValue(UNSUPPORTED);

        if (survives) {
            // reset counter if it was non-zero
            if (unsupported != 0) {
                BlockState reset = state.setValue(UNSUPPORTED, 0);
                level.setBlock(pos, reset, 3);
            }
            return;
        }

        // still unsupported: increment counter
        int next = Math.min(UNSUPPORTED_THRESHOLD, unsupported + CHECK_DELAY);
        BlockState updated = state.setValue(UNSUPPORTED, next);
        level.setBlock(pos, updated, 3);

        if (next >= UNSUPPORTED_THRESHOLD) {
            // destroy and drop items
            level.destroyBlock(pos, true);
        } else {
            // schedule another check
            level.scheduleTick(pos, this, CHECK_DELAY);
        }
    }

    private boolean isConnectorFacing(BlockGetter world, BlockPos checkPos, Direction requiredFacing) {
        BlockState s = world.getBlockState(checkPos);

        // Always accept ground grape and plain trellis (they don't have FACING)
        if (s.is(ModBlocks.GRAPE_CROP_RED.get()) || s.is(ModBlocks.GRAPE_CROP_WHITE.get()) || s.getBlock() instanceof TrellisBlock) {
            return true;
        }

        // For wire-like blocks, require a FACING that points toward this block
        if (s.is(ModBlocks.GRAPE_WIRE_CROP_RED.get()) || s.is(ModBlocks.GRAPE_WIRE_CROP_WHITE.get()) || s.is(ModBlocks.TRELLIS_WIRE.get())) {
            if (s.hasProperty(GrapeWireCropBlock.FACING)) {
                Direction neighborFacing = s.getValue(GrapeWireCropBlock.FACING);
                return neighborFacing == requiredFacing || neighborFacing == requiredFacing.getOpposite();
            }
            // If the wire block doesn't have a FACING property, treat it as NOT a valid facing connector.
            // (Change to `return true;` here if you want non-directional trellis_wire to count.)
        }

        return false;
    }
}
