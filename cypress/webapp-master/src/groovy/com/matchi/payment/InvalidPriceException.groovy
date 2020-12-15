package com.matchi.payment

/**
 * Checked exception that represents a price that is invalid for the user
 */
class InvalidPriceException extends Exception {
    InvalidPriceException() {
        super()
    }

    InvalidPriceException(String message) {
        super(message)
    }
}
