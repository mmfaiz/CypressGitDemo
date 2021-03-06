<%@ page import="org.joda.time.LocalTime; org.joda.time.DateTime; com.matchi.Facility"%>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="facilityLayout" />
    <title>${message(code: "facilityCourseSubmission.accept.title")}</title>
    <r:require modules="bootstrap-wysihtml5,zero-clipboard"/>
</head>
<body>

<g:form class="form-inline">
    <g:render template="/templates/wizard"
              model="[steps: [message(code: 'facilityCourseSubmission.acceptFlow.general.wizard.step1'), message(code: 'facilityCourseSubmission.acceptFlow.general.wizard.step2'), message(code: 'default.completed.message')], current: 2]"/>

    <g:if test="${error}">
        <div class="alert alert-error">
            <a class="close" data-dismiss="alert" href="#">×</a>

            <h4 class="alert-heading">
                ${ new LocalTime().toString("HH:mm:ss") }:  <g:message code="default.error.heading"/>
            </h4>
            ${error}
        </div>
    </g:if>

    <h1>${message(code: "facilityCourseSubmission.accept.title")}</h1>
    <p class="lead no-bottom-margin">
        ${message(code: "facilityCourseSubmission.accept.complete.description")}
    </p>

    <div class="well" style="overflow-x: scroll">
        <table id="columns" class="table table-transparent table-condensed">
            <thead>
            <th><g:message code="default.name.label"/></th>
            <th><g:message code="course.label"/></th>
            </thead>
            <tbody>
            <g:each in="${submissions}" var="submission" status="i">
                <tr>
                    <td class="ellipsis">${submission.customerName}</td>
                    <g:if test="${submission.isAlreadyParticipant}">
                        <td class="ellipsis"><span class="label label-warning"><g:message code="facilityCourseSubmission.accept.isAlreadyParticipantWarning" args="${[submission.courseName]}" /></span></td>
                    </g:if>
                    <g:else>
                        <td class="ellipsis">${submission.courseName}</td>
                    </g:else>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="form-actions">
        <g:submitButton class="btn right btn-success" name="next" value="${message(code: 'default.paginate.finish')}"
                        show-loader="${message(code: 'default.loader.label')}" tabindex="0"/>
    </div>
</g:form>
<r:script>
</r:script>
</body>
</html>