package net.tadacko.tadackosdrinks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.effect.ModEffects;

// No mixins: lets GameRenderer's own vanilla warp code run untouched.
// We only (a) clamp the public spinningEffectIntensity field to an amplifier-based ceiling,
// and (b) keep a hidden real Confusion instance alive so GameRenderer picks
// the slow (7) rotation speed instead of the fast (20) portal speed.
@Mod.EventBusSubscriber(modid = TadackosDrinks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class NauseaWarpHandler {
    // Must stay above LocalPlayer's 60-tick "endsWithin" fade-out threshold while our
    // custom effect is active, or the vanilla ramp-down would kick in prematurely.
    private static final int HIDDEN_EFFECT_DURATION = 100;

    private NauseaWarpHandler() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        MobEffectInstance custom = player.getEffect(ModEffects.MILD_NAUSEA.get());

        if (custom != null) {
            // Refresh every tick so it never drops into the 60-tick fade-out window
            // while our effect is still active.
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, HIDDEN_EFFECT_DURATION, 0, true, false, false));

            float target = (custom.getAmplifier() + 1) / 4.0F;
            if (player.spinningEffectIntensity > target) {
                player.spinningEffectIntensity = target;
            }
            return;
        }

        // Our effect just ended (expired, milk, /effect clear...). Stop refreshing:
        // the hidden instance's own duration keeps counting down on its own (tick()
        // still runs client-side, it's only the *removal* that's server-gated), so
        // once it drops under 60 remaining, LocalPlayer's own vanilla logic takes
        // over and fades spinningEffectIntensity out smoothly via the -0.05F/tick decay branch —
        // no extra code needed from us for that part.
        //
        // Only once the warp has fully settled do we clean up the orphaned instance,
        // so removal never causes a visible snap (removeEffect -> removeEffectNoUpdate
        // -> LocalPlayer's override zeroes spinningEffectIntensity instantly, which is fine here
        // since it's already at 0).
        if (player.hasEffect(MobEffects.CONFUSION) && player.spinningEffectIntensity <= 0.0F) {
            player.removeEffect(MobEffects.CONFUSION);
        }
    }
}