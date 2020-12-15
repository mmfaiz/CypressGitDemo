package com.matchi

class PrivacyHintTagLib {

    def privateInfoLabel = { attrs, body ->
        out << "<span class=\"help-inline\"><a href=\"#\" rel=\"tooltip\" class=\"privacy\" title=\"${message(code: 'privacyHint.privateInfoLabel.title')}\"><i class=\"fas fa-lock\"></i></a></span>"

    }

    def publicInfoLabel = { attrs, body ->
        out << "<span class=\"help-inline\"><a href=\"#\" rel=\"tooltip\" class=\"privacy\" title=\"${message(code: 'privacyHint.publicInfoLabel.title')}\"><i class=\"fas fa-globe\"></i></a></span>"
    }

    def inputHelp = { attrs, body ->

        def id = attrs.id
        def title = attrs.title

        out << "<i ${id ? "id=\"${id}\"" : ""} rel=\"tooltip\" title=\"${title}\" class=\"fas fa-question-circle text-info\"></i>"
    }
}
