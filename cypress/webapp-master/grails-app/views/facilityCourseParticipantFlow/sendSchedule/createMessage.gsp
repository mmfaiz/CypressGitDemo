<%@ page import="org.joda.time.LocalTime; org.joda.time.DateTime; com.matchi.Facility"%>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="facilityCourseParticipation.sendSchedule.title"/></title>
    <r:require modules="bootstrap-wysihtml5,zero-clipboard"/>
</head>
<body>

<g:form class="form-inline">
    <g:render template="/templates/wizard"
              model="[steps: [message(code: 'facilityCourseParticipant.sendSchedule.wizard.step1'), message(code: 'default.completed.message')], current: 0]"/>

    <g:if test="${error}">
        <div class="alert alert-error">
            <a class="close" data-dismiss="alert" href="#">×</a>

            <h4 class="alert-heading">
                ${ new LocalTime().toString("HH:mm:ss") }:  <g:message code="default.error.heading"/>
            </h4>
            ${error}
        </div>
    </g:if>
    <g:if test="${cantRecieve}">
        <div class="alert alert-warning" role="alert">
            <a class="close" data-dismiss="alert" href="#">×</a>
            <g:message code="facilityCourseParticipation.sendSchedule.cantReceive"
                    args="[cantRecieve.size()]"/>
            <br>
        </div>
    </g:if>

    <h1><g:message code="facilityCourseParticipation.sendSchedule.title"/></h1>
    <p class="lead no-bottom-margin">
        <g:message code="facilityCourseParticipation.sendSchedule.description"/>
    </p>

    <div class="well">
        <div class="row">
            <div class="span4">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="fromMail">
                            <g:message code="facilityCourseParticipation.sendSchedule.fromMail.label"/>
                        </label>
                        <select id="fromMail" name="fromMail">
                            <option value="${user?.email}">${user?.email}</option>
                            <g:if test="${facility?.email}">
                                <option value="${facility?.email}">${facility?.email}</option>
                            </g:if>
                        </select>
                    </div>
                </fieldset>
            </div>
            <div class="span2">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label">
                            <g:message code="facility.customer.message.includeGuardian"/>
                        </label>
                        <div>
                            <g:radioGroup name="includeGuardian" values="[false, true]"
                                          value="${params.includeGuardian ?: 'false'}"
                                          labels="[message(code: 'default.no.label'),
                                                   message(code: 'default.yes.label')]">
                                <label class="radio inline">
                                    ${it.radio} ${it.label}
                                </label>
                            </g:radioGroup>
                        </div>
                    </div>
                </fieldset>
            </div>
            <div class="span2">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label">
                            <g:message code="facilityCourseParticipation.sendSchedule.includeTrainers"/>
                        </label>
                        <div>
                            <g:radioGroup name="includeTrainers" values="[false, true]"
                                          value="${params.includeTrainers ?: 'false'}"
                                          labels="[message(code: 'default.no.label'),
                                                   message(code: 'default.yes.label')]">
                                <label class="radio inline">
                                    ${it.radio} ${it.label}
                                </label>
                            </g:radioGroup>
                        </div>
                    </div>
                </fieldset>
            </div>
            <div class="span2">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label">
                            <g:message code="facilityCourseParticipation.sendSchedule.includeParticipants"/>
                        </label>
                        <div>
                            <g:radioGroup name="includeParticipants" values="[false, true]"
                                          value="${params.includeParticipants ?: 'false'}"
                                          labels="[message(code: 'default.no.label'),
                                                   message(code: 'default.yes.label')]">
                                <label class="radio inline">
                                    ${it.radio} ${it.label}
                                </label>
                            </g:radioGroup>
                        </div>
                    </div>
                </fieldset>
            </div>
        </div>

        <div class="accordion" id="accordion2">
            <div class="accordion-group">
                <div class="accordion-heading" style="background-color: white">
                    <a id="recipientsCollapseTitle" class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#rows">
                        <g:message code="facilityCourseParticipation.sendSchedule.participantsCount"
                                args="[participantInfo.size(), nMails]"/>
                    </a>
                </div>
                <div id="rows" class="accordion-body collapse">
                    <div class="accordion-inner">
                        <table class="table table-transparent">
                            <thead>
                            <tr>
                                <th width="200"><g:message code="default.name.label"/></th>
                                <th width="200"><g:message code="facilityCourseParticipation.sendSchedule.participant.email"/></th>
                            </tr>
                            </thead>
                            <tbody id="recipientsCollapseBody">
                                <g:each in="${participantInfo}" var="participant">
                                    <tr>
                                        <td>${participant.name}</td>
                                        <td>${participant.email}</td>
                                    </tr>
                                </g:each>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <g:textArea id="message" name="message" rows="15" cols="100" class="span12"
            placeholder="${message(code: 'facilityCourseParticipation.sendSchedule.message.placeholder')}"/>

    <div class="form-actions">
        <g:submitButton class="btn btn-danger" name="cancel" value="${message(code: 'button.cancel.label')}" tabindex="1"/>
        <g:submitButton class="btn right btn-success" name="next" value="${message(code: 'button.submit.label')}"
                show-loader="${message(code: 'default.loader.label')}" tabindex="0"/>
    </div>
</g:form>
<r:script>
    $(function() {
        $(":radio[name=includeGuardian]").change(function() {

            if ($(this).val() == "true") {
                $("#recipientsCollapseTitle").html("<g:message code="facilityCourseParticipation.sendSchedule.participantsCount" args="[participantInfoAll?.size() ?: 0, nMailsAll]"/>");
                $("#recipientsCollapseBody").html("<g:each in="${participantInfoAll}" var="participant"><tr><td>${participant?.name}</td><td>${participant?.email}</td></tr></g:each>");
            } else {
                $("#recipientsCollapseTitle").html("<g:message code="facilityCourseParticipation.sendSchedule.participantsCount" args="[participantInfo?.size() ?: 0, nMails]"/>");
                $("#recipientsCollapseBody").html("<g:each in="${participantInfo}" var="participant"><tr><td>${participant?.name}</td><td>${participant?.email}</td></tr></g:each>");
            }
        });
    });

    $('#message').wysihtml5({
        "image": false,
        parserRules: wysihtml5ParserRules,
        stylesheets: ["${resource(dir: 'js/bootstrap/bootstrap-wysihtml5', file: 'wysiwyg-color.css')}"]
    });
    $("#message").focus();
</r:script>
</body>
</html>