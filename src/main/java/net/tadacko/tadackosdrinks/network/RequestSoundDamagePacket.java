package net.tadacko.tadackosdrinks.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.network.NetworkEvent;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.effect.ModEffects;
import net.tadacko.tadackosdrinks.damage.ModDamageSources;

import java.util.function.Supplier;

public class RequestSoundDamagePacket {
    public RequestSoundDamagePacket() {}

    public static void encode(RequestSoundDamagePacket pkt, FriendlyByteBuf buf) {
        // no data
    }

    public static RequestSoundDamagePacket decode(FriendlyByteBuf buf) {
        return new RequestSoundDamagePacket();
    }

    public static void handle(RequestSoundDamagePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            long nowTicks = player.level().getGameTime();

            final String NBT_KEY = "lastHangoverSoundHurt";
            final long HURT_COOLDOWN_TICKS = 5L; // 0.25s

            // confirm player still has hangover amp >= 2
            MobEffectInstance inst = player.getEffect(ModEffects.HANGOVER.get());
            if (inst == null || inst.getAmplifier() < 2) return;

            // per-player cooldown stored in persistent data
            net.minecraft.nbt.CompoundTag pdata = player.getPersistentData().getCompound(TadackosDrinks.MOD_ID);
            long last = pdata.getLong(NBT_KEY);
            if (nowTicks - last < HURT_COOLDOWN_TICKS) return;

            // apply damage on server (small, tune as desired)
            float dmg = 1F;
            if (!(player.level() instanceof ServerLevel serverLevel)) return;
            boolean applied = player.hurt(ModDamageSources.hangover(serverLevel), dmg);
            if (applied) {
                pdata.putLong(NBT_KEY, nowTicks);
            }
        });

        ctx.setPacketHandled(true);
    }
}
