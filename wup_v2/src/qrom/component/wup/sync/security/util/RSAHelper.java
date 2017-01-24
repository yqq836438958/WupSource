package qrom.component.wup.sync.security.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import android.text.TextUtils;
import android.util.Base64;

public class RSAHelper {
    public static PublicKey getPublicKey(String key) throws Exception {
        if (key == null)
            return null;

        byte[] keyBytes = base64Dec(key);

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }

    public static PrivateKey getPrivateKey(String key) throws Exception {
        if (key == null)
            return null;

        byte[] keyBytes = base64Dec(key);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }

    public static String getKeyString(Key key) throws Exception {
        if (key == null)
            return null;

        byte[] keyBytes = key.getEncoded();
        String s = base64Enc(keyBytes);
        return s;
    }

    public static byte[] signData(byte[] data, PrivateKey key, String algorithm) {
        if (data == null || key == null || TextUtils.isEmpty(algorithm))
            return null;

        byte[] result = null;
        try {
            Signature signature = Signature.getInstance(algorithm);
            signature.initSign(key);
            signature.update(data);
            result = signature.sign();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean verifySignature(byte[] originData, PublicKey key,
            String algorithm, byte[] signData) {
        if (originData == null || key == null || TextUtils.isEmpty(algorithm)
                || signData == null)
            return false;

        try {
            Signature signature = Signature.getInstance(algorithm);
            signature.initVerify(key);
            signature.update(originData);
            return signature.verify(signData);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static byte[] encrypt(byte[] data, Key key) {
        if (data == null || key == null)
            return null;

        byte[] result = null;

        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // "算法/工作模式/填充模式"
            cipher.init(Cipher.ENCRYPT_MODE, key);
            result = cipher.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static byte[] decrypt(byte[] encryptData, Key key) {
        if (encryptData == null || key == null)
            return null;

        byte[] result = null;
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // "算法/工作模式/填充模式"
            cipher.init(Cipher.DECRYPT_MODE, key);
            result = cipher.doFinal(encryptData);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        return result;
    }
    
    public static byte[] md5(byte[] data) {
        return digest(data, "MD5");
    }
    
    public static byte[] md5(File file) {
        return digest(file, "MD5");
    }
    
    public static String md5HexString(byte[] data) {
        if (data == null)
            return null;
        byte[] md5Result = md5(data);
        if (md5Result == null)
            return null;
        
        return bytes2HexString(md5Result);
    }
    
    public static String md5HexString(File file) {
        if (file == null)
            return null;
        byte[] md5Result = md5(file);
        if (md5Result == null)
            return null;
        
        return bytes2HexString(md5Result);
    }
    
    public static byte[] sha(byte[] data) {
        return digest(data, "SHA");
    }
    
    public static byte[] sha(File file) {
        return digest(file, "SHA");
    }
    
    public static String shaHexString(byte[] data) {
        if (data == null)
            return null;
        byte[] md5Result = sha(data);
        if (md5Result == null)
            return null;
        
        return bytes2HexString(md5Result);
    }
    
    public static String shaHexString(File file) {
        if (file == null)
            return null;
        byte[] md5Result = sha(file);
        if (md5Result == null)
            return null;
        
        return bytes2HexString(md5Result);
    }

    public static byte[] base64Dec(String decStr) {
        return Base64.decode(decStr.getBytes(), Base64.DEFAULT);
    }

    public static String base64Enc(byte[] b) {
        return new String(Base64.encode(b, Base64.DEFAULT));
    }
    
    public static String bytes2HexString(byte[] bytes) {
        if (bytes == null)
            return null;
        
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            int val = ((int) bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
 
        return hexValue.toString();
    }
    
    // ---------------- Private --------------------
    
    private static byte[] digest(byte[] data, String algorithm) {
        if (data == null)
            return null;
        
        byte[] result = null; 
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            result = digest.digest(data);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    private static byte[] digest(File file, String algorithm) {
        if (file == null)
            return null;
        
        byte[] result = null; 
        FileInputStream fis = null;
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            fis = new FileInputStream(file);
            
            byte[] buffer = new byte[8192];
            int length = 0;
            while ((length = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, length);
            }
            
            result = digest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return result;
    }
}
