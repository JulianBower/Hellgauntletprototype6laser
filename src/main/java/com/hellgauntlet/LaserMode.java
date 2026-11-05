package com.hellgauntlet;

/** Modes for laser mode (toggled with X). Shift + right click cycles through them. Add more here later. */
public enum LaserMode {
    SINGLE("Laser", 4, 10, false),
    CONTINUOUS("Continuous laser", 4, 10, true);

    public final String displayName;
    /** Radius of the ball at the end of the beam (for the single laser the pulses grow from this). */
    public final int ballRadius;
    /** Durability used per shot, or per 2.5 seconds while the continuous beam is held. */
    public final int durabilityCost;
    public final boolean continuous;

    LaserMode(String displayName, int ballRadius, int durabilityCost, boolean continuous) {
        this.displayName = displayName;
        this.ballRadius = ballRadius;
        this.durabilityCost = durabilityCost;
        this.continuous = continuous;
    }
}
