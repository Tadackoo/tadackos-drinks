package net.tadacko.tadackosdrinks.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.tadacko.tadackosdrinks.block.DrinkVariant;
import net.tadacko.tadackosdrinks.effect.ModEffects;
import net.tadacko.tadackosdrinks.util.BacUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TequilaDrinkItem extends DrinkItem {
    private final int amplifier;

    public static double tequilaDurationMultiplier = 2; // fallback default, overridden by config value

    public TequilaDrinkItem(Properties properties, int amplifier, double abv, double volumeL, DrinkVariant variant, Item emptyDrinkware) {
        super(properties, null, 0, 0, abv, volumeL, variant, emptyDrinkware);
        this.amplifier = amplifier;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        // Call parent to handle food effects, stats, etc.
        ItemStack result = super.finishUsingItem(pStack, pLevel, pLivingEntity);

        if (!pLevel.isClientSide) {
            if (!drinkSecondaryEffects) return result;
            long baseDuration = BacUtils.computeEffectDurationTicks(this.abv, this.volumeL, pLivingEntity);

            // Apply multiplier
            long totalDuration = (long) (baseDuration * tequilaDurationMultiplier * (amplifier + 1));
            if (totalDuration > Integer.MAX_VALUE) totalDuration = Integer.MAX_VALUE;

            // Get all active effects and filter them
            Collection<MobEffectInstance> activeEffects = pLivingEntity.getActiveEffects();
            List<MobEffectInstance> eligibleEffects = new ArrayList<>();

            for (MobEffectInstance effect : activeEffects) {
                // Skip if effect is not beneficial
                if (!effect.getEffect().isBeneficial()) {
                    continue;
                }

                // Skip Camaraderie
                if (effect.getEffect() == ModEffects.CAMARADERIE.get()) {
                    continue;
                }

                // Skip Luck
                if (effect.getEffect() == MobEffects.LUCK) {
                    continue;
                }

                // This effect is eligible
                eligibleEffects.add(effect);
            }

            if (!eligibleEffects.isEmpty()) {
                // Calculate duration to add to each eligible effect
                int durationPerEffect = (int) (totalDuration / eligibleEffects.size());

                // Add duration to each eligible effect
                for (MobEffectInstance effect : eligibleEffects) {
                    int newDuration = effect.getDuration() + durationPerEffect;
                    // Create new effect instance with extended duration
                    MobEffectInstance extendedEffect = new MobEffectInstance(
                            effect.getEffect(),
                            newDuration,
                            effect.getAmplifier(),
                            effect.isAmbient(),
                            effect.isVisible(),
                            effect.showIcon()
                    );
                    pLivingEntity.addEffect(extendedEffect);
                }
            }
            // If no eligible effects, the duration is simply not applied anywhere
        }

        return result;
    }
}