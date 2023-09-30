package com.framework.common.core.lang;

import com.sun.xml.internal.ws.util.UtilException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Provide a Universal Unique Identifier（universally unique identifier）（UUID）accomplish
 *
 * @author ruoyi
 */
public final class UUID implements java.io.Serializable, Comparable<UUID>
{
    private static final long serialVersionUID = -1185015143654744140L;

    /**
     * SecureRandom singleton
     *
     */
    private static class Holder
    {
        static final SecureRandom numberGenerator = getSecureRandom();
    }

    /** The most significant 64 bits of this UUID */
    private final long mostSigBits;

    /** The least significant 64 bits of this UUID */
    private final long leastSigBits;

    /**
     * private construct
     *
     * @param data data
     */
    private UUID(byte[] data)
    {
        long msb = 0;
        long lsb = 0;
        assert data.length == 16 : "data must be 16 bytes in length";
        for (int i = 0; i < 8; i++)
        {
            msb = (msb << 8) | (data[i] & 0xff);
        }
        for (int i = 8; i < 16; i++)
        {
            lsb = (lsb << 8) | (data[i] & 0xff);
        }
        this.mostSigBits = msb;
        this.leastSigBits = lsb;
    }

    /**
     * Constructs a new UUID using the specified data.
     *
     * @param mostSigBits for {@code UUID} Most significant 64 bits of
     * @param leastSigBits for {@code UUID} The least significant 64 bits of
     */
    public UUID(long mostSigBits, long leastSigBits)
    {
        this.mostSigBits = mostSigBits;
        this.leastSigBits = leastSigBits;
    }

    /**
     * Get a static factory for type 4 (pseudo-randomly generated) UUIDs. The UUID is generated using a cryptographically thread-local pseudo-random number generator.
     *
     * @return randomly generated {@code UUID}
     */
    public static UUID fastUUID()
    {
        return randomUUID(false);
    }

    /**
     * Get a static factory for type 4 (pseudo-randomly generated) UUIDs. The UUID is generated using a cryptographically strong pseudorandom number generator.
     *
     * @return randomly generated {@code UUID}
     */
    public static UUID randomUUID()
    {
        return randomUUID(true);
    }

    /**
     * Get a static factory for type 4 (pseudo-randomly generated) UUIDs. The UUID is generated using a cryptographically strong pseudorandom number generator.
     *
     * @param isSecure Whether to use {@link SecureRandom} If yes, you can get a more secure random code, otherwise you can get better performance
     * @return randomly generated {@code UUID}
     */
    public static UUID randomUUID(boolean isSecure)
    {
        final Random ng = isSecure ? Holder.numberGenerator : getRandom();

        byte[] randomBytes = new byte[16];
        ng.nextBytes(randomBytes);
        randomBytes[6] &= 0x0f; /* clear version */
        randomBytes[6] |= 0x40; /* set to version 4 */
        randomBytes[8] &= 0x3f; /* clear variant */
        randomBytes[8] |= 0x80; /* set to IETF variant */
        return new UUID(randomBytes);
    }

    /**
     * Gets a static factory for a Type 3 (name-based) UUID from the specified byte array.
     *
     * @param name The byte array used to construct the UUID.
     *
     * @returnGenerated from the specified array {@code UUID}
     */
    public static UUID nameUUIDFromBytes(byte[] name)
    {
        MessageDigest md;
        try
        {
            md = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException nsae)
        {
            throw new InternalError("MD5 not supported");
        }
        byte[] md5Bytes = md.digest(name);
        md5Bytes[6] &= 0x0f; /* clear version */
        md5Bytes[6] |= 0x30; /* set to version 3 */
        md5Bytes[8] &= 0x3f; /* clear variant */
        md5Bytes[8] |= 0x80; /* set to IETF variant */
        return new UUID(md5Bytes);
    }

    /**
     * Creates a {@code UUID} from the canonical representation of a string as described in the {@link #toString()} method.
     *
     * @param name Specify {@code UUID} string
     * @return with specified value {@code UUID}
     * @throws IllegalArgumentException This exception is thrown if name does not match the string representation described in {@link #toString}
     *
     */
    public static UUID fromString(String name)
    {
        String[] components = name.split("-");
        if (components.length != 5)
        {
            throw new IllegalArgumentException("Invalid UUID string: " + name);
        }
        for (int i = 0; i < 5; i++)
        {
            components[i] = "0x" + components[i];
        }

        long mostSigBits = Long.decode(components[0]).longValue();
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(components[1]).longValue();
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(components[2]).longValue();

        long leastSigBits = Long.decode(components[3]).longValue();
        leastSigBits <<= 48;
        leastSigBits |= Long.decode(components[4]).longValue();

        return new UUID(mostSigBits, leastSigBits);
    }

    /**
     *Returns the least significant 64 bits of this UUID's 128-bit value.
     *
     * @return The least significant 64 bits of this UUID's 128-bit value.
     */
    public long getLeastSignificantBits()
    {
        return leastSigBits;
    }

    /**
     * Returns the most significant 64 bits of this UUID's 128-bit value.
     *
     * @return The most significant 64 bits of this UUID's 128-bit value.
     */
    public long getMostSignificantBits()
    {
        return mostSigBits;
    }

    /**
     * The version number associated with this {@code UUID}. The version number describes how this {@code UUID} was built.
     * <p>
     * Version numbers have the following meanings:
     * <ul>
     * <li>1 time-based UUID
     * <li>2 DCE Safety UUID
     * <li>3 name based UUID
     * <li>4randomly generated UUID
     * </ul>
     *
     * @return 此 {@code UUID} version number of
     */
    public int version()
    {
        // Version is bits masked by 0x000000000000F000 in MS long
        return (int) ((mostSigBits >> 12) & 0x0f);
    }

    /**
     * The variant number associated with this {@code UUID}. The variant number describes the layout of the {@code UUID}.
     * <p>
     * Variant numbers have the following meanings:
     * <ul>
     * <li>0 is reserved for NCS backward compatibility
     * <li>2 <a href="http://www.ietf.org/rfc/rfc4122.txt">IETF&nbsp;RFC&nbsp;4122</a>(Leach-Salz), for such
     * <li>6 Reserved, Microsoft backward compatible
     * <li>7 is reserved for future definition
     * </ul>
     *
     * @return The variant number associated with this {@code UUID}
     */
    public int variant()
    {
        // This field is composed of a varying number of bits.
        // 0 - - Reserved for NCS backward compatibility
        // 1 0 - The IETF aka Leach-Salz variant (used by this class)
        // 1 1 0 Reserved, Microsoft backward compatibility
        // 1 1 1 Reserved for future definition.
        return (int) ((leastSigBits >>> (64 - (leastSigBits >>> 62))) & (leastSigBits >> 63));
    }

    /**
     * The timestamp value associated with this UUID.
     *
     * <p>
     * A 60-bit timestamp value constructed from the time_low, time_mid and time_hi fields of this {@code UUID}. <br>
     * The resulting timestamp is in units of 100 nanoseconds since 00:00 UTC (Universal Coordinated Time) October 15, 1582.
     *
     * <p>
     * Timestamp values ​​are only meaningful in time-based UUIDs whose version type is 1. <br>
     * If this {@code UUID} is not a time-based UUID, this method throws UnsupportedOperationException.
     *
     * @throws UnsupportedOperationException if this {@code UUID} is not a UUID with version 1.
     */
    public long timestamp() throws UnsupportedOperationException
    {
        checkTimeBase();
        return (mostSigBits & 0x0FFFL) << 48//
                | ((mostSigBits >> 16) & 0x0FFFFL) << 32//
                | mostSigBits >>> 32;
    }

    /**
     * The clock sequence value associated with this UUID.
     *
     * <p>
     * The 14-bit clock sequence value constructed from the clock_seq field of this UUID. The clock_seq field is used to guarantee temporal uniqueness among time-based UUIDs.
     * <p>
     * {@code clockSequence} values ​​are only meaningful in time-based UUIDs whose version type is 1. If this UUID is not a time-based UUID, this method throws
     * UnsupportedOperationException.
     *
     * @return The clock sequence for this {@code UUID}
     *
     * @throws UnsupportedOperationException if version of this UUID is not 1
     */
    public int clockSequence() throws UnsupportedOperationException
    {
        checkTimeBase();
        return (int) ((leastSigBits & 0x3FFF000000000000L) >>> 48);
    }

    /**
     * The node value associated with this UUID.
     *
     * <p>
     * The 48-bit node value is constructed from the node field of this UUID. This field is intended to hold the IEEE 802 address of the machine that was used to generate this UUID for spatial uniqueness.
     * <p>
     * Node values ​​are only meaningful in time-based UUIDs whose version type is 1. <br>
     * If this UUID is not a time-based UUID, this method throws UnsupportedOperationException.
     *
     * @return the node value for this {@code UUID}
     *
     * @throws UnsupportedOperationException if version of this UUID is not 1
     */
    public long node() throws UnsupportedOperationException
    {
        checkTimeBase();
        return leastSigBits & 0x0000FFFFFFFFFFFFL;
    }

    /**
     * Returns the string representation of this {@code UUID}.
     *
     * <p>
     * The string representation of a UUID is described by this BNF:
     *
     * <pre>
     * {@code
     * UUID                   = <time_low>-<time_mid>-<time_high_and_version>-<variant_and_sequence>-<node>
     * time_low               = 4*<hexOctet>
     * time_mid               = 2*<hexOctet>
     * time_high_and_version  = 2*<hexOctet>
     * variant_and_sequence   = 2*<hexOctet>
     * node                   = 6*<hexOctet>
     * hexOctet               = <hexDigit><hexDigit>
     * hexDigit               = [0-9a-fA-F]
     * }
     * </pre>
     *
     * </blockquote>
     *
     * @return A string representation of this {@code UUID}
     * @see #toString(boolean)
     */
    @Override
    public String toString()
    {
        return toString(false);
    }

    /**
     * Returns the string representation of this {@code UUID}.
     *
     * <p>
     * The string representation of a UUID is described by this BNF:
     *
     * <pre>
     * {@code
     * UUID                   = <time_low>-<time_mid>-<time_high_and_version>-<variant_and_sequence>-<node>
     * time_low               = 4*<hexOctet>
     * time_mid               = 2*<hexOctet>
     * time_high_and_version  = 2*<hexOctet>
     * variant_and_sequence   = 2*<hexOctet>
     * node                   = 6*<hexOctet>
     * hexOctet               = <hexDigit><hexDigit>
     * hexDigit               = [0-9a-fA-F]
     * }
     * </pre>
     *
     * </blockquote>
     *
     * @param isSimple Whether it is a simple mode, the simple mode is a UUID string without '-'
     * @return the string representation of this {@code UUID}
     */
    public String toString(boolean isSimple)
    {
        final StringBuilder builder = new StringBuilder(isSimple ? 32 : 36);
        // time_low
        builder.append(digits(mostSigBits >> 32, 8));
        if (false == isSimple)
        {
            builder.append('-');
        }
        // time_mid
        builder.append(digits(mostSigBits >> 16, 4));
        if (false == isSimple)
        {
            builder.append('-');
        }
        // time_high_and_version
        builder.append(digits(mostSigBits, 4));
        if (false == isSimple)
        {
            builder.append('-');
        }
        // variant_and_sequence
        builder.append(digits(leastSigBits >> 48, 4));
        if (false == isSimple)
        {
            builder.append('-');
        }
        // node
        builder.append(digits(leastSigBits, 12));

        return builder.toString();
    }

    /**
     * Returns the hash code for this UUID.
     *
     * @return UUID hash code value。
     */
    public int hashCode()
    {
        long hilo = mostSigBits ^ leastSigBits;
        return ((int) (hilo >> 32)) ^ (int) hilo;
    }

    /**
     * Compares this object with the specified object.
     * <p>
     * The result is {@code true} if and only if the argument is not {@code null}, but a UUID object, with the same varriant as this UUID, containing the same value (every bit is the same).
     *
     * @param obj the object to compare with
     *
     * @return {@code true} if the objects are identical; otherwise {@code false}
     */
    public boolean equals(Object obj)
    {
        if ((null == obj) || (obj.getClass() != UUID.class))
        {
            return false;
        }
        UUID id = (UUID) obj;
        return (mostSigBits == id.mostSigBits && leastSigBits == id.leastSigBits);
    }

    // Comparison Operations

    /**
     * Compare this UUID with the specified UUID.
     *
     * <p>
     * If two UUIDs are different, and the most significant field of the first UUID is greater than the corresponding field of the second UUID, then the first UUID is greater than the second UUID.
     *
     * @param val the UUID to compare with this UUID
     *
     * @return -1, 0, or 1 when this UUID is less than, equal to, or greater than val, respectively.
     *
     */
    public int compareTo(UUID val)
    {
        // The ordering is intentionally set up so that the UUIDs
        // can simply be numerically compared as two numbers
        return (this.mostSigBits < val.mostSigBits ? -1 : //
                (this.mostSigBits > val.mostSigBits ? 1 : //
                        (this.leastSigBits < val.leastSigBits ? -1 : //
                                (this.leastSigBits > val.leastSigBits ? 1 : //
                                        0))));
    }

    // -------------------------------------------------------------------------------------------------------------------
    // Private method start
    /**
     * Return the hex value corresponding to the specified number
     *
     * @param val value
     * @param digits bits
     * @return value
     */
    private static String digits(long val, int digits)
    {
        long hi = 1L << (digits * 4);
        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
    }

    /**
     *Check if it is a time-based version UUID
     */
    private void checkTimeBase()
    {
        if (version() != 1)
        {
            throw new UnsupportedOperationException("Not a time-based UUID");
        }
    }

    /**
     * Get {@link SecureRandom}, a class providing a cryptographically strong random number generator (RNG)
     *
     * @return {@link SecureRandom}
     */
    public static SecureRandom getSecureRandom()
    {
        try
        {
            return SecureRandom.getInstance("SHA1PRNG");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new UtilException(e);
        }
    }

    /**
     * Get the random number generator object<br>
     * ThreadLocalRandom provides concurrent generation of random numbers after JDK 7, which can solve the competition between multiple threads.
     *
     * @return {@link ThreadLocalRandom}
     */
    public static ThreadLocalRandom getRandom()
    {
        return ThreadLocalRandom.current();
    }
}