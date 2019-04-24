package com.example.turbomobile;

public class SearchDetails {
    private String uuid;
    private String displayName;

    public SearchDetails(String uuid, String displayName) {
        this.uuid = uuid;
        this.displayName = displayName;
    }

    public String getUuid() {
        return uuid;
    }

    public String getDisplayName() {
        return displayName;
    }
}
