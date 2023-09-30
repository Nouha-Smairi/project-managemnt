package com.framework.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * precise floating-point arithmetic
 * 
 */
public class Arith
{

    /** Default division precision */
    private static final int DEF_DIV_SCALE = 10;

    /**This class cannot be instantiated */
    private Arith()
    {
    }

    /**
     * Provides precise addition operations.
     * @param v1 summand
     * @param v2 addend
     * @return the sum of the two parameters
     */
    public static double add(double v1, double v2)
    {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }

    /**
     * Provides precise subtraction operations.
     * @param v1 minuend
     * @param v2 subtrahend
     * @return The difference between the two parameters
     */
    public static double sub(double v1, double v2)
    {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).doubleValue();
    }

    /**
    * Provides precise multiplication.
     * @param v1 Multiplicand
     * @param v2 multiplier
     * @return the product of the two parameters
     */
    public static double mul(double v1, double v2)
    {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2).doubleValue();
    }

    /**
     * Provides (relatively) accurate division operations, accurate to
     * 10 digits after the decimal point, and the following numbers are rounded up.
     * @param v1 dividend
     * @param v2 divisor
     * @return the quotient of the two parameters
     */
    public static double div(double v1, double v2)
    {
        return div(v1, v2, DEF_DIV_SCALE);
    }

    /**
     * Provides (relatively) exact division operations. When an indivisible situation occurs, it is indicated by the scale parameter
     * Given precision, subsequent numbers are rounded.
     * @param v1 dividend
     * @param v2 divisor
     * @param scale indicates that it needs to be accurate to several digits after the decimal point.
     * @return the quotient of the two parameters
     */
    public static double div(double v1, double v2, int scale)
    {
        if (scale < 0)
        {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        if (b1.compareTo(BigDecimal.ZERO) == 0)
        {
            return BigDecimal.ZERO.doubleValue();
        }
        return b1.divide(b2, scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Provides precise decimal rounding.
     * @param v number to be rounded
     * @param scale How many digits are reserved after the decimal point
     * @return rounded result
     */
    public static double round(double v, int scale)
    {
        if (scale < 0)
        {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b = new BigDecimal(Double.toString(v));
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, scale, RoundingMode.HALF_UP).doubleValue();
    }
}
