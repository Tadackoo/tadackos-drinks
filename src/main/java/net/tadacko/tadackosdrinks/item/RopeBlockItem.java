package net.tadacko.tadackosdrinks.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.tadacko.tadackosdrinks.block.ModBlocks;
import net.tadacko.tadackosdrinks.block.TrellisBlock;

public class RopeBlockItem extends BlockItem {

    public RopeBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        Player player = context.getPlayer();

        if (clickedState.getBlock() instanceof TrellisBlock || clickedState.is(ModBlocks.ROPE.get())) {
            BlockPos belowPos = clickedPos.below();
            BlockState belowState = level.getBlockState(belowPos);

            if (belowState.canBeReplaced(new BlockPlaceContext(context)) || belowState.isAir()) {
                // Create a new hit result for the position below
                Vec3 hitVec = Vec3.atCenterOf(belowPos);
                BlockHitResult newHit = new BlockHitResult(
                        hitVec,
                        Direction.UP, // Place from above
                        belowPos,
                        false
                );

                // Create a new context for the position below
                UseOnContext modifiedContext = new UseOnContext(context.getPlayer(), context.getHand(), newHit);

                // Place the block below
                return super.useOn(modifiedContext);
            } else {
                // Space below is blocked - check if normal placement would work
                BlockPos targetPos = clickedPos.relative(context.getClickedFace());
                BlockPos aboveTarget = targetPos.above();
                BlockState aboveState = level.getBlockState(aboveTarget);
                BlockState targetState = level.getBlockState(targetPos);

                boolean hasSupport = aboveState.getBlock() instanceof TrellisBlock || aboveState.is(ModBlocks.ROPE.get());
                boolean canPlaceAtTarget = targetState.canBeReplaced(new BlockPlaceContext(context)) || targetState.isAir();

                if (!hasSupport || !canPlaceAtTarget) {
                    if (player != null && level.isClientSide) player.displayClientMessage(Component.translatable("message.tadackosdrinks.rope_fail_obstructed"), true);
                    return InteractionResult.FAIL;
                }
                // Otherwise fall through to normal placement
            }
        }

        // For normal placement, check if rope can survive at target position
        BlockPos targetPos = clickedPos.relative(context.getClickedFace());
        BlockPos aboveTarget = targetPos.above();
        BlockState aboveState = level.getBlockState(aboveTarget);
        BlockState targetState = level.getBlockState(targetPos);

        boolean hasSupport = aboveState.getBlock() instanceof TrellisBlock || aboveState.is(ModBlocks.ROPE.get());
        boolean canPlaceAtTarget = targetState.canBeReplaced(new BlockPlaceContext(context)) || targetState.isAir();

        if (!hasSupport || !canPlaceAtTarget) {
            if (player != null && level.isClientSide) player.displayClientMessage(Component.translatable("message.tadackosdrinks.rope_fail_target"), true);
            return InteractionResult.FAIL;
        }

        // Otherwise, use normal placement logic
        return super.useOn(context);
    }
}