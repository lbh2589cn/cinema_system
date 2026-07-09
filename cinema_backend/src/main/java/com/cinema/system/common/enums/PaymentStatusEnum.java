package com.cinema.system.common.enums;

public enum PaymentStatusEnum {
    PENDING("PENDING", "Pending Payment"),
    SUCCESS("SUCCESS", "Payment Successful"),
    FAILED("FAILED", "Payment Failed"),
    REFUNDED("REFUNDED", "Refunded");

    private final String code;
    private final String desc;

    PaymentStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }
}
