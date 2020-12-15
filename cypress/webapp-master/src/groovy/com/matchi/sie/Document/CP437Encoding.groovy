package com.matchi.sie.Document

class CP437Encoding {

    static convertFromUTF8ToCP437(String text)
    {
        return new String(text.getBytes("CP437"), "UTF-8")
    }
    static convertFromCP437ToUTF8(String text)
    {
        return new String(text.getBytes("UTF-8"), "CP437")
    }
}
