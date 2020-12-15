package com.matchi

/**
 * Exception that indicates that no price could be found for the current article/slot.
 */
class PriceNotFoundException extends RuntimeException {
    PriceNotFoundException() {
    }

    PriceNotFoundException(String s) {
        super(s)
    }

    PriceNotFoundException(String s, Throwable throwable) {
        super(s, throwable)
    }

    PriceNotFoundException(Throwable throwable) {
        super(throwable)
    }
}
