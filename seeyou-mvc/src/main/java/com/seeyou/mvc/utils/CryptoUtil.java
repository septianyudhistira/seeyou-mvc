package com.seeyou.mvc.utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2025-01-06
 */
public class CryptoUtil {

    public static final String MODE_CBC = "CBC";
    public static final String MODE_CFB = "CFB";
    public static final String MODE_CTR = "CTR";
    public static final String MODE_CTS = "CTS";
    public static final String MODE_ECB = "ECB";
    public static final String MODE_NONE = "NONE";
    public static final String MODE_OFB = "OFB";
    public static final String MODE_PCBC = "PCBC";
    public static final String PADDING_ISO10126 = "ISO10126Padding";
    public static final String PADDING_NONE = "NoPadding";
    public static final String PADDING_PKCS1 = "PKCS1Padding";
    public static final String PADDING_PKCS5 = "PKCS5Padding";
    public static final String PADDING_SSL3 = "SSL3Padding";
    public static final String TYPE_3DES = "DESede";
    public static final String TYPE_3DES_WRAP = "DESedeWrap";
    public static final String TYPE_AES = "AES";
    public static final String TYPE_AES_WRAP = "AESWrap";
    public static final String TYPE_ARCFOUR = "ARCFOUR";
    public static final String TYPE_BLOWFISH = "Blowfish";
    public static final String TYPE_DES = "DES";
    public static final String TYPE_ECIES = "ECIES";
    public static final String TYPE_MD2 = "MD2";
    public static final String TYPE_MD5 = "MD5";
    public static final String TYPE_RC2 = "RC2";
    public static final String TYPE_RC4 = "RC4";
    public static final String TYPE_RC5 = "RC5";
    public static final String TYPE_RSA = "RSA";
    public static final String TYPE_SHA1 = "SHA-1";
    public static final String TYPE_SHA256 = "SHA-256";
    public static final String TYPE_SHA384 = "SHA-384";
    public static final String TYPE_SHA512 = "SHA-512";
    public static final String cRevisionNumber = "$Revision: 10017 $";
    public static final int ACTION_ENCRYPT = 1;
    public static final int ACTION_DECRYPT = 2;
    private static CryptoUtil INSTANCE;
    private Cipher mCipherDec;
    private Cipher mCipherEnc;

    public CryptoUtil() {
    }

    public CryptoUtil(String pKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        this.initCrypto("AES", "CBC", "PKCS5Padding", pKey, new IvParameterSpec(pKey.getBytes()));
    }

    public CryptoUtil(String pEncryptType, String pKey) throws Exception {
        this.initCrypto(pEncryptType, "CBC", "PKCS5Padding", pKey, new IvParameterSpec(pKey.getBytes()));
    }

    public CryptoUtil(String pEncryptType, String pEncryptMode, String pEncryptPadding, String pKey, AlgorithmParameterSpec pParam) throws Exception {
        this.initCrypto(pEncryptType, pEncryptMode, pEncryptPadding, pKey, pParam);
    }

    public void defaultInitAES() throws NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException {
        String aesKey = "1234567890ABCDEF";
        this.initCrypto("AES", "CBC", "PKCS5Padding", "1234567890ABCDEF", new IvParameterSpec("1234567890ABCDEF".getBytes()));
    }

    public static CryptoUtil getInstance() {
        if (INSTANCE == null) {
            Class var0 = CryptoUtil.class;
            synchronized(CryptoUtil.class) {
                try {
                    INSTANCE = new CryptoUtil();
                } catch (Exception var3) {
                }
            }
        }

        return INSTANCE;
    }

    public String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();

        for(int i = 0; i < data.length; ++i) {
            int halfbyte = data[i] >> 4 & 15;
            int var5 = 0;

            do {
                if (0 <= halfbyte && halfbyte <= 9) {
                    buf.append((char)(48 + halfbyte));
                } else {
                    buf.append((char)(97 + (halfbyte - 10)));
                }

                halfbyte = data[i] & 15;
            } while(var5++ < 1);
        }

        return buf.toString();
    }

    public String getCryptoMessage(int ACTION_ENCRYPT, String toString, String tAESType, String tAESMode, String tAESPadding, String tAESKey, Object object) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public String getCryptoMessage(int pAction, String pMessage) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, IOException {
        byte[] tResult;
        if (pAction == 1) {
            tResult = this.mCipherEnc.doFinal(pMessage.getBytes(Charset.defaultCharset()));
            return Base64.getEncoder().encodeToString(tResult);
        } else if (pAction == 2) {
            tResult = this.mCipherDec.doFinal(Base64.getDecoder().decode(pMessage));
            return (new String(tResult)).replace("\n", "");
        } else {
            return null;
        }
    }

    public String getHash(String pHashType, String pMessage) {
        String tHashResult = "";

        try {
            MessageDigest tMD = MessageDigest.getInstance(pHashType);
            tMD.update(pMessage.getBytes());
            tHashResult = this.convertToHex(tMD.digest());
        } catch (NoSuchAlgorithmException var5) {
        }

        return tHashResult;
    }

    private void initCrypto(String pEncryptType, String pEncryptMode, String pEncryptPadding, String pKey, AlgorithmParameterSpec pParam) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        SecretKeySpec tKey = new SecretKeySpec(pKey.getBytes(), pEncryptType);
        this.mCipherEnc = Cipher.getInstance(pEncryptType + "/" + pEncryptMode + "/" + pEncryptPadding);
        this.mCipherDec = Cipher.getInstance(pEncryptType + "/" + pEncryptMode + "/" + pEncryptPadding);
        if (pParam == null) {
            this.mCipherEnc.init(1, tKey);
            this.mCipherDec.init(2, tKey);
        } else {
            this.mCipherEnc.init(1, tKey, pParam);
            this.mCipherDec.init(2, tKey, pParam);
        }

    }

    public String encryptDecrypt(String val, String formatKeys, int lengthKey) {
        String[] arrText = formatKeys.split("#");
        List<String> arrTextLists = new ArrayList();
        List<String> arrTextList = Arrays.asList(arrText);

        for(int i = 0; i < lengthKey; ++i) {
            arrTextLists.addAll(arrTextList);
        }

        char[] paramChars = val.toCharArray();
        int length = arrText.length;
        StringBuffer arrHasil = new StringBuffer();

        for(int i = 0; i < paramChars.length; ++i) {
            String name = String.valueOf(paramChars[i]);
            int orignIndexPlusOne = length + arrTextList.indexOf(name) - i;
            arrHasil.append((String)arrTextLists.get(orignIndexPlusOne));
        }

        return arrHasil.toString();
    }
}
