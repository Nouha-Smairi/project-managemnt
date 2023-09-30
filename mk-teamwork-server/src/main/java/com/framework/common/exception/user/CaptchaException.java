package com.framework.common.exception.user;

/**
 * Verification code error exception class
 * 
 */
public class CaptchaException extends UserException
{
    private static final long serialVersionUID = 1L;

    public CaptchaException()
    {
        super("user.jcaptcha.error", null);
    }
}
