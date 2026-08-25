package net.tadacko.tadackosdrinks.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.tadacko.tadackosdrinks.effect.ModEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {
    @Unique
    private Player tadackosdrinks$currentPlayer;

    @Inject(method = "tick", at = @At("HEAD"))
    private void capturePlayer(Player player, CallbackInfo ci) {
        this.tadackosdrinks$currentPlayer = player;
    }

    /**
     * Modify the 4.0F constant that exhaustion is compared against
     * Making it higher means it takes more exhaustion to lose hunger
     */
    @ModifyConstant(method = "tick", constant = @Constant(floatValue = 4.0F))
    private float modifyExhaustionThreshold(float original) {
        if (this.tadackosdrinks$currentPlayer != null && this.tadackosdrinks$currentPlayer.hasEffect(ModEffects.IMPROVED_DIGESTION.get())) {
            int amplifier = this.tadackosdrinks$currentPlayer.getEffect(ModEffects.IMPROVED_DIGESTION.get()).getAmplifier();

            // At amp 0: 50% slower = need 8.0 exhaustion
            // At amp 1+: 75% slower = need 16.0 exhaustion
            float multiplier = (amplifier + 1) * 2.0f;

            return original * multiplier;
        }
        return original;
    }
}