package com.cinema.system.common.exception;

public class UnauthorizedException extends RuntimeException {
    private final int code;

    public UnauthorizedException(String message) {
        super(message);
        this.code = 401;
    }

    public int getCode() {
        return code;
    }
}
