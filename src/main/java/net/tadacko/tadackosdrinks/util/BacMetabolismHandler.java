package net.tadacko.tadackosdrinks.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.effect.ModEffects;

/**
 * Handles ongoing BAC metabolism and automatic inebriation level adjustments.
 * Works on server side only.
 */
@Mod.EventBusSubscriber(modid = TadackosDrinks.MOD_ID)
public class BacMetabolismHandler {
    private static final String KEY_BAC_PERCENT = "bac_percent";

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        Player player = event.player;
        CompoundTag root = player.getPersistentData();
        CompoundTag persistent = root.getCompound(TadackosDrinks.MOD_ID);

        if (!persistent.contains(KEY_BAC_PERCENT)) return;

        double currentBacPercent = persistent.getDouble(KEY_BAC_PERCENT);
        if (currentBacPercent <= 0.0) {
            persistent.remove(KEY_BAC_PERCENT);
            player.removeEffect(ModEffects.INEBRIATION.get());
            return;
        }

        double ratePerTick = BacUtils.BACEliminationRatePercentPerHour / 72000.0; // 72000 ticks is 1h
        currentBacPercent -= ratePerTick;
        if (currentBacPercent < 0.0) currentBacPercent = 0.0;

        int currentAmp = player.hasEffect(ModEffects.INEBRIATION.get()) ? player.getEffect(ModEffects.INEBRIATION.get()).getAmplifier() : -1;
        int newAmp = currentBacPercent > 0.0 ? BacUtils.bacToAmplifier(currentBacPercent) : -1;

        if (newAmp != currentAmp) {
            if (newAmp >= 0) {
                long segmentTicks = BacUtils.computeSegmentTicksForAmp(currentBacPercent, newAmp);
                int applyDuration = segmentTicks > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) segmentTicks;
                player.addEffect(new MobEffectInstance(ModEffects.INEBRIATION.get(), applyDuration, newAmp, false, true, true));
            } else {
                player.removeEffect(ModEffects.INEBRIATION.get());
            }
        }

        if (currentBacPercent <= 0.0) {
            persistent.remove(KEY_BAC_PERCENT);
        } else {
            persistent.putDouble(KEY_BAC_PERCENT, currentBacPercent);
            root.put(TadackosDrinks.MOD_ID, persistent);
        }
        // System.out.printf("BAC %.5f%% amp=%d%n", currentBacPercent, newAmp);
    }
}
