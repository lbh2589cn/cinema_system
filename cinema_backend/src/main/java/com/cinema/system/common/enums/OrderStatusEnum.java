package com.cinema.system.common.enums;

public enum OrderStatusEnum {
    PENDING("PENDING", "Pending Payment"),
    PAID("PAID", "Paid"),
    REFUNDING("REFUNDING", "Refunding"),
    REFUNDED("REFUNDED", "Refunded"),
    CANCELLED("CANCELLED", "Cancelled");

    private final String code;
    private final String desc;

    OrderStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }
}
