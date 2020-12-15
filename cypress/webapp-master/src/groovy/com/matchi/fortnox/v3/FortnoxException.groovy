package com.matchi.fortnox.v3

/**
 * @author Michael Astreiko
 */
class FortnoxException extends RuntimeException {
    public static String ERROR_CODE = "fortnox.v3"
    FortnoxException(String s) {
        super("FortnoxException: " + s)
    }
}
