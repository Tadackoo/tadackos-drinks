package net.tadacko.tadackosdrinks.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.block.DrinkVariant;
import net.minecraft.resources.ResourceLocation;

import java.util.EnumMap;
import java.util.Map;

public class DrinkRenderHelper {
    public static final float SHADE_TOP    = 1.0f;
    public static final float SHADE_BOTTOM = 0.5f;
    public static final float SHADE_NORTH  = 0.8f;
    public static final float SHADE_SOUTH  = 0.8f;
    public static final float SHADE_EAST   = 0.6f;
    public static final float SHADE_WEST   = 0.6f;

    private static final Map<DrinkVariant, ResourceLocation> FLUID_TEXTURES = new EnumMap<>(DrinkVariant.class);
    private static final Map<DrinkVariant, ResourceLocation> FOAM_TEXTURES = new EnumMap<>(DrinkVariant.class);

    static {
        FLUID_TEXTURES.put(DrinkVariant.BEER,         new ResourceLocation(TadackosDrinks.MOD_ID, "item/beer_texture"));
        FLUID_TEXTURES.put(DrinkVariant.WINE_RED,     new ResourceLocation(TadackosDrinks.MOD_ID, "item/wine_red_texture"));
        FLUID_TEXTURES.put(DrinkVariant.WINE_ROSE,    new ResourceLocation(TadackosDrinks.MOD_ID, "item/wine_rose_texture"));
        FLUID_TEXTURES.put(DrinkVariant.WINE_ORANGE,  new ResourceLocation(TadackosDrinks.MOD_ID, "item/wine_orange_texture"));
        FLUID_TEXTURES.put(DrinkVariant.WINE_WHITE,   new ResourceLocation(TadackosDrinks.MOD_ID, "item/wine_white_texture"));
        FLUID_TEXTURES.put(DrinkVariant.CIDER,        new ResourceLocation(TadackosDrinks.MOD_ID, "item/cider_texture"));
        FLUID_TEXTURES.put(DrinkVariant.MEAD,         new ResourceLocation(TadackosDrinks.MOD_ID, "item/mead_texture"));
        FLUID_TEXTURES.put(DrinkVariant.WHISKY,       new ResourceLocation(TadackosDrinks.MOD_ID, "item/whisky_texture"));
        FLUID_TEXTURES.put(DrinkVariant.BRANDY,       new ResourceLocation(TadackosDrinks.MOD_ID, "item/brandy_texture"));
        FLUID_TEXTURES.put(DrinkVariant.RUM,          new ResourceLocation(TadackosDrinks.MOD_ID, "item/spirit_texture"));
        FLUID_TEXTURES.put(DrinkVariant.RUM_AGED,     new ResourceLocation(TadackosDrinks.MOD_ID, "item/rum_texture"));
        FLUID_TEXTURES.put(DrinkVariant.VODKA,        new ResourceLocation(TadackosDrinks.MOD_ID, "item/spirit_texture"));
        FLUID_TEXTURES.put(DrinkVariant.GIN,          new ResourceLocation(TadackosDrinks.MOD_ID, "item/spirit_texture"));
        FLUID_TEXTURES.put(DrinkVariant.TEQUILA,      new ResourceLocation(TadackosDrinks.MOD_ID, "item/spirit_texture"));
        FLUID_TEXTURES.put(DrinkVariant.TEQUILA_AGED, new ResourceLocation(TadackosDrinks.MOD_ID, "item/tequila_texture"));
        // empty variants intentionally omitted — getFluidTexture returns null for them

        FOAM_TEXTURES.put(DrinkVariant.BEER,  new ResourceLocation(TadackosDrinks.MOD_ID, "item/foam"));
        FOAM_TEXTURES.put(DrinkVariant.CIDER, new ResourceLocation(TadackosDrinks.MOD_ID, "item/foam"));
    }

    public record Volume(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {}

    private static final Map<DrinkVariant, Volume> VOLUMES = new EnumMap<>(DrinkVariant.class);

    static {
        Volume beer = new Volume(6.5f, 0.5f, 6.5f, 9.5f, 5f, 9.5f);
        Volume wine = new Volume(6.5f, 4.5f, 6.5f, 9.5f, 6f, 9.5f);
        Volume whisky = new Volume(7f, 0.5f, 7f, 9f, 1.5f, 9f);
        Volume brandy = new Volume(6.5f, 2f, 6.5f, 9.5f, 2.5f, 9.5f);
        Volume shot = new Volume(7.5f, 0.5f, 7.5f, 8.5f, 2f, 8.5f);

        VOLUMES.put(DrinkVariant.BEER,         beer);
        VOLUMES.put(DrinkVariant.CIDER,        beer);
        VOLUMES.put(DrinkVariant.WINE_RED,     wine);
        VOLUMES.put(DrinkVariant.WINE_ROSE,    wine);
        VOLUMES.put(DrinkVariant.WINE_ORANGE,  wine);
        VOLUMES.put(DrinkVariant.WINE_WHITE,   wine);
        VOLUMES.put(DrinkVariant.MEAD,         wine);
        VOLUMES.put(DrinkVariant.WHISKY,       whisky);
        VOLUMES.put(DrinkVariant.BRANDY,       brandy);
        VOLUMES.put(DrinkVariant.RUM,          whisky);
        VOLUMES.put(DrinkVariant.RUM_AGED,     whisky);
        VOLUMES.put(DrinkVariant.VODKA,        shot);
        VOLUMES.put(DrinkVariant.GIN,          shot);
        VOLUMES.put(DrinkVariant.TEQUILA,      shot);
        VOLUMES.put(DrinkVariant.TEQUILA_AGED, shot);
        // empty variants intentionally omitted — getVolume returns null for them
    }

    /** Returns null for empty/no-fluid variants. */
    public static ResourceLocation getFluidTexture(DrinkVariant variant) { return FLUID_TEXTURES.get(variant); }

    /** Returns null for variants with no foam. */
    public static ResourceLocation getFoamTexture(DrinkVariant variant) { return FOAM_TEXTURES.get(variant); }

    /** Returns null for empty/no-fluid variants. */
    public static Volume getVolume(DrinkVariant variant) { return VOLUMES.get(variant); }

    /**
     * Renders the fluid box for the given volume using the provided sprite.
     */
    public static void renderFluid(VertexConsumer vc, PoseStack.Pose p,
                                   TextureAtlasSprite sprite, Volume vol,
                                   int light, int overlay) {
        float eps = 0.0015f;
        float x1 = (vol.minX() / 16f) + eps;
        float y1 = (vol.minY() / 16f) + eps;
        float z1 = (vol.minZ() / 16f) + eps;
        float x2 = (vol.maxX() / 16f) - eps;
        float y2 = (vol.maxY() / 16f) - eps;
        float z2 = (vol.maxZ() / 16f) - eps;

        float r = 1f, g = 1f, b = 1f, a = 1f;

        quadHorizontal(vc, p, sprite, x2, x1, z1, z2, y2,  0,  1,  0, r, g, b, a, light, overlay);
        quadHorizontal(vc, p, sprite, x2, x1, z2, z1, y1,  0, -1,  0, r, g, b, a, light, overlay);
        quadVertical  (vc, p, sprite, x2, z1, x1, z1, y1, y2,  0,  0, -1, r, g, b, a, light, overlay);
        quadVertical  (vc, p, sprite, x1, z2, x2, z2, y1, y2,  0,  0,  1, r, g, b, a, light, overlay);
        quadVertical  (vc, p, sprite, x1, z1, x1, z2, y1, y2, -1,  0,  0, r, g, b, a, light, overlay);
        quadVertical  (vc, p, sprite, x2, z2, x2, z1, y1, y2,  1,  0,  0, r, g, b, a, light, overlay);
    }

    /**
     * Renders the beer/cider foam geometry using the provided sprite.
     */
    public static void renderFoam(VertexConsumer vc, PoseStack.Pose p,
                                  TextureAtlasSprite s, int light, int overlay) {
        float r = 1f, g = 1f, b = 1f, a = 1f;
        renderBox(vc, p, s, 6.5f, 5f,   6.5f, 9.5f, 6.5f, 9.5f, r, g, b, a, light, overlay); // main foam disc
        renderBox(vc, p, s, 7f,   6.5f, 7f,   9f,   7f,   9f,   r, g, b, a, light, overlay); // foam top cap
        renderBox(vc, p, s, 7f,   5f,   6f,   9f,   6f,   6.5f, r, g, b, a, light, overlay); // front spill
        renderBox(vc, p, s, 7f,   5f,   9.5f, 9f,   6f,   10f,  r, g, b, a, light, overlay); // back spill
        renderBox(vc, p, s, 6f,   5f,   7f,   6.5f, 6f,   9f,   r, g, b, a, light, overlay); // left spill
        renderBox(vc, p, s, 9.5f, 5f,   7f,   10f,  6f,   9f,   r, g, b, a, light, overlay); // right spill
    }

    public static void renderBox(VertexConsumer vc, PoseStack.Pose p, TextureAtlasSprite s,
                                 float fromX, float fromY, float fromZ,
                                 float toX,   float toY,   float toZ,
                                 float r, float g, float b, float a,
                                 int light, int overlay) {
        float x1 = fromX / 16f, y1 = fromY / 16f, z1 = fromZ / 16f;
        float x2 = toX   / 16f, y2 = toY   / 16f, z2 = toZ   / 16f;

        quadHorizontal(vc, p, s, x2, x1, z1, z2, y2,  0,  1,  0, r*SHADE_TOP,    g*SHADE_TOP,    b*SHADE_TOP,    a, light, overlay);
        quadHorizontal(vc, p, s, x2, x1, z2, z1, y1,  0, -1,  0, r*SHADE_BOTTOM, g*SHADE_BOTTOM, b*SHADE_BOTTOM, a, light, overlay);
        quadVertical  (vc, p, s, x2, z1, x1, z1, y1, y2,  0,  0, -1, r*SHADE_NORTH,  g*SHADE_NORTH,  b*SHADE_NORTH,  a, light, overlay);
        quadVertical  (vc, p, s, x1, z2, x2, z2, y1, y2,  0,  0,  1, r*SHADE_SOUTH,  g*SHADE_SOUTH,  b*SHADE_SOUTH,  a, light, overlay);
        quadVertical  (vc, p, s, x1, z1, x1, z2, y1, y2, -1,  0,  0, r*SHADE_WEST,   g*SHADE_WEST,   b*SHADE_WEST,   a, light, overlay);
        quadVertical  (vc, p, s, x2, z2, x2, z1, y1, y2,  1,  0,  0, r*SHADE_EAST,   g*SHADE_EAST,   b*SHADE_EAST,   a, light, overlay);
    }

    public static void quadHorizontal(
            VertexConsumer vc, PoseStack.Pose p, TextureAtlasSprite s,
            float x1, float x2, float z1, float z2, float y,
            int nx, int ny, int nz,
            float r, float g, float b, float a,
            int light, int overlay
    ) {
        vc.vertex(p.pose(), x1, y, z1).color(r,g,b,a).uv(s.getU0(),s.getV0()).overlayCoords(overlay).uv2(light).normal(p.normal(),nx,ny,nz).endVertex();
        vc.vertex(p.pose(), x2, y, z1).color(r,g,b,a).uv(s.getU1(),s.getV0()).overlayCoords(overlay).uv2(light).normal(p.normal(),nx,ny,nz).endVertex();
        vc.vertex(p.pose(), x2, y, z2).color(r,g,b,a).uv(s.getU1(),s.getV1()).overlayCoords(overlay).uv2(light).normal(p.normal(),nx,ny,nz).endVertex();
        vc.vertex(p.pose(), x1, y, z2).color(r,g,b,a).uv(s.getU0(),s.getV1()).overlayCoords(overlay).uv2(light).normal(p.normal(),nx,ny,nz).endVertex();
    }

    public static void quadVertical(
            VertexConsumer vc, PoseStack.Pose p, TextureAtlasSprite s,
            float x1, float z1, float x2, float z2, float y1, float y2,
            int nx, int ny, int nz,
            float r, float g, float b, float a,
            int light, int overlay
    ) {
        vc.vertex(p.pose(), x1, y1, z1).color(r,g,b,a).uv(s.getU0(),s.getV1()).overlayCoords(overlay).uv2(light).normal(p.normal(),nx,ny,nz).endVertex();
        vc.vertex(p.pose(), x2, y1, z2).color(r,g,b,a).uv(s.getU1(),s.getV1()).overlayCoords(overlay).uv2(light).normal(p.normal(),nx,ny,nz).endVertex();
        vc.vertex(p.pose(), x2, y2, z2).color(r,g,b,a).uv(s.getU1(),s.getV0()).overlayCoords(overlay).uv2(light).normal(p.normal(),nx,ny,nz).endVertex();
        vc.vertex(p.pose(), x1, y2, z1).color(r,g,b,a).uv(s.getU0(),s.getV0()).overlayCoords(overlay).uv2(light).normal(p.normal(),nx,ny,nz).endVertex();
    }
}