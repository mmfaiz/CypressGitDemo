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
                  model="[steps: [message(code: 'facilitySubscriptionCopy.copy.wizard.step1'), message(code: 'facilitySubscriptionCopy.copy.wizard.step2'), message(code: 'default.completed.message')], current: 1]"/>

        <g:if test="${error}">
            <div class="alert alert-error">
                <a class="close" data-dismiss="alert" href="#">×</a>

                <h4 class="alert-heading">
                    ${ new LocalTime().toString("HH:mm:ss") }:  <g:message code="default.error.heading"/>
                </h4>
                ${error}
            </div>
        </g:if>

        <h1><g:message code="facilitySubscriptionCopy.copy.confirm.message2"/></h1>
        <p class="lead"><g:message code="facilitySubscriptionCopy.copy.confirm.message3"/></p>

        <hr>
        <div class="row">
            <div class="span2">
                <strong><g:message code="facilitySubscriptionCopy.copy.confirm.message4"/></strong><br>
                ${copySubscriptionResult.fromDate.toString("yyyy-MM-dd")}
            </div>

            <div class="span2">
                <strong><g:message code="facilitySubscriptionCopy.copy.confirm.message5"/></strong><br>
                ${copySubscriptionResult.toDate.toString("yyyy-MM-dd")}
            </div>
        </div>

        <hr>

        <table class="table table-transparent">
            <thead>
            <tr>
            <th width="30"><g:message code="default.status.label"/></th>
            <th><g:message code="customer.label"/></th>
            <th><g:message code="default.day.label"/></th>
            <th><g:message code="court.label"/></th>
            <th><g:message code="default.date.time"/></th>
            <th><g:message code="subscription.startDate.label"/></th>
            <th><g:message code="subscription.endDate.label"/></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${copySubscriptionResult.subscriptions}" var="subscription">
                <g:hiddenField name="subscriptionId" value="${subscription.id}"/>
                <tr>
                    <td>
                        <g:if test="${copySubscriptionResult.subscriptionUnavailableSlots[subscription.id]}">
                            <span class="label label-warning" rel="popover"
                                    data-content="<p>${message(code: 'facilitySubscriptionCopy.copy.confirm.occupiedSlots')}:</p><table class='table' style='font-size: 12px; color: #2c2c2c;'><tr><td nowrap>${copySubscriptionResult.subscriptionUnavailableSlots[subscription.id].join('</td></tr><tr><td nowrap>')}</td></tr></table>"><i class="icon-exclamation-sign"></i></span>
                        </g:if>
                        <g:else>
                            <span class="label label-success"><g:message code="default.status.ok"/></span>
                        </g:else>
                    </td>
                    <td>${subscription.customer}</td>
                    <td><g:message code="time.weekDay.plural.${subscription.weekDay}"/></td>
                    <td>${subscription.court}</td>
                    <td>${subscription.startTime}</td>
                    <td>${copySubscriptionResult.fromDate.toString("yyyy-MM-dd")}</td>
                    <td>${copySubscriptionResult.toDate.toString("yyyy-MM-dd")}</td>
                </tr>
            </g:each>
            </tbody>
        </table>

        <div class="form-actions">
            <g:submitButton class="btn btn-danger" name="cancel" value="${message(code: 'button.cancel.label')}" />
            <g:submitButton class="btn right btn-success" name="next" value="${message(code: 'facilitySubscriptionCopy.copy.title')}" show-loader="${message(code: 'default.loader.label')}"/>
            <g:submitButton class="btn right btn-info right-margin5" name="previous" value="${message(code: 'button.back.label')}" />
        </div>

    </g:form>

    <r:script>
        $(document).ready(function() {
            $("span[rel=popover]").popover({trigger: "hover", placement: "top"});
        });
    </r:script>
</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>

</body>
</html>