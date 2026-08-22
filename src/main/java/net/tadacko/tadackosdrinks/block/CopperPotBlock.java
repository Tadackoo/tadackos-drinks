package net.tadacko.tadackosdrinks.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.tadacko.tadackosdrinks.block.entity.CopperPotBlockEntity;
import net.tadacko.tadackosdrinks.block.entity.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CopperPotBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0.0D, 1.0D, 0.0D, 16.0D, 2.0D, 16.0D),  // Bottom
            Block.box(0.0D, 2.0D, 0.0D, 16.0D, 16.0D, 2.0D),  // North wall
            Block.box(0.0D, 2.0D, 14.0D, 16.0D, 16.0D, 16.0D), // South wall
            Block.box(0.0D, 2.0D, 1.0D, 2.0D, 16.0D, 15.0D),   // West wall
            Block.box(14.0D, 2.0D, 1.0D, 16.0D, 16.0D, 15.0D)  // East wall
    );

    public CopperPotBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos blockPos,
                                 Player player, InteractionHand hand, BlockHitResult result) {
        // Check if it's on the server side
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof CopperPotBlockEntity copperPotBlockEntity) {
                // Call a method in the block entity to handle the interaction
                if (copperPotBlockEntity.handleRightClick(player, hand)) {
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.CONSUME;
    }

    /* BLOCK ENTITY */

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CopperPotBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.COPPER_POT.get(),
                CopperPotBlockEntity::tick);
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
        if (beObj instanceof CopperPotBlockEntity pot) {
            // if it's default/empty, return a plain block item so it stacks with freshly crafted ones
            if (pot.isDefaultState()) {
                return Collections.singletonList(new ItemStack(this.asItem()));
            }

            // otherwise save the BE NBT and attach it
            // create single ItemStack for this block (the BlockItem registered for this block)
            ItemStack stack = new ItemStack(this.asItem());

            // use your public helper that returns the BE NBT (add saveToItemTag() to your BE if not present)
            CompoundTag tag = pot.saveToItemTag();

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
