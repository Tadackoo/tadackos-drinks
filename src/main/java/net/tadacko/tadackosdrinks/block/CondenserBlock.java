package net.tadacko.tadackosdrinks.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CondenserBlock extends Block {
    public static final BooleanProperty CONNECT_NORTH = BooleanProperty.create("connect_north");
    public static final BooleanProperty CONNECT_SOUTH = BooleanProperty.create("connect_south");
    public static final BooleanProperty CONNECT_EAST = BooleanProperty.create("connect_east");
    public static final BooleanProperty CONNECT_WEST = BooleanProperty.create("connect_west");
    private static final VoxelShape SHAPE = Block.box(
            3.0D, 3.0D, 3.0D,
            13.0D, 13.0D, 13.0D
    );

    public CondenserBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(CONNECT_NORTH,  false)
                .setValue(CONNECT_SOUTH,  false)
                .setValue(CONNECT_EAST,  false)
                .setValue(CONNECT_WEST,  false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        BlockState state = this.defaultBlockState();

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BooleanProperty connectProp = getConnectProp(dir);
            BlockPos checkPos = pos.relative(dir);
            BlockState neighborState = level.getBlockState(checkPos);

            boolean connected = neighborState.is(ModBlocks.POT_STILL.get()) || neighborState.is(ModBlocks.COLUMN_STILL.get());

            state = state.setValue(connectProp, connected);
        }

        return state;
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
        BlockState state = pState;

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BooleanProperty connectProp = getConnectProp(dir);
            BlockPos checkPos = pPos.relative(dir);
            BlockState neighborState = pLevel.getBlockState(checkPos);

            boolean connected = neighborState.is(ModBlocks.POT_STILL.get()) || neighborState.is(ModBlocks.COLUMN_STILL.get());

            state = state.setValue(connectProp, connected);
        }

        return state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CONNECT_NORTH, CONNECT_SOUTH, CONNECT_EAST, CONNECT_WEST);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    public static BooleanProperty getConnectProp(Direction dir) {
        return switch(dir) {
            case NORTH -> CONNECT_NORTH;
            case SOUTH -> CONNECT_SOUTH;
            case EAST  -> CONNECT_EAST;
            case WEST  -> CONNECT_WEST;
            default -> throw new IllegalArgumentException("getConnectProp only supports horizontal directions");
        };
    }
}
