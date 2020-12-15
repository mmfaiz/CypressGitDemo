package com.matchi

class FileArchiveException extends RuntimeException {
    FileArchiveException() {
    }

    FileArchiveException(String s) {
        super(s)
    }

    FileArchiveException(String s, Throwable throwable) {
        super(s, throwable)
    }

    FileArchiveException(Throwable throwable) {
        super(throwable)
    }
}
