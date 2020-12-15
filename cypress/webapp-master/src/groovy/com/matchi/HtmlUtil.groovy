package com.matchi

class HtmlUtil {

    private static List TAGS = ["img", "br"]

    static String closeInlineElements(String html) {
        def result = new StringBuilder(html)
        TAGS.each {
            def currentIdx = 0

            // This searches through the string for several occurrences of the same tag
            while(currentIdx < result.length()) {
                def tagFromIdx = result.indexOf("<${it}", currentIdx)
                if (tagFromIdx != -1) {
                    def tagCloseIdx = result.indexOf(">", tagFromIdx)

                    // This means unclosed tag and we jump out of the loop
                    if(tagCloseIdx == -1) {
                        return
                    }

                    if (result.charAt(tagCloseIdx - 1) == '/') {
                        currentIdx = tagCloseIdx + 1
                    } else {
                        result.insert(tagCloseIdx, "/")
                        currentIdx = tagCloseIdx + 1
                    }

                } else {
                    currentIdx = result.length()
                }

            }
        }

        return result
    }

    static String escapeAmpersands(String source) {
        return source?.replaceAll(/&(?!amp;)/, "&amp;")
    }
}
