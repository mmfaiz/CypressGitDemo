<ul class="myBookings unstyled">
    <g:each in="${bookings}" var="booking" status="it">
        <li class="${it % 2 == 0 ? 'even' : 'odd'} bookingInfo
        ${it == (bookings?.size()-1) ? 'last':''}" style="display:${it >= 4 ? 'none':'block'}">
            <g:remoteLink class="userCancelBooking" action="cancelConfirm" controller="userBooking"
                          update="userBookingModal"
                          onSuccess="showLayer('userBookingModal')"
                          params="[slotId:booking.slot.id]"
                          title="Avboka"
                          id="${booking.slot.id}_slot">
            </g:remoteLink>
            <div style="width: 100%; height: 100%;position: relative;font-size: 13px;" onclick="$('#${booking.slot.id}_slot').click()">
                <span class="courtname">${booking.slot.court.facility.name}</span><br>
                <g:formatDate format="yyyy-MM-dd" date="${booking.slot.startTime}" />&nbsp;&nbsp;<g:formatDate format="HH:mm" date="${booking.slot.startTime}" />-<g:formatDate format="HH:mm" date="${booking.slot.endTime}" /><br>
                ${booking.slot.court.name}
            </div>
        </li>
        <g:if test="${it == 4}">
            <li class="showMore" onclick="showMore()">
                <a href="javascript:void(0)"><g:message code="templates.userBookings.message1"/></a>
            </li>
        </g:if>
    </g:each>
    <g:if test="${bookings?.size() < 1}">
        <li class="last"><strong><g:message code="templates.userBookings.message2"/></strong></li>
    </g:if>
</ul>
<div id="userBookingModal" class="modal hide fade"></div>
<r:script>
    $(document).ready(function() {
        $('#userBookingModal').modal({ show:false });

        $(".bookingInfo").tooltip({
            placement: "left",
            trigger: "hover",
            title: "${message(code: 'templates.userBookings.message3')}"
        });

        <g:if test="${params.showBooking}">
            $('#${g.forJavaScript(data: params.showBooking)}_slot').click();
        </g:if>
    });
    function showMore() {
        $('ul.myBookings li:hidden').show();
        $('li.showMore').hide();
    }
</r:script>

