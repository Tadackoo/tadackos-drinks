package net.tadacko.tadackosdrinks.util;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tadacko.tadackosdrinks.TadackosDrinks; // replace with your main mod class
import net.tadacko.tadackosdrinks.block.KegBlock;
import net.tadacko.tadackosdrinks.block.PlaceableDrinkwareBlock;
import net.tadacko.tadackosdrinks.block.entity.KegBlockEntity;

@Mod.EventBusSubscriber(modid = TadackosDrinks.MOD_ID)
public class DrinkwareInteractionHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getEntity().isCrouching()) return;

        Level level = event.getLevel();
        BlockState state = level.getBlockState(event.getPos());
        if (!(state.getBlock() instanceof PlaceableDrinkwareBlock) && !(state.getBlock() instanceof KegBlock)) return;

        if (level.isClientSide) {
            // client just needs to swing/consume; server does the actual work
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        if (state.getBlock() instanceof PlaceableDrinkwareBlock jarBlock) {
            boolean picked = jarBlock.tryPickup(level, event.getPos(), event.getEntity());
            if (picked) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        } else if (level.getBlockEntity(event.getPos()) instanceof KegBlockEntity kegBE) {
            KegBlock.pickUpKeg(level, event.getPos(), kegBE, event.getEntity());
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}