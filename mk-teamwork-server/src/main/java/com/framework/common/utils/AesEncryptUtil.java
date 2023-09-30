package com.framework.common.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES 128bit Encryption and decryption tools
 */
public class AesEncryptUtil {

    //Use AES-128-CBC encryption mode, the key needs to be 16 bits, and the key and iv can be the same!
    private static String KEY = "1234567890123456";

    private static String IV = "1234567890123456";


    /**
     * encryption method
     * @param data data to be encrypted
     * @param key encryption key
     * @param iv encrypted iv
     * @return encrypted result
     * @throws Exception
     */
    public static String encrypt(String data, String key, String iv) throws Exception {
        try {

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");//"Algorithm/Mode/Complement Mode" NoPadding PkcsPadding
            int blockSize = cipher.getBlockSize();

            byte[] dataBytes = data.getBytes();
            int plaintextLength = dataBytes.length;
            if (plaintextLength % blockSize != 0) {
                plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
            }

            byte[] plaintext = new byte[plaintextLength];
            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);

            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            byte[] encrypted = cipher.doFinal(plaintext);

            return new Base64().encodeToString(encrypted);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Decryption method
     * @param data data to be decrypted
     * @param key decryption key
     * @param iv decryption iv
     * @return decrypted result
     * @throws Exception
     */
    public static String desEncrypt(String data, String key, String iv) throws Exception {
        try {
            byte[] encrypted1 = new Base64().decode(data);

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original);
            return originalString;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Encrypt with default key and iv
     * @param data
     * @return
     * @throws Exception
     */
    public static String encrypt(String data) throws Exception {
        return encrypt(data, KEY, IV);
    }

    /**
     * Decrypt using the default key and iv
     * @param data
     * @return
     * @throws Exception
     */
    public static String desEncrypt(String data) throws Exception {
        return desEncrypt(data, KEY, IV);
    }



    /**
     * test
     */
    public static void main(String args[]) throws Exception {

        String test1 = "sa";
        String test =new String(test1.getBytes(),"UTF-8");
        String data = null;
        String key =  KEY;
        String iv = IV;
        // /g2wzfqvMOeazgtsUVbq1kmJawROa6mcRAzwG1/GeJ4=
        data = encrypt(test, key, iv);
        System.out.println("data："+test);
        System.out.println("encryption："+data);
        String jiemi =desEncrypt(data, key, iv).trim();
        System.out.println("decrypt："+jiemi);


    }
}
