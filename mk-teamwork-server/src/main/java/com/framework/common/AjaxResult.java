package com.framework.common;

import com.framework.common.exception.CustomException;
import com.framework.common.utils.StringUtils;

import java.util.HashMap;

public class AjaxResult extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    public static final String CODE_TAG = "code";

    public static final String MSG_TAG = "msg";

    public static final String DATA_TAG = "data";

    /**
     * status type
     */
    public enum Type {

        /**
         * success
         */
        SUCCESS(200),
        /**
         *warn
         */
        WARN(201),
        /**
         * mistake
         */
        ERROR(500);
        private final int value;

        Type(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }

    /**
     * Initializes a newly created AjaxResult object to represent an empty message.
     */
    public AjaxResult() {
    }

    /**
     * Initialize a newly created AjaxResult object
     *
     * @param type status type
     * @param msg return content
     */
    public AjaxResult(Type type, String msg) {
        super.put(CODE_TAG, type.value);
        super.put(MSG_TAG, msg);
    }

    /**
     * Initialize a newly created AjaxResult object
     *
     * @param type status type
     * @param msg return content
     * @param data data object
     */
    public AjaxResult(Type type, String msg, Object data) {
        super.put(CODE_TAG, type.value);
        super.put(MSG_TAG, msg);
        if (StringUtils.isNotNull(data)) {
            super.put(DATA_TAG, data);
        }
    }

    /**
     * return success message
     *
     * @return success message
     */
    public static AjaxResult success() {
        return AjaxResult.success("");
    }

    /**
     * return success data
     *
     * @return success message
     */
    public static AjaxResult success(Object data) {
        return AjaxResult.success("", data);
    }

    /**
     * return success message
     *
     * @param msg return content
     * @return success message
     */
    public static AjaxResult success(String msg) {
        return AjaxResult.success(msg, null);
    }

    /**
     * return success message
     *
     * @param msg return content
     * @param data data object
     * @return success message
     */
    public static AjaxResult success(String msg, Object data) {
        return new AjaxResult(Type.SUCCESS, msg, data);
    }

    /**
     * return warning message
     *
     * @param msg return content
     * @return warning message
     */
    public static AjaxResult warn(String msg) {
        return AjaxResult.warn(msg, null);
    }

    /**
     * return warning message
     *
     * @param msg return content
     * @param data data object
     * @return warning message
     */
    public static AjaxResult warn(String msg, Object data) {
        return new AjaxResult(Type.WARN, msg, data);
    }

    /**
     * return error message
     *
     * @return
     */
    public static AjaxResult error() {
        return AjaxResult.error("操作失败");
    }

    /**
     * return error message
     *
     * @param msg return content
     * @return warning message
     */
    public static AjaxResult error(String msg) {
        return AjaxResult.error(msg, null);
    }

    /**
     * return error message
     *
     * @param msg return content
     * @param data data object
     * @return warning message
     */
    public static AjaxResult error(String msg, Object data) {
        return new AjaxResult(Type.ERROR, msg, data);
    }

    /**
     * Return exception information constructor
     *
     * @param resultCode custom exception
     */
    public AjaxResult(ResultCode resultCode){
        super.put(CODE_TAG, resultCode.getCode());
        super.put(MSG_TAG, resultCode.getMsg());
    }


    /**
     * Return exception information constructor (custom msg)
     *
     * @param resultCode custom exception
     */
    public AjaxResult(ResultCode resultCode, String msg){
        super.put(CODE_TAG, resultCode.getCode());
        super.put(MSG_TAG, msg);
    }
}


