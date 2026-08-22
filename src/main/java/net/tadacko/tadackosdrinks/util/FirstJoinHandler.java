package net.tadacko.tadackosdrinks.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.item.ModItems;

/**
 * Gives the guide book to players on first login.
 */
@Mod.EventBusSubscriber(modid = TadackosDrinks.MOD_ID, bus = Bus.FORGE)
public class FirstJoinHandler {
    private static final String GOT_BOOK_KEY = "gotGuideBook";

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        // PlayerLoggedInEvent is always server side
        // Get the player's persistent compound used across respawns/logins
        CompoundTag root = serverPlayer.getPersistentData();
        CompoundTag persistent = root.getCompound(TadackosDrinks.MOD_ID);

        // If we've already given the book, do nothing
        if (persistent.getBoolean(GOT_BOOK_KEY)) return;

        // Check the player's inventory to avoid duplicate books
        boolean hasBook = false;
        for (ItemStack stack : serverPlayer.getInventory().items) {
            if (!stack.isEmpty() && stack.getItem() == ModItems.GUIDE_BOOK.get()) {
                hasBook = true;
                break;
            }
        }
        if (!hasBook) {
            ItemStack book = new ItemStack(ModItems.GUIDE_BOOK.get());
            // try to add to inventory; addItem returns true if put into inventory
            boolean added = serverPlayer.addItem(book);
            if (!added) {
                // inventory full — drop on the ground for the player
                serverPlayer.drop(book, false);
            }
        }

        // mark as given and save back
        persistent.putBoolean(GOT_BOOK_KEY, true);
        root.put(TadackosDrinks.MOD_ID, persistent);
    }
}
