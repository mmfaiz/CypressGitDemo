package com.matchi

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware
import org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib
import org.codehaus.groovy.grails.support.encoding.CodecLookup
import org.codehaus.groovy.grails.support.encoding.Encoder
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.codehaus.groovy.grails.web.pages.GroovyPage
import org.codehaus.groovy.grails.web.pages.TagLibraryLookup
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.web.servlet.support.RequestContextUtils
import org.springframework.web.servlet.support.RequestDataValueProcessor

class LinksTagLib implements ApplicationContextAware, InitializingBean, GrailsApplicationAware {

    static returnObjectForTags = ['createLink']

    ApplicationContext applicationContext
    TagLibraryLookup gspTagLibraryLookup
    CodecLookup codecLookup
    GrailsApplication grailsApplication

    @Autowired
    LinkGenerator linkGenerator

    RequestDataValueProcessor requestDataValueProcessor

    boolean useJsessionId = false
    boolean hasResourceProcessor = false

    void afterPropertiesSet() {
        def config = grailsApplication.config
        if (config.grails.views.enable.jsessionid instanceof Boolean) {
            useJsessionId = config.grails.views.enable.jsessionid
        }

        hasResourceProcessor = applicationContext.containsBean('grailsResourceProcessor')

        if (applicationContext.containsBean('requestDataValueProcessor')) {
            requestDataValueProcessor = applicationContext.getBean('requestDataValueProcessor', RequestDataValueProcessor)
        }
    }

    def userProfilePage = { attrs, body ->
        out << createLink([controller: 'user', absolute: 'true']).toString()
    }

    // TODO: workaround; it overrides UrlMappingTagLib#sortableColumn to fix issue described here:
    // https://github.com/grails/grails-core/issues/10819
    // Check whether this issue is fixed or not in next Grails version.
    // If it will be fixed, remove tag from here (after upgrade)
    Closure sortableColumn = { Map attrs ->
        def writer = out
        if (!attrs.property) {
            throwTagError("Tag [sortableColumn] is missing required attribute [property]")
        }

        if (!attrs.title && !attrs.titleKey) {
            throwTagError("Tag [sortableColumn] is missing required attribute [title] or [titleKey]")
        }

        def property = attrs.remove("property")
        def action = attrs.action ? attrs.remove("action") : (actionName ?: "list")
        def namespace = attrs.remove("namespace")

        def defaultOrder = attrs.remove("defaultOrder")
        if (defaultOrder != "desc") defaultOrder = "asc"

        // current sorting property and order
        def sort = params.sort
        def order = params.order

        // add sorting property and params to link params
        Map linkParams = [:]
        if (params.id) linkParams.put("id", params.id)
        def paramsAttr = attrs.remove("params")
        if (paramsAttr instanceof Map) linkParams.putAll(paramsAttr)
        linkParams.sort = property

        // propagate "max" and "offset" standard params
        if (params.max) linkParams.max = params.max
        if (params.offset) linkParams.offset = params.offset

        // determine and add sorting order for this column to link params
        attrs['class'] = (attrs['class'] ? "${attrs['class']} sortable" : "sortable")
        if (property == sort) {
            attrs['class'] = (attrs['class'] as String) + " sorted " + order
            if (order == "asc") {
                linkParams.order = "desc"
            }
            else {
                linkParams.order = "asc"
            }
        }
        else {
            linkParams.order = defaultOrder
        }

        // determine column title
        String title = attrs.remove("title") as String
        String titleKey = attrs.remove("titleKey") as String
        Object mapping = attrs.remove('mapping')
        if (titleKey) {
            if (!title) title = titleKey
            def messageSource = grailsAttributes.messageSource
            def locale = RequestContextUtils.getLocale(request)
            title = messageSource.getMessage(titleKey, null, title, locale)
        }

        writer << "<th "
        // process remaining attributes
        Encoder htmlEncoder = codecLookup.lookupEncoder('HTML')
        attrs.each { k, v ->
            writer << k
            writer << "=\""
            writer << htmlEncoder.encode(v)
            writer << "\" "
        }
        writer << '>'
        Map linkAttrs = [:]
        linkAttrs.params = linkParams
        if (mapping) {
            linkAttrs.mapping = mapping
        }

        linkAttrs.action = action
        linkAttrs.namespace = namespace

        writer << callLink((Map)linkAttrs) {
            title
        }
        writer << '</th>'
    }

    // TODO: workaround; it overrides ApplicationTagLib#createLink to add "execution" request parameter
    // to make weblow links working properly ("execution" param is not added in Grails 2.4.5 anymore)
    Closure createLink = { attrs ->
        def urlAttrs = attrs
        if (attrs.url instanceof Map) {
           urlAttrs = attrs.url
        }
        def params = urlAttrs.params && urlAttrs.params instanceof Map ? urlAttrs.params : [:]
        if (request.flowExecutionKey) {
            if (attrs.controller == null && attrs.action == null && attrs.url == null && attrs.uri == null) {
                urlAttrs[LinkGenerator.ATTRIBUTE_ACTION] = GrailsWebRequest.lookup().actionName
            }
        }
        if (urlAttrs.event) {
            if (request.flowExecutionKey) {
                params.execution = request.flowExecutionKey
            }
            params."_eventId" = urlAttrs.remove('event')
            urlAttrs.params = params
        }

        String generatedLink = linkGenerator.link(urlAttrs, request.characterEncoding)
        generatedLink = processedUrl(generatedLink, request)

        return useJsessionId ? response.encodeURL(generatedLink) : generatedLink
    }

    String processedUrl(String link, request) {
        if (requestDataValueProcessor == null) {
            return link
        }

        return requestDataValueProcessor.processUrl(request, link)
    }

    private callLink(Map attrs, Object body) {
        GroovyPage.captureTagOutput(gspTagLibraryLookup, 'g', 'link', attrs, body, webRequest)
    }
}
