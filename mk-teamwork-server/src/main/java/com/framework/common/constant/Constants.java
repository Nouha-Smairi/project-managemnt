package com.framework.common.constant;

/**
 * Common Constant Information
 *
 */
public class Constants
{
    /**
     * UTF-8 character set
     */
    public static final String UTF8 = "UTF-8";

    /**
     * Universal Success Indicator
     */
    public static final String SUCCESS = "0";

    /**
     * Generic failure flag
     */
    public static final String FAIL = "1";

    /**
     * login successful
     */
    public static final String LOGIN_SUCCESS = "Success";

    /**
     * log out
     */
    public static final String LOGOUT = "Logout";

    /**
     * Login failed
     */
    public static final String LOGIN_FAIL = "Error";

    /**
     * verification code redis key
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /**
     * login user redis key
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * Verification code validity period (minutes)
     */
    public static final Integer CAPTCHA_EXPIRATION = 2;

    /**
     * token
     */
    public static final String ORGCODE = "organizationcode";

    /**
     * token
     */
    public static final String TOKEN = "Authorization";

    /**
     * token prefix
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * token prefix
     */
    public static final String LOGIN_USER_KEY = "login_user_key";

    /**
     * User ID
     */
    public static final String JWT_USERID = "userid";

    /**
     * profile picture
     */
    public static final String JWT_AVATAR = "avatar";

    /**
     * creation time
     */
    public static final String JWT_CREATED = "created";

    /**
     * User rights
     */
    public static final String JWT_AUTHORITIES = "authorities";

    /**
     * Resource Mapping Path Prefix
     */
    public static final String RESOURCE_PREFIX = "/profile";

    //------- Business constant configuration ------

    /**
     * Task Priority - Very Urgent
     */
    public static final String VERY_URGENT_STR = "very urgent";
    public static final Integer VERY_URGENT = 2;

    /**
     * Task Priority - Urgent
     */
    public static final String URGENT_STR = "urgent";
    public static final Integer URGENT = 1;

    /**
     * Task Priority - Normal
     */
    public static final String GENERAL_STR = "ordinary";
    public static final Integer GENERAL = 0;

    /**
     * Message notification type--information
     */
    public static final String MESSAGE = "message";
    /**
     * Message notification type--notification
     */
    public static final String NOTICE = "notice";
    /**
     *Message notification type--task
     */
    public static final String TASK = "task";
    /**
     * Project task flow rule prefix
     */
    public static final String PROJECTRULE = "projectrule";
    /**
    * WebSocket prefix

    */
    public static final String WEBSOCKET = "webSocket";
}
