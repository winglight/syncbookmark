package com.favekeeper.android;
import java.util.*;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class AESHandler {
	private static Cipher ecipher;
    private static Cipher dcipher;
    private static String masterkey = "y@favekeeper.com";
    private static final String HEXES = "0123456789ABCDEF";
    
    public static String byteToHex( byte [] raw ) {
        if ( raw == null ) {
            return null;
        }
        final StringBuilder hex = new StringBuilder( 2 * raw.length );
        for ( final byte b : raw ) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
                .append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    public static byte[] hexToByte(String hexString) {
        int len = hexString.length();
        byte[] ba = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            ba[i/2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i+1), 16));
        }
        return ba;
    }

    public static String encrypt(String plaintext) {
        try {
            byte[] cipherbyte = ecipher.doFinal(plaintext.getBytes("UTF-8")); 
            return Base64.encodeBytes(cipherbyte);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static String decrypt(String ciphertext) {
        try {
            String plaintext = new String(dcipher.doFinal(Base64.decode(ciphertext.trim())), "UTF-8");
            return plaintext;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setupCrypto(byte[] iv, String key) {
        try {
             byte[] keyb = key.getBytes("UTF8");
             AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
             SecretKeySpec skey = new SecretKeySpec(keyb, "AES");
             ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
             dcipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
             ecipher.init(Cipher.ENCRYPT_MODE, skey, paramSpec);
             dcipher.init(Cipher.DECRYPT_MODE, skey, paramSpec);
        } catch (Exception e) {
            System.err.println("Error:" + e);
        }
    }
    /**
     * 生成AES密钥
     * @return AES密钥
     */
    public static SecretKey createKey() {
           try {
                  KeyGenerator keygen = KeyGenerator.getInstance("AES");
                  Date date=new Date();
//                  System.out.println(date.getTime());
                  SecureRandom random = new SecureRandom((String.valueOf(date.getTime())).getBytes());
                  keygen.init(random);
                  SecretKey key = keygen.generateKey();
                  return key;
           } catch (NoSuchAlgorithmException e) {
                  System.out.println("AES生成密钥错误");
                  e.printStackTrace();
                  return null;
           }
    }
    public static String DeCodeString(String ensource)
    {
    	String skey=ensource.substring(0,ensource.indexOf("$"));
    	String source=ensource.substring(ensource.indexOf("$")+1);
    	System.out.println(skey);
    	System.out.println(source);
    	byte [] ivb = Base64.decode(skey);
    	setupCrypto(ivb, masterkey);
    	String deResult = decrypt(source);
    	return deResult;
    }
    public static String EnCodeString(String source)
    {
    	SecretKey ckey=createKey();
        byte [] ivb = ckey.getEncoded();
        setupCrypto(ivb, masterkey);
        String bky=Base64.encodeBytes(ivb);
        String ciphertext = encrypt(source);//"AKIAIEBAX2273FYNCTXQ@1ZdLRgMo3aQ5ev/Cge3adT630nwBp6TrSUzOLFIj"
        return bky+"$"+ciphertext;
    }
    
    public static void test()
    {
    	try {
            SecretKey ckey=createKey();
            byte [] ivb = ckey.getEncoded();
            String bky=Base64.encodeBytes(ivb);
            System.out.println("IV:" + bky);
            setupCrypto(ivb, masterkey);
            String ciphertext;
            String plaintext;
            ciphertext = encrypt("AKIAIEBAX2273FYNCTXQ@1ZdLRgMo3aQ5ev/Cge3adT630nwBp6TrSUzOLFIj");
            System.out.println("Sent: " + bky+"$"+ciphertext);
            plaintext = decrypt(ciphertext);
            System.out.println("Source: " + plaintext);
            
        } catch (Exception e) {
            System.err.println("IOException: " + e);
        }
    }
    public static void main(String[] args)
    {
    	
    	String source="AKIAIEBAX2273FYNCTXQ@1ZdLRgMo3aQ5ev/Cge3adT630nwBp6TrSUzOLFIj";
    	
    	String result = AESHandler.EnCodeString(source);
    	System.out.println("Result: " + result);
    	
    	
    	String deResult = AESHandler.DeCodeString(result);
        System.out.println("Source: " + deResult);
        
    	
    }
}
