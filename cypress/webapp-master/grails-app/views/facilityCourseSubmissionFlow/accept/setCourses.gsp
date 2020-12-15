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
              model="[steps: [message(code: 'facilityCourseSubmission.acceptFlow.general.wizard.step1'), message(code: 'facilityCourseSubmission.acceptFlow.general.wizard.step2'), message(code: 'default.completed.message')], current: 0]"/>

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
        ${message(code: "facilityCourseSubmission.accept.description")}
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
                    <td><g:select from="${coursesToSelect}" optionKey="id" optionValue="name" name="coursePerSubmission[${submission.id}]" value="${submission.courseId}"/></td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="form-actions">
        <g:submitButton class="btn btn-danger" name="cancel" value="${message(code: 'button.cancel.label')}" tabindex="1"/>
        <g:submitButton class="btn right btn-success" name="next" value="${message(code: 'default.paginate.next')}"
                        show-loader="${message(code: 'default.loader.label')}" tabindex="0"/>
    </div>
</g:form>
<r:script>
</r:script>
</body>
</html>