package com.matchi.slots

class SlotDeleteException extends Exception {

    SlotDeleteException() {
    }

    SlotDeleteException(String message) {
        super(message)
    }

    public SlotDeleteException(Throwable cause) {
        super (cause)
    }

    public SlotDeleteException(String message, Throwable cause) {
        super (message, cause)
    }
}
