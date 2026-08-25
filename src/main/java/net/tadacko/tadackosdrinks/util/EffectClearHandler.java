package net.tadacko.tadackosdrinks.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.effect.ModEffects;

@Mod.EventBusSubscriber(modid = TadackosDrinks.MOD_ID)
public class EffectClearHandler {
    private static final String KEY_BAC_PERCENT = "bac_percent";

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffectInstance() == null) return;
        if (event.getEntity() == null || event.getEntity().level().isClientSide) return;

        MobEffect effect = event.getEffectInstance().getEffect();
        if (effect == ModEffects.INEBRIATION.get()) {
            LivingEntity entity = event.getEntity();
            CompoundTag persistent = entity.getPersistentData().getCompound(TadackosDrinks.MOD_ID);

            if (persistent.contains(KEY_BAC_PERCENT)) {
                persistent.remove(KEY_BAC_PERCENT);
                //System.out.println("BAC cleared due to /effect clear or other removal.");
            }
        }
    }
}
