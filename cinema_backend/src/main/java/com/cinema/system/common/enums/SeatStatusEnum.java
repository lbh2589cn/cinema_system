package com.cinema.system.common.enums;

public enum SeatStatusEnum {
    AVAILABLE("AVAILABLE", "可选"),
    LOCKED("LOCKED", "已锁定"),
    BOOKED("BOOKED", "已预订");

    private final String code;
    private final String desc;

    SeatStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }
}
