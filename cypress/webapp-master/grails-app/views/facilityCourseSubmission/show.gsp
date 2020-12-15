<%@ page import="com.matchi.dynamicforms.Submission" %>
<html>
<head>
    <meta name="layout" content="b3facility"/>
    <title><g:message code="facilitySubmission.show.title"/></title>
</head>

<body>
<div class="container vertical-padding20">

    <ol class="breadcrumb">
        <li><g:link controller="facilityCustomer" action="show" id="${submission.customer.id}">${submission.customer?.fullName()}</g:link></li>
        <li class="active"><g:message code="facilitySubmission.show.title"/></li>
    </ol>

    <div class="panel panel-default panel-admin">
        <div class="panel-heading">
            <span class="pull-right"><i class="ti-calendar"></i> <g:formatDate date="${submission.dateCreated}" format="${g.message(code: 'date.format.dateOnly')}"/></span>
            <h1 class="h4 panel-title">
                <i class="ti-user"></i>
                <g:if test="${submission.customer}">
                    <span class="text-link">
                        <g:link controller="facilityCustomer" action="show" id="${submission.customer.id}">${submission.customer?.fullName()}</g:link>
                    </span>
                </g:if>
                <g:else>
                    <span class="text-muted">Not a MATCHi user</span>
                </g:else>
            </h1>
        </div>
        <table class="table">
            <thead>
            <tr>
                <th><g:message code="submissionValue.form.labels"/></th>
                <th><g:message code="submissionValue.value.label"/></th>
            </tr>
            </thead>
            <g:if test="${course}">
                <tr>
                    <td><b><g:message code="courseSubmission.course.label"/></b></td>
                    <td>${course.name}</td>
                </tr>
            </g:if>
            <g:each in="${submissionValues}" var="valueGroup">
                <tr>
                    <td><b>${valueGroup.key.encodeAsHTML()}</b></td>
                    <td><g:renderSubmittedValue values="${valueGroup.value}"/></td>
                </tr>
            </g:each>
        </table>
        <div class="text-right panel-footer">
            <g:if test="${submission.status.equals(Submission.Status.WAITING)}">
                <g:link controller="facilityCourseSubmission" action="discard" id="${submission.id}" class="btn btn-danger"
                        onclick="return confirm('${message(code: 'courseSubmission.discard.confirm')}');">
                    <g:message code="courseSubmission.discard.label"/>
                </g:link>
                <a href="javascript:void(0);" class="btn btn-success" onclick="submitFormTo('#submissions',
                    '<g:createLink controller="facilityCourseSubmissionFlow" action="accept"
                                                               params="[returnUrl: createLink(absolute: true, controller: 'facilityCourseSubmission', action: 'index')]" />');">
                    <g:message code="courseSubmission.addToCourse.label"/>
                </a>
            </g:if>
            <g:elseif test="${submission.status.equals(Submission.Status.DISCARDED)}">
                <g:link controller="facilityCourseSubmission" action="readdDiscarded" id="${submission.id}" class="btn btn-primary"
                        onclick="return confirm('${message(code: 'courseSubmission.readd.confirm')}');">
                    <g:message code="courseSubmission.readd.label"/>
                </g:link>
            </g:elseif>
            <g:link controller="facilityForm" action="editSubmission" id="${submission.id}" class="btn btn-success">
                <g:message code="button.edit.label"/>
            </g:link>
        </div>
        <g:form name="submissions">
            <g:hiddenField name="submissionIds" value="${submission.id}" />
        </g:form>
    </div>
</div><!-- /.container -->
</body>
</html>
