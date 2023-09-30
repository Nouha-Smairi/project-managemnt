package com.framework.common;

/**
 * status code
 */
public enum ResultCode {
    /*
    The request returns status code and description information
     */
    SUCCESS(200, "success"),
    BAD_REQUEST(400, "Bad or incomplete request parameters"),
    JSON_FORMAT_ERROR(400, "Malformed JSON"),
    UNAUTHORIZED(401, "Please authenticate first"),
    NOT_FOUND(404, "Resource does not exist!"),
    METHOD_NOT_ALLOWED(405, "The request method does not support"),
    NOT_ACCEPTABLE(406, "unacceptable request"),
    LENGTH_REQUIRED(411, "length limited"),
    UNSUPPORTED_MEDIA_TYPE(415, "unsupported media type"),
    REQUESTED_RANGE_NOT_SATISFIABLE(416, "Could not satisfy requested range"),
    INTERNAL_SERVER_ERROR(500, "The server is being upgraded, please be patient"),
    SERVICE_UNAVAILABLE(503, "请求超时"),
    MSG_EXCEPTION(500, "Default message exception"),

    VERIFY_TOKEN_FAIL(401, "Session expired, log in again!"),
    LOGIN_EXCEPTION(401, "Login failed!"),
    USER_ERROR_EXCEPTION(1001, "wrong user name or password!"),
    LOCKED_EXCEPTION(1002, "Account locked, please contact administrator!"),
    CREDENTIALS_EXPIRED_EXCEPTION(1003, "Password expired, please contact administrator!"),
    ACCOUNT_EXPIRED_EXCEPTION(1004, "Account expired, please contact administrator!"),
    DISABLED_EXCEPTION(1005, "Account disabled, please contact administrator!"),
    FORBIDDEN(403, "Unauthorized operation resource"),
    OPERATE_ERROR(405, "The operation failed, the resource requested for the operation does not exist"),
    TIME_OUT(408, "Request timed out"),

    EMAIL_USED(201, "Email has been used!"),
    MOBILE_USED(201, "Number already in use!"),
    CAPTCHA_EXPIRED(201, "Verification code has expired!"),

    SERVER_ERROR(500, "internal server error"),
    ;
    private int code;
    private String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
