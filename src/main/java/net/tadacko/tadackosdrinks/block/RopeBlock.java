package net.tadacko.tadackosdrinks.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.tadacko.tadackosdrinks.item.ModItems;

public class RopeBlock extends Block {
    public static final IntegerProperty UNSUPPORTED = IntegerProperty.create("unsupported", 0, 2);
    // how many scheduled ticks without support before breaking
    private static final int UNSUPPORTED_THRESHOLD = 2;
    // schedule delay between checks (in ticks)
    private static final int CHECK_DELAY = 1; // check every tick while unsupported

    public RopeBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(UNSUPPORTED, 0));
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        return pLevel.getBlockState(pPos.above()).getBlock() instanceof TrellisBlock ||
               pLevel.getBlockState(pPos.above()).is(ModBlocks.ROPE.get());
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Block.box(6.0D, 0.0D, 6.0D, 10.0D, 16.0D, 10.0D);
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
        if (pDirection == Direction.UP) {
            // schedule a survival check shortly after neighbor changed
            pLevel.scheduleTick(pPos, this, CHECK_DELAY);
        }
        return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
    }

    @Override
    public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
        Item item = pUseContext.getItemInHand().getItem();
        return item == ModItems.HOP_SEEDS.get() || item == ModBlocks.HOP_CROP.get().asItem();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(UNSUPPORTED);
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
}
