<%@ page import="org.joda.time.LocalTime; org.joda.time.DateTime; com.matchi.Facility"%>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="facilityCustomerUpdateRequest.sendRequest.createMessage.message1"/></title>
</head>
<body>

<g:form class="form-inline">
    <g:render template="/templates/wizard"
              model="[steps: [message(code: 'facilityCustomerUpdateRequest.sendRequest.wizard.step1'), message(code: 'default.completed.message')], current: 0]"/>

    <g:if test="${error}">
        <div class="alert alert-error">
            <a class="close" data-dismiss="alert" href="#">×</a>

            <h4 class="alert-heading">
                ${ new LocalTime().toString("HH:mm:ss") }:  <g:message code="default.error.heading"/>
            </h4>
            ${error}
        </div>
    </g:if>

    <h1><g:message code="facilityCustomerUpdateRequest.sendRequest.createMessage.message2"/></h1>
    <p class="lead">
        <g:message code="facilityCustomerUpdateRequest.sendRequest.createMessage.message7"/><br>
        <g:message code="facilityCustomerUpdateRequest.sendRequest.createMessage.message8"/><br>
        <g:message code="facilityCustomerUpdateRequest.sendRequest.createMessage.message9"/>
        <g:if test="${cantRecieve?.size() > 0}">
            <br><br><g:message code="facilityCustomerUpdateRequest.sendRequest.createMessage.message3" args="[cantRecieve?.size()]"/>
        </g:if>
    </p>
    <div class="well">
        <div class="accordion" id="accordion2">
            <div class="accordion-group">
                <div class="accordion-heading" style="background-color: white">
                    <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#rows">
                        <g:message code="facilityCustomerMessage.message.createMessage.selectedRecipients" args="[customerInfo.size()]"/>
                    </a>
                </div>
                <div id="rows" class="accordion-body collapse">
                    <div class="accordion-inner">
                        <table class="table table-transparent">
                            <thead>
                            <tr>
                                <th width="60"><g:message code="facilityCustomerUpdateRequest.sendRequest.createMessage.message4"/></th>
                                <th width="200"><g:message code="default.name.label"/></th>
                                <th width="200"><g:message code="facilityCustomerUpdateRequest.sendRequest.createMessage.message6"/></th>
                            </tr>
                            </thead>
                            <g:each in="${customerInfo}" var="customer">
                                <tr>
                                    <td>${customer.number}</td>
                                    <td>${customer.name}</td>
                                    <td>${customer.email}</td>
                                </tr>
                            </g:each>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <g:textArea name="message" rows="10" cols="100" class="span12" placeholder="${message(code: 'facilityCustomerMessage.message.createMessage.placeholder')}"
                value="${textMessage ?: message(code: 'facilityCustomerUpdateRequest.sendRequest.createMessage.placeholder')}"/>

    <div class="form-actions">
        <g:submitButton class="btn btn-danger" name="cancel" value="${message(code: 'button.cancel.label')}" tabindex="1"/>
        <g:submitButton class="btn right btn-success" name="next" value="${message(code: 'facilityCustomerUpdateRequest.sendRequest.createMessage.message12')}" show-loader="${message(code: 'default.loader.label')}" tabindex="0"/>
    </div>
</g:form>
<r:script>
    $("#message").focus();
</r:script>
</body>
</html>