<%@ page import="com.matchi.MatchiConfigKey; com.matchi.MatchiConfig; com.matchi.FacilityProperty; org.joda.time.DateTime; com.matchi.Slot; com.matchi.Booking" %>
<g:set var="disableSubscriptionsConfig" value="${MatchiConfig.findByKey(MatchiConfigKey.DISABLE_SUBSCRIPTIONS)}" />
<p class="lead header">
    <span class="${subscriptions.size() < 1?"transparent-60":""}">
        <g:message code="subscription.label"/> ${subscriptions?.size() > 0 ? " - ${subscriptions?.size()}${message(code: 'unit.st')}" : ""}
    </span>
    <g:if test="${subscriptions?.size() > 4}">
        <g:remoteLink controller="facilityCustomer" action="showSubscriptions" update="customerModal" class="btn btn-small"
                      style="vertical-align: text-bottom;" title="${message(code: 'templates.customer.customerSubscription.message9')}"
                     onFailure="handleAjaxError()" onSuccess="showLayer('customerModal')"
                     params="[ 'subscriptionIds': subscriptions?.collect { it.id }, 'customerId':customer.id, ]"><g:message code="default.multiselect.checkAllText"/></g:remoteLink>
    </g:if>

    <g:if test="${!customer.archived && !disableSubscriptionsConfig.isBlocked()}">
        <g:link class="pull-right" controller="facilitySubscription" action="create"
                params="[customerId: customer.id, 'returnUrl': g.createLink(absolute: false, action: 'show', id: customer.id)]"
                title="${message(code: 'templates.customer.customerSubscription.message10')}"><i class="icon-plus"></i></g:link>
    </g:if>

    <g:if test="${subscriptions.size() < 1}">
        <br>
        <small class="empty"><g:message code="templates.customer.customerSubscription.message2"/></small>
        <g:if test="${disableSubscriptionsConfig.isBlocked()}">
            <p><small>${disableSubscriptionsConfig.isBlockedMessage()}</small></p>
        </g:if>
    </g:if>
</p>
<g:if test="${subscriptions.size() > 0}">
    <table class="table table-transparent table-condensed table-noborder table-striped no-bottom-margin">
        <thead class="table-header-transparent">
        <th><g:message code="default.day.label"/></th>
        <th><g:message code="default.date.time"/></th>
        <th><g:message code="court.label"/></th>
        <th><g:message code="subscription.startDate.label"/></th>
        <th><g:message code="subscription.endDate.label"/></th>
        <th><g:message code="subscription.status.label"/></th>
        <th><g:message code="subscription.price.label"/></th>
        <th width="10"></th>
        <g:if test="${customer.facility.hasApplicationInvoice()}">
            <th width="10"></th>
        </g:if>
        <g:if test="${customer.facility.getFacilityProperty(com.matchi.FacilityProperty.FacilityPropertyKey.FEATURE_SUBSCRIPTION_CONTRACT)}">
            <th width="10"></th>
        </g:if>
        <th width="10"></th>
        </thead>
        <tbody>
        <g:each in="${subscriptions}" var="subscription" status="i">
            <g:if test="${i < 4}">
                <g:set var="subscriptionPrice" value="${subscription.getPrice()}"/>
                <tr>
                    <td><g:message code="time.halfWeekDay.${subscription.weekday}"/></td>
                    <td>${subscription.time.toString("HH:mm")}</td>
                    <td>${subscription?.court?.name?.encodeAsHTML()}</td>
                    <td><g:formatDate date="${subscription.slots.first().startTime}" formatName="date.format.dateOnly"/></td>
                    <td><g:formatDate date="${subscription.slots.last().startTime}" formatName="date.format.dateOnly"/></td>
                    <td><g:message code="subscription.status.${subscription.status.name()}"/></td>
                    <g:if test="${subscriptionPrice == -1}">
                        <td rel="tooltip" title="${message(code: 'templates.customer.customerSubscription.noPricelist')}">
                            <g:message code="default.na.label"/>
                        </td>
                    </g:if>
                    <g:else>
                        <td rel="tooltip" title="${g.toRichHTML(text: message(code: 'templates.customer.customerSubscription.details',
                                 args: [subscription.getNumberOfSlots(), subscription.getPricePerBooking(), customer.facility?.currency]))}">
                            <g:formatMoney value="${subscriptionPrice}" facility="${customer.facility}"/>
                        </td>
                    </g:else>
                    <td class="center-text"><g:link class="pull-right" title="${message(code: 'facilitySubscription.edit.heading')}"
                                controller="facilitySubscription" action="edit"
                                params="[ 'id':subscription.id, 'returnUrl': g.createLink(absolute: true, action: 'show', id: customer.id)]"><i class="icon-edit"></i></g:link></td>
                    <g:if test="${customer.facility.hasApplicationInvoice()}">
                        <td class="center-text"><g:link class="pull-right" title="${message(code: 'templates.customer.customerSubscription.message12')}"
                                    controller="facilitySubscriptionInvoice" action="createSubscriptionInvoice"
                                    params="[subscriptionId: subscription.id, 'returnUrl': g.createLink(absolute: true, action: 'show', id: customer.id)]">
                                    <i class="icon-shopping-cart"></i></g:link></td>
                    </g:if>
                    <g:if test="${customer.facility.getFacilityProperty(com.matchi.FacilityProperty.FacilityPropertyKey.FEATURE_SUBSCRIPTION_CONTRACT)}">
                        <td class="center-text">
                            <g:link controller="facilitySubscriptionMessage" action="message" class="pull-right" title="${message(code: 'templates.customer.customerSubscription.message13')}"
                                          params="['subscriptionId': subscription.id,
                                                  'returnUrl': g.createLink(absolute: true, action: 'show', id: customer.id)]"><i class="icon-envelope"></i></g:link>
                        </td>
                    </g:if>
                    <td class="center-text">
                        <g:link controller="facilitySubscription" action="cancel" class="pull-right" title="${message(code: 'button.delete.label')}"
                                      onclick="return confirm('${message(code: 'templates.customer.customerSubscription.message14')}')"
                                      params="['id': subscription.id,
                                              'returnUrl': g.createLink(absolute: true, action: 'show', id: customer.id)]"><i class="icon-remove"></i></g:link>
                    </td>
                </tr>
            </g:if>
        </g:each>
        </tbody>
    </table>
</g:if>