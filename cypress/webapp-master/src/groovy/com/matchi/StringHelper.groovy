package com.matchi

import org.apache.commons.lang.StringUtils

class StringHelper {

    static final String LIST_PLACE_HOLDER = '_listPlaceHolder'

    static String safeSubstring(String str, int beginIndex, int endIndex) {
        // Checking specifically for null
        if(str == null) return

        return str.size() > endIndex ? str.substring(beginIndex, endIndex) : str
    }

    static Map splitUrl(String url) {
        Map result = [:]
        result.put("uri", StringUtils.substringBefore(url,"?"))

        HashMap<String, String> queryParams = new HashMap<String, String>()
        String queryString = StringUtils.substringAfter(url,"?")

        if (queryString.size() > 0) {
            for (String param : queryString.split("&")) {
                param = URLDecoder.decode(param, "UTF-8")
                queryParams.put(StringUtils.substringBefore(param, "="), StringUtils.substringAfter(param, "="))
            }

            result.put("queryParams", queryParams)
        } else {
            result.put("queryParams", [:])
        }

        return result
    }

    static boolean endsWithWhiteSpace(String input) {
        return input && Character.isWhitespace(input.charAt(input.length() - 1))
    }

    //Will also replace ZERO WIDTH SPACE character
    static extendedTrim(String s) {
        if (!s) {
            return s
        }
        return s.trim().replaceAll("\u200B", "")
    }
}
