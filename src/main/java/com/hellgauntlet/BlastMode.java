package com.hellgauntlet;

/** Modes for the normal orb. Shift + right click cycles through them in this order. */
public enum BlastMode {
    SMALL("Small sphere", 3, true, 3),
    ENTITY_ONLY("Entity only", 0, false, 5), // radius is worked out from the entity that gets hit
    MEDIUM("Medium sphere", 5, true, 5),
    LARGE("Large sphere", 7, true, 8);

    public final String displayName;
    public final int radius;
    public final boolean deleteBlocks;
    /** Durability used up per shot. */
    public final int durabilityCost;

    BlastMode(String displayName, int radius, boolean deleteBlocks, int durabilityCost) {
        this.displayName = displayName;
        this.radius = radius;
        this.deleteBlocks = deleteBlocks;
        this.durabilityCost = durabilityCost;
    }
}
