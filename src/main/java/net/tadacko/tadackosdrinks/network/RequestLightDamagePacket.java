package net.tadacko.tadackosdrinks.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.effect.ModEffects;
import net.tadacko.tadackosdrinks.damage.ModDamageSources;

import java.util.function.Supplier;

public class RequestLightDamagePacket {
    public RequestLightDamagePacket() {}

    public static void encode(RequestLightDamagePacket pkt, FriendlyByteBuf buf) {
        // no data
    }

    public static RequestLightDamagePacket decode(FriendlyByteBuf buf) {
        return new RequestLightDamagePacket();
    }

    public static void handle(RequestLightDamagePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            // server tick time (monotonic per-server tick)
            long nowTicks = player.level().getGameTime();

            // per-player persistent NBT key to avoid shared maps/race conditions
            final String NBT_KEY = "lastHangoverHurt";
            final long HURT_COOLDOWN_TICKS = 10L; // 20 ticks = 1s, tune as needed
            final double SUN_DOT_THRESHOLD = 0.992;
            final double SKY_CHECK_RANGE = 1000.0D;
            final double BLOCK_RANGE = 20.0D;

            // quick checks: effect still present and amplifier >= 2
            MobEffectInstance inst = player.getEffect(ModEffects.HANGOVER.get());
            if (inst == null || inst.getAmplifier() < 2) return;

            // check cooldown stored on the player NBT
            net.minecraft.nbt.CompoundTag pdata = player.getPersistentData().getCompound(TadackosDrinks.MOD_ID);
            long last = pdata.getLong(NBT_KEY);
            if (nowTicks - last < HURT_COOLDOWN_TICKS) return;

            // Validate client claim by re-checking whether player is looking at a valid light source
            boolean shouldHurt = false;

            Vec3 eye = player.getEyePosition(1.0F);
            Vec3 look = player.getViewVector(1.0F).normalize();

            // 1) block light check (conservative range)
            Vec3 end = eye.add(look.scale(BLOCK_RANGE));
            ClipContext ctxClip = new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
            HitResult hit = player.level().clip(ctxClip);

            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockHitResult bhr = (BlockHitResult) hit;
                BlockPos pos = bhr.getBlockPos();
                int emitted = player.level().getBlockState(pos).getLightEmission(player.level(), pos);
                if (emitted > 10) {
                    shouldHurt = true;
                }
            }

            // 2) sun check if not block
            if (!shouldHurt) {
                if (player.level().dimensionType().hasSkyLight() && player.level().isDay() && !player.level().isThundering()) {
                    // ensure player's view to sky is unobstructed
                    Vec3 skyEnd = eye.add(look.scale(SKY_CHECK_RANGE));
                    ClipContext skyCtx = new ClipContext(eye, skyEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
                    HitResult skyHit = player.level().clip(skyCtx);
                    boolean skyVisible = skyHit.getType() == HitResult.Type.MISS;

                    //System.out.println("[HangoverDebug] server: skyVisible=" + skyVisible);

                    if (skyVisible) {
                        float sunAngle = player.level().getSunAngle(1.0F);
                        float shifted = sunAngle + (float) Math.PI / 2.0F;

                        // same candidate vectors as the client
                        Vec3 candidateA = new Vec3(Math.cos(shifted), Math.sin(shifted), 0.0).normalize();
                        Vec3 candidateB = new Vec3(0.0, Math.sin(shifted), -Math.cos(shifted)).normalize();
                        Vec3 candidateC = new Vec3(Math.cos(shifted), 0.0, -Math.sin(shifted)).normalize();

                        double dotA = look.dot(candidateA);
                        double dotB = look.dot(candidateB);
                        double dotC = look.dot(candidateC);

                        double bestDot = Math.max(Math.max(dotA, dotB), dotC);

                        if (bestDot > SUN_DOT_THRESHOLD) {
                            shouldHurt = true;
                        }
                    }
                }
            }

            if (shouldHurt) {
                // attempt to apply damage; only update cooldown if damage actually applied
                float dmg = 0.5F; // tune
                if (!(player.level() instanceof ServerLevel serverLevel)) return; // safety
                boolean applied = player.hurt(ModDamageSources.hangover(serverLevel), dmg);
                if (applied) {
                    pdata.putLong(NBT_KEY, nowTicks);
                }
            }
        });

        ctx.setPacketHandled(true);
    }
}