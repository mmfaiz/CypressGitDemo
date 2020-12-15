<%@ page import="com.matchi.FacilityProperty; org.joda.time.DateTime; com.matchi.Slot; com.matchi.Booking" %>
<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">&times;</button>
    <h3><g:message code="templates.customer.customerSubscriptionsPopup.message6" args="[customer.fullName()]"/></h3>
    <div class="clearfix"></div>
</div>
<div class="modal-body">
    <table style="width: 100%" class="table-striped table-fixed">
        <thead>
        <tr>
            <th style="text-align: left;"><g:message code="default.day.label"/></th>
            <th style="text-align: left;"><g:message code="default.date.time"/></th>
            <th style="text-align: left;"><g:message code="court.label"/></th>
            <th style="text-align: left;"><g:message code="subscription.startDate.label"/></th>
            <th style="text-align: left;"><g:message code="subscription.endDate.label"/></th>
            <th style="text-align: left;"><g:message code="subscription.status.label"/></th>
            <th style="text-align: left;"><g:message code="subscription.price.label"/></th>
            <th width="10"></th>
            <g:if test="${customer.facility.hasApplicationInvoice()}">
                <th width="10"></th>
            </g:if>
            <g:if test="${customer.facility.getFacilityProperty(com.matchi.FacilityProperty.FacilityPropertyKey.FEATURE_SUBSCRIPTION_CONTRACT)}">
                <th width="10"></th>
            </g:if>
            <th width="10"></th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${subscriptions}" var="subscription">
            <g:set var="subscriptionPrice" value="${subscription.getPrice()}"/>
            <tr>
                <% Slot firstSlot = subscription.slots.first() %>
                <% Slot lastSlot = subscription.slots.last() %>
                <td><g:message code="time.halfWeekDay.${new DateTime(firstSlot.startTime).dayOfWeek}"/></td>
                <td><g:formatDate date="${firstSlot.startTime}" format="HH:mm"/>-<g:formatDate date="${firstSlot.endTime}" format="HH:mm"/></td>
                <td>${subscription?.court?.name?.encodeAsHTML()}</td>
                <td><g:formatDate date="${firstSlot.startTime}" formatName="date.format.dateOnly"/></td>
                <td><g:formatDate date="${lastSlot.startTime}" formatName="date.format.dateOnly"/></td>
                <td><g:message code="subscription.status.${subscription.status}"/></td>
                <g:if test="${subscriptionPrice == -1}">
                    <td rel="tooltip" title="${message(code: 'templates.customer.customerSubscription.noPricelist')}">
                        <g:message code="default.na.label"/>
                    </td>
                </g:if>
                <g:else>
                    <td rel="tooltip" title="${g.toRichHTML(text: message(code: 'templates.customer.customerSubscription.details',
                             args: [Booking.countByGroup(subscription.bookingGroup), subscription.getPricePerBooking(), customer.facility?.currency]))}">
                        <g:formatMoney value="${subscriptionPrice}" facility="${customer.facility}"/>
                    </td>
                </g:else>
                <td class="center-text"><g:link class="pull-right" title="${message(code: 'facilitySubscription.edit.heading')}"
                                                controller="facilitySubscription" action="edit"
                                                params="[id: subscription.id, 'returnUrl': g.createLink(absolute: true, action: 'show', id: customer.id)]">
                    <i class="icon-edit"></i></g:link></td>
                <g:if test="${customer.facility.hasApplicationInvoice()}">
                    <td class="center-text"><g:link class="pull-right" title="${message(code: 'templates.customer.customerSubscriptionsPopup.message8')}"
                                                    controller="facilitySubscriptionInvoice" action="createSubscriptionInvoice"
                                                    params="[subscriptionId: subscription.id, 'returnUrl': g.createLink(absolute: true, action: 'show', id: customer.id)]"><i class="icon-shopping-cart"></i></g:link></td>
                </g:if>
                <g:if test="${customer.facility.getFacilityProperty(com.matchi.FacilityProperty.FacilityPropertyKey.FEATURE_SUBSCRIPTION_CONTRACT)}">
                    <td class="center-text">
                        <g:link controller="facilitySubscriptionMessage" action="message" class="pull-right" title="${message(code: 'templates.customer.customerSubscriptionsPopup.message9')}"
                                params="['subscriptionId': subscription.id,
                                         'returnUrl': g.createLink(absolute: true, action: 'show', id: customer.id)]"><i class="icon-envelope"></i></g:link>
                    </td>
                </g:if>
                <td class="center-text">
                    <g:link controller="facilitySubscription" action="cancel" class="pull-right"
                            onclick="return confirm('${message(code: 'templates.customer.customerSubscriptionsPopup.message10')}')"
                            params="['id': subscription.id,
                                     'returnUrl': g.createLink(absolute: true, action: 'show', id: customer.id)]"><i class="icon-remove"></i></g:link>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>
</div>
<div class="modal-footer">
    <div class="pull-left">
        <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-danger btn-md"><g:message code="button.close.label" default="Stäng"/></a>
    </div>
</div>
<script>
    $(document).ready(function() {
        $("[rel=tooltip]").tooltip();
    });
</script>
