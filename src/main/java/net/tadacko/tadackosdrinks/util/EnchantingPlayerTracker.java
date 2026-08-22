package net.tadacko.tadackosdrinks.util;

import net.minecraft.world.entity.player.Player;

/**
 * Utility class to track which player is currently using an enchanting table.
 * This is used by the mixins to determine if treasure enchantments should be available.
 */
public class EnchantingPlayerTracker {

    private static final ThreadLocal<Player> CURRENT_ENCHANTING_PLAYER = new ThreadLocal<>();

    public static void setCurrentEnchantingPlayer(Player player) {
        CURRENT_ENCHANTING_PLAYER.set(player);
    }

    public static void clearCurrentEnchantingPlayer() {
        CURRENT_ENCHANTING_PLAYER.remove();
    }

    public static Player getCurrentEnchantingPlayer() {
        return CURRENT_ENCHANTING_PLAYER.get();
    }
}
