package com.matchi
import org.springframework.web.servlet.support.RequestContextUtils as RCU
class LocaleTagLib {
    static defaultEncodeAs = [taglib:'html']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]
    def locale = { attrs ->
        def locale = RCU.getLocale(request).toString().substring(0,2) ?: ""
        if(locale=="en" && attrs.i18n) { locale = ''} //jquery ui i18n bug fix solved in next versions

        out << locale
    }
}