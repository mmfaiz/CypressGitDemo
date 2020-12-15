<%@ page import="java.text.SimpleDateFormat; com.matchi.User" %>
<html>
<head>
    <meta name="layout" content="b3main" />
    <title><g:message code="default.trainer.plural"/> - MATCHi</title>
</head>
<body>

<section class="block block-white vertical-padding30">
    <div class="container">
        <h2 class="page-header no-top-margin"><g:message code="default.trainer.plural"/></h2>

        <div class="row">
            <div class="col-md-12">
                <div class="panel panel-default">
                    <header class="panel-heading">
                        <div class="row row-full">
                            <div class="col-sm-4"><strong><g:message code="default.facility.label"/></strong></div>
                            <div class="col-sm-4"><strong><g:message code="default.name.label"/></strong></div>
                            <div class="col-sm-4"><strong><g:message code="default.request.plural.label"/></strong></div>
                        </div>
                    </header>
                    <div class="list-group alt">
                        <g:each in="${trainers}" var="trainer" status="i">
                            <div class="list-group-item row row-full bg-grey-light bottom-border-3-inverse vertical-padding15">
                                <div class="col-sm-4">
                                    <strong>
                                        ${trainer.facility}
                                    </strong>
                                </div>
                                <div class="col-sm-4">
                                    ${trainer}
                                </div>
                                <div class="col-sm-4">
                                    ${trainer.getRequests()?.size()}
                                </div>
                            </div>

                            <div class="list-group-item row row-full">
                                <div class="col-sm-12">
                                    <g:form action="update">
                                        <g:hiddenField name="id" value="${trainer.id}"/>
                                        <g:render template="/templates/trainer/availability" model="[ trainer: trainer ]"/>
                                        <div class="row top-margin10">
                                            <div class="col-sm-12">
                                                <g:submitButton name="update" value="${message(code: 'button.update.label')}" class="btn btn-success"/>
                                            </div>
                                        </div>
                                    </g:form>
                                </div>
                            </div>
                        </g:each>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="space-40"></div>
</section>
</body>
</html>
