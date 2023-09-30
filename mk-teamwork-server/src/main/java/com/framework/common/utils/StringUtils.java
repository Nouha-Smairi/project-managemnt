package com.framework.common.utils;

import com.framework.common.core.text.StrFormatter;

import java.util.*;

/**
 * String tool class
 * 
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils
{
    /** empty string */
    private static final String NULLSTR = "";

    /** underline */
    private static final char SEPARATOR = '_';

    /**
    * get parameter is not empty
     *
     * @param value defaultValue The value to be judged
     * @return value return value
     */
    public static <T> T nvl(T value, T defaultValue)
    {
        return value != null ? value : defaultValue;
    }

    /**
     * * Determine whether a Collection is empty, including List, Set, Queue
     *
     * @param coll Collection to be judged
     * @return true: empty false: not empty
     */
    public static boolean isEmpty(Collection<?> coll)
    {
        return isNull(coll) || coll.isEmpty();
    }

    /**
     * * Determine whether a Collection is not empty, including List, Set, Queue
     *
     * @param coll Collection to be judged
     * @return true: not empty false: empty
     */
    public static boolean isNotEmpty(Collection<?> coll)
    {
        return !isEmpty(coll);
    }

    /**
     ** Check if an array of objects is empty
     *
     * @param objects Array of objects to be judged
     ** @return true: empty false: not empty
     */
    public static boolean isEmpty(Object[] objects)
    {
        return isNull(objects) || (objects.length == 0);
    }

    /**
     * * Determine whether an object array is not empty
     *
     * @param objects Array of objects to be judged
     * @return true: not empty false: empty
     */
    public static boolean isNotEmpty(Object[] objects)
    {
        return !isEmpty(objects);
    }

    /**
     * * Determine whether a Map is empty
     *
     * @param map The Map to be judged
     * @return true: empty false: not empty
     */
    public static boolean isEmpty(Map<?, ?> map)
    {
        return isNull(map) || map.isEmpty();
    }

    /**
     * * Determine whether a Map is empty
     *
     * @param map The Map to be judged
     * @return true: not empty false: empty
     */
    public static boolean isNotEmpty(Map<?, ?> map)
    {
        return !isEmpty(map);
    }

    /**
     * * Check if a string is empty
     *
     * @param str String
     * @return true: empty false: not empty
     */
    public static boolean isEmpty(String str)
    {
        return isNull(str) || NULLSTR.equals(str.trim());
    }

    /**
    * * Determine whether a string is a non-empty string
     *
     * @param str String
     * @return true: non-empty string false: empty string
     */
    public static boolean isNotEmpty(String str)
    {
        return !isEmpty(str);
    }

    /**
     * * Check if an object is empty
     *
     * @param object Object
     * @return true: empty false: not empty
     */
    public static boolean isNull(Object object)
    {
        return object == null;
    }

    /**
     * * Check if an object is non-null
     *
     * @param object Object
     * @return true: not empty false: empty
     */
    public static boolean isNotNull(Object object)
    {
        return !isNull(object);
    }

    /**
     * * Determine whether an object is an array type (Java basic type array)
     *
     * @param object object
     * @return true: is an array false: not an array
     */
    public static boolean isArray(Object object)
    {
        return isNotNull(object) && object.getClass().isArray();
    }

    /**
     * remove spaces
     */
    public static String trim(String str)
    {
        return (str == null ? "" : str.trim());
    }

    /**
     * Intercept string
     *
     * @param str string
     * @param start start
     * @return result
     */
    public static String substring(final String str, int start)
    {
        if (str == null)
        {
            return NULLSTR;
        }

        if (start < 0)
        {
            start = str.length() + start;
        }

        if (start < 0)
        {
            start = 0;
        }
        if (start > str.length())
        {
            return NULLSTR;
        }

        return str.substring(start);
    }

    /**
     * Intercept string
     *
     * @param str string
     * @param start start
     * @param end end
     * @return result
     */
    public static String substring(final String str, int start, int end)
    {
        if (str == null)
        {
            return NULLSTR;
        }

        if (end < 0)
        {
            end = str.length() + end;
        }
        if (start < 0)
        {
            start = str.length() + start;
        }

        if (end > str.length())
        {
            end = str.length();
        }

        if (start > end)
        {
            return NULLSTR;
        }

        if (start < 0)
        {
            start = 0;
        }
        if (end < 0)
        {
            end = 0;
        }

        return str.substring(start, end);
    }

    /**
     * formatted text, {} means placeholder<br>
     * This method simply replaces the placeholders {} with parameters in order<br>
     * If you want to output {} use \\escape {, if you want to output \ before {} use double escape character \\\\<br>
     * Example: <br>
     * Usually use: format("this is {} for {}", "a", "b") -> this is a for b<br>
     * escape {}: format("this is \\{} for {}", "a", "b") -> this is \{} for a<br>
     * escape \: format("this is \\\\{} for {}", "a", "b") -> this is \a for b<br>
     *
     * @param template text template, the replaced part is represented by {}
     * @param params parameter value
     * @return formatted text
     */
    public static String format(String template, Object... params)
    {
        if (isEmpty(params) || isEmpty(template))
        {
            return template;
        }
        return StrFormatter.format(template, params);
    }

    /**
     * String to set
     *
     * @param str string
     * @param sep separator
     * @return set collection
     */
    public static final Set<String> str2Set(String str, String sep)
    {
        return new HashSet<String>(str2List(str, sep, true, false));
    }

    /**
     * 字符串转list
     * 
     * @param str 字符串
     * @param sep 分隔符
     * @param filterBlank 过滤纯空白
     * @param trim 去掉首尾空白
     * @return list集合
     */
    public static final List<String> str2List(String str, String sep, boolean filterBlank, boolean trim)
    {
        List<String> list = new ArrayList<String>();
        if (StringUtils.isEmpty(str))
        {
            return list;
        }

        // filter blank string
        if (filterBlank && StringUtils.isBlank(str))
        {
            return list;
        }
        String[] split = str.split(sep);
        for (String string : split)
        {
            if (filterBlank && StringUtils.isBlank(string))
            {
                continue;
            }
            if (trim)
            {
                string = string.trim();
            }
            list.add(string);
        }

        return list;
    }

    /**
     * Convert underscore to camel case
     */
    public static String toUnderScoreCase(String str)
    {
        if (str == null)
        {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        // Whether the leading character is capitalized
        boolean preCharIsUpperCase = true;
        // Whether the current character is capitalized
        boolean curreCharIsUpperCase = true;
        // Whether the next character is capitalized
        boolean nexteCharIsUpperCase = true;
        for (int i = 0; i < str.length(); i++)
        {
            char c = str.charAt(i);
            if (i > 0)
            {
                preCharIsUpperCase = Character.isUpperCase(str.charAt(i - 1));
            }
            else
            {
                preCharIsUpperCase = false;
            }

            curreCharIsUpperCase = Character.isUpperCase(c);

            if (i < (str.length() - 1))
            {
                nexteCharIsUpperCase = Character.isUpperCase(str.charAt(i + 1));
            }

            if (preCharIsUpperCase && curreCharIsUpperCase && !nexteCharIsUpperCase)
            {
                sb.append(SEPARATOR);
            }
            else if ((i != 0 && !preCharIsUpperCase) && curreCharIsUpperCase)
            {
                sb.append(SEPARATOR);
            }
            sb.append(Character.toLowerCase(c));
        }

        return sb.toString();
    }

    /**
     * Whether to contain a string
     *
     * @param str validation string
     * @param strs string group
     * @return contains return true
     */
    public static boolean inStringIgnoreCase(String str, String... strs)
    {
        if (str != null && strs != null)
        {
            for (String s : strs)
            {
                if (str.equalsIgnoreCase(trim(s)))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Convert underscore-capitalized strings to camel case. Returns an empty string if the underscore-capitalized string before conversion is empty. For example: HELLO_WORLD->HelloWorld
     *
     * @param name The string named underline and uppercase before conversion
     * @return converted camel case named string
     */
    public static String convertToCamelCase(String name)
    {
        StringBuilder result = new StringBuilder();
        // quick check
        if (name == null || name.isEmpty())
        {
            // no need to convert
            return "";
        }
        else if (!name.contains("_"))
        {
            // No underscores, just capitalize the first letter
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        // Split raw string with underscore
        String[] camels = name.split("_");
        for (String camel : camels)
        {
            // Skip leading, trailing underscores or double underscores in the original string
            if (camel.isEmpty())
            {
                continue;
            }
            //capitalized
            result.append(camel.substring(0, 1).toUpperCase());
            result.append(camel.substring(1).toLowerCase());
        }
        return result.toString();
    }

    /**
     * Camel case naming method For example: user_name->userName
     */
    public static String toCamelCase(String s)
    {
        if (s == null)
        {
            return null;
        }
        s = s.toLowerCase();
        StringBuilder sb = new StringBuilder(s.length());
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);

            if (c == SEPARATOR)
            {
                upperCase = true;
            }
            else if (upperCase)
            {
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            }
            else
            {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}