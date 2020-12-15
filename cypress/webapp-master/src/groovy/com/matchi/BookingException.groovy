package com.matchi

class BookingException extends RuntimeException {

    Slot slot

    BookingException() {
    }

    BookingException(String s) {
        super(s)
    }

    BookingException(String s, Throwable throwable) {
        super(s, throwable)
    }

    BookingException(Throwable throwable) {
        super(throwable)
    }

    BookingException(Slot slot) {
        this.slot = slot
    }

    BookingException(String s, Slot slot) {
        super(s)
        this.slot = slot
    }

    BookingException(String s, Throwable throwable, Slot slot) {
        super(s, throwable)
        this.slot = slot
    }

    BookingException(Throwable throwable, Slot slot) {
        super(throwable)
        this.slot = slot
    }
}
