package com.framework.common.exception.user;


import com.framework.common.exception.BaseException;

/**
 * User information exception class
 * 
 */
public class UserException extends BaseException
{
    private static final long serialVersionUID = 1L;

    public UserException(String code, Object[] args)
    {
        super("user", code, args, null);
    }
}
