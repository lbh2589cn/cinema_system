package com.cinema.system.common.enums;

public enum UserRoleEnum {
    USER("USER", "User"),
    ADMIN("ADMIN", "Admin");

    private final String code;
    private final String desc;

    UserRoleEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }
}
