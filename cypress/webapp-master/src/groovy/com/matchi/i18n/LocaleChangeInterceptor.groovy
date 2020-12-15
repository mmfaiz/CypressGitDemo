package com.matchi.i18n

import grails.compiler.GrailsCompileStatic
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.codehaus.groovy.grails.web.i18n.ParamsAwareLocaleChangeInterceptor
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.propertyeditors.LocaleEditor
import org.springframework.web.servlet.DispatcherServlet
import org.springframework.web.servlet.support.RequestContextUtils

/**
 * Locale change interceptor based on ParamsAwareLocaleChangeInterceptor implementation
 * with small change that allows to switch to language from supported list only.
 */
@GrailsCompileStatic
class LocaleChangeInterceptor extends ParamsAwareLocaleChangeInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(LocaleChangeInterceptor)

    Set<String> availableLanguages

    @Override
    boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        GrailsWebRequest webRequest = GrailsWebRequest.lookup(request)

        def params = webRequest.params

        def localeParam = params?.get(paramName)
        if (!localeParam) {
            return super.preHandle(request, response, handler)
        }

        try {
            // choose first if multiple specified
            if (localeParam.getClass().isArray()) {
                localeParam = ((Object[])localeParam)[0]
            }
            def localeResolver = RequestContextUtils.getLocaleResolver(request)
            if(localeResolver == null) {
                localeResolver = this.localeResolver
                request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, localeResolver)
            }

            def lang = localeParam?.toString()
            if (localeResolver && lang && availableLanguages.contains(lang.toLowerCase())) {
                def localeEditor = new LocaleEditor()
                localeEditor.setAsText lang
                localeResolver.setLocale request, response, (Locale)localeEditor.value
            }

            return true
        } catch (Exception e) {
            LOG.error("Error intercepting locale change: ${e.message}", e)
            return true
        }
    }
}