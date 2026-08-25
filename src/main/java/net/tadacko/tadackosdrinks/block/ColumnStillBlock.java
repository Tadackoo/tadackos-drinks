package net.tadacko.tadackosdrinks.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.tadacko.tadackosdrinks.block.entity.ColumnStillBlockEntity;
import net.tadacko.tadackosdrinks.block.entity.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class ColumnStillBlock extends BaseEntityBlock {
    public static final int MAX_HEIGHT = 8;

    public static final EnumProperty<net.minecraft.core.Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty SEGMENT = IntegerProperty.create("segment", 0, MAX_HEIGHT - 1);
    // Total height of the column this segment currently belongs to (replicated on every segment)
    public static final IntegerProperty HEIGHT = IntegerProperty.create("height", 1, MAX_HEIGHT);
    public static final EnumProperty<CondenserPos> CONDENSER = EnumProperty.create("condenser", CondenserPos.class);
    public static final BooleanProperty CLOCK = BooleanProperty.create("clock");
    // Drives blockstate model selection
    public static final EnumProperty<ColumnStillPart> PART = EnumProperty.create("part", ColumnStillPart.class);

    private static final VoxelShape SHAPE = Block.box(
            3.0D, 0.0D, 3.0D,
            13.0D, 16.0D, 13.0D
    );

    public ColumnStillBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(SEGMENT, 0)
                .setValue(HEIGHT, 1)
                .setValue(CONDENSER, CondenserPos.NONE)
                .setValue(CLOCK, false)
                .setValue(PART, ColumnStillPart.BOTTOM)
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        BlockState below = level.getBlockState(pos.below());

        if (level.getBlockState(pos.above()).is(this)) {
            Player player = pContext.getPlayer();
            if (!level.isClientSide && player != null) {
                player.displayClientMessage(Component.translatable("message.tadackosdrinks.column_still_place_fail_bottom"), true);
            }
            return null;
        }

        // Stacking on top of an existing column segment: inherit facing, extend height.
        // PART/HEIGHT here are provisional - refreshColumn() corrects the whole column right after placement.
        if (below.is(this)) {
            int belowSegment = below.getValue(SEGMENT);

            if (belowSegment + 1 >= MAX_HEIGHT) {
                Player player = pContext.getPlayer();
                if (!level.isClientSide && player != null) {
                    player.displayClientMessage(Component.translatable("message.tadackosdrinks.column_still_place_fail_height"), true);
                }
                return null;
            }

            return this.defaultBlockState()
                    .setValue(FACING, below.getValue(FACING))
                    .setValue(SEGMENT, Math.min(belowSegment + 1, MAX_HEIGHT - 1))
                    .setValue(HEIGHT, Math.min(belowSegment + 2, MAX_HEIGHT))
                    .setValue(PART, ColumnStillPart.TOP);
        }

        return this.defaultBlockState()
                .setValue(FACING, pContext.getHorizontalDirection().getOpposite())
                .setValue(SEGMENT, 0)
                .setValue(HEIGHT, 1)
                .setValue(PART, ColumnStillPart.BOTTOM);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (level.getBlockState(pos.above()).is(this)) {
            return false;
        }

        BlockState below = level.getBlockState(pos.below());
        if (below.is(this)) {
            return below.getValue(SEGMENT) + 1 < MAX_HEIGHT;
        }
        return true;
    }

    /* COLUMN MAINTENANCE */

    /**
     * Re-scans the whole vertical column containing anyPos: finds the bottom, counts contiguous height,
     * recomputes SEGMENT/HEIGHT/PART/CONDENSER for every segment, and keeps the block entity anchored on the bottom.
     */
    private static void refreshColumn(Level level, BlockPos anyPos) {
        if (!level.getBlockState(anyPos).is(ModBlocks.COLUMN_STILL.get())) return;

        BlockPos bottomPos = anyPos;
        while (level.getBlockState(bottomPos.below()).is(ModBlocks.COLUMN_STILL.get())) {
            bottomPos = bottomPos.below();
        }

        int height = 0;
        while (height < MAX_HEIGHT && level.getBlockState(bottomPos.above(height)).is(ModBlocks.COLUMN_STILL.get())) {
            height++;
        }

        Direction facing = level.getBlockState(bottomPos).getValue(FACING);

        // Direct (own-neighbour) condenser attachment per segment, computed first so we can propagate upward.
        CondenserPos[] direct = new CondenserPos[height];
        for (int i = 0; i < height; i++) {
            direct[i] = PotStillBlock.findCondenserDir(level, bottomPos.above(i));
        }

        for (int i = 0; i < height; i++) {
            BlockPos pos = bottomPos.above(i);
            BlockState current = level.getBlockState(pos);

            CondenserPos resolvedCondenser = direct[i];
            if (resolvedCondenser == CondenserPos.NONE) {
                // Propagate the nearest attachment from below as a pass-through marker
                for (int j = i - 1; j >= 0; j--) {
                    if (direct[j] != CondenserPos.NONE) {
                        resolvedCondenser = direct[j].asBelowVariant();
                        break;
                    }
                }
            }

            ColumnStillPart part = (i == 0)
                    ? ColumnStillPart.BOTTOM
                    : (i == height - 1 ? ColumnStillPart.TOP : ColumnStillPart.MIDDLE);

            BlockState updated = current
                    .setValue(SEGMENT, i)
                    .setValue(HEIGHT, height)
                    .setValue(FACING, facing)
                    .setValue(CONDENSER, resolvedCondenser)
                    .setValue(CLOCK, i == 0 && current.getValue(CLOCK))
                    .setValue(PART, part);

            if (!updated.equals(current)) {
                level.setBlock(pos, updated, Block.UPDATE_ALL);
            }
        }

        syncBlockEntities(level, bottomPos);
    }

    /**
     * Ensures exactly the bottom position of the column has a live ColumnStillBlockEntity.
     * If restructuring left a stray block entity elsewhere in the column (e.g. the old bottom
     * got removed and a segment above it became the new bottom), its data is migrated across.
     */
    private static void syncBlockEntities(Level level, BlockPos bottomPos) {
        if (!(level.getBlockEntity(bottomPos) instanceof ColumnStillBlockEntity)) {
            level.setBlockEntity(new ColumnStillBlockEntity(bottomPos, level.getBlockState(bottomPos)));
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            refreshColumn(level, pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide) {
            refreshColumn(level, pos);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult result) {
        if (!level.isClientSide) {
            ItemStack held = player.getItemInHand(hand);
            int segment = state.getValue(SEGMENT);
            BlockPos bottomPos = pos.below(segment);
            BlockState bottomState = level.getBlockState(bottomPos);

            if (player.isCrouching() && bottomState.getValue(CLOCK)) {
                level.setBlock(bottomPos, bottomState.setValue(CLOCK, false), Block.UPDATE_ALL);

                if (!player.isCreative()) {
                    ItemStack clockStack = new ItemStack(Items.CLOCK);
                    boolean added = player.getInventory().add(clockStack);
                    if (!added) {
                        ItemEntity drop = new ItemEntity(level,
                                pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                                clockStack);
                        level.addFreshEntity(drop);
                    }
                }

                return InteractionResult.SUCCESS;
            }

            if (held.is(Items.CLOCK) && !bottomState.getValue(CLOCK)) {
                level.setBlock(bottomPos, bottomState.setValue(CLOCK, true), Block.UPDATE_ALL);
                if (!player.isCreative()) held.shrink(1);
                return InteractionResult.SUCCESS;
            }

            // Fluid / GUI interaction always routes to the master (bottom) block entity
            BlockEntity be = level.getBlockEntity(bottomPos);
            if (be instanceof ColumnStillBlockEntity master) {
                if (master.handleRightClick(player, hand)) {
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.CONSUME;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SEGMENT, HEIGHT, CONDENSER, CLOCK, PART);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // Only the bottom segment carries a block entity; others are gated out.
        return state.getValue(SEGMENT) == 0 ? new ColumnStillBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.COLUMN_STILL.get(), ColumnStillBlockEntity::tick);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (state.getValue(SEGMENT) == 0) {
            Object beObj = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
            if (beObj instanceof ColumnStillBlockEntity still && !still.isDefaultState()) {
                ItemStack stack = new ItemStack(this.asItem());
                CompoundTag tag = still.saveToItemTag();
                tag.remove("x");
                tag.remove("y");
                tag.remove("z");
                stack.getOrCreateTag().put("BlockEntityTag", tag);
                return Collections.singletonList(stack);
            }
        }

        // Non-bottom segments (or an empty bottom) fallback to loot table
        return super.getDrops(state, builder);
    }
}