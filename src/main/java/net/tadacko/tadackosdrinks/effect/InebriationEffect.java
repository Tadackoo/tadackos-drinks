package net.tadacko.tadackosdrinks.effect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.tadacko.tadackosdrinks.TadackosDrinks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InebriationEffect extends MobEffect {
    // fallback defaults, overridden by config values
    public static double stumbleStrength = 0.02; // horizontal speed component (blocks/tick)
    public static double stumbleDampFactor = 0.8; // keep some of previous velocity
    public static int stumbleChangeMinTicks = 5; // shorter -> more frequent direction changes
    public static int stumbleChangeMaxTicks = 10; // upper bound for interpolation time
    public static double stumbleDirectionChangeMax = 0.9;
    private static final double STUMBLE_OFFSET_ANGLE_MAX = Math.PI * stumbleDirectionChangeMax; // max offset when choosing new direction (~162°)
    public static double stumbleSharpTurnChance = 0.2; // chance to force a very sharp turn (reverse-ish)
    public static double stumbleJitterStrength = 0.05; // per-tick random angular jitter (radians)
    public static int hangoverBaseDuration = 24000; // 600s

    private static final String SESSION_TAG = "inebriation_session";
    private static final String KEY_MAX_AMP = "max_amp";
    private static final String KEY_HANGOVER_PENDING = "hangover_pending";

    private static final String KEY_LUCK = "applied_luck";
    private static final String KEY_UNLUCK = "applied_unluck";
    private static final String KEY_MILD_NAUSEA = "applied_mild_nausea";
    private static final String KEY_CONFUSION = "applied_confusion";
    private static final String KEY_SLOW = "applied_slow";
    private static final String KEY_DIGSLOW = "applied_digslow";
    private static final String KEY_WEAKNESS = "applied_weakness";
    private static final String KEY_BLIND = "applied_blind";
    private static final String KEY_POISON = "applied_poison";
    private static final String KEY_CAMARADERIE = "applied_camaraderie";

    // in-memory stumble state, keyed by entity UUID (avoids per-tick NBT writes)
    // ConcurrentHashMap because removal can happen from a server.execute() callback
    private static final Map<UUID, StumbleState> STUMBLE_STATES = new ConcurrentHashMap<>();

    private static class StumbleState {
        double curAngle;
        double targetAngle;
        int ticksToTarget;
        int progress;
    }

    public InebriationEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        if (entity.level().isClientSide) return;
        MobEffectInstance inst = entity.getEffect(this);
        if (inst == null) return;

        // instead of direct entity.addEffect(...), call helper and give each effect a unique session key
        if (amplifier <= 0) {
            // remove when downgrading
            removeHiddenIfRecorded(entity, MobEffects.LUCK, KEY_LUCK);

            applyOrRefreshHidden(entity, MobEffects.LUCK, -1, 0, KEY_LUCK);
        } else if (amplifier == 1) {
            // remove when downgrading
            removeHiddenIfRecorded(entity, ModEffects.MILD_NAUSEA.get(), KEY_MILD_NAUSEA);

            applyOrRefreshHidden(entity, MobEffects.LUCK, -1, 1, KEY_LUCK);
        } else if (amplifier == 2) {
            // remove when upgrading
            removeHiddenIfRecorded(entity, MobEffects.LUCK, KEY_LUCK);

            // remove when downgrading
            removeHiddenIfRecorded(entity, MobEffects.UNLUCK, KEY_UNLUCK);
            removeHiddenIfRecorded(entity, ModEffects.MILD_NAUSEA.get(), KEY_MILD_NAUSEA);
            removeHiddenIfRecorded(entity, MobEffects.MOVEMENT_SLOWDOWN, KEY_SLOW);

            applyOrRefreshHidden(entity, ModEffects.MILD_NAUSEA.get(), -1, 0, KEY_MILD_NAUSEA);
        } else if (amplifier == 3) {
            // remove when downgrading
            removeHiddenIfRecorded(entity, MobEffects.UNLUCK, KEY_UNLUCK);
            removeHiddenIfRecorded(entity, MobEffects.CONFUSION, KEY_CONFUSION);
            removeHiddenIfRecorded(entity, MobEffects.MOVEMENT_SLOWDOWN, KEY_SLOW);
            removeHiddenIfRecorded(entity, MobEffects.DIG_SLOWDOWN, KEY_DIGSLOW);
            removeHiddenIfRecorded(entity, MobEffects.WEAKNESS, KEY_WEAKNESS);
            removeHiddenIfRecorded(entity, MobEffects.BLINDNESS, KEY_BLIND);
            removeHiddenIfRecorded(entity, MobEffects.POISON, KEY_POISON);

            applyOrRefreshHidden(entity, MobEffects.UNLUCK, -1, 0, KEY_UNLUCK);
            applyOrRefreshHidden(entity, ModEffects.MILD_NAUSEA.get(), -1, 1, KEY_MILD_NAUSEA);
            applyOrRefreshHidden(entity, MobEffects.MOVEMENT_SLOWDOWN, -1, 0, KEY_SLOW);
        } else if (amplifier == 4) {
            // remove when upgrading
            removeHiddenIfRecorded(entity, ModEffects.MILD_NAUSEA.get(), KEY_MILD_NAUSEA);

            applyOrRefreshHidden(entity, MobEffects.UNLUCK, -1, 1, KEY_UNLUCK);
            applyOrRefreshHidden(entity, MobEffects.CONFUSION, -1, 0, KEY_CONFUSION);
            applyOrRefreshHidden(entity, MobEffects.MOVEMENT_SLOWDOWN, -1, 1, KEY_SLOW);
            applyOrRefreshHidden(entity, MobEffects.DIG_SLOWDOWN, -1, 0, KEY_DIGSLOW);
            applyOrRefreshHidden(entity, MobEffects.WEAKNESS, -1, 0, KEY_WEAKNESS);
            applyOrRefreshHidden(entity, MobEffects.BLINDNESS, -1, 0, KEY_BLIND);
            applyOrRefreshHidden(entity, MobEffects.POISON, -1, 0, KEY_POISON);
        }
        applyOrRefreshHidden(entity, ModEffects.CAMARADERIE.get(), -1, 0, KEY_CAMARADERIE);

        if (amplifier == 3 || amplifier == 4) initStumble(entity);

        CompoundTag root = entity.getPersistentData();
        CompoundTag persistent = root.getCompound(TadackosDrinks.MOD_ID);
        CompoundTag session = persistent.contains(SESSION_TAG) ? persistent.getCompound(SESSION_TAG) : new CompoundTag();

        int recordedMax = session.getInt(KEY_MAX_AMP);
        if (amplifier > recordedMax) {
            session.putInt(KEY_MAX_AMP, amplifier);
        }

        persistent.put(SESSION_TAG, session);
        root.put(TadackosDrinks.MOD_ID, persistent);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        if (entity.level().isClientSide) return;

        if (entity.level() instanceof ServerLevel server) {
            server.getServer().execute(() -> {
                if (!entity.isAlive()) return;
                // If the inebriation effect is STILL present on the entity, do nothing.
                // This covers amplifier changes where the effect is removed then re-applied:
                // we avoid removing the hidden secondaries in that case to prevent flicker.
                if (entity.getEffect(this) != null) return;

                // effect truly gone -> safely remove only the hidden effects we applied
                removeHiddenIfRecorded(entity, MobEffects.LUCK, KEY_LUCK);
                removeHiddenIfRecorded(entity, MobEffects.UNLUCK, KEY_UNLUCK);
                removeHiddenIfRecorded(entity, ModEffects.MILD_NAUSEA.get(), KEY_MILD_NAUSEA);
                removeHiddenIfRecorded(entity, MobEffects.CONFUSION, KEY_CONFUSION);
                removeHiddenIfRecorded(entity, MobEffects.MOVEMENT_SLOWDOWN, KEY_SLOW);
                removeHiddenIfRecorded(entity, MobEffects.DIG_SLOWDOWN, KEY_DIGSLOW);
                removeHiddenIfRecorded(entity, MobEffects.WEAKNESS, KEY_WEAKNESS);
                removeHiddenIfRecorded(entity, MobEffects.BLINDNESS, KEY_BLIND);
                removeHiddenIfRecorded(entity, MobEffects.POISON, KEY_POISON);
                removeHiddenIfRecorded(entity, ModEffects.CAMARADERIE.get(), KEY_CAMARADERIE);

                STUMBLE_STATES.remove(entity.getUUID());

                CompoundTag root = entity.getPersistentData();
                CompoundTag persistent = root.getCompound(TadackosDrinks.MOD_ID);
                CompoundTag session = persistent.contains(SESSION_TAG) ? persistent.getCompound(SESSION_TAG) : new CompoundTag();

                if (session.getBoolean(KEY_HANGOVER_PENDING)) {
                    int recordedMax = session.getInt(KEY_MAX_AMP);
                    int hangoverDuration = -1;
                    if (hangoverBaseDuration != -1) hangoverDuration = hangoverBaseDuration * (recordedMax - 1);
                    int ampForEffect = Math.max(0, recordedMax - 2);

                    entity.addEffect(new MobEffectInstance(ModEffects.HANGOVER.get(), hangoverDuration, ampForEffect, false, true,
                            true));
                }

                // clear session entirely
                persistent.remove(SESSION_TAG);
                root.put(TadackosDrinks.MOD_ID, persistent);
            });
        }
    }

    // run every tick for stumble and downgrade logic
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    // per-tick: smoothly interpolate angle and apply a small continuous velocity
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;
        // stumbling
        if (amplifier == 3 || amplifier == 4) {
            StumbleState s = STUMBLE_STATES.get(entity.getUUID());
            if (s == null) {
                initStumble(entity);
                s = STUMBLE_STATES.get(entity.getUUID());
            }

            // progress interpolation
            s.progress++;
            if (s.progress >= s.ticksToTarget) {
                // arrive at target and choose a new one
                s.curAngle = s.targetAngle;

                // choose next offset across a wide range to allow sharp changes
                double offset = (entity.getRandom().nextDouble() * STUMBLE_OFFSET_ANGLE_MAX) - (STUMBLE_OFFSET_ANGLE_MAX / 2.0);

                // sometimes make a sharp reversal turn instead of a small drift
                if (entity.getRandom().nextDouble() < stumbleSharpTurnChance) {
                    // reverse-ish direction with small randomness
                    offset = Math.PI * (0.85 - entity.getRandom().nextDouble() * 0.3);
                    // random left/right of that reversal
                    if (entity.getRandom().nextBoolean()) offset = -offset;
                }

                s.targetAngle = s.curAngle + offset;
                s.ticksToTarget = stumbleChangeMinTicks + entity.getRandom().nextInt(Math.max(1, stumbleChangeMaxTicks - stumbleChangeMinTicks + 1));
                s.progress = 0;
            }

            // eased interpolation (cosine ease-in-out)
            double t = s.ticksToTarget > 0 ? (double) s.progress / (double) s.ticksToTarget : 1.0;
            double ease = (1 - Math.cos(t * Math.PI)) * 0.5;
            double currentAngle = s.curAngle + (s.targetAngle - s.curAngle) * ease;

            // add a small per-tick jitter so direction isn't perfectly smooth
            double jitter = (entity.getRandom().nextDouble() - 0.5) * stumbleJitterStrength;
            // scale jitter by (1 - stability) so it's stronger when mid-change
            double stability = Math.abs(0.5 - t) * 2.0; // 0 at mid-change, 1 at ends
            currentAngle += jitter * (1.0 - stability);

            // compute nudge vector and apply it
            double nx = Math.cos(currentAngle) * stumbleStrength;
            double nz = Math.sin(currentAngle) * stumbleStrength;

            Vec3 vel = entity.getDeltaMovement();
            double newX = vel.x * stumbleDampFactor + nx;
            double newZ = vel.z * stumbleDampFactor + nz;
            Vec3 newVel = new Vec3(newX, vel.y, newZ);

            entity.setDeltaMovement(newVel);

            if (entity instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(entity.getId(), newVel));
            }
        }

        // check if time is about to expire
        MobEffectInstance inst = entity.getEffect(this);
        if (inst != null && inst.getDuration() <= 1) {
            CompoundTag root = entity.getPersistentData();
            CompoundTag persistent = root.getCompound(TadackosDrinks.MOD_ID);
            CompoundTag session = persistent.contains(SESSION_TAG) ? persistent.getCompound(SESSION_TAG) : new CompoundTag();
            int recordedMax = session.getInt(KEY_MAX_AMP);
            if (amplifier > 0) {
                // downgrade
                entity.addEffect(new MobEffectInstance(this, 120, amplifier - 1, false, true, true));
            } else if (amplifier == 0 && recordedMax >= 2) {
                session.putBoolean(KEY_HANGOVER_PENDING, true);
                persistent.put(SESSION_TAG, session);
                root.put(TadackosDrinks.MOD_ID, persistent);
            }
        }
    }


    /** Apply a hidden effect only if missing or amplifier differs. Records application in session NBT. */
    private void applyOrRefreshHidden(LivingEntity entity, MobEffect effectType, int duration, int amp, String sessionKey) {
        MobEffectInstance existing = entity.getEffect(effectType);
        boolean needsApply = existing == null || existing.getAmplifier() != amp;

        if (needsApply) {
            entity.addEffect(new MobEffectInstance(effectType, duration, amp, false, false, false));
        }

        CompoundTag root = entity.getPersistentData();
        CompoundTag persistent = root.getCompound(TadackosDrinks.MOD_ID);
        CompoundTag session = persistent.contains(SESSION_TAG) ? persistent.getCompound(SESSION_TAG) : new CompoundTag();
        session.putBoolean(sessionKey, true);
        persistent.put(SESSION_TAG, session);
        root.put(TadackosDrinks.MOD_ID, persistent);
    }

    /** Remove only the hidden effect we recorded as applied. */
    private void removeHiddenIfRecorded(LivingEntity entity, MobEffect effectType, String sessionKey) {
        CompoundTag root = entity.getPersistentData();
        CompoundTag persistent = root.getCompound(TadackosDrinks.MOD_ID);
        if (!persistent.contains(SESSION_TAG)) return;
        CompoundTag session = persistent.getCompound(SESSION_TAG);
        if (!session.contains(sessionKey) || !session.getBoolean(sessionKey)) return;

        entity.removeEffect(effectType);
        session.remove(sessionKey);
        root.put(TadackosDrinks.MOD_ID, persistent);
    }

    /** Initialize/reset stumble interpolation state for an entity. */
    private void initStumble(LivingEntity entity) {
        StumbleState s = new StumbleState();
        double startAngle = entity.getRandom().nextDouble() * Math.PI * 2.0;

        // pick initial target with moderate randomness
        double offset = (entity.getRandom().nextDouble() * stumbleDirectionChangeMax) - (stumbleDirectionChangeMax / 2.0); // ±MAX/2
        double targetAngle = startAngle + offset;

        // sometimes force a sharp initial turn/reverse to avoid one-direction bias
        if (entity.getRandom().nextDouble() < stumbleSharpTurnChance) {
            targetAngle = startAngle + Math.PI * (0.85 - entity.getRandom().nextDouble() * 0.3); // ~reverse ± small noise
        }

        s.curAngle = startAngle;
        s.targetAngle = targetAngle;
        s.ticksToTarget = stumbleChangeMinTicks + entity.getRandom().nextInt(Math.max(1, stumbleChangeMaxTicks - stumbleChangeMinTicks + 1));
        s.progress = 0;

        STUMBLE_STATES.put(entity.getUUID(), s);
    }

    public static void onEntityLeave(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide) {
            STUMBLE_STATES.remove(event.getEntity().getUUID());
        }
    }
}