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
              model="[steps: [message(code: 'facilityCourseParticipant.editParticipant.general.wizard.step1'), message(code: 'facilityCourseParticipant.editParticipant.general.wizard.step2'), message(code: 'default.completed.message')], current: 0]"/>

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
        ${message(code: "facilityCourseParticipation.${actionTitle}.description")}
    </p>

    <div class="well">
        <div class="row">
            <div class="span5">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="toCourse">
                            <g:message code="facilityCourseParticipation.${actionTitle}.toCourse.label"/>
                        </label>
                        <g:select from="${courses}" optionKey="id" optionValue="name" name="toCourse" />
                    </div>
                </fieldset>
            </div>
        </div>
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