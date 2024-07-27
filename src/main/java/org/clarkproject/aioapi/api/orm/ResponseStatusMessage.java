package org.clarkproject.aioapi.api.orm;

public enum ResponseStatusMessage {
    SUCCESS("success"),
    ERROR("error");
    private String value;
    private ResponseStatusMessage(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
