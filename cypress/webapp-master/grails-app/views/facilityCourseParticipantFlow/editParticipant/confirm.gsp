<%@ page import="org.joda.time.LocalTime; org.joda.time.DateTime; com.matchi.Facility"%>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="facilityLayout" />
    <title>${message(code: "facilityCourseParticipation.${actionTitle}.title")}</title>
    <r:require modules="bootstrap-wysihtml5,zero-clipboard"/>
</head>
<body>

<g:form class="form-inline">
    <g:render template="/templates/wizard"
              model="[steps: [message(code: 'facilityCourseParticipant.editParticipant.general.wizard.step1'), message(code: 'facilityCourseParticipant.editParticipant.general.wizard.step2'), message(code: 'default.completed.message')], current: 1]"/>

    <g:if test="${error}">
        <div class="alert alert-error">
            <a class="close" data-dismiss="alert" href="#">×</a>

            <h4 class="alert-heading">
                ${ new LocalTime().toString("HH:mm:ss") }:  <g:message code="default.error.heading"/>
            </h4>
            ${error}
        </div>
    </g:if>

    <h1>${message(code: "facilityCourseParticipation.${actionTitle}.title")}</h1>
    <p class="lead no-bottom-margin">
        ${message(code: "facilityCourseParticipation.${actionTitle}.confirm.description", args: [chosenCourse.name])}
    </p>

    <div class="well" style="overflow-x: scroll">
        <table id="columns" class="table table-transparent table-condensed">
            <thead>
                <th><g:message code="default.name.label"/></th>
                <th><g:message code="default.status.label"/></th>
            </thead>
            <tbody>
            <g:each in="${customers}" var="customer" status="i">
                <tr>
                    <g:if test="${customer.courseOriginName}">
                        <td class="ellipsis">${customer.name} ${message(code: "default.from.label").toString().toLowerCase()} <strong>${customer.courseOriginName}</strong></td>
                    </g:if>
                    <g:else>
                        <td class="ellipsis">${customer.name}</td>
                    </g:else>
                    <td class="ellipsis">
                        <g:if test="${customer.alreadyParticipant}">
                            <span class="label label-danger">
                                <g:message code="facilityCourseParticipation.${actionTitle}.confirm.alreadyParticipant"/>
                            </span>
                        </g:if>
                        <g:elseif test="${customer.alreadyInFlow}">
                            <span class="label label-danger">
                                <g:message code="facilityCourseParticipation.${actionTitle}.confirm.alreadyInFlow"/>
                            </span>
                        </g:elseif>
                        <g:else>
                            <span class="label label-success">
                                <g:message code="default.status.ok"/>
                            </span>
                        </g:else>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="form-actions">
        <g:submitButton class="btn btn-danger" name="cancel" value="${message(code: 'button.cancel.label')}" tabindex="1"/>
        <g:submitButton class="btn right btn-success" name="next" value="${message(code: 'default.paginate.confirm')}"
                        show-loader="${message(code: 'default.loader.label')}" tabindex="0"/>
    </div>
</g:form>
<r:script>
</r:script>
</body>
</html>