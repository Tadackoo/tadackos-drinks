package net.tadacko.tadackosdrinks.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tadacko.tadackosdrinks.TadackosDrinks;

import java.util.Random;

public class PiracyEffect extends MobEffect {
    public PiracyEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Mod.EventBusSubscriber(modid = TadackosDrinks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class PiracyEventHandler {
        private static final Random RANDOM = new Random();

        // fallback defaults, overridden by config values
        public static float piracy0NothingChance = 50f;
        public static float piracy0GoldChance = 40f;
        public static float piracy0EmeraldChance = 7f;
        public static float piracy1NothingChance = 30f;
        public static float piracy1GoldChance = 50f;
        public static float piracy1EmeraldChance = 15f;

        @SubscribeEvent
        public static void onEntityDeath(LivingDeathEvent event) {
            if (event.getEntity().level().isClientSide) return;
            if (event.getEntity() instanceof net.minecraft.world.entity.player.Player) return;

            if (event.getSource().getEntity() instanceof LivingEntity attacker) {
                if (attacker.hasEffect(ModEffects.PIRACY.get())) {
                    int amplifier = attacker.getEffect(ModEffects.PIRACY.get()).getAmplifier();
                    ItemStack drop = getPiracyDrop(amplifier);
                    if (!drop.isEmpty()) event.getEntity().spawnAtLocation(drop);
                }
            }
        }

        private static ItemStack getPiracyDrop(int amplifier) {
            float roll = RANDOM.nextFloat() * 100f;

            if (amplifier == 0) {
                if (roll < piracy0NothingChance) {
                    return ItemStack.EMPTY; // 50% nothing
                } else if (roll < piracy0NothingChance + piracy0GoldChance) {
                    return new ItemStack(Items.GOLD_INGOT); // 40% gold
                } else if (roll < piracy0NothingChance + piracy0GoldChance + piracy0EmeraldChance) {
                    return new ItemStack(Items.EMERALD); // 7% emerald
                } else {
                    return new ItemStack(Items.DIAMOND); // 3% diamond
                }
            } else if (amplifier >= 1) {
                if (roll < piracy1NothingChance) {
                    return ItemStack.EMPTY; // 30% nothing
                } else if (roll < piracy1NothingChance + piracy1GoldChance) {
                    return new ItemStack(Items.GOLD_INGOT); // 50% gold
                } else if (roll < piracy1NothingChance + piracy1GoldChance + piracy1EmeraldChance) {
                    return new ItemStack(Items.EMERALD); // 15% emerald
                } else {
                    return new ItemStack(Items.DIAMOND); // 5% diamond
                }
            }

            return ItemStack.EMPTY;
        }
    }
}