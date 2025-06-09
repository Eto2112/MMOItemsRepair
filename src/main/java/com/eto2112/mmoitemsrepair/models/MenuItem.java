package com.eto2112.mmoitemsrepair.models;

import java.util.List;

public class MenuItem {

    private final String id;
    private final String material;
    private final String displayName;
    private final List<String> lore;
    private final List<Integer> slots;

    public MenuItem(String id, String material, String displayName, List<String> lore, List<Integer> slots) {
        this.id = id;
        this.material = material;
        this.displayName = displayName;
        this.lore = lore;
        this.slots = slots;
    }

    public String getId() {
        return id;
    }

    public String getMaterial() {
        return material;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public List<Integer> getSlots() {
        return slots;
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "id='" + id + '\'' +
                ", material='" + material + '\'' +
                ", displayName='" + displayName + '\'' +
                ", lore=" + lore +
                ", slots=" + slots +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        MenuItem menuItem = (MenuItem) obj;
        return id.equals(menuItem.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}