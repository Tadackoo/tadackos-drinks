package net.tadacko.tadackosdrinks.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.tadacko.tadackosdrinks.util.EnchantingPlayerTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin into EnchantmentMenu to track which player is using the enchanting table
 */
@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin {

    @Shadow @Final private Container enchantSlots;

    /**
     * Track player from the constructor when the menu is opened
     */
    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"))
    private void onInit(int containerId, Inventory playerInventory, ContainerLevelAccess access, CallbackInfo ci) {
        Player player = playerInventory.player;
        EnchantingPlayerTracker.setCurrentEnchantingPlayer(player);
    }

    /**
     * Keep player tracked during slot changes (when enchantments are calculated)
     */
    @Inject(method = "slotsChanged", at = @At("HEAD"))
    public void onSlotsChanged(Container inventory, CallbackInfo ci) {
        // Player remains tracked from constructor
    }

    /**
     * Inject at the start of clickMenuButton to ensure player is still tracked
     */
    @Inject(method = "clickMenuButton", at = @At("HEAD"))
    public void onClickMenuButton(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        EnchantingPlayerTracker.setCurrentEnchantingPlayer(player);
    }

    /**
     * Don't clear player after clicking - keep them tracked while menu is open
     */
    @Inject(method = "clickMenuButton", at = @At("RETURN"))
    public void afterClickMenuButton(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        // Keep player tracked for next enchant
    }

    /**
     * Clear player when menu is closed
     */
    @Inject(method = "removed", at = @At("HEAD"))
    public void onMenuClosed(Player player, CallbackInfo ci) {
        EnchantingPlayerTracker.clearCurrentEnchantingPlayer();
    }
}