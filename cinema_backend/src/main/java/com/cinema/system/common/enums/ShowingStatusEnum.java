package com.cinema.system.common.enums;

public enum ShowingStatusEnum {
    SCHEDULED("SCHEDULED", "已排片"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String desc;

    ShowingStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }
}
