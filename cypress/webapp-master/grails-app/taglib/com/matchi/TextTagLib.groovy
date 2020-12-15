package com.matchi

import grails.converters.JSON
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.grails.plugins.sanitizer.MarkupSanitizerResult

class TextTagLib {

    static encodeAsForTags = [ toRichHTML: "raw", forJavaScript: "raw" ]
    static final String EMPTY_RESPONSE = ""

    def markupSanitizerService

    def toHTML = { attrs, body ->
        if (attrs.text) {
            out << attrs.text.encodeAsHTML().replace('\n', '<br/>\n')
        } else {
            out << EMPTY_RESPONSE
        }
    }

    def toRichHTML = { attrs, body ->
        if(attrs.text) {
            MarkupSanitizerResult result = markupSanitizerService.sanitize(attrs.text)
            out << result.cleanString
        } else {
            out << EMPTY_RESPONSE
        }
    }

    def expectJsonInTag = { attrs, body ->
        if(attrs.json) {
            if(attrs.json instanceof JSON) {
                out << attrs.json.toString()
            } else {
                try {
                    out << (attrs.json as JSON).toString()
                } catch(GroovyCastException e) {
                    log.error("Tried to cast ${attrs.json} to JSON")
                    out << EMPTY_RESPONSE
                }
            }
        } else {
            out << EMPTY_RESPONSE
        }
    }

    def forJavaScript = { attrs, body ->
        if(attrs.json) {
            String json = expectJsonInTag(attrs, body).toString()
            if (json) {
                out << json.decodeHTML()
            } else {
                out << EMPTY_RESPONSE
            }
        } else if (attrs.data != null) {
            out << attrs.data.encodeAsJavaScript()
        } else {
            out << EMPTY_RESPONSE
        }
    }
}
