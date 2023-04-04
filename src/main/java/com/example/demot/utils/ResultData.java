package com.example.demot.utils;


import java.io.Serializable;

/**
 * ResultData.
 *
 * @author LKRLKR
 */
public class ResultData implements Serializable {

    private Integer code;

    private Object data;

    private String msg;

    private Integer count;

    /**
     * @param code
     * @param data
     * @param msg
     * @param count
     */
    public ResultData(Integer code, Object data, String msg, Integer count) {
        super();
        this.code = code;
        this.data = data;
        this.msg = msg;
        this.count = count;
    }

    /**
     * @return the code
     */
    public Integer getCode() {
        return this.code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(Integer code) {
        this.code = code;
    }

    /**
     * @return the data
     */
    public Object getData() {
        return this.data;
    }

    /**
     * @param data the data to set
     */
    public void setData(Object data) {
        this.data = data;
    }

    /**
     * @return the msg
     */
    public String getMsg() {
        return this.msg;
    }

    /**
     * @param msg the msg to set
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * @return the count
     */
    public Integer getCount() {
        return this.count;
    }

    /**
     * @param count the count to set
     */
    public void setCount(Integer count) {
        this.count = count;
    }

}
