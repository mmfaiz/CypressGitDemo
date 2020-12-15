<%@ page import="com.matchi.payment.PaymentStatus" %>
<table class="booking-info no-border" width="100%" border="0" cellpadding="0" cellspacing="0">
    <tr>
        <td class="booking paymentstatus">
            <input type="checkbox" name="slotId" value="${booking.slot.id}" data-paid="${paidStatus.equals(PaymentStatus.OK)}"/>
        </td>
        <td class="booking paymentstatus ${paidStatus}">
            <g:if test="${paidStatus.equals(PaymentStatus.OK)}">
                <i class="icon-ok" rel="tooltip" title="BETALD"></i>
            </g:if>
        </td>
        <td class="booking status">
            <div class="booking faq">
                <i class="${color}"></i>
            </div>
        </td>
        <td onclick="$('#s${booking.slot.id}').click()" class="booking time">${booking.slot.timeSpan.getFormatted("HH:mm", " - ")}</td>

        <td onclick="$('#s${booking.slot.id}').click()" class="booking courtname">${booking.slot.court.name}</td>
        <td onclick="$('#s${booking.slot.id}').click()" class="booking name">
            ${booking.customer.fullName()}
        </td>

    <td onclick="$('#s${booking.slot.id}').click()" class="booking comment">
        <g:if test="${booking.comments}">
            <i class="icon-comment"></i>&nbsp;&nbsp; - "${booking.comments}"</td>
        </g:if>
    </tr>
</table>