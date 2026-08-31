package net.tadacko.tadackosdrinks.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.network.PacketDistributor;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.config.ModCommonConfigs;
import net.tadacko.tadackosdrinks.item.ModItems;
import net.tadacko.tadackosdrinks.network.ModNetwork;

/** Gives the guide book to players on first login. */
@Mod.EventBusSubscriber(modid = TadackosDrinks.MOD_ID, bus = Bus.FORGE)
public class JoinHandler {
    private static final String GOT_BOOK_KEY = "got_guide_book";

    public static boolean firstLoginBook = true;

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        // PlayerLoggedInEvent is always server side
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), ModCommonConfigs.createSyncPacket());

        if (!firstLoginBook) return;

        CompoundTag root = serverPlayer.getPersistentData();
        CompoundTag persistent = root.getCompound(TadackosDrinks.MOD_ID);

        if (persistent.getBoolean(GOT_BOOK_KEY)) return;

        // avoid duplicate books
        boolean hasBook = false;
        for (ItemStack stack : serverPlayer.getInventory().items) {
            if (!stack.isEmpty() && stack.getItem() == ModItems.GUIDE_BOOK.get()) {
                hasBook = true;
                break;
            }
        }
        if (!hasBook) {
            ItemStack book = new ItemStack(ModItems.GUIDE_BOOK.get());
            boolean added = serverPlayer.addItem(book);
            if (!added) {
                serverPlayer.drop(book, false);
            }
        }

        persistent.putBoolean(GOT_BOOK_KEY, true);
        root.put(TadackosDrinks.MOD_ID, persistent);
    }
}
