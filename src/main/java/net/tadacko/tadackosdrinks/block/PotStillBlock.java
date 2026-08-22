package net.tadacko.tadackosdrinks.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.tadacko.tadackosdrinks.block.entity.ModBlockEntities;
import net.tadacko.tadackosdrinks.block.entity.PotStillBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class PotStillBlock extends BaseEntityBlock {
    public static final EnumProperty<CondenserPos> CONDENSER = EnumProperty.create("condenser", CondenserPos.class);
    public static final BooleanProperty CLOCK = BooleanProperty.create("clock");
    private static final VoxelShape SHAPE = Block.box(
            2.0D, 0.0D, 2.0D,
            14.0D, 13.0D, 14.0D
    );
    
    public PotStillBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(CONDENSER, CondenserPos.NONE)
                .setValue(CLOCK, false)
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, pContext.getHorizontalDirection().getOpposite())
                .setValue(CONDENSER, CondenserPos.NONE)
                .setValue(CLOCK, false);
    }

    // Condenser tracking
    /** Scan the four horizontal neighbours and return the first CondenserPos found. */
    private static CondenserPos findCondenserDir(Level level, BlockPos pos) {
        for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            if (level.getBlockState(pos.relative(dir)).is(ModBlocks.CONDENSER.get())) {
                return CondenserPos.fromDirection(dir);
            }
        }
        return CondenserPos.NONE;
    }

    /** Re-evaluate which neighbour is a condenser and push the new state. */
    private static void refreshCondenserState(Level level, BlockPos pos, BlockState state) {
        CondenserPos found = findCondenserDir(level, pos);
        if (state.getValue(CONDENSER) != found) {
            level.setBlock(pos, state.setValue(CONDENSER, found), Block.UPDATE_ALL);
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            refreshCondenserState(level, pos, state);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide) {
            refreshCondenserState(level, pos, state);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos blockPos,
                                 Player player, InteractionHand hand, BlockHitResult result) {
        ItemStack held = player.getItemInHand(hand);

        // SERVER ONLY
        if (!level.isClientSide) {
            // 1) Sneak + right-click: remove the clock if present and give it back to the player
            if (player.isCrouching()) {
                if (state.getValue(CLOCK)) {
                    // set CLOCK = false
                    level.setBlock(blockPos, state.setValue(CLOCK, false), 3);

                    // give clock back in survival
                    if (!player.isCreative()) {
                        ItemStack clockStack = new ItemStack(Items.CLOCK);
                        boolean added = player.getInventory().add(clockStack);
                        if (!added) {
                            // drop into the world if player's inventory is full
                            ItemEntity drop = new ItemEntity(level,
                                    blockPos.getX() + 0.5, blockPos.getY() + 1.0, blockPos.getZ() + 0.5,
                                    clockStack);
                            level.addFreshEntity(drop);
                        }
                    }

                    return InteractionResult.SUCCESS;
                }
            }

            // 2) Right-click with a clock: insert it (if not already present)
            if (held.is(Items.CLOCK)) {
                if (!state.getValue(CLOCK)) {
                    level.setBlock(blockPos, state.setValue(CLOCK, true), 3);

                    // consume 1 clock in survival
                    if (!player.isCreative()) {
                        held.shrink(1);
                    }

                    return InteractionResult.SUCCESS;
                }
            }

            // 3) Otherwise fallback to existing block-entity interaction
            BlockEntity be = level.getBlockEntity(blockPos);
            if (be instanceof PotStillBlockEntity still) {
                if (still.handleRightClick(player, hand)) {
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.CONSUME;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING, CONDENSER, CLOCK);
    }

    /* BLOCK ENTITY */

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PotStillBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.POT_STILL.get(),
                PotStillBlockEntity::tick);
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
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        // Try to fetch the BlockEntity from the loot context
        Object beObj = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (beObj instanceof PotStillBlockEntity still) {
            // if it's default/empty, return a plain block item so it stacks with freshly crafted ones
            if (still.isDefaultState()) {
                return Collections.singletonList(new ItemStack(this.asItem()));
            }

            // otherwise save the BE NBT and attach it
            // create single ItemStack for this block (the BlockItem registered for this block)
            ItemStack stack = new ItemStack(this.asItem());

            // use your public helper that returns the BE NBT (add saveToItemTag() to your BE if not present)
            CompoundTag tag = still.saveToItemTag();

            // remove position fields
            tag.remove("x");
            tag.remove("y");
            tag.remove("z");

            // attach under standard key so vanilla will restore it on place
            stack.getOrCreateTag().put("BlockEntityTag", tag);

            return Collections.singletonList(stack);
        }

        // fallback to default behavior (loot table)
        return super.getDrops(state, builder);
    }
}
