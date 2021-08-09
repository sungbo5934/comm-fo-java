package com.ssb.comm.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EncryptUtil {
	
	public static String encrypt(String unencryptStr, String algorizm) {
		
		String encryptStr = StringUtils.EMPTY;
		
		try {
			
			MessageDigest md = MessageDigest.getInstance(algorizm);
			md.update(unencryptStr.getBytes());
			encryptStr = bytesToHex(md.digest());
			
		} catch (NoSuchAlgorithmException e) {
			log.error(e.getMessage());
		}
		
		return encryptStr;
		
	}
	
	public static String bytesToHex(byte[] bytes) {
		
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        
        return builder.toString();
    }

}
