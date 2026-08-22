package net.tadacko.tadackosdrinks.block;

import net.minecraft.util.StringRepresentable;

public enum BarrelState implements StringRepresentable {
    OPEN("open"),
    CLOSED("closed"),
    YEAST("yeast");

    private final String name;

    BarrelState(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}