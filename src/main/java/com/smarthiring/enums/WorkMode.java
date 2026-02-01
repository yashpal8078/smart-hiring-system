package com.smarthiring.enums;

public enum WorkMode {
    ONSITE("On-site"),
    REMOTE("Remote"),
    HYBRID("Hybrid");

    private final String displayName;

    WorkMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}