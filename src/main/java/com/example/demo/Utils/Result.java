package com.example.demo.Utils;

public class Result {
    private Integer code;
    private String message;
    private Object data;

    public static Result sucess(Object o)
    {
        return new Result(RespBeanEnum.SUCESS.getCode(),RespBeanEnum.SUCESS.getMessage(),o);
    }
    public static Result sucess()
    {
        return new Result(RespBeanEnum.SUCESS.getCode(),RespBeanEnum.SUCESS.getMessage(),null);
    }

    public static Result error()
    {
        return new Result(RespBeanEnum.ERROR.getCode(),RespBeanEnum.ERROR.getMessage(), null);
    }
    public static Result error(Object o)
    {
        return new Result(RespBeanEnum.ERROR.getCode(),RespBeanEnum.ERROR.getMessage(),o);
    }






    public Result()
    {
        super();
    }

    public Result(Integer code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
