<%@ page import="org.joda.time.LocalTime; org.joda.time.DateTime; com.matchi.Facility"%>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="facilitySubscriptionCopy.copy.title"/></title>
</head>
<body>

<g:if test="${!facility?.isMasterFacility()}">
    <g:form class="form-inline">
        <g:render template="/templates/wizard"
                  model="[steps: [message(code: 'facilitySubscriptionImport.import.wizard.step1'), message(code: 'facilitySubscriptionImport.import.wizard.step2'), message(code: 'facilitySubscriptionImport.import.wizard.step3'), message(code: 'default.completed.message')], current: 2]"/>

        <g:if test="${error}">
            <div class="alert alert-error">
                <a class="close" data-dismiss="alert" href="#">×</a>

                <h4 class="alert-heading">
                    ${ new LocalTime().toString("HH:mm:ss") }:  <g:message code="default.error.heading"/>
                </h4>
                ${error}
            </div>
        </g:if>

        <h1><g:message code="facilitySubscriptionImport.import.confirm.message2"/></h1>
        <p class="lead"><g:message code="facilitySubscriptionImport.import.confirm.message3"/></p>
        <hr>

        <table class="table table-transparent">
            <thead>
            <th width="40" class="center-text"><g:message code="subscription.status.label"/></th>
            <th><g:message code="customer.label"/></th>
            <th><g:message code="default.day.label"/></th>
            <th><g:message code="default.date.time"/></th>
            <th><g:message code="court.label"/></th>
            <th><g:message code="subscription.startDate.label"/></th>
            <th><g:message code="subscription.endDate.label"/></th>
            </thead>
            <tbody>
            <g:each in="${subscriptionData}" var="data">
                <tr>
                    <td class="center-text">
                        <g:if test="${data.error}"><span class="label label-warning" rel="tooltip" title="${message(code: 'facilitySubscriptionImport.import.confirm.message12')}"><i class="icon-exclamation-sign"></i></span></g:if>
                        <g:else><span class="label label-success"><g:message code="default.status.ok"/></span></g:else>
                    </td>
                    <td>${data?.customerNumber}</td>
                    <td><g:message code="time.weekDay.plural.${data?.weekDay}"/></td>
                    <td>${data?.cmd?.time}</td>
                    <td>${data?.court}</td>
                    <td>${data?.cmd?.dateFrom}</td>
                    <td>${data?.cmd?.dateTo}</td>
                </tr>
            </g:each>
            </tbody>
        </table>

        <div class="form-actions">
            <g:submitButton class="btn btn-danger" name="cancel" value="${message(code: 'button.cancel.label')}" />
            <g:submitButton class="btn right btn-success" name="submit"
                            onclick="return confirm('${message(code: 'facilitySubscriptionImport.import.confirm.message13')}')"
                            value="${message(code: 'facilitySubscriptionImport.import.title')}" show-loader="${message(code: 'default.loader.label')}"/>
            <g:submitButton class="btn right btn-info right-margin5" name="previous" value="${message(code: 'button.back.label')}" />
        </div>

    </g:form>
    <r:script>
        $("[rel=tooltip]").tooltip();
    </r:script>
</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>
</body>
</html>