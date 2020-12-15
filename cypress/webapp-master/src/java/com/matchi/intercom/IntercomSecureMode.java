package com.matchi.intercom;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class IntercomSecureMode {

    public static String generateUserHash(String secret, String userIdOrEmail)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        hmacSHA256.init(secretKey);

        byte[] output = hmacSHA256.doFinal(userIdOrEmail.getBytes());
        return Hex.encodeHexString(output);
    }
}
