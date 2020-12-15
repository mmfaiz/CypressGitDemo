package com.matchi

/**
 * Created with IntelliJ IDEA.
 * User: calle
 * Date: 7/19/12
 * Time: 12:14 PM
 * To change this template use File | Settings | File Templates.
 */
class SecurityException extends RuntimeException {

    SecurityException() {
    }

    SecurityException(String s) {
        super(s)
    }

    SecurityException(String s, Throwable throwable) {
        super(s, throwable)
    }

    SecurityException(Throwable throwable) {
        super(throwable)
    }
}
