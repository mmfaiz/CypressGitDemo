package com.matchi

class LoadersTagLib {

    def ajaxLoader = { attrs, body ->
        if(attrs.small.equals("true")) {
            out << '<div class="loader-wrapper" style="display:' + attrs.display  + '"><div class="loader small"></div></div>'
        } else {
            out << '<div class="loader-wrapper" style="display:' + attrs.display  + '"><div class="loader"></div></div>'
        }
    }
}
