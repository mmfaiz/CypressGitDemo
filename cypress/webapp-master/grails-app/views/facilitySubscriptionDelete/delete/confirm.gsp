<html>
    <head>
        <meta name="layout" content="facilityLayout">
        <title><g:message code="facility.subscription.bulkDelete"/></title>
    </head>

    <body>
        <ul class="breadcrumb">
            <li>
                <g:link controller="facilitySubscription" action="index">
                    <g:message code="subscription.label"/>
                </g:link>
                <span class="divider">/</span>
            </li>
            <li class="active"><g:message code="facility.subscription.bulkDelete"/></li>
        </ul>

        <g:if test="${!facility?.isMasterFacility()}">
            <g:render template="/templates/wizard"
                    model="[steps: [ message(code: 'facility.subscription.bulkDelete.confirm')], current: 0]"/>

            <h3><g:message code="facility.subscription.bulkDelete.confirm"/></h3>
            <p class="lead">
                <g:message code="facility.subscription.bulkDelete.confirm.desc"/>
            </p>

            <g:form>
                <table class="table table-transparent">
                    <thead>
                        <tr>
                            <th><g:message code="customer.number.label"/></th>
                            <th><g:message code="customer.label"/></th>
                            <th><g:message code="subscription.startDate.label"/></th>
                            <th><g:message code="subscription.endDate.label"/></th>
                            <th><g:message code="default.booking.plural"/></th>
                        </tr>
                    </thead>
                    <tbody>
                        <g:each in="${subscriptions}" var="subscription">
                            <tr>
                                <td>${subscription[1]}</td>
                                <td>
                                    <g:if test="${subscription[2]}">
                                        ${subscription[2]} ${subscription[3]}
                                    </g:if>
                                    <g:else>
                                        ${subscription[4]}
                                    </g:else>
                                </td>
                                <td><g:formatDate date="${subscription[5]}" formatName="date.format.dateOnly"/></td>
                                <td><g:formatDate date="${subscription[6]}" formatName="date.format.dateOnly"/></td>
                                <td>${subscription[7]}</td>
                            </tr>
                        </g:each>
                    </tbody>
                </table>

                <div class="form-actions">
                    <g:link event="cancel" class="btn btn-danger">
                        <g:message code="button.cancel.label"/>
                    </g:link>
                    <g:submitButton name="submit" type="submit" class="btn btn-success pull-right"
                            value="${message(code: 'button.delete.label')}" data-toggle="button"
                            show-loader="${message(code: 'default.loader.label')}"/>
                </div>
            </g:form>
        </g:if>
        <g:else><g:message code="facility.onlyLocal"/></g:else>
    </body>
</html>
