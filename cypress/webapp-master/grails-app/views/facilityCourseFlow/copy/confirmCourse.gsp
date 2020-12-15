<!doctype html>
<html>
<head>
    <meta name="layout" content="facilityLayout">
    <title><g:message code="facilityCourse.copy.title"/></title>
</head>

<body>
    <g:render template="copy/breadcrumb" model="[wizardStep: 1]"/>

    <h3><g:message code="facilityCourse.copy.step2.title"/></h3>
    <p class="lead"><g:message code="facilityCourse.copy.step2.desc"/></p>

    <g:form class="form-horizontal">
        <div class="well">
            <div class="row">
                <div class="span3 text-right">
                    <strong><g:message code="course.name.label"/></strong>
                </div>
                <div class="span8">${cmd.name.encodeAsHTML()}</div>
            </div>
            <div class="row">
                <div class="span3 text-right">
                    <strong><g:message code="facilityCourse.copy.courseDates"/></strong>
                </div>
                <div class="span8">
                    <g:formatDate date="${cmd.startDate}" formatName="date.format.dateOnly"/>
                    -
                    <g:formatDate date="${cmd.endDate}" formatName="date.format.dateOnly"/>
                </div>
            </div>
            <div class="row">
                <div class="span3 text-right">
                    <strong><g:message code="facilityCourse.copy.formDates"/></strong>
                </div>
                <div class="span8">
                    <g:formatDate date="${cmd.activeFrom}" formatName="date.format.dateOnly"/>
                    -
                    <g:formatDate date="${cmd.activeTo}" formatName="date.format.dateOnly"/>
                </div>
            </div>
            <g:if test="${trainers}">
                <div class="row">
                    <div class="span3 text-right">
                        <strong><g:message code="course.trainers.label"/></strong>
                    </div>
                    <div class="span8">${trainers.encodeAsHTML()}</div>
                </div>
            </g:if>
            <g:if test="${formSettings?.maxSubmissions}">
                <div class="row">
                    <div class="span3 text-right">
                        <strong><g:message code="form.maxSubmissions.label"/></strong>
                    </div>
                    <div class="span8"><g:formatNumber number="${formSettings.maxSubmissions}" type="number"/></div>
                </div>
            </g:if>
            <g:if test="${formSettings?.membershipRequired}">
                <div class="row">
                    <div class="span3 text-right">
                        <strong><g:message code="form.membershipRequired.label"/></strong>
                    </div>
                    <div class="span8"><g:message code="default.yes.label"/></div>
                </div>
            </g:if>
            <g:if test="${formSettings?.paymentRequired}">
                <div class="row">
                    <div class="span3 text-right">
                        <strong><g:message code="form.paymentRequired.label"/></strong>
                    </div>
                    <div class="span8"><g:message code="default.yes.label"/></div>
                </div>
            </g:if>
            <g:if test="${formSettings?.price}">
                <div class="row">
                    <div class="span3 text-right">
                        <strong><g:message code="form.price.label"/></strong>
                    </div>
                    <div class="span8"><g:formatMoney value="${formSettings.price}"/></div>
                </div>
            </g:if>
        </div>

        <div class="form-actions">
            <g:link event="cancel" class="btn btn-danger">
                <g:message code="button.cancel.label"/>
            </g:link>
            <g:submitButton name="submit" class="btn btn-success pull-right"
                    value="${message(code: 'button.next.label')}" data-toggle="button"
                    show-loader="${message(code: 'default.loader.label')}"/>
            <g:submitButton name="back" class="btn btn-info pull-right right-margin5"
                    value="${message(code: 'button.back.label')}" data-toggle="button"
                    show-loader="${message(code: 'default.loader.label')}"/>
        </div>
    </g:form>
</body>
</html>