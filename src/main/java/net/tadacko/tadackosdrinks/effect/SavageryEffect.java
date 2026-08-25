package net.tadacko.tadackosdrinks.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tadacko.tadackosdrinks.TadackosDrinks;

public class SavageryEffect extends MobEffect {
    protected SavageryEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Mod.EventBusSubscriber(modid = TadackosDrinks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class SavageryEventHandler {
        public static float savageryMultiplier = 0.25f; // fallback default, overridden by config value

        @SubscribeEvent
        public static void onCriticalHit(CriticalHitEvent event) {
            if (event.getEntity().level().isClientSide) return;

            Player player = event.getEntity();
            if (player.hasEffect(ModEffects.SAVAGERY.get())) {
                int amplifier = player.getEffect(ModEffects.SAVAGERY.get()).getAmplifier();

                // Default crit is 1.5x (50% bonus)
                // Amplifier 0: 1.75x (75% bonus)
                // Amplifier 1: 2.0x (100% bonus)
                float damageMultiplier = 1.5f + (amplifier + 1) * savageryMultiplier;

                event.setDamageModifier(damageMultiplier);
            }
        }
    }
}
