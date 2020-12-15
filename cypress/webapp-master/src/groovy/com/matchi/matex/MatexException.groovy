package com.matchi.matex

class MatexException extends RuntimeException {

    MatexException() {
    }

    MatexException(String var1) {
        super(var1)
    }

    MatexException(String var1, Throwable var2) {
        super(var1, var2)
    }

    MatexException(Throwable var1) {
        super(var1)
    }

    MatexException(String var1, Throwable var2, boolean var3, boolean var4) {
        super(var1, var2, var3, var4)
    }
}
