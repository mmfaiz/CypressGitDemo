package com.matchi.i18n

import com.matchi.GlobalNotification
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest

/**
 * @author Sergei Shushkevich
 */
class Translatable implements Serializable {
    private static final long serialVersionUID = 12L

    Map<String, String> translations        // map of lang:value (lang is two-letter ISO 639-1 code)

    static belongsTo = [GlobalNotification]

    static constraints = {
        translations validator: { val ->
            val?.size() && val.every {it.value?.trim()}
        }
    }

    static mapping = {
        version false
    }

    String toString() {
        if (!translations) {
            return null
        }

        def locale = GrailsWebRequest.lookup()?.getLocale() ?: new Locale("en")
        def value = translations[locale.language]
        if (!value) {
            value = translations["en"]
        }
        if (!value) {
            value = translations.iterator().next().value
        }

        value
    }
}
