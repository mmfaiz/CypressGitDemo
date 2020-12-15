package com.matchi

import org.springframework.web.servlet.support.RequestContextUtils

class BootstrapPaginateTagLib {

    def paginateTwitterBootstrap = { attrs ->
        def writer = out
        if (attrs.total == null) {
            throwTagError("Tag [paginate] is missing required attribute [total]")
        }
        def messageSource = grailsAttributes.messageSource
        def locale = RequestContextUtils.getLocale(request)

        def total = attrs.int('total') ?: 0
        def action = (attrs.action ? attrs.action : (params.action ? params.action : "list"))
        def offset = params.int('offset') ?: 0
        def max = params.int('max')
        def maxsteps = (attrs.int('maxsteps') ?: 10)

        if (!offset) offset = (attrs.int('offset') ?: 0)
        if (!max) max = (attrs.int('max') ?: 10)

        def linkParams = [:]
        if (attrs.params) linkParams.putAll(attrs.params)
        linkParams.offset = offset - max
        linkParams.max = max
        if (params.sort) linkParams.sort = params.sort
        if (params.order) linkParams.order = params.order

        def linkTagAttrs = [action:action]
        if (attrs.controller) {
            linkTagAttrs.controller = attrs.controller
        }
        if (attrs.id != null) {
            linkTagAttrs.id = attrs.id
        }
        if (attrs.fragment != null) {
            linkTagAttrs.fragment = attrs.fragment
        }
        //add the mapping attribute if present
        if (attrs.mapping) {
            linkTagAttrs.mapping = attrs.mapping
        }

        linkTagAttrs.params = linkParams

        def cssClasses = "pagination"
        if (attrs.class) {
            cssClasses = "pagination " + attrs.class
        }

        // determine paging variables
        def steps = maxsteps > 0
        int currentstep = (offset / max) + 1
        int firststep = 1
        int laststep = Math.round(Math.ceil(total / max))

        writer << "<div class=\"${cssClasses}\"><ul>"
        // display previous link when not on firststep
        if (currentstep > firststep) {
            linkParams.offset = offset - max
            writer << '<li class="prev">'
            writer << link(linkTagAttrs.clone()) {
                (attrs.prev ?: messageSource.getMessage('paginate.prev', null, messageSource.getMessage('default.paginate.prev', null, 'Previous', locale), locale))
            }
            writer << '</li>'
        }
        else {
            writer << '<li class="prev disabled">'
            writer << '<span>'
            writer << (attrs.prev ?: messageSource.getMessage('paginate.prev', null, messageSource.getMessage('default.paginate.prev', null, 'Previous', locale), locale))
            writer << '</span>'
            writer << '</li>'
        }

        // display steps when steps are enabled and laststep is not firststep
        if (steps && laststep > firststep) {
            linkTagAttrs.class = 'step'

            // determine begin and endstep paging variables
            int beginstep = currentstep - Math.round(maxsteps / 2) + (maxsteps % 2)
            int endstep = currentstep + Math.round(maxsteps / 2) - 1

            if (beginstep < firststep) {
                beginstep = firststep
                endstep = maxsteps
            }
            if (endstep > laststep) {
                beginstep = laststep - maxsteps + 1
                if (beginstep < firststep) {
                    beginstep = firststep
                }
                endstep = laststep
            }

            // display firststep link when beginstep is not firststep
            if (beginstep > firststep) {
                linkParams.offset = 0
                writer << '<li>'
                writer << link(linkTagAttrs.clone()) {firststep.toString()}
                writer << '</li>'
                writer << '<li class="disabled"><span>...</span></li>'
            }

            // display paginate steps
            (beginstep..endstep).each { i ->
                if (currentstep == i) {
                    writer << "<li class=\"active\">"
                    writer << "<span>${i}</span>"
                    writer << "</li>";
                }
                else {
                    linkParams.offset = (i - 1) * max
                    writer << "<li>";
                    writer << link(linkTagAttrs.clone()) {i.toString()}
                    writer << "</li>";
                }
            }

            // display laststep link when endstep is not laststep
            if (endstep < laststep) {
                writer << '<li class="disabled"><span>...</span></li>'
                linkParams.offset = (laststep -1) * max
                writer << '<li>'
                writer << link(linkTagAttrs.clone()) { laststep.toString() }
                writer << '</li>'
            }
        }

        // display next link when not on laststep
        if (currentstep < laststep) {
            linkParams.offset = offset + max
            writer << '<li class="next">'
            writer << link(linkTagAttrs.clone()) {
                (attrs.next ? attrs.next : messageSource.getMessage('paginate.next', null, messageSource.getMessage('default.paginate.next', null, 'Next', locale), locale))
            }
            writer << '</li>'
        }
        else {
            linkParams.offset = offset + max
            writer << '<li class="next disabled">'
            writer << '<span>'
            writer << (attrs.next ? attrs.next : messageSource.getMessage('paginate.next', null, messageSource.getMessage('default.paginate.next', null, 'Next', locale), locale))
            writer << '</span>'
            writer << '</li>'
        }

        writer << '</ul></div>'
    }

    def b3PaginateTwitterBootstrap = { attrs ->
        def writer = out
        if (attrs.total == null) {
            throwTagError("Tag [paginate] is missing required attribute [total]")
        }
        def messageSource = grailsAttributes.messageSource
        def locale = RequestContextUtils.getLocale(request)

        def total = attrs.int('total') ?: 0
        def action = (attrs.action ? attrs.action : (params.action ? params.action : "list"))
        def offset = params.int('offset') ?: 0
        def max = params.int('max')
        def maxsteps = (attrs.int('maxsteps') ?: 10)

        Boolean showMaxDropdown = attrs.get('showMaxDropdown')
        List maxDropdownValues = attrs.get('maxDropdownValues')

        if (!maxDropdownValues) {
            maxDropdownValues = [10, 50, 100, 250, 1000]
        }

        if (!offset) offset = (attrs.int('offset') ?: 0)
        if (!maxDropdownValues.contains(max)) {
            max = null
        }
        if (!max) max = (attrs.int('max') ?: 10)

        def linkParams = [:]
        if (attrs.params) linkParams.putAll(attrs.params)

        if (params.sort) linkParams.sort = params.sort
        if (params.order) linkParams.order = params.order

        def linkTagAttrs = [action:action]
        if (attrs.controller) {
            linkTagAttrs.controller = attrs.controller
        }
        if (attrs.id != null) {
            linkTagAttrs.id = attrs.id
        }
        if (attrs.fragment != null) {
            linkTagAttrs.fragment = attrs.fragment
        }
        //add the mapping attribute if present
        if (attrs.mapping) {
            linkTagAttrs.mapping = attrs.mapping
        }

        linkTagAttrs.params = linkParams

        def cssClasses = "pagination"
        if (attrs.class) {
            cssClasses = "pagination " + attrs.class
        }

        // determine paging variables
        def steps = maxsteps > 0
        int currentstep = (offset / max) + 1
        int firststep = 1
        int laststep = Math.round(Math.ceil(total / max))
        
        writer << "<nav class=\"pagination-container\">"

        if (showMaxDropdown) {
            String selected
            writer << '<script>' +
                    'function postMax(a) {' +
                    'console.log($(a).val());' +
                    'window.location.href = $(a).val();' +
                    '}' +
                    '</script>'
            writer << '<form method="GET" action="" class="pagination-max-selector">'
            writer << message(code: "default.paginate.maxSelectorLabel")+': '
            writer << '<select name="max" class="pagination-max" onchange="postMax(this)">'
            maxDropdownValues.each {
                selected = ""
                if (max==it) {
                    selected = 'selected="selected"'
                }
                linkParams.max = it
                linkParams.offset = null
                writer << "<option ${selected} value='${createLink(linkTagAttrs.clone())}'>"
                writer << it
                writer << "</option>"
            }
            writer << "</select>"
            writer << "</form>"
        }

        linkParams.offset = offset - max
        linkParams.max = max

        writer << '<div class=\"pagination-inner\">'

        if (total>max) {

            writer << '<span class=\"pagination-label\">'+message(code: "default.paginate.page")+': </span>'

            writer << "<ul class=\"${cssClasses}\">"
            // display previous link when not on firststep
            if (currentstep > firststep) {
                linkParams.offset = offset - max
                writer << '<li class="prev">'
                writer << link(linkTagAttrs.clone()) {
                    (attrs.prev ?: messageSource.getMessage('paginate.prev', null, messageSource.getMessage('default.paginate.prev', null, 'Previous', locale), locale))
                }
                writer << '</li>'
            } else {
                writer << '<li class="prev disabled">'
                writer << '<span>'
                writer << (attrs.prev ?: messageSource.getMessage('paginate.prev', null, messageSource.getMessage('default.paginate.prev', null, 'Previous', locale), locale))
                writer << '</span>'
                writer << '</li>'
            }


            // display steps when steps are enabled and laststep is not firststep
            if (steps && laststep > firststep) {
                linkTagAttrs.class = 'step'

                // determine begin and endstep paging variables
                int beginstep = currentstep - Math.round(maxsteps / 2) + (maxsteps % 2)
                int endstep = currentstep + Math.round(maxsteps / 2) - 1

                if (beginstep < firststep) {
                    beginstep = firststep
                    endstep = maxsteps
                }
                if (endstep > laststep) {
                    beginstep = laststep - maxsteps + 1
                    if (beginstep < firststep) {
                        beginstep = firststep
                    }
                    endstep = laststep
                }

                // display firststep link when beginstep is not firststep
                if (beginstep > firststep) {
                    linkParams.offset = 0
                    writer << '<li>'
                    writer << link(linkTagAttrs.clone()) {firststep.toString()}
                    writer << '</li>'
                    writer << '<li class="disabled"><span>...</span></li>'
                }

                // display paginate steps
                (beginstep..endstep).each { i ->
                    if (currentstep == i) {
                        writer << "<li class=\"active\">"
                        writer << "<span>${i}</span>"
                        writer << "</li>";
                    }
                    else {
                        linkParams.offset = (i - 1) * max
                        writer << "<li>";
                        writer << link(linkTagAttrs.clone()) {i.toString()}
                        writer << "</li>";
                    }
                }

                // display laststep link when endstep is not laststep
                if (endstep < laststep) {
                    writer << '<li class="disabled"><span>...</span></li>'
                    linkParams.offset = (laststep -1) * max
                    writer << '<li>'
                    writer << link(linkTagAttrs.clone()) { laststep.toString() }
                    writer << '</li>'
                }
            }

            // display next link when not on laststep
            if (currentstep < laststep) {
                linkParams.offset = offset + max
                writer << '<li class="next">'
                writer << link(linkTagAttrs.clone()) {
                    (attrs.next ? attrs.next : messageSource.getMessage('paginate.next', null, messageSource.getMessage('default.paginate.next', null, 'Next', locale), locale))
                }
                writer << '</li>'
            }
            else {
                linkParams.offset = offset + max
                writer << '<li class="next disabled">'
                writer << '<span>'
                writer << (attrs.next ? attrs.next : messageSource.getMessage('paginate.next', null, messageSource.getMessage('default.paginate.next', null, 'Next', locale), locale))
                writer << '</span>'
                writer << '</li>'
            }

            writer << '</ul>'
        }

        writer << '<span class=\"results-label\">' + total + ' ' + messageSource.getMessage('default.paginate.results', null, 'Next', locale) + '</span>'
        writer << '</div>'
        writer << '</nav>'
    }

}
