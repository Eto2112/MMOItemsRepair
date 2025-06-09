package com.eto2112.mmoitemsrepair.models;

import java.util.Map;

public class RepairMenu {

    private final String title;
    private final int size;
    private final int itemToRepairSlot;
    private final int repairMaterialsSlot;
    private final int repairedPreviewSlot;
    private final int repairButtonSlot;
    private final Map<String, MenuItem> menuItems;

    public RepairMenu(String title, int size, int itemToRepairSlot, int repairMaterialsSlot,
                      int repairedPreviewSlot, int repairButtonSlot, Map<String, MenuItem> menuItems) {
        this.title = title;
        this.size = size;
        this.itemToRepairSlot = itemToRepairSlot;
        this.repairMaterialsSlot = repairMaterialsSlot;
        this.repairedPreviewSlot = repairedPreviewSlot;
        this.repairButtonSlot = repairButtonSlot;
        this.menuItems = menuItems;
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        return size;
    }

    public int getItemToRepairSlot() {
        return itemToRepairSlot;
    }

    public int getRepairMaterialsSlot() {
        return repairMaterialsSlot;
    }

    public int getRepairedPreviewSlot() {
        return repairedPreviewSlot;
    }

    public int getRepairButtonSlot() {
        return repairButtonSlot;
    }

    public Map<String, MenuItem> getMenuItems() {
        return menuItems;
    }

    public MenuItem getMenuItem(String id) {
        return menuItems.get(id);
    }

    public boolean hasMenuItem(String id) {
        return menuItems.containsKey(id);
    }

    @Override
    public String toString() {
        return "RepairMenu{" +
                "title='" + title + '\'' +
                ", size=" + size +
                ", itemToRepairSlot=" + itemToRepairSlot +
                ", repairMaterialsSlot=" + repairMaterialsSlot +
                ", repairedPreviewSlot=" + repairedPreviewSlot +
                ", repairButtonSlot=" + repairButtonSlot +
                ", menuItems=" + menuItems +
                '}';
    }
}