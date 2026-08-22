package net.tadacko.tadackosdrinks.block;

import net.minecraft.util.StringRepresentable;

public enum DrinkVariant implements StringRepresentable {
    BEER_EMPTY("beer_empty"),
    BEER("beer"),
    WINE_EMPTY("wine_empty"),
    WINE_RED("wine_red"),
    WINE_ROSE("wine_rose"),
    WINE_ORANGE("wine_orange"),
    WINE_WHITE("wine_white"),
    CIDER("cider"),
    MEAD("mead"),
    WHISKY_EMPTY("whisky_empty"),
    WHISKY("whisky"),
    BRANDY_EMPTY("brandy_empty"),
    BRANDY("brandy"),
    RUM_LIGHT("rum_light"),
    RUM("rum"),
    SHOT_EMPTY("shot_empty"),
    SHOT("shot"),
    TEQUILA("tequila");

    private final String name;
    DrinkVariant(String name) { this.name = name; }
    @Override
    public String getSerializedName() { return name; }
}
