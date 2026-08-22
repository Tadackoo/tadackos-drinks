package net.tadacko.tadackosdrinks.block;

import net.minecraft.util.StringRepresentable;

public enum ColumnStillPart implements StringRepresentable {
    BOTTOM("bottom"),
    MIDDLE("middle"),
    TOP("top");

    private final String serializedName;

    ColumnStillPart(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}