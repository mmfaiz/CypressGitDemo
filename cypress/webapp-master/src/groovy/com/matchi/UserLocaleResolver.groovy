package com.matchi

import grails.plugin.springsecurity.SpringSecurityService
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.i18n.CookieLocaleResolver
import org.springframework.web.util.WebUtils
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.apache.commons.lang.StringUtils

/**
 * @author Sergei Shushkevich
 */
class UserLocaleResolver implements LocaleResolver {

    CookieLocaleResolver cookieLocaleResolver
    SpringSecurityService springSecurityService

    Locale defaultLocale
    Set<String> availableLanguages

    Locale resolveLocale(HttpServletRequest request) {
        def locale
        if (springSecurityService.loggedIn) {
            locale = new Locale(User.get(springSecurityService.currentUser.id).language)
        } else if (request.getAttribute(CookieLocaleResolver.LOCALE_REQUEST_ATTRIBUTE_NAME) ||
                WebUtils.getCookie(request, cookieLocaleResolver.getCookieName())) {
            locale = cookieLocaleResolver.resolveLocale(request)
        } else if (request.getHeader("accept-language")) {
            locale = request.getLocale()
        }

        if (!locale || !availableLanguages.contains(
                StringUtils.substring(new Locale(locale.language).language, 0, 2))) {
            locale = defaultLocale
        }

        locale
    }

    void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        if (springSecurityService.loggedIn) {
            User.withTransaction {
                def user = (User) springSecurityService.getCurrentUser()
                user.language = locale.language
                user.save()
            }
        }
        cookieLocaleResolver.setLocale(request, response, locale)
    }
}
