package com.framework.common.utils.sql;

import com.framework.common.utils.StringUtils;

/**
 * sql Operation tools
 * 
 * @author ruoyi
 */
public class SqlUtil
{
    /**
     * Only letters, numbers, underscores, spaces, commas are supported (multiple field sorting is supported)
     */
    public static String SQL_PATTERN = "[a-zA-Z0-9_\\ \\,]+";

    /**
     * Check characters to prevent injection bypass
     */
    public static String escapeOrderBySql(String value)
    {
        if (StringUtils.isNotEmpty(value) && !isValidOrderBySql(value))
        {
            return StringUtils.EMPTY;
        }
        return value;
    }

    /**
     * Verify that the order by syntax conforms to the specification
     */
    public static boolean isValidOrderBySql(String value)
    {
        return value.matches(SQL_PATTERN);
    }
}
