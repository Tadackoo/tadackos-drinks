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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
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

        // must click a trellis as the first/second block
        BlockState clickedState = level.getBlockState(pos);
        if (!(clickedState.getBlock() instanceof TrellisBlock) && !clickedState.is(ModBlocks.GRAPE_CROP_RED.get()) &&
                !clickedState.is(ModBlocks.GRAPE_CROP_WHITE.get())) {
            player.displayClientMessage(Component.translatable("message.tadackosdrinks.rope_fail_target"), true);
            return InteractionResult.FAIL;
        }

        // If we're on the client, don't modify persistent data or place blocks — server is authoritative.
        // Return SUCCESS so client shows correct hand animation but the actual logic runs server-side only.
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // From here on, we are guaranteed to be on the server side.
        CompoundTag pdata = player.getPersistentData();
        String TAG_FIRST = "TrellisWireFirstPos";
        final Runnable clearFirst = () -> {
            if (pdata.contains(TAG_FIRST)) {
                pdata.remove(TAG_FIRST);
                if (player instanceof ServerPlayer sp) {
                    ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp),
                            new SetTrellisFirstPacket(0L, false)); // pos value ignored when has==false
                }
            }
        };

        // If no first pos stored -> store this as first selection on the player (server-side only)
        if (!pdata.contains(TAG_FIRST)) {
            pdata.putLong(TAG_FIRST, pos.asLong());
            // send client packet to update overlay cache
            if (player instanceof ServerPlayer sp) {
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp),
                        new SetTrellisFirstPacket(pos.asLong(), true));
            }
            player.displayClientMessage(Component.translatable("message.tadackosdrinks.trellis_wire_success_first_target"), true);
            return InteractionResult.SUCCESS;
        }

        // We have a first pos -> attempt to place between first and this pos
        BlockPos firstPos = BlockPos.of(pdata.getLong(TAG_FIRST));

        // 1) Must be same Y
        if (firstPos.getY() != pos.getY()) {
            clearFirst.run();
            player.displayClientMessage(Component.translatable("message.tadackosdrinks.trellis_wire_fail_height"), true);
            return InteractionResult.FAIL;
        }

        // 2) Must be aligned on X or Z (not diagonal) — exactly one axis must match
        boolean sameX = firstPos.getX() == pos.getX();
        boolean sameZ = firstPos.getZ() == pos.getZ();
        if (!(sameX ^ sameZ)) {
            clearFirst.run();
            player.displayClientMessage(Component.translatable("message.tadackosdrinks.trellis_wire_fail_aligned"), true);
            return InteractionResult.FAIL;
        }

        // 3) Compute number of wire blocks needed (spaces between)
        int distance = sameX ? Math.abs(firstPos.getZ() - pos.getZ()) : Math.abs(firstPos.getX() - pos.getX());
        int countNeeded = distance - 1; // exclude the trellis blocks themselves

        if (countNeeded <= 0) {
            clearFirst.run();
            player.displayClientMessage(Component.translatable("message.tadackosdrinks.trellis_wire_fail_adjacent"), true);
            return InteractionResult.FAIL;
        }

        // NEW: Count total available of this wire item across the player's inventory and offhand
        Item targetItem = this; // this TrellisWireItem instance
        int totalAvailable = countItemInPlayerInventory(player, targetItem);

        if (!player.isCreative() && totalAvailable < countNeeded) {
            clearFirst.run();
            player.displayClientMessage(Component.translatable("message.tadackosdrinks.trellis_wire_fail_inventory"), true);
            return InteractionResult.FAIL;
        }

        // 5) Build the list of positions between them
        List<BlockPos> positions = new ArrayList<>();
        if (sameX) { // Z changes
            int minZ = Math.min(firstPos.getZ(), pos.getZ());
            for (int i = 1; i <= countNeeded; i++) {
                positions.add(new BlockPos(pos.getX(), pos.getY(), minZ + i));
            }
        } else { // sameZ -> X changes
            int minX = Math.min(firstPos.getX(), pos.getX());
            for (int i = 1; i <= countNeeded; i++) {
                positions.add(new BlockPos(minX + i, pos.getY(), pos.getZ()));
            }
        }

        // 6) Check for obstacles (replaceable or air)
        for (BlockPos wirePos : positions) {
            BlockState stateAt = level.getBlockState(wirePos);
            if (!stateAt.isAir() && !stateAt.canBeReplaced()) {
                clearFirst.run();
                player.displayClientMessage(Component.translatable("message.tadackosdrinks.trellis_wire_fail_obstructed"), true);
                return InteractionResult.FAIL;
            }
        }

        // 7) Place wires (set facing depending on line direction)
        Direction facingForPlaced = sameX ? Direction.NORTH : Direction.EAST; // wires face north for Z-lines, east for X-lines
        for (BlockPos wirePos : positions) {
            BlockState wireState = ModBlocks.TRELLIS_WIRE.get().defaultBlockState()
                    .setValue(TrellisWireBlock.FACING, facingForPlaced);
            level.setBlock(wirePos, wireState, 3);
            // notify neighbors so trellises update immediately
            level.updateNeighborsAt(wirePos, ModBlocks.TRELLIS_WIRE.get());
        }

        // NEW: Consume items from player's inventory (unless creative)
        if (!player.isCreative()) {
            consumeItemFromPlayerInventory(player, targetItem, positions.size());
        }

        // 9) clear stored first pos and success message
        clearFirst.run();
        player.displayClientMessage(Component.translatable("message.tadackosdrinks.trellis_wire_success_placed", positions.size()), true);
        return InteractionResult.SUCCESS;
    }

    /* --------------------
    Helper methods below
    -------------------- */

    private int countItemInPlayerInventory(Player player, Item target) {
        int count = 0;
        // main inventory
        for (ItemStack s : player.getInventory().items) {
            if (!s.isEmpty() && s.getItem() == target) count += s.getCount();
        }
        // offhand
        for (ItemStack s : player.getInventory().offhand) {
            if (!s.isEmpty() && s.getItem() == target) count += s.getCount();
        }
        return count;
    }

    private void consumeItemFromPlayerInventory(Player player, Item target, int amount) {
        int remaining = amount;

        // Consume from main inventory first (slots 0..)
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

        // Then consume from offhand if still needed
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
