package com.seeyou.mvc.utils;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.*;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2025-01-06
 */
public class StringCodec {
    public static String urlencode(String original) {
        try {
            return URLEncoder.encode(original, "utf-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
        } catch (UnsupportedEncodingException var2) {
            return null;
        }
    }

    public static String urldecode(String encoded) {
        try {
            return URLDecoder.decode(encoded, "utf-8");
        } catch (UnsupportedEncodingException var2) {
            return null;
        }
    }

    public static String hmacSha1Digest(String original, String key) {
        return hmacSha1Digest(original.getBytes(), key.getBytes());
    }

    public static String hmacSha1Digest(byte[] original, byte[] key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] rawHmac = mac.doFinal(original);
            return new String(Base64Coder.encode(rawHmac));
        } catch (Exception var4) {
            return null;
        }
    }

    public static String md5sum(byte[] original) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(original, 0, original.length);
            StringBuffer md5sum = new StringBuffer((new BigInteger(1, md.digest())).toString(16));

            while(md5sum.length() < 32) {
                md5sum.insert(0, "0");
            }

            return md5sum.toString();
        } catch (NoSuchAlgorithmException var3) {
            return null;
        }
    }

    public static String md5sum(String original) {
        return md5sum(original.getBytes());
    }

    public static byte[] aesEncrypt(byte[] original, byte[] key, byte[] iv) {
        if (key != null && (key.length == 16 || key.length == 24 || key.length == 32)) {
            if (iv != null && iv.length != 16) {
                return null;
            } else {
                try {
                    SecretKeySpec keySpec = null;
                    Cipher cipher = null;
                    if (iv != null) {
                        keySpec = new SecretKeySpec(key, "AES/CBC/PKCS7Padding");
                        cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
                        cipher.init(1, keySpec, new IvParameterSpec(iv));
                    } else {
                        keySpec = new SecretKeySpec(key, "AES/ECB/PKCS7Padding");
                        cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
                        cipher.init(1, keySpec);
                    }

                    return cipher.doFinal(original);
                } catch (Exception var5) {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    public static byte[] aesDecrypt(byte[] encrypted, byte[] key, byte[] iv) {
        if (key != null && (key.length == 16 || key.length == 24 || key.length == 32)) {
            if (iv != null && iv.length != 16) {
                return null;
            } else {
                try {
                    SecretKeySpec keySpec = null;
                    Cipher cipher = null;
                    if (iv != null) {
                        keySpec = new SecretKeySpec(key, "AES/CBC/PKCS7Padding");
                        cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
                        cipher.init(2, keySpec, new IvParameterSpec(iv));
                    } else {
                        keySpec = new SecretKeySpec(key, "AES/ECB/PKCS7Padding");
                        cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
                        cipher.init(2, keySpec);
                    }

                    return cipher.doFinal(encrypted);
                } catch (Exception var5) {
                    var5.printStackTrace();
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    public static KeyPair generateRsaKeyPair(int keySize, BigInteger publicExponent) {
        KeyPair keys = null;

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(keySize, publicExponent);
            keyGen.initialize(spec);
            keys = keyGen.generateKeyPair();
        } catch (Exception var5) {
        }

        return keys;
    }

    public static PublicKey generateRsaPublicKey(BigInteger modulus, BigInteger publicExponent) {
        try {
            return KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, publicExponent));
        } catch (Exception var3) {
            return null;
        }
    }

    public static PrivateKey generateRsaPrivateKey(BigInteger modulus, BigInteger privateExponent) {
        try {
            return KeyFactory.getInstance("RSA").generatePrivate(new RSAPrivateKeySpec(modulus, privateExponent));
        } catch (Exception var3) {
            return null;
        }
    }

    public static byte[] rsaEncrypt(byte[] original, PublicKey key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(1, key);
            return cipher.doFinal(original);
        } catch (Exception var3) {
            return null;
        }
    }

    public static byte[] rsaDecrypt(byte[] encrypted, PrivateKey key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(2, key);
            return cipher.doFinal(encrypted);
        } catch (Exception var3) {
            return null;
        }
    }

    public static String byteArrayToHexString(byte[] bytes) {
        StringBuffer buffer = new StringBuffer();

        for(int i = 0; i < bytes.length; ++i) {
            if ((bytes[i] & 255) < 16) {
                buffer.append("0");
            }

            buffer.append(Long.toString((long)(bytes[i] & 255), 16));
        }

        return buffer.toString();
    }

    public static final byte[] hexStringToByteArray(String str) {
        int i = 0;
        byte[] results = new byte[str.length() / 2];

        for(int k = 0; k < str.length(); ++i) {
            results[i] = (byte)(Character.digit(str.charAt(k++), 16) << 4);
            results[i] += (byte)Character.digit(str.charAt(k++), 16);
        }

        return results;
    }
}
