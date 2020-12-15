<%@ page import="com.matchi.requests.Request" %>
<html>
<head>
    <meta name="layout" content="b3main" />
    <title><g:message code="default.request.plural.label"/> - MATCHi</title>
</head>
<body>

<section class="block block-white vertical-padding30">
    <div class="container">
        <h2 class="page-header no-top-margin"><g:message code="default.request.plural.label"/></h2>

        <div class="row">
            <div class="col-md-12">
                <div class="panel panel-default">
                    <header class="panel-heading">
                        <div class="row row-full">
                            <div class="col-sm-3"><strong><g:message code="default.facility.label"/></strong></div>
                            <div class="col-sm-3"><strong><g:message code="default.requester.label"/></strong></div>
                            <div class="col-sm-2"><strong><g:message code="default.time.label"/></strong></div>
                            <div class="col-sm-2"></div>
                            <div class="col-sm-2"></div>
                        </div>
                    </header>
                    <div class="list-group alt">
                        <g:each in="${requestMap}">
                            <div class="list-group-item row row-full bg-grey-light no-vertical-padding
${it.key == Request.Status.ACCEPTED?'bottom-border-3-success':(it.key == Request.Status.DENIED ? 'bottom-border-3-danger':'bottom-border-3-inverse')}" style="border-bottom: 2px solid #85b20b">
                                <div class="col-sm-12">
                                    <h5><g:message code="request.status.${it.key}"/></h5>
                                </div>
                            </div>
                            <g:each in="${it.value}" var="request">
                                <div class="list-group-item row row-full vertical-padding15">
                                    <div class="col-sm-3">
                                        ${request.trainer.facility}
                                    </div>
                                    <div class="col-sm-3">
                                        ${request.requester.fullName()}
                                    </div>
                                    <div class="col-sm-2">
                                        <g:formatDate date="${request.start}" format="${message(code: "date.format.timeShort")}"/>
                                        -
                                        <g:formatDate date="${request.end}" format="${message(code: "date.format.timeOnly")}"/>
                                    </div>
                                    <div class="col-sm-2 text-center">
                                        <g:link action="accept" id="${request.id}" class="btn-sm btn-success">
                                            <i class="fas fa-thumbs-up"></i> <g:message code="button.accept.label"/>
                                        </g:link>
                                    </div>
                                    <div class="col-sm-2 text-center">
                                        <g:link action="deny" id="${request.id}" class="btn-sm btn-danger">
                                            <i class="fas fa-thumbs-down"></i>  <g:message code="button.deny.label"/>
                                        </g:link>
                                    </div>
                                </div>
                            </g:each>
                        </g:each>
                        <g:if test="${requestMap.isEmpty()}">
                            <div class="list-group-item row row-full vertical-padding15">
                                <div class="col-sm-12 text-center"><g:message code="default.no.requests.available.label"/></div>
                            </div>
                        </g:if>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="space-40"></div>
</section>
</body>
</html>
