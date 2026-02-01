package com.smarthiring.enums;

public enum ShortlistStage {
    INITIAL("Initial Screening"),
    TECHNICAL("Technical Round"),
    HR("HR Round"),
    FINAL("Final Round");

    private final String displayName;

    ShortlistStage(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}