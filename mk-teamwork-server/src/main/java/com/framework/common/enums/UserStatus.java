package com.framework.common.enums;

/**
 * user status
 * 
 */
public enum UserStatus
{
    OK("1", "normal"), DISABLE("0", "disabled"), DELETED("2", "delete");

    private final String code;
    private final String info;

    UserStatus(String code, String info)
    {
        this.code = code;
        this.info = info;
    }

    public String getCode()
    {
        return code;
    }

    public String getInfo()
    {
        return info;
    }
}
