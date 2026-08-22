package net.tadacko.tadackosdrinks.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.effect.ModEffects;

/**
 * Client-side renderer for Hangover custom visual with reliable sun detection.
 *
 * - Raycasts to check blocks (block light) like before.
 * - Raycasts far to make sure the player is looking at the sky (not blocked).
 * - Uses level.getSunAngle(...) and checks multiple candidate axis conventions, picking the best dot product.
 * - If the best dot is above SUN_DOT_THRESHOLD and sky is visible, apply sun glare.
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HangoverClientRenderer {
    // tuning
    private static final int LIGHT_THRESHOLD = 10;
    private static final double BLOCK_RANGE = 20.0D;
    //private static final float SOFTEN_POWER = 1.5f;

    // how close your view must be to the sun direction (cosine): 0.996≈5°, 0.985≈10°
    private static final double SUN_DOT_THRESHOLD = 0.992;

    // long range for checking unobstructed sky
    private static final double SKY_CHECK_RANGE = 1000.0D;

    // visual tuning
    private static final float SUN_SCALE = 2.5f;    // how large the glow is when looking at sun
    private static final float BLOCK_SCALE = 1.5f;  // how large the glow is when looking at a block source

    private static float currentIntensity = 0f;

    // which source produced the intensity last tick (affects scaling)
    private static boolean lastWasSun = false;

    // client-side send cooldown to avoid flooding the server
    private static long clientLastSent = 0L;
    private static final long CLIENT_SEND_COOLDOWN_MS = 600L; // send at most ~ once per 0.6s
    private static final float DAMAGE_TRIGGER_INTENSITY = 0.6f; // threshold to trigger a request

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (mc.level == null || player == null) {
            currentIntensity = 0f;
            lastWasSun = false;
            return;
        }

        MobEffectInstance hangover = player.getEffect(ModEffects.HANGOVER.get());
        if (hangover == null) {
            currentIntensity = 0f;
            lastWasSun = false;
            return;
        }

        int amp = hangover.getAmplifier();

        // ---- block light check ----
        float blockIntensity = 0f;
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F).normalize();
        Vec3 end = eye.add(look.scale(BLOCK_RANGE));
        ClipContext ctx = new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
        HitResult hit = mc.level.clip(ctx);

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult bhr = (BlockHitResult) hit;
            BlockPos pos = bhr.getBlockPos();
            int emittedLight = mc.level.getBlockState(pos).getLightEmission(mc.level, pos);
            if (emittedLight > LIGHT_THRESHOLD) {
                float norm = (float) (emittedLight - LIGHT_THRESHOLD) / (15 - LIGHT_THRESHOLD); // increments of .2 up to 1 (with threshold of 10)
                //norm = Mth.clamp(norm, 0f, 1f);
                float ampScale = 0.5f + amp * 0.25f;
                //blockIntensity = (float) Math.pow(norm * ampScale, SOFTEN_POWER);
                blockIntensity = norm * ampScale;
                blockIntensity = Mth.clamp(blockIntensity, 0f, 1f);
            }
        }

        // ---- sun light check ----
        float sunIntensity = 0f;

        // only in sky-lit dimensions and when daytime and not storming
        if (mc.level.dimensionType().hasSkyLight() && mc.level.isDay() && !mc.level.isThundering()) {
            // first: ensure player's view to sky is unobstructed
            Vec3 skyEnd = eye.add(look.scale(SKY_CHECK_RANGE));
            ClipContext skyCtx = new ClipContext(eye, skyEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
            HitResult skyHit = mc.level.clip(skyCtx);
            boolean skyVisible = skyHit.getType() == HitResult.Type.MISS;

            if (skyVisible) {
                // get vanilla sun angle and shift it like vanilla rendering often does
                float sunAngle = mc.level.getSunAngle(1.0F);
                float shifted = sunAngle + (float) Math.PI / 2.0F;

                // generate several plausible sun direction vectors (different axis conventions)
                Vec3 candidateA = new Vec3(Math.cos(shifted), Math.sin(shifted), 0.0).normalize();           // X,Y plane
                Vec3 candidateB = new Vec3(0.0, Math.sin(shifted), -Math.cos(shifted)).normalize();         // Y,Z plane (east/west on Z)
                Vec3 candidateC = new Vec3(Math.cos(shifted), 0.0, -Math.sin(shifted)).normalize();         // X,Z variant

                double dotA = look.dot(candidateA);
                double dotB = look.dot(candidateB);
                double dotC = look.dot(candidateC);

                double bestDot = Math.max(Math.max(dotA, dotB), dotC);

                if (bestDot > SUN_DOT_THRESHOLD) {
                    float ampScale = 0.8f + amp * 0.1f;
                    float closeness = (float) ((bestDot - SUN_DOT_THRESHOLD) / (1.0 - SUN_DOT_THRESHOLD));
                    closeness = Mth.clamp(closeness, 0f, 1f);
                    sunIntensity = closeness * ampScale;
                    sunIntensity = Mth.clamp(sunIntensity, 0f, 1f);
                }
            } // end skyVisible
        } // end sky-capable dimension/day

        // decide source and intensity
        if (sunIntensity > blockIntensity && sunIntensity > 0.001f) {
            lastWasSun = true;
            currentIntensity = lerp(currentIntensity, sunIntensity, 0.35f);
        } else if (blockIntensity > 0.001f) {
            lastWasSun = false;
            currentIntensity = lerp(currentIntensity, blockIntensity, 0.35f);
        } else {
            // neither source
            lastWasSun = false;
            currentIntensity = lerp(currentIntensity, 0f, 0.35f);
        }

        // only attempt to request damage if amplifier >= 2 and intensity high enough
        if (amp >= 2) {
            float effective = Math.max(blockIntensity, sunIntensity);
            if (effective >= DAMAGE_TRIGGER_INTENSITY) {
                long now = System.currentTimeMillis();
                if (now - clientLastSent >= CLIENT_SEND_COOLDOWN_MS) {
                    clientLastSent = now;
                    // send request to server
                    net.tadacko.tadackosdrinks.network.ModNetwork.CHANNEL.sendToServer(
                            new net.tadacko.tadackosdrinks.network.RequestLightDamagePacket()
                    );
                }
            }
        }
    }

    private static final ResourceLocation GLOW_TEXTURE =
            new ResourceLocation(TadackosDrinks.MOD_ID, "textures/misc/hangover_light_effect.png");

    // ----------------------- overlay renderer -----------------------
    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        // draw after vignette so glare appears above dark edges
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.VIGNETTE.id())) return;
        if (currentIntensity <= 0.001f) return;

        Minecraft mc = Minecraft.getInstance();
        GuiGraphics guiGraphics = event.getGuiGraphics();

        // screen size
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        // final alpha from intensity (clamped)
        float alphaF = Math.min(1f, currentIntensity);

        // texture size (the PNG we added). Keep this in sync with the actual file.
        final int texW = 256;
        final int texH = 256;

        // choose scale depending on source (sun = large, block = smaller)
        float baseScale = lastWasSun ? SUN_SCALE : BLOCK_SCALE;

        // scale the texture so it covers the screen (square texture -> scale uniformly)
        int drawSize = (int) (Math.max(sw, sh) * baseScale);
        int x = (sw - drawSize) / 2;
        int y = (sh - drawSize) / 2;

        // setup GL state
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // GuiGraphics.blit binds the shader/texture itself; setColor() supplies the tint/alpha
        guiGraphics.setColor(1f, 1f, 1f, alphaF);
        guiGraphics.blit(GLOW_TEXTURE, x, y, drawSize, drawSize, 0, 0, texW, texH, texW, texH);
        guiGraphics.setColor(1f, 1f, 1f, 1f);

        // reset GL state
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}