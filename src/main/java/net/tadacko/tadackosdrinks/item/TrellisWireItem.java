package net.tadacko.tadackosdrinks.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.block.ModBlocks;
import net.tadacko.tadackosdrinks.block.TrellisBlock;
import net.tadacko.tadackosdrinks.block.TrellisWireBlock;
import net.tadacko.tadackosdrinks.network.ModNetwork;
import net.tadacko.tadackosdrinks.network.SetTrellisFirstPacket;

import java.util.ArrayList;
import java.util.List;

public class TrellisWireItem extends Item {

    public TrellisWireItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        if (player == null) return InteractionResult.FAIL;

        BlockState clickedState = level.getBlockState(pos);
        if (!(clickedState.getBlock() instanceof TrellisBlock) && !clickedState.is(ModBlocks.GRAPE_CROP_RED.get()) &&
                !clickedState.is(ModBlocks.GRAPE_CROP_WHITE.get())) {
            player.displayClientMessage(Component.translatable("message.tadackosdrinks.rope_fail_target"), true);
            return InteractionResult.FAIL;
        }

        if (level.isClientSide) return InteractionResult.SUCCESS; // Return SUCCESS so client shows correct hand animation

        CompoundTag root = player.getPersistentData();
        CompoundTag persistent = root.getCompound(TadackosDrinks.MOD_ID);
        String KEY_FIRST = "trellis_wire_fist_pos";
        final Runnable clearFirst = () -> {
            if (persistent.contains(KEY_FIRST)) {
                persistent.remove(KEY_FIRST);
                if (player instanceof ServerPlayer sp) {
                    ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp),
                            new SetTrellisFirstPacket(0L, false)); // pos value ignored when has==false
                }
            }
        };

        if (!persistent.contains(KEY_FIRST)) {
            persistent.putLong(KEY_FIRST, pos.asLong());
            root.put(TadackosDrinks.MOD_ID, persistent);
            // send client packet to update overlay cache
            if (player instanceof ServerPlayer sp) {
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp),
                        new SetTrellisFirstPacket(pos.asLong(), true));
            }
            player.displayClientMessage(Component.translatable("message.tadackosdrinks.trellis_wire_success_first_target"), true);
            return InteractionResult.SUCCESS;
        }

        BlockPos firstPos = BlockPos.of(persistent.getLong(KEY_FIRST));

        if (firstPos.getY() != pos.getY()) {
            clearFirst.run();
            player.displayClientMessage(Component.translatable("message.tadackosdrinks.trellis_wire_fail_height"), true);
            return InteractionResult.FAIL;
        }

        boolean sameX = firstPos.getX() == pos.getX();
        boolean sameZ = firstPos.getZ() == pos.getZ();
        if (sameX == sameZ) {
            clearFirst.run();
            player.displayClientMessage(Component.translatable("message.tadackosdrinks.trellis_wire_fail_aligned"), true);
            return InteractionResult.FAIL;
        }

        int distance = sameX ? Math.abs(firstPos.getZ() - pos.getZ()) : Math.abs(firstPos.getX() - pos.getX());
        int countNeeded = distance - 1; // exclude the trellis blocks themselves

        if (countNeeded <= 0) {
            clearFirst.run();
            player.displayClientMessage(Component.translatable("message.tadackosdrinks.trellis_wire_fail_adjacent"), true);
            return InteractionResult.FAIL;
        }

        Item targetItem = this;
        int totalAvailable = countItemInPlayerInventory(player, targetItem);

        if (!player.isCreative() && totalAvailable < countNeeded) {
            clearFirst.run();
            player.displayClientMessage(Component.translatable("message.tadackosdrinks.trellis_wire_fail_inventory"), true);
            return InteractionResult.FAIL;
        }

        List<BlockPos> positions = new ArrayList<>();
        if (sameX) {
            int minZ = Math.min(firstPos.getZ(), pos.getZ());
            for (int i = 1; i <= countNeeded; i++) {
                positions.add(new BlockPos(pos.getX(), pos.getY(), minZ + i));
            }
        } else {
            int minX = Math.min(firstPos.getX(), pos.getX());
            for (int i = 1; i <= countNeeded; i++) {
                positions.add(new BlockPos(minX + i, pos.getY(), pos.getZ()));
            }
        }

        for (BlockPos wirePos : positions) {
            BlockState stateAt = level.getBlockState(wirePos);
            if (!stateAt.isAir() && !stateAt.canBeReplaced()) {
                clearFirst.run();
                player.displayClientMessage(Component.translatable("message.tadackosdrinks.trellis_wire_fail_obstructed"), true);
                return InteractionResult.FAIL;
            }
        }

        Direction facingForPlaced = sameX ? Direction.NORTH : Direction.EAST;
        for (BlockPos wirePos : positions) {
            BlockState wireState = ModBlocks.TRELLIS_WIRE.get().defaultBlockState()
                    .setValue(TrellisWireBlock.FACING, facingForPlaced);
            level.setBlock(wirePos, wireState, Block.UPDATE_ALL);
            // notify neighbors so trellises update immediately
            level.updateNeighborsAt(wirePos, ModBlocks.TRELLIS_WIRE.get());
        }

        if (!player.isCreative()) consumeItemFromPlayerInventory(player, targetItem, positions.size());

        clearFirst.run();
        player.displayClientMessage(Component.translatable("message.tadackosdrinks.trellis_wire_success_placed", positions.size()), true);
        return InteractionResult.SUCCESS;
    }

    private int countItemInPlayerInventory(Player player, Item target) {
        int count = 0;
        for (ItemStack s : player.getInventory().items) {
            if (!s.isEmpty() && s.getItem() == target) count += s.getCount();
        }
        for (ItemStack s : player.getInventory().offhand) {
            if (!s.isEmpty() && s.getItem() == target) count += s.getCount();
        }
        return count;
    }

    private void consumeItemFromPlayerInventory(Player player, Item target, int amount) {
        int remaining = amount;

        NonNullList<ItemStack> items = player.getInventory().items;
        for (int i = 0; i < items.size() && remaining > 0; i++) {
            ItemStack s = items.get(i);
            if (!s.isEmpty() && s.getItem() == target) {
                int take = Math.min(s.getCount(), remaining);
                s.shrink(take);
                remaining -= take;
                // if stack becomes empty, set to ItemStack.EMPTY to keep inventory clean
                if (s.isEmpty()) items.set(i, ItemStack.EMPTY);
            }
        }

        NonNullList<ItemStack> offhand = player.getInventory().offhand;
        for (int i = 0; i < offhand.size() && remaining > 0; i++) {
            ItemStack s = offhand.get(i);
            if (!s.isEmpty() && s.getItem() == target) {
                int take = Math.min(s.getCount(), remaining);
                s.shrink(take);
                remaining -= take;
                if (s.isEmpty()) offhand.set(i, ItemStack.EMPTY);
            }
        }

        // As a safeguard, if something went wrong and remaining > 0, we don't try to refund.
        // But this shouldn't happen because we checked availability before.
    }
}
