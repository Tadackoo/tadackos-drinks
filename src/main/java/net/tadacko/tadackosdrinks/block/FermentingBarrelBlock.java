package net.tadacko.tadackosdrinks.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.tadacko.tadackosdrinks.block.entity.FermentingBarrelBlockEntity;
import net.tadacko.tadackosdrinks.block.entity.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class FermentingBarrelBlock extends BaseEntityBlock {
    public static final EnumProperty<BarrelState> STATE = EnumProperty.create("state", BarrelState.class);
    public static final BooleanProperty CLOCK = BooleanProperty.create("clock");
    private static final VoxelShape SHAPE = Block.box(
            1.0D, 0.0D, 1.0D,
            15.0D, 16.0D, 15.0D
    );
    private static final VoxelShape SHAPE_OPEN = Shapes.or(
            Block.box(1.0D, 0.0D, 1.0D, 15.0D, 1.0D, 15.0D),  // Bottom
            Block.box(1.0D, 1.0D, 1.0D, 15.0D, 16.0D, 2.0D),  // North wall
            Block.box(1.0D, 1.0D, 14.0D, 15.0D, 16.0D, 15.0D), // South wall
            Block.box(1.0D, 1.0D, 2.0D, 2.0D, 16.0D, 14.0D),   // West wall
            Block.box(14.0D, 1.0D, 2.0D, 15.0D, 16.0D, 14.0D)  // East wall
    );

    public static boolean throwIngredientBarrel = false; // fallback default, overridden by config value

    public FermentingBarrelBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(STATE, BarrelState.OPEN)
                .setValue(CLOCK, false)
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, pContext.getHorizontalDirection().getOpposite())
                .setValue(STATE, BarrelState.OPEN)
                .setValue(CLOCK, false);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos blockPos,
                                 Player player, InteractionHand hand, BlockHitResult result) {
        ItemStack held = player.getItemInHand(hand);

        if (!level.isClientSide) {
            if (player.isCrouching()) {
                if (state.getValue(CLOCK)) {
                    level.setBlock(blockPos, state.setValue(CLOCK, false), Block.UPDATE_ALL);

                    if (!player.isCreative()) {
                        ItemStack clockStack = new ItemStack(Items.CLOCK);
                        boolean added = player.getInventory().add(clockStack);
                        if (!added) {
                            ItemEntity drop = new ItemEntity(level, blockPos.getX() + 0.5, blockPos.getY() + 1.0, blockPos.getZ() + 0.5,
                                    clockStack);
                            level.addFreshEntity(drop);
                        }
                    }

                    return InteractionResult.SUCCESS;
                }
            }

            if (held.is(Items.CLOCK)) {
                if (!state.getValue(CLOCK)) {
                    level.setBlock(blockPos, state.setValue(CLOCK, true), Block.UPDATE_ALL);
                    if (!player.isCreative()) held.shrink(1);
                    return InteractionResult.SUCCESS;
                }
            }

            BlockEntity be = level.getBlockEntity(blockPos);
            if (be instanceof FermentingBarrelBlockEntity barrel) {
                if (barrel.handleRightClick(player, hand)) {
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.CONSUME;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING, STATE, CLOCK);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FermentingBarrelBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.FERMENTING_BARREL.get(), FermentingBarrelBlockEntity::tick);
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
        BarrelState barrelState = state.getValue(STATE);
        return switch (barrelState) {
            case CLOSED, YEAST -> SHAPE;
            default -> SHAPE_OPEN;
        };
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        Object beObj = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (beObj instanceof FermentingBarrelBlockEntity barrel && !barrel.isDefaultState()) {
            ItemStack stack = new ItemStack(this.asItem());
            CompoundTag tag = barrel.saveToItemTag();
            tag.remove("x");
            tag.remove("y");
            tag.remove("z");
            stack.getOrCreateTag().put("BlockEntityTag", tag);
            return Collections.singletonList(stack);
        }

        // fallback to loot table
        return super.getDrops(state, builder);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide) return;

        if (throwIngredientBarrel) {
            if (entity instanceof ItemEntity itemEntity) {
                ItemStack stack = itemEntity.getItem();

                if (level.getBlockEntity(pos) instanceof FermentingBarrelBlockEntity blockEntity) {
                    if (blockEntity.handleItemEntityCollision(stack)) {
                        stack.shrink(1);
                        if (stack.isEmpty()) {
                            itemEntity.discard();
                        }
                    }
                }
            }
        }

        super.entityInside(state, level, pos, entity);
    }
}
