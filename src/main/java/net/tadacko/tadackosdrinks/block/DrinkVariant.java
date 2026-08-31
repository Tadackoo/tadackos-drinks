package net.tadacko.tadackosdrinks.block;

import net.minecraft.util.StringRepresentable;

// used for rendering and abv
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
    RUM("rum"),
    RUM_AGED("rum_aged"),
    SHOT_EMPTY("shot_empty"),
    VODKA("vodka"),
    GIN("gin"),
    TEQUILA("tequila"),
    TEQUILA_AGED("tequila_aged");

    private final String name;
    DrinkVariant(String name) { this.name = name; }
    @Override
    public String getSerializedName() { return name; }
}
