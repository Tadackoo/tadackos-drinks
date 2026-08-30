package net.tadacko.tadackosdrinks.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.tadacko.tadackosdrinks.item.ModItems;

public class TrellisWireBlock extends Block {
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
    public static final IntegerProperty UNSUPPORTED = IntegerProperty.create("unsupported", 0, 2);
    // how many scheduled ticks without support before breaking
    private static final int UNSUPPORTED_THRESHOLD = 2;
    private static final int CHECK_DELAY = 1; // check every tick while unsupported

    public TrellisWireBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(UNSUPPORTED, 0));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        boolean east = isConnectorFacing(level, pos.east(), Direction.WEST);
        boolean west = isConnectorFacing(level, pos.west(), Direction.EAST);
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

    @Override
    public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
        Item item = pUseContext.getItemInHand().getItem();
        return item == ModItems.HOP_SEEDS.get() || item == ModBlocks.HOP_CROP.get().asItem();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, UNSUPPORTED); }

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
            if (neighborState.getBlock() instanceof TrellisBlock || neighborState.getBlock() instanceof GrapeCropBlock ||
                    neighborState.is(ModBlocks.TRELLIS_WIRE.get()) || neighborState.getBlock() instanceof GrapeWireCropBlock) {
                return this.defaultBlockState().setValue(FACING, dir.getOpposite());
            }
        }

        return this.defaultBlockState();
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        boolean survives = this.canSurvive(state, level, pos);

        int unsupported = state.getValue(UNSUPPORTED);

        if (survives) {
            if (unsupported != 0) {
                BlockState reset = state.setValue(UNSUPPORTED, 0);
                level.setBlock(pos, reset, Block.UPDATE_ALL);
            }
            return;
        }

        // still unsupported: increment counter
        int next = Math.min(UNSUPPORTED_THRESHOLD, unsupported + CHECK_DELAY);
        BlockState updated = state.setValue(UNSUPPORTED, next);
        level.setBlock(pos, updated, Block.UPDATE_ALL);

        if (next == UNSUPPORTED_THRESHOLD) {
            level.destroyBlock(pos, true);
        } else {
            level.scheduleTick(pos, this, CHECK_DELAY);
        }
    }

    @Override
    public Item asItem() { return ModItems.TRELLIS_WIRE_ITEM.get(); }

    public static boolean isConnectorFacing(BlockGetter world, BlockPos checkPos, Direction requiredFacing) {
        BlockState state = world.getBlockState(checkPos);

        if (state.getBlock() instanceof GrapeCropBlock || state.getBlock() instanceof TrellisBlock) {
            return true;
        }

        if (state.getBlock() instanceof TrellisWireBlock) {
            Direction neighborFacing = state.getValue(TrellisWireBlock.FACING);
            return neighborFacing == requiredFacing || neighborFacing == requiredFacing.getOpposite();
        } else if (state.getBlock() instanceof GrapeWireCropBlock) {
            Direction neighborFacing = state.getValue(GrapeWireCropBlock.FACING);
            return neighborFacing == requiredFacing || neighborFacing == requiredFacing.getOpposite();
        }

        return false;
    }
}
