package com.matchi

class AppleIDToken {

    public String iss;
    public String aud;
    public Long exp;
    public Long iat;
    public String sub;//users unique id
    public String at_hash;
    public Long auth_time;
    public String email;

    def getUserIdentifier() {
        return sub
    }

    @Override
    String toString() {
        return "email: $email, sub: ${sub.substring(0,10)}"
    }
}