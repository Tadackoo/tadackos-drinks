package net.tadacko.tadackosdrinks.util;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.block.RopeBlock;
import net.tadacko.tadackosdrinks.block.TrellisBlock;

@Mod.EventBusSubscriber(modid = TadackosDrinks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HoeTillHandler {
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack itemInHand = player.getItemInHand(hand);
        if (!itemInHand.canPerformAction(ToolActions.HOE_TILL)) return;
        Level level = event.getLevel();
        BlockPos clickedPos = event.getPos();
        Block block = level.getBlockState(clickedPos).getBlock();
        Block aboveBlock = level.getBlockState(clickedPos.above()).getBlock();
        if ((block == Blocks.GRASS_BLOCK || block == Blocks.DIRT_PATH || block == Blocks.DIRT) &&
                (aboveBlock instanceof TrellisBlock || aboveBlock instanceof RopeBlock)) {
            level.playSound(player, clickedPos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!level.isClientSide) {
                level.setBlock(clickedPos, Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
                level.gameEvent(GameEvent.BLOCK_CHANGE, clickedPos, GameEvent.Context.of(player, Blocks.FARMLAND.defaultBlockState()));
                itemInHand.hurtAndBreak(1, player, (p_150845_) -> { p_150845_.broadcastBreakEvent(hand); });
                player.awardStat(Stats.ITEM_USED.get(itemInHand.getItem()));
            }
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
            event.setCanceled(true);
        }
    }
}