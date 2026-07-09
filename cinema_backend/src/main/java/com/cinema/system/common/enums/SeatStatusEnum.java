package com.cinema.system.common.enums;

public enum SeatStatusEnum {
    AVAILABLE("AVAILABLE", "Available"),
    LOCKED("LOCKED", "Locked"),
    BOOKED("BOOKED", "Booked");

    private final String code;
    private final String desc;

    SeatStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }
}
