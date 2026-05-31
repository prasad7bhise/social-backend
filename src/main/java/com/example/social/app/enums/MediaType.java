package com.example.social.app.enums;

public enum MediaType {
    IMAGE(1),
    VIDEO(2);

    private final int code;

    MediaType(int code) {
        this.code = code;
    }

    public static MediaType fromCode(int code) {
        for (MediaType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid MediaType code: " + code);
    }

    public int getCode() {
        return code;
    }
}
