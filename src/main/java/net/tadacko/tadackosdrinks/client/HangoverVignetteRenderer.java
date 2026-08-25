package net.tadacko.tadackosdrinks.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tadacko.tadackosdrinks.effect.ModEffects;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.HashSet;
import java.util.Set;

/**
 * Vignette handler — whitelist triggers, instant-on linear fade-out.
 * Contains debug logging to identify whether triggers/rendering run.
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HangoverVignetteRenderer {
    private static final float MAX_VIGNETTE_ALPHA = 1f;
    private static final float MAX_HEARING_DISTANCE = 32f;
    private static final int DECAY_TICKS = 100;
    // client-side send cooldown to avoid spamming the server
    private static long clientLastSent = 0L;
    private static final long CLIENT_SEND_COOLDOWN_MS = 250L; // ~0.25s
    private static final float SOUND_DAMAGE_THRESHOLD = 0.3f;  // client intensity threshold

    private static final Set<ResourceLocation> SOUND_WHITELIST = new HashSet<>();
    static {
        SOUND_WHITELIST.add(new ResourceLocation("entity.generic.explode"));
        SOUND_WHITELIST.add(new ResourceLocation("entity.ghast.shoot"));
        SOUND_WHITELIST.add(new ResourceLocation("entity.blaze.shoot"));
        SOUND_WHITELIST.add(new ResourceLocation("entity.wither.shoot"));
        SOUND_WHITELIST.add(new ResourceLocation("entity.wither.spawn"));
        SOUND_WHITELIST.add(new ResourceLocation("entity.ender_dragon.growl"));
        SOUND_WHITELIST.add(new ResourceLocation("entity.ender_dragon.death"));
        SOUND_WHITELIST.add(new ResourceLocation("block.anvil.land"));
        SOUND_WHITELIST.add(new ResourceLocation("block.anvil.place"));
        SOUND_WHITELIST.add(new ResourceLocation("block.anvil.use"));
        SOUND_WHITELIST.add(new ResourceLocation("block.anvil.destroy"));
        SOUND_WHITELIST.add(new ResourceLocation("entity.firework_rocket.blast"));
        SOUND_WHITELIST.add(new ResourceLocation("entity.firework_rocket.large_blast"));
        SOUND_WHITELIST.add(new ResourceLocation("entity.firework_rocket.twinkle"));
        SOUND_WHITELIST.add(new ResourceLocation("ambient.weather.thunder"));
        SOUND_WHITELIST.add(new ResourceLocation("ambient.weather.lightning.impact"));
    }

    private static float lastIntensity = 0f;
    private static int ticksSinceLast = Integer.MAX_VALUE;

    private static final ResourceLocation VIGNETTE_TEX = new ResourceLocation("textures/misc/vignette.png");

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        LocalPlayer player = mc.player;

        if (!player.hasEffect(ModEffects.HANGOVER.get())) return;

        SoundInstance snd = event.getSound();
        if (snd == null) return;
        if (snd.isRelative()) return; // skip UI / non-positional

        ResourceLocation id = snd.getLocation();
        if (!SOUND_WHITELIST.contains(id)) return;

        Vec3 soundPos = new Vec3(snd.getX(), snd.getY(), snd.getZ());
        double dist = player.position().distanceTo(soundPos);
        if (dist > MAX_HEARING_DISTANCE) return;

        // intensity from proximity
        float intensity = Math.max(0f, 1f - (float)(dist / MAX_HEARING_DISTANCE));
        if (intensity <= 0f) return;

        // instant-on: set lastIntensity to the new (or stronger) value and reset counter
        lastIntensity = Math.max(lastIntensity, intensity);
        ticksSinceLast = 0;

        MobEffectInstance hang = player.getEffect(ModEffects.HANGOVER.get());
        int amp = (hang == null) ? 0 : hang.getAmplifier();
        if (amp >= 2 && lastIntensity >= SOUND_DAMAGE_THRESHOLD) {
            long now = System.currentTimeMillis();
            if (now - clientLastSent >= CLIENT_SEND_COOLDOWN_MS) {
                clientLastSent = now;
                net.tadacko.tadackosdrinks.network.ModNetwork.CHANNEL.sendToServer(new net.tadacko.tadackosdrinks.network.RequestSoundDamagePacket());
            }
        }
        //System.out.println("[VIGNETTE TRIGGER] id=" + id + " dist=" + dist + " intensity=" + intensity + " lastIntensity=" + lastIntensity);
    }

    @SubscribeEvent
    public static void onClientTick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        if (ticksSinceLast < Integer.MAX_VALUE) ticksSinceLast++;
        if (ticksSinceLast > DECAY_TICKS) lastIntensity = 0f;
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        // draw after vanilla vignette
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.VIGNETTE.id())) return;

        if (lastIntensity <= 0f) return;
        if (ticksSinceLast < 0) return;

        // linear time-based fade
        float progress = Math.min(1f, (float) ticksSinceLast / (float) DECAY_TICKS);
        float current = Math.max(0f, lastIntensity * (1f - progress));
        if (current <= 0.05f) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!mc.player.hasEffect(ModEffects.HANGOVER.get())) return;

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        float alpha = Math.min(1f, current) * MAX_VIGNETTE_ALPHA;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        // flush GuiGraphics' own buffered draws first so our raw quads render in the correct order
        guiGraphics.flush();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();

        // vanilla vignette blend: darken where texture is dark
        RenderSystem.blendFunc(GL11.GL_ZERO, GL11.GL_ONE_MINUS_SRC_COLOR);

        // IMPORTANT: multiply the texture RGB by 'alpha' so the darkening strength is controllable.
        // We set RGB = alpha, A = 1 so blend uses the scaled RGB values.
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, VIGNETTE_TEX);
        RenderSystem.setShaderColor(alpha, alpha, alpha, 1f);

        // draw full-screen textured quad (POSITION_TEX)
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        Matrix4f mat = guiGraphics.pose().last().pose();
        buf.vertex(mat, 0f, sh, 0f).uv(0f, 1f).endVertex();
        buf.vertex(mat, sw, sh, 0f).uv(1f, 1f).endVertex();
        buf.vertex(mat, sw, 0f, 0f).uv(1f, 0f).endVertex();
        buf.vertex(mat, 0f, 0f, 0f).uv(0f, 0f).endVertex();
        t.end();

        // second pass (makes it darker)
        buf.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buf.vertex(mat, 0f, sh, 0f).uv(0f, 1f).endVertex();
        buf.vertex(mat, sw, sh, 0f).uv(1f, 1f).endVertex();
        buf.vertex(mat, sw, 0f, 0f).uv(1f, 0f).endVertex();
        buf.vertex(mat, 0f, 0f, 0f).uv(0f, 0f).endVertex();
        t.end();

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }
}