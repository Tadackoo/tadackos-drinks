package net.tadacko.tadackosdrinks.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tadacko.tadackosdrinks.TadackosDrinks;

import java.util.List;

public class CamaraderieEffect extends MobEffect {
    public static int camaraderieRange = 50; // fallback default, overridden by config value
    public static int camaraderiePlayerCap = 4; // fallback default, overridden by config value

    public CamaraderieEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    /**
     * Runs periodically (isDurationEffectTick controls frequency).
     * We only store the nearby-player count in the amplifier and refresh
     * the effect when the count changes. All damage/attack changes are
     * handled in the event handler (CamaraderieEventHandler).
     */
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;
        // only run on players
        if (!(entity instanceof Player player)) return;

        // Count nearby players (excluding self), within camaraderieRadius blocks, cap at camaraderiePlayerCap
        AABB box = new AABB(
                player.getX() - camaraderieRange, player.getY() - camaraderieRange, player.getZ() - camaraderieRange,
                player.getX() + camaraderieRange, player.getY() + camaraderieRange, player.getZ() + camaraderieRange
        );
        List<Player> nearbyPlayers = player.level().getEntitiesOfClass(Player.class, box,
                p -> p != player && p.isAlive());

        int count = Math.min(nearbyPlayers.size(), camaraderiePlayerCap);

        // store the last known count in the amplifier field of the effect
        MobEffectInstance selfInstance = player.getEffect(this);
        if (selfInstance == null) return;

        int lastCount = selfInstance.getAmplifier();
        if (lastCount != count) {
            int duration = selfInstance.getDuration();
            // Reapply the camaraderie effect with amplifier == count so events can read it.
            // amplifier stores nearby player count
            player.addEffect(new MobEffectInstance(this, duration, count, true, false, true));
        }
    }

    /**
     * Check once per second (20 ticks) — that's sufficient to detect players moving
     * in/out of range and is low overhead.
     */
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    /**
     * Event handlers for the Camaraderie effect.
     *
     * - Adds flat "Strength-like" damage to attacks by entities that have the Camaraderie effect.
     * - Applies multiplicative "Resistance-like" damage reduction to targets that have the Camaraderie effect.
     *
     * This file assumes your ModEffects class exposes the effect as a RegistryObject<MobEffect> named CAMARADERIE,
     * so we call ModEffects.CAMARADERIE.get() to obtain the actual MobEffect instance.
     */
    @Mod.EventBusSubscriber(modid = TadackosDrinks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class CamaraderieEventHandler {
        public static double camaraderieMultiplier = 0.5; // fallback default, overridden by config value
        /**
         * Handles both attacker-side Strength simulation and target-side Resistance simulation.
         *
         * Strength simulation:
         *  - If the attacker has the Camaraderie effect, read the stored nearby-player count from the effect's amplifier.
         *  - simulatedLevel = camaraderieMultiplier * count (allows fractional levels).
         *  - extra flat damage = 3.0 * simulatedLevel (vanilla Strength = +3 damage per full level).
         *  - Add that flat damage to event.getAmount() before other reductions.
         *
         * Resistance simulation:
         *  - If the target has the Camaraderie effect, read stored count similarly.
         *  - reduction = 0.2 * simulatedLevel (≈20% per level like vanilla Resistance).
         *  - Multiply damage by (1 - reduction).
         */
        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            LivingEntity target = event.getEntity();
            if (target == null || target.level().isClientSide) return; // damage calc must be server-authoritative

            // --- Strength: attacker-side flat damage bonus ---
            if (event.getSource() != null && event.getSource().getEntity() instanceof LivingEntity attacker) {
                MobEffectInstance atkInst = attacker.getEffect(ModEffects.CAMARADERIE.get());
                if (atkInst != null) {
                    int count = atkInst.getAmplifier(); // stored nearby-player count
                    double simulatedLevel = camaraderieMultiplier * count; // camaraderieMultiplier levels per nearby player
                    if (simulatedLevel > 0.0) {
                        double extraDamage = 3.0 * simulatedLevel;
                        // Add the flat bonus now so it is included before reductions
                        float newAmount = (float) (event.getAmount() + extraDamage);
                        event.setAmount(newAmount);
                    }
                }
            }

            // --- Resistance: target-side multiplicative reduction ---
            MobEffectInstance tgtInst = target.getEffect(ModEffects.CAMARADERIE.get());
            if (tgtInst != null) {
                int count = tgtInst.getAmplifier();
                double simulatedLevel = camaraderieMultiplier * count;
                if (simulatedLevel > 0.0) {
                    double reduction = 0.2 * simulatedLevel;  // ≈20% per level
                    if (reduction >= 0.99) reduction = 0.99;
                    float original = event.getAmount();
                    float reduced = (float) (original * (1.0 - reduction));
                    event.setAmount(Math.max(0f, reduced));
                }
            }
        }
    }
}
