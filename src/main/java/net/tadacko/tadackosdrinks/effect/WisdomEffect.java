package net.tadacko.tadackosdrinks.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tadacko.tadackosdrinks.TadackosDrinks;

import java.util.HashMap;
import java.util.UUID;

public class WisdomEffect extends MobEffect {
    protected WisdomEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Mod.EventBusSubscriber(modid = TadackosDrinks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class WisdomEventHandler {
        private static final HashMap<UUID, Float> fractionalXp = new HashMap<>();

        public static float wisdomMultiplier = 1.5f; // fallback default, overridden by config value

        @SubscribeEvent
        public static void onXpPickup(PlayerXpEvent.PickupXp event) {
            Player player = event.getEntity();
            if (player.level().isClientSide()) return;

            if (player.hasEffect(ModEffects.WISDOM.get())) {
                int amplifier = player.getEffect(ModEffects.WISDOM.get()).getAmplifier();
                int originalXp = event.getOrb().getValue();

                // Calculate multiplier: amp 0 = 1.5x, amp 1 = 3x
                float multiplier = (amplifier + 1) * wisdomMultiplier - 1.0f; // adding bonus xp, not the whole amount, so - 1.0f
                float exactBonus = originalXp * multiplier;

                // Get accumulated fractional XP
                UUID playerId = player.getUUID();
                float accumulated = fractionalXp.getOrDefault(playerId, 0f) + exactBonus;

                int bonusXp = (int)accumulated;
                // store leftover for next time
                fractionalXp.put(playerId, accumulated - bonusXp);

                // Add the bonus XP
                player.giveExperiencePoints(bonusXp);
            }
        }
    }
}
