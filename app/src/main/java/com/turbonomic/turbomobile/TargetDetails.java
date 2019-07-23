package com.turbonomic.turbomobile;

public class TargetDetails {
    private String uuid;
    private String displayName;
    private String type;

    public TargetDetails(String uuid, String displayName, String type) {
        this.uuid = uuid;
        this.displayName = displayName;
        this.type = type;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
