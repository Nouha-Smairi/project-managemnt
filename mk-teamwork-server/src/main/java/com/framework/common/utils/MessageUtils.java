package com.framework.common.utils;

import com.framework.common.utils.spring.SpringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Get the i18n resource file
 * 
 */
public class MessageUtils
{
    /**
     * According to the message key and parameters, get the message and delegate it to spring messageSource
     *
     * @param code message key
     * @param args parameters
     * @return Get the internationalized translation value
     */
    public static String message(String code, Object... args)
    {
        MessageSource messageSource = SpringUtils.getBean(MessageSource.class);
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
