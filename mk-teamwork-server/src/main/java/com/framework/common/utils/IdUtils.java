package com.framework.common.utils;

import com.framework.common.core.lang.UUID;

/**
 * ID generator tool class
 * 
 */
public class IdUtils
{
    /**
     * Get random UUID
     * 
     * @return Random UUID
     */
    public static String randomUUID()
    {
        return UUID.randomUUID().toString();
    }

    /**
     * Simplified UUID, remove the horizontal line
     *
     * @return Simplified UUID, remove the horizontal line
     */
    public static String simpleUUID()
    {
        return UUID.randomUUID().toString(true);
    }

    /**
     * Get a random UUID and use ThreadLocalRandom with better performance to generate a UUID
     * 
     * @return Random UUID
     */
    public static String fastUUID()
    {
        return UUID.fastUUID().toString();
    }

    /**
     * Simplified UUID, remove the horizontal line, use ThreadLocalRandom with better performance to generate UUID
     *
     * @return Simplified UUID, remove the horizontal line
     */
    public static String fastSimpleUUID()
    {
        return UUID.fastUUID().toString(true);
    }
}
