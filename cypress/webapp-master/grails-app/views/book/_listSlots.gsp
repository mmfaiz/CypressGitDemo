<%@ page import="com.matchi.FacilityProperty; org.joda.time.Duration; org.joda.time.DateTime; org.joda.time.Seconds; groovy.time.TimeCategory;" %>
<g:if test="${!timeSlots}">
    <div>
        <g:if test="${noSlotsMessage}">
            ${noSlotsMessage.encodeAsHTML()}
        </g:if>
        <g:else>
            <g:message code="templates.bookingSchedules.userFacilitySchedule.noSlots"/>
        </g:else>
    </div>
</g:if>
<g:else>
    <ul class="list-inline no-margin">
        <g:each in="${timeSlots}" var="timeSlot">
            <g:set var="slotIds" value="${timeSlot?.slots?.collect { it.id }}"/>

            <li class="no-margin no-padding">
                <button class="btn btn-slot collapse-trigger" type="button" data-parent="#collapse-items"
                        data-toggle="collapse" data-target="#${timeSlot.facilityId}_${timeSlot.start.getTime()}"
                        aria-expanded="false" aria-controls="${timeSlot.facilityId}_${timeSlot.start.getTime()}"
                        data-slots="${g.expectJsonInTag(json: slotIds)}">
                    <g:formatDate date="${timeSlot.start}" format="HH"/>
                    <sup><g:formatDate date="${timeSlot.start}" format="mm"/></sup>
                </button>
            </li>
        </g:each>
    </ul>
    <div id="collapse-items">
        <g:each in="${timeSlots}" var="timeSlot">
            <div class="panel panel-default collapse" id="${timeSlot.facilityId}_${timeSlot.start.getTime()}">
                <ul class="list-group alt">
                    <li class="list-group-item">
                        <h6>
                            <g:message code="book.listSlots.availableTime"
                                       args="[formatDate(date: timeSlot.start, format: 'EEEE, dd MMM yyyy', locale: g.locale())]"/>
                            <strong><g:formatDate date="${timeSlot.start}" format="HH"/><sup><g:formatDate date="${timeSlot.start}" format="mm"/></sup></strong>
                        </h6>
                    </li>
                    <g:each in="${timeSlot.slots}">
                        <li class="list-group-item">
                            <table width="100%">
                                <tr>
                                    <td width="45%">
                                    <g:if test="${it.court?.facility?.hasCameraFeature() && it.court.hasCamera()}">
                                        <i class="fas fa-video"></i>&nbsp
                                    </g:if>
                                        ${it.court.name}
                                    </td>
                                    <td width="15%">${Seconds.secondsBetween(new DateTime(it.startTime), new DateTime(it.endTime)).getSeconds()/60}<g:message code="unit.min"/></td>
                                    <td width="20%">
                                        <div><g:message code="sport.name.${it.court.sport.id}"/></div>
                                        <div><small><g:message code="court.surface.${it.court.surface?.toString()}"/></small></div>
                                        <div><small>${!it.court.indoor ? "(" + message(code: 'court.outdoors.label') + ")" : ""}</small></div>
                                    </td>
                                    <td width="20%" class="text-right">
                                        <div id="price_${it.id}"><i class="fas fa-spinner fa-spin"/></div>
                                        <sec:ifLoggedIn>
                                            <g:remoteLink
                                                    elementId="s${it.id}"
                                                    slotid="${it.id}"
                                                    class="btn btn-success btn-sm slot free"
                                                    controller="bookingPayment"
                                                    action="confirm"
                                                    params="[ slotIds: it.id, facilityId: timeSlot.facilityId, start: it.startTime.getTime(), end: it.endTime.getTime(), sportIds: it.court.sport.id ]"
                                                    update="userBookingModal"
                                                    before="if(typeof blockBooking !== 'undefined' && blockBooking.isStarted()) return false"
                                                    onFailure="handleAjaxError()"
                                                    onSuccess="showLayer('userBookingModal')"
                                                    encodeAs="Raw">
                                                <g:message code="button.book.label"/>
                                            </g:remoteLink>
                                        </sec:ifLoggedIn>
                                        <sec:ifNotLoggedIn>
                                            <g:link elementId="s${it.id}"
                                                    class="btn btn-success btn-sm"
                                                    controller="login" action="auth"
                                                    params="${[returnUrl:createLink(controller: 'book', action: 'index', params: [ slotIds: it.id, facilityId: timeSlot.facilityId, start: it.startTime.getTime(), end: it.endTime.getTime(), sportIds: it.court.sport.id ,comeback:true])]}" onclick="loginBeforeBooking(this.href, '${it.id}');">
                                                <g:message code="button.book.label"/>
                                            </g:link>
                                        </sec:ifNotLoggedIn>
                                    </td>
                                </tr>
                            </table>
                        </li>
                    </g:each>
                </ul>
            </div>
        </g:each>
    </div>
</g:else>
<g:ifFacilityPropertyEnabled facility="${facility}" name="${FacilityProperty.FacilityPropertyKey.FEATURE_QUEUE.name()}">
    <g:addSlotWatch date="${date}" facility="${facility}" sports="${sports}"/>
</g:ifFacilityPropertyEnabled>
<script>
    $(document).ready(function() {
        $('button.collapse-trigger').unbind().on('click', function() {
            var collapsableID = $(this).attr('data-target');
            $('.collapse.in:not('+collapsableID+')').removeClass('in');

            $('.collapse-trigger').removeClass('active');

            if(!$(this).hasClass('collapsed')) {
                $(this).addClass('active');

                var slotIds = $(this).data('slots');
                var params = "";
                _.each(slotIds, function (slot, index) {
                    if(index == 0)
                        params += "?slotId="+slot;
                    else
                        params += "&slotId="+slot;
                });

                $.ajax({
                    cache: false,
                    url: "${g.forJavaScript(data: createLink(action: 'getSlotPrices'))}"+params,
                    dataType : 'json',
                    success: function (data) {
                        _.each(data, function (priceItem) {
                            $('#price_'+priceItem.slotId).html(priceItem.price + " " + priceItem.currency);
                        });
                    },
                    error: function() {
                        alert("${message(code: 'book.facilities.listSlots.price.error')}");
                    }
                });
            }
            else {
                $(this).removeClass('active');
            }
        });
    });

    var renderPrice = function (/*Object*/ priceItem) {
        console.log(priceItem);
    };
</script>
<g:if test="${request.xhr}">
    <r:layoutResources disposition="defer"/>
</g:if>