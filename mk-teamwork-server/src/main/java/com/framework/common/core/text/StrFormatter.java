package com.framework.common.core.text;

import com.framework.common.utils.StringUtils;

/**
* String formatting
 * 
 */
public class StrFormatter
{
    public static final String EMPTY_JSON = "{}";
    public static final char C_BACKSLASH = '\\';
    public static final char C_DELIM_START = '{';
    public static final char C_DELIM_END = '}';

    /**
     * format string<br>
     * This method simply replaces the placeholders {} with parameters in order<br>
     * If you want to output {} use \\escape {, if you want to output \ before {} use double escape character \\\\<br>
     * Example: <br>
     * Usually use: format("this is {} for {}", "a", "b") -> this is a for b<br>
     * escape {}: format("this is \\{} for {}", "a", "b") -> this is \{} for a<br>
     * escape \: format("this is \\\\{} for {}", "a", "b") -> this is \a for b<br>
     *
     * @param strPattern string template
     * @param argArray parameter list
     * @return result
     */
    public static String format(final String strPattern, final Object... argArray)
    {
        if (StringUtils.isEmpty(strPattern) || StringUtils.isEmpty(argArray))
        {
            return strPattern;
        }
        final int strPatternLength = strPattern.length();

        // Initialize the defined length for better performance
        StringBuilder sbuf = new StringBuilder(strPatternLength + 50);

        int handledPosition = 0;
        int delimIndex;// where the placeholder is located
        for (int argIndex = 0; argIndex < argArray.length; argIndex++)
        {
            delimIndex = strPattern.indexOf(EMPTY_JSON, handledPosition);
            if (delimIndex == -1)
            {
                if (handledPosition == 0)
                {
                    return strPattern;
                }
                else
                { //The remaining part of the string template no longer contains placeholders, and the result is returned after adding the remaining part
                    sbuf.append(strPattern, handledPosition, strPatternLength);
                    return sbuf.toString();
                }
            }
            else
            {
                if (delimIndex > 0 && strPattern.charAt(delimIndex - 1) == C_BACKSLASH)
                {
                    if (delimIndex > 1 && strPattern.charAt(delimIndex - 2) == C_BACKSLASH)
                    {
                        // There is an escape character before the escape character, and the placeholder is still valid
                        sbuf.append(strPattern, handledPosition, delimIndex - 1);
                        sbuf.append(Convert.utf8Str(argArray[argIndex]));
                        handledPosition = delimIndex + 2;
                    }
                    else
                    {
                        // placeholders are escaped
                        argIndex--;
                        sbuf.append(strPattern, handledPosition, delimIndex - 1);
                        sbuf.append(C_DELIM_START);
                        handledPosition = delimIndex + 1;
                    }
                }
                else
                {
                    // normal placeholder
                    sbuf.append(strPattern, handledPosition, delimIndex);
                    sbuf.append(Convert.utf8Str(argArray[argIndex]));
                    handledPosition = delimIndex + 2;
                }
            }
        }
        // Add all characters after the last placeholder
        sbuf.append(strPattern, handledPosition, strPattern.length());

        return sbuf.toString();
    }
}
