package com.example.demo.Utils;

public enum RespBeanEnum {
    SUCESS(200,"sucess"),
    ERROR(500,"error");
    private final Integer code;
    private final String message;
    private RespBeanEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
